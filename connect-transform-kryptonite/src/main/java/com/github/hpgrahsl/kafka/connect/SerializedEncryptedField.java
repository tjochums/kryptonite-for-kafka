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

package com.github.hpgrahsl.kafka.connect;

import java.util.Arrays;

import com.github.hpgrahsl.kryptonite.EncryptedField;
import com.github.hpgrahsl.kryptonite.PayloadMetaData;

public class SerializedEncryptedField {

  public PayloadMetaData metaData;
  public byte[] ciphertext;

  public SerializedEncryptedField() {
  }

    public SerializedEncryptedField(EncryptedField encryptedField) {
        this.metaData = encryptedField.getMetaData();
        this.ciphertext = encryptedField.ciphertext();
    }

  public SerializedEncryptedField(PayloadMetaData metaData, byte[] ciphertext) {
    this.metaData = metaData;
    this.ciphertext = ciphertext;
  }

  public byte[] getCiphertext() {
    return ciphertext;
  }

  public PayloadMetaData getMetaData() {
    return metaData;
  }
}
