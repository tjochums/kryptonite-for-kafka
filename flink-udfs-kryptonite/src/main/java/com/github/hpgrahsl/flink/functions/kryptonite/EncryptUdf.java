/*
 * Copyright (c) 2024. Hans-Peter Grahsl (grahslhp@gmail.com)
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

package com.github.hpgrahsl.flink.functions.kryptonite;

import java.util.Optional;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.InputGroup;
import org.apache.flink.table.functions.FunctionContext;

import com.github.hpgrahsl.kryptonite.FieldMetaData;
import com.github.hpgrahsl.kryptonite.KryptoniteException;
import com.github.hpgrahsl.kryptonite.config.KryptoniteSettings;

public class EncryptUdf extends AbstractCipherFieldUdf {

    private transient String defaultCipherDataKeyIdentifier;
    
    @Override
    public void open(FunctionContext context) throws Exception {
        super.open(context);
        var cipherDataKeyIdentifier = getConfigurationSetting(KryptoniteSettings.CIPHER_DATA_KEY_IDENTIFIER);
        if (cipherDataKeyIdentifier == null || KryptoniteSettings.CIPHER_DATA_KEY_IDENTIFIER_DEFAULT.equals(cipherDataKeyIdentifier)) {
            throw new KryptoniteException("missing required setting for "+ KryptoniteSettings.CIPHER_DATA_KEY_IDENTIFIER_DEFAULT
                + " which is neither defined by environment variables nor by job parameters");
        }
        defaultCipherDataKeyIdentifier = cipherDataKeyIdentifier;
    }

    public String eval(@DataTypeHint(inputGroup = InputGroup.ANY) final Object data) {
        var fmd = new FieldMetaData(
            KryptoniteSettings.CIPHER_ALGORITHM_DEFAULT,
            Optional.ofNullable(data).map(o -> o.getClass().getName()).orElse(""),
            defaultCipherDataKeyIdentifier
        );
        return encryptData(data,fmd);
    }

    public String eval(
        @DataTypeHint(inputGroup = InputGroup.ANY) final Object data,
        String cipherDataKeyIdentifier, String cipherAlgorithm) {
        if (cipherDataKeyIdentifier == null || cipherAlgorithm == null) {
            throw new IllegalArgumentException("error: cipher data key identifier and/or cipher algorithm must not be null");
        }
        var fmd = new FieldMetaData(
            cipherAlgorithm,
            Optional.ofNullable(data).map(o -> o.getClass().getName()).orElse(""),
            cipherDataKeyIdentifier
        );
        return encryptData(data,fmd);
    }

}
