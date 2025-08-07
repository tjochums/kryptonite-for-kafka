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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.hpgrahsl.kafka.connect.transforms.kryptonite.CipherField.FieldMode;
import com.github.hpgrahsl.kafka.connect.transforms.kryptonite.messagepack.MessagePackSerdesProcessor;
import com.github.hpgrahsl.kryptonite.Kryptonite.CipherSpec;
import com.github.hpgrahsl.kryptonite.config.KryptoniteSettings;
import com.github.hpgrahsl.kryptonite.config.KryptoniteSettings.KekType;
import com.github.hpgrahsl.kryptonite.config.KryptoniteSettings.KeySource;
import com.github.hpgrahsl.kryptonite.config.KryptoniteSettings.KmsType;
import com.github.hpgrahsl.kryptonite.crypto.tink.TinkAesGcm;
import com.github.hpgrahsl.kryptonite.crypto.tink.TinkAesGcmSiv;
import com.github.hpgrahsl.kryptonite.serdes.KryoSerdeProcessor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CipherFieldSmtFunctionalTest {

  @Nested
  class WithoutCloudKmsConfig {
    @ParameterizedTest
    @MethodSource("com.github.hpgrahsl.kafka.connect.transforms.kryptonite.CipherFieldSmtFunctionalTest#generateValidParamsWithoutCloudKms")
    @DisplayName("apply SMT decrypt(encrypt(plaintext)) = plaintext for schemaless record with param combinations")
    void encryptDecryptSchemalessRecordTest(String cipherDataKeys,FieldMode fieldMode, CipherSpec cipherSpec, String keyId1, String keyId2, 
        KeySource keySource, KmsType kmsType, String kmsConfig, KekType kekType, String kekConfig, String kekUri, String serdesProcessorClass) {

      performSchemalessRecordTest(cipherDataKeys, fieldMode, cipherSpec, keyId1, keyId2, keySource, kmsType, kmsConfig, kekType, kekConfig, kekUri, serdesProcessorClass);
    }
   
    @ParameterizedTest
    @MethodSource("com.github.hpgrahsl.kafka.connect.transforms.kryptonite.CipherFieldSmtFunctionalTest#generateValidParamsWithoutCloudKms")
    @DisplayName("apply SMT decrypt(encrypt(plaintext)) = plaintext for schemaful record with param combinations")
    void encryptDecryptSchemafulRecordTest(String cipherDataKeys,FieldMode fieldMode, CipherSpec cipherSpec, String keyId1, String keyId2, 
        KeySource keySource, KmsType kmsType, String kmsConfig, KekType kekType, String kekConfig, String kekUri, String serdesProcessorClass) {

      performSchemafulRecordTest(cipherDataKeys, fieldMode, cipherSpec, keyId1, keyId2, keySource, kmsType, kmsConfig, kekType, kekConfig, kekUri, serdesProcessorClass);
  }

  @Nested
  @EnabledIfSystemProperty(named = "cloud.kms.tests", matches = "true")
  class WithCloudKmsConfig {
    @ParameterizedTest
    @MethodSource("com.github.hpgrahsl.kafka.connect.transforms.kryptonite.CipherFieldSmtFunctionalTest#generateValidParamsWithCloudKms")
    @DisplayName("apply SMT decrypt(encrypt(plaintext)) = plaintext for schemaless record with param combinations")
    void encryptDecryptSchemalessRecordTest(String cipherDataKeys,FieldMode fieldMode, CipherSpec cipherSpec, String keyId1, String keyId2, 
        KeySource keySource, KmsType kmsType, String kmsConfig, KekType kekType, String kekConfig, String kekUri, String serdesProcessorClass) {

      performSchemalessRecordTest(cipherDataKeys, fieldMode, cipherSpec, keyId1, keyId2, keySource, kmsType, kmsConfig, kekType, kekConfig, kekUri, serdesProcessorClass);
    }
   
    @ParameterizedTest
    @MethodSource("com.github.hpgrahsl.kafka.connect.transforms.kryptonite.CipherFieldSmtFunctionalTest#generateValidParamsWithCloudKms")
    @DisplayName("apply SMT decrypt(encrypt(plaintext)) = plaintext for schemaful record with param combinations")
    void encryptDecryptSchemafulRecordTest(String cipherDataKeys,FieldMode fieldMode, CipherSpec cipherSpec, String keyId1, String keyId2, 
        KeySource keySource, KmsType kmsType, String kmsConfig, KekType kekType, String kekConfig, String kekUri, String serdesProcessorClass) {

      performSchemafulRecordTest(cipherDataKeys, fieldMode, cipherSpec, keyId1, keyId2, keySource, kmsType, kmsConfig, kekType, kekConfig, kekUri, serdesProcessorClass);
    }
   }

  @SuppressWarnings("unchecked")
  void performSchemalessRecordTest(String cipherDataKeys,FieldMode fieldMode, CipherSpec cipherSpec, String keyId1, String keyId2, 
        KeySource keySource, KmsType kmsType, String kmsConfig, KekType kekType, String kekConfig, String kekUri, String serdesProcessorClass)  {
      
      var encProps = new HashMap<String, Object>();
      encProps.put(KryptoniteSettings.CIPHER_MODE, "ENCRYPT");
      encProps.put(KryptoniteSettings.FIELD_CONFIG,
              "["
              + "    {\"name\":\"id\",\"keyId\":\""+keyId1+"\"},"
              + "    {\"name\":\"myString\",\"keyId\":\""+keyId2+"\"},"
              + "    {\"name\":\"myInt32\"},"
              + "    {\"name\":\"myInt64\"},"
              + "    {\"name\":\"myBoolean\",\"keyId\":\""+keyId2+"\"},"
              + "    {\"name\":\"mySubDoc1\",\"keyId\":\""+keyId1+"\"},"
              + "    {\"name\":\"myArray1\"},"
              + "    {\"name\":\"mySubDoc2\",\"keyId\":\""+keyId1+"\"},"
              + "    {\"name\":\"myBytes\",\"keyId\":\""+keyId2+"\"}"
              + "]"
      );
      encProps.put(KryptoniteSettings.CIPHER_ALGORITHM,cipherSpec.getName());
      encProps.put(KryptoniteSettings.CIPHER_DATA_KEYS,cipherDataKeys);
      encProps.put(KryptoniteSettings.CIPHER_DATA_KEY_IDENTIFIER,keyId1);
      encProps.put(KryptoniteSettings.FIELD_MODE,fieldMode.name());
      encProps.put(KryptoniteSettings.KEY_SOURCE,keySource.name());
      encProps.put(KryptoniteSettings.KMS_TYPE,kmsType.name());
      encProps.put(KryptoniteSettings.KMS_CONFIG,kmsConfig);
      encProps.put(KryptoniteSettings.KEK_TYPE,kekType.name());
      encProps.put(KryptoniteSettings.KEK_CONFIG,kekConfig);
      encProps.put(KryptoniteSettings.KEK_URI,kekUri);
      encProps.put(CipherField.SERDES_TYPE, serdesProcessorClass);
  
      var encryptTransform = new CipherField.Value<SourceRecord>();
      encryptTransform.configure(encProps);
      var encryptedRecord = (Map<String,Object>)encryptTransform.apply(
          new SourceRecord(null,null,"some-kafka-topic",0,null,TestFixtures.TEST_OBJ_MAP_1)
      ).value();
  
      if(fieldMode == FieldMode.OBJECT) {
        assertAll(
            () -> assertEquals(String.class, encryptedRecord.get("mySubDoc1").getClass()),
            () -> assertEquals(String.class, encryptedRecord.get("myArray1").getClass()),
            () -> assertEquals(String.class, encryptedRecord.get("mySubDoc2").getClass())
        );
      } else {
        assertAll(
            () -> assertAll(
                () -> assertTrue(encryptedRecord.get("mySubDoc1") instanceof Map),
                () -> assertEquals(1, ((Map<?,?>)encryptedRecord.get("mySubDoc1")).size())
            ),
            () -> assertAll(
                () -> assertTrue(encryptedRecord.get("myArray1") instanceof List),
                () -> assertEquals(4, ((List<?>)encryptedRecord.get("myArray1")).size())
            ),
            () -> assertAll(
                () -> assertTrue(encryptedRecord.get("mySubDoc2") instanceof Map),
                () -> assertEquals(3, ((Map<?,?>)encryptedRecord.get("mySubDoc2")).size())
            )
        );
      }
  
      var decProps = new HashMap<String, Object>();
      decProps.put(KryptoniteSettings.CIPHER_MODE, "DECRYPT");
      decProps.put(KryptoniteSettings.FIELD_CONFIG,
          "["
              + "    {\"name\":\"id\"},"
              + "    {\"name\":\"myString\"},"
              + "    {\"name\":\"myInt32\"},"
              + "    {\"name\":\"myInt64\"},"
              + "    {\"name\":\"myBoolean\"},"
              + "    {\"name\":\"mySubDoc1\"},"
              + "    {\"name\":\"myArray1\"},"
              + "    {\"name\":\"mySubDoc2\"},"
              + "    {\"name\":\"myBytes\"}"
              + "]"
      );
      decProps.put(KryptoniteSettings.CIPHER_ALGORITHM,encProps.get(KryptoniteSettings.CIPHER_ALGORITHM));
      decProps.put(KryptoniteSettings.CIPHER_DATA_KEYS,encProps.get(KryptoniteSettings.CIPHER_DATA_KEYS));
      decProps.put(KryptoniteSettings.FIELD_MODE,fieldMode.name());
      decProps.put(KryptoniteSettings.KEY_SOURCE,encProps.get(KryptoniteSettings.KEY_SOURCE));
      decProps.put(KryptoniteSettings.KMS_TYPE,encProps.get(KryptoniteSettings.KMS_TYPE));
      decProps.put(KryptoniteSettings.KMS_CONFIG,encProps.get(KryptoniteSettings.KMS_CONFIG));
      decProps.put(KryptoniteSettings.KEK_TYPE,encProps.get(KryptoniteSettings.KEK_TYPE));
      decProps.put(KryptoniteSettings.KEK_CONFIG,encProps.get(KryptoniteSettings.KEK_CONFIG));
      decProps.put(KryptoniteSettings.KEK_URI,encProps.get(KryptoniteSettings.KEK_URI));
      decProps.put(CipherField.SERDES_TYPE, encProps.get(CipherField.SERDES_TYPE));
  
      var decryptTransform = new CipherField.Value<SinkRecord>();
      decryptTransform.configure(decProps);
      var decryptedRecord = (Map<String,Object>)decryptTransform.apply(
          new SinkRecord("some-kafka-topic",0,null,null,null,encryptedRecord,0)
      ).value();
  
      assertAllResultingFieldsSchemalessRecord(TestFixtures.TEST_OBJ_MAP_1,decryptedRecord);
  }

  void performSchemafulRecordTest(String cipherDataKeys,FieldMode fieldMode, CipherSpec cipherSpec, String keyId1, String keyId2, 
        KeySource keySource, KmsType kmsType, String kmsConfig, KekType kekType, String kekConfig, String kekUri, String serdesProcessorClass) {
var encProps = new HashMap<String, Object>();
      encProps.put(KryptoniteSettings.CIPHER_MODE, "ENCRYPT");
      encProps.put(KryptoniteSettings.FIELD_CONFIG,
          "["
              + "    {\"name\":\"id\"},"
              + "    {\"name\":\"myString\",\"keyId\":\""+keyId1+"\"},"
              + "    {\"name\":\"myInt32\",\"keyId\":\""+keyId2+"\"},"
              + "    {\"name\":\"myInt64\",\"keyId\":\""+keyId2+"\"},"
              + "    {\"name\":\"myBoolean\"},"
              + "    {\"name\":\"mySubDoc1\",\"keyId\":\""+keyId1+"\"},"
              + "    {\"name\":\"mySubDoc1.myString\",\"keyId\":\""+keyId1+"\"},"
              + "    {\"name\":\"myArray1\",\"keyId\":\""+keyId2+"\"},"
              + "    {\"name\":\"mySubDoc2\"},"
              + "    {\"name\":\"myBytes\",\"keyId\":\""+keyId1+"\"}"
              + "]"
      );
      encProps.put(KryptoniteSettings.CIPHER_ALGORITHM,cipherSpec.getName());
      encProps.put(KryptoniteSettings.CIPHER_DATA_KEYS,cipherDataKeys);
      encProps.put(KryptoniteSettings.CIPHER_DATA_KEY_IDENTIFIER,keyId1);
      encProps.put(KryptoniteSettings.FIELD_MODE,fieldMode.name());
      encProps.put(KryptoniteSettings.KEY_SOURCE,keySource.name());
      encProps.put(KryptoniteSettings.KMS_TYPE,kmsType.name());
      encProps.put(KryptoniteSettings.KMS_CONFIG,kmsConfig);
      encProps.put(KryptoniteSettings.KEK_TYPE,kekType.name());
      encProps.put(KryptoniteSettings.KEK_CONFIG,kekConfig);
      encProps.put(KryptoniteSettings.KEK_URI,kekUri);
      encProps.put(CipherField.SERDES_TYPE, serdesProcessorClass);
  
      var encryptTransform = new CipherField.Value<SourceRecord>();
      encryptTransform.configure(encProps);
      var encryptedRecord = (Struct)encryptTransform.apply(
          new SourceRecord(null,null,"some-kafka-topic",0,TestFixtures.TEST_OBJ_SCHEMA_1,TestFixtures.TEST_OBJ_STRUCT_1)
      ).value();
  
      if (fieldMode == FieldMode.OBJECT) {
        assertAll(
            () -> assertEquals(String.class, encryptedRecord.get("mySubDoc1").getClass()),
            () -> assertEquals(String.class,encryptedRecord.get("myArray1").getClass()),
            () -> assertEquals(String.class,encryptedRecord.get("mySubDoc2").getClass())
        );
      } else {
        assertAll(
            () -> assertAll(
                () -> assertTrue(encryptedRecord.get("mySubDoc1") instanceof Struct),
                () -> assertEquals(1, ((Struct)encryptedRecord.get("mySubDoc1")).schema().fields().size()),
                () -> assertTrue(((Struct)encryptedRecord.get("mySubDoc1")).get("myString") instanceof String)
            ),
            () -> assertAll(
                () -> assertTrue(encryptedRecord.get("myArray1") instanceof List),
                () -> assertEquals(4, ((List<?>)encryptedRecord.get("myArray1")).size())
            ),
            () -> assertAll(
                () -> assertTrue(encryptedRecord.get("mySubDoc2") instanceof Map),
                () -> assertEquals(3, ((Map<?,?>)encryptedRecord.get("mySubDoc2")).size())
            )
        );
      }
  
      var decProps = new HashMap<String, Object>();
      decProps.put(KryptoniteSettings.CIPHER_MODE, "DECRYPT");
      decProps.put(KryptoniteSettings.FIELD_CONFIG,
          "["
              + "    {\"name\":\"id\",\"schema\": {\"type\": \"STRING\"}},"
              + "    {\"name\":\"myString\",\"schema\": {\"type\": \"STRING\"}},"
              + "    {\"name\":\"myInt32\",\"schema\": {\"type\": \"INT32\"}},"
              + "    {\"name\":\"myInt64\",\"schema\": {\"type\": \"INT64\"}},"
              + "    {\"name\":\"myBoolean\",\"schema\": {\"type\": \"BOOLEAN\"}},"
              + "    {\"name\":\"mySubDoc1\",\"schema\": { \"type\": \"STRUCT\",\"fields\": [ { \"name\": \"myString\", \"schema\": { \"type\": \"STRING\"}}]}},"
              + "    {\"name\":\"mySubDoc1.myString\",\"schema\": {\"type\": \"STRING\"}},"
              + "    {\"name\":\"myArray1\",\"schema\": {\"type\": \"ARRAY\",\"valueSchema\": {\"type\": \"STRING\"}}},"
              + "    {\"name\":\"mySubDoc2\",\"schema\": { \"type\": \"MAP\", \"keySchema\": { \"type\": \"STRING\" }, \"valueSchema\": { \"type\": \"INT32\"}}},"
              + "    {\"name\":\"myBytes\",\"schema\": {\"type\": \"BYTES\"}}"
              + "]"
      );
      decProps.put(KryptoniteSettings.CIPHER_ALGORITHM,encProps.get(KryptoniteSettings.CIPHER_ALGORITHM));
      decProps.put(KryptoniteSettings.CIPHER_DATA_KEYS,encProps.get(KryptoniteSettings.CIPHER_DATA_KEYS));
      decProps.put(KryptoniteSettings.FIELD_MODE,fieldMode.name());
      decProps.put(KryptoniteSettings.KEY_SOURCE,encProps.get(KryptoniteSettings.KEY_SOURCE));
      decProps.put(KryptoniteSettings.KMS_TYPE,encProps.get(KryptoniteSettings.KMS_TYPE));
      decProps.put(KryptoniteSettings.KMS_CONFIG,encProps.get(KryptoniteSettings.KMS_CONFIG));
      decProps.put(KryptoniteSettings.KEK_TYPE,encProps.get(KryptoniteSettings.KEK_TYPE));
      decProps.put(KryptoniteSettings.KEK_CONFIG,encProps.get(KryptoniteSettings.KEK_CONFIG));
      decProps.put(KryptoniteSettings.KEK_URI,encProps.get(KryptoniteSettings.KEK_URI));
      decProps.put(CipherField.SERDES_TYPE, encProps.get(CipherField.SERDES_TYPE));
  
      var decryptTransform = new CipherField.Value<SinkRecord>();
      decryptTransform.configure(decProps);
      var decryptedRecord = (Struct)decryptTransform.apply(
          new SinkRecord("some-kafka-topic",0,null,null,encryptedRecord.schema(),encryptedRecord,0)
      ).value();
  
      assertAllResultingFieldsSchemafulRecord(TestFixtures.TEST_OBJ_STRUCT_1,decryptedRecord);
    }
  }
  

  void assertAllResultingFieldsSchemalessRecord(Map<String,Object> expected, Map<String,Object> actual) {
    assertAll(
            expected.entrySet().stream().map(
                e -> e.getValue() instanceof byte[]
                    ? () -> assertArrayEquals((byte[])e.getValue(),(byte[])actual.get(e.getKey()))
                    : () -> assertEquals(e.getValue(),actual.get(e.getKey()))
            )
    );
  }

  void assertAllResultingFieldsSchemafulRecord(Struct expected, Struct actual) {
    assertAll(
        Stream.concat(
            Stream.<Executable>of(() -> assertEquals(expected.schema(),actual.schema())),
            expected.schema().fields().stream().map(
                f -> f.schema().equals(Schema.BYTES_SCHEMA)
                    ? () -> assertArrayEquals((byte[])expected.get(f.name()),(byte[])actual.get(f.name()))
                    : () -> assertEquals(expected.get(f.name()),actual.get(f.name()))
            )
        )
    );
  }

  static List<Arguments> generateValidParamsWithoutCloudKms() {
    return List.of(
        Arguments.of(
          TestFixtures.CIPHER_DATA_KEYS_CONFIG,FieldMode.ELEMENT,CipherSpec.fromName(TinkAesGcm.CIPHER_ALGORITHM),"keyA","keyB",
          KeySource.CONFIG,KmsType.NONE,"{}",KekType.NONE,"{}","",KryoSerdeProcessor.class.getName()
        ),
        Arguments.of(
          TestFixtures.CIPHER_DATA_KEYS_CONFIG,FieldMode.OBJECT,CipherSpec.fromName(TinkAesGcmSiv.CIPHER_ALGORITHM),"key9","key8",
          KeySource.CONFIG,KmsType.NONE,"{}",KekType.NONE,"{}","", KryoSerdeProcessor.class.getName()
        ),
        Arguments.of(
          TestFixtures.CIPHER_DATA_KEYS_CONFIG,FieldMode.ELEMENT,CipherSpec.fromName(TinkAesGcm.CIPHER_ALGORITHM),"keyA","keyB",
          KeySource.CONFIG,KmsType.NONE,"{}",KekType.NONE,"{}","",MessagePackSerdesProcessor.class.getName()
        ),
        Arguments.of(
          TestFixtures.CIPHER_DATA_KEYS_CONFIG,FieldMode.OBJECT,CipherSpec.fromName(TinkAesGcmSiv.CIPHER_ALGORITHM),"key9","key8",
          KeySource.CONFIG,KmsType.NONE,"{}",KekType.NONE,"{}","",MessagePackSerdesProcessor.class.getName()
        )
    );
  }

  static List<Arguments> generateValidParamsWithCloudKms() throws IOException {
    var credentials = TestFixturesCloudKms.readCredentials();
    return List.of(
        Arguments.of(
          TestFixtures.CIPHER_DATA_KEYS_CONFIG_ENCRYPTED,FieldMode.OBJECT,CipherSpec.fromName(TinkAesGcm.CIPHER_ALGORITHM),"keyX","keyY",
          KeySource.CONFIG_ENCRYPTED,KmsType.NONE,"{}",
          KekType.GCP,credentials.getProperty("test.kek.config"),credentials.getProperty("test.kek.uri"),
          KryoSerdeProcessor.class.getName()
        ),
        Arguments.of(
          TestFixtures.CIPHER_DATA_KEYS_CONFIG_ENCRYPTED,FieldMode.ELEMENT,CipherSpec.fromName(TinkAesGcmSiv.CIPHER_ALGORITHM),"key1","key0",
          KeySource.CONFIG_ENCRYPTED,KmsType.NONE,"{}",
          KekType.GCP,credentials.getProperty("test.kek.config"),credentials.getProperty("test.kek.uri"),
          KryoSerdeProcessor.class.getName()
        ),
        Arguments.of(
          TestFixtures.CIPHER_DATA_KEYS_EMPTY,FieldMode.ELEMENT,CipherSpec.fromName(TinkAesGcm.CIPHER_ALGORITHM),"keyA","keyB",
          KeySource.KMS,KmsType.AZ_KV_SECRETS,credentials.getProperty("test.kms.config"),
          KekType.NONE,"{}","",
          KryoSerdeProcessor.class.getName()
        ),
        Arguments.of(
          TestFixtures.CIPHER_DATA_KEYS_EMPTY,FieldMode.OBJECT,CipherSpec.fromName(TinkAesGcmSiv.CIPHER_ALGORITHM),"key9","key8",
          KeySource.KMS,KmsType.AZ_KV_SECRETS,credentials.getProperty("test.kms.config"),
          KekType.NONE,"{}","",
          KryoSerdeProcessor.class.getName()
        ),
        Arguments.of(
          TestFixtures.CIPHER_DATA_KEYS_EMPTY,FieldMode.ELEMENT,CipherSpec.fromName(TinkAesGcm.CIPHER_ALGORITHM),"keyX","keyY",
          KeySource.KMS_ENCRYPTED,KmsType.AZ_KV_SECRETS,credentials.getProperty("test.kms.config.encrypted"),
          KekType.GCP,credentials.getProperty("test.kek.config"),credentials.getProperty("test.kek.uri"),
          KryoSerdeProcessor.class.getName()
        ),
        Arguments.of(
          TestFixtures.CIPHER_DATA_KEYS_EMPTY,FieldMode.OBJECT,CipherSpec.fromName(TinkAesGcmSiv.CIPHER_ALGORITHM),"key1","key0",
          KeySource.KMS_ENCRYPTED,KmsType.AZ_KV_SECRETS,credentials.getProperty("test.kms.config.encrypted"),
          KekType.GCP,credentials.getProperty("test.kek.config"),credentials.getProperty("test.kek.uri"),
          KryoSerdeProcessor.class.getName()
        )
    );
  }

}
