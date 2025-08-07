/*
 * Copyright (c) 2021. Hans-Peter Grahsl (grahslhp@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.hpgrahsl.kafka.connect.transforms.kryptonite;

import com.github.hpgrahsl.kryptonite.*;
import com.github.hpgrahsl.kryptonite.config.KryptoniteSettings;
import com.github.hpgrahsl.kryptonite.serdes.SerdeProcessor;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.connect.errors.DataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public abstract class RecordHandler implements FieldPathMatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(RecordHandler.class);

  private final AbstractConfig config;
  private final SerdeProcessor serdeProcessor;
  private final Kryptonite kryptonite;

  protected final String pathDelimiter;
  protected final CipherMode cipherMode;
  protected final Map<String, FieldConfig> fieldConfig;

  public RecordHandler(AbstractConfig config,
      SerdeProcessor serdeProcessor, Kryptonite kryptonite,
      CipherMode cipherMode,
      Map<String, FieldConfig> fieldConfig) {
    this.config = config;
    this.serdeProcessor = serdeProcessor;
    this.kryptonite = kryptonite;
    this.pathDelimiter = config.getString(KryptoniteSettings.PATH_DELIMITER);
    this.cipherMode = cipherMode;
    this.fieldConfig = fieldConfig;
  }

  public AbstractConfig getConfig() {
    return config;
  }

  public Kryptonite getKryptonite() {
    return kryptonite;
  }

  public Object processField(Object object,String matchedPath) {
    try {
      LOGGER.debug("{} field {}",cipherMode,matchedPath);
      var fieldMetaData = determineFieldMetaData(object,matchedPath);
      LOGGER.trace("field meta-data for path '{}' {}",matchedPath,fieldMetaData);
      if (CipherMode.ENCRYPT == cipherMode) {
        var valueBytes = serdeProcessor.objectToBytes(object);
        var encryptedField = kryptonite.cipherField(valueBytes, PayloadMetaData.from(fieldMetaData));
        LOGGER.debug("encrypted field: {}",encryptedField);
        String encodedField = serdeProcessor.encodeField(encryptedField);
        LOGGER.trace("encoded field: {}",encodedField);
        return encodedField;
      } else {
        var encryptedField = serdeProcessor.decodeField((String)object);
        LOGGER.debug("decoded translated field: {}",encryptedField);
        var plaintext = kryptonite.decipherField(encryptedField);
        LOGGER.trace("decrypted field: {}",plaintext);
        var restoredField = serdeProcessor.bytesToObject(plaintext);
        LOGGER.debug("restored field: {}",restoredField);
        return restoredField;
      }
    } catch (Exception e) {
      throw new DataException("error: "+cipherMode+" of field path '"+matchedPath+"' having data '"+object+ "' failed unexpectedly",e);
    }
  }

  public List<?> processListField(List<?> list,String matchedPath) {
    return list.stream().map(e -> {
          if(e instanceof List)
            return processListField((List<?>)e,matchedPath);
          if(e instanceof Map)
            return processMapField((Map<?,?>)e,matchedPath);
          return processField(e,matchedPath);
        }
    ).collect(Collectors.toList());
  }

  public Map<?, ?> processMapField(Map<?, ?> map,String matchedPath) {
    return map.entrySet().stream()
        .map(e -> {
          var pathUpdate = matchedPath+pathDelimiter+e.getKey();
            if(e.getValue() instanceof List)
              return new AbstractMap.SimpleEntry<>(e.getKey(),processListField((List<?>)e.getValue(),pathUpdate));
            if(e.getValue() instanceof Map)
              return new AbstractMap.SimpleEntry<>(e.getKey(), processMapField((Map<?,?>)e.getValue(),pathUpdate));
            return new AbstractMap.SimpleEntry<>(e.getKey(), processField(e.getValue(),pathUpdate));
        }).collect(LinkedHashMap::new,(lhm,e) -> lhm.put(e.getKey(),e.getValue()), HashMap::putAll);
  }

  private FieldMetaData determineFieldMetaData(Object object, String fieldPath) {
    return Optional.ofNullable(fieldConfig.get(fieldPath))
        .map(fc -> new FieldMetaData(
            fc.getAlgorithm().orElseGet(() -> config.getString(KryptoniteSettings.CIPHER_ALGORITHM)),
            Optional.ofNullable(object).map(o -> o.getClass().getName()).orElse(""),
            fc.getKeyId().orElseGet(() -> config.getString(KryptoniteSettings.CIPHER_DATA_KEY_IDENTIFIER))
            )
        ).orElseGet(
            () -> new FieldMetaData(
                config.getString(KryptoniteSettings.CIPHER_ALGORITHM),
                Optional.ofNullable(object).map(o -> o.getClass().getName()).orElse(""),
                config.getString(KryptoniteSettings.CIPHER_DATA_KEY_IDENTIFIER)
            )
        );
  }

}
