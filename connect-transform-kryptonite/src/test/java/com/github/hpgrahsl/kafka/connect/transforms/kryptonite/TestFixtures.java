/*
 * Copyright (c) 2023. Hans-Peter Grahsl (grahslhp@gmail.com)
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;

public class TestFixtures {

    public static final String CIPHER_DATA_KEYS_EMPTY = "[]";

    public static final String CIPHER_DATA_KEYS_CONFIG = "["
            + "{\"identifier\":\"keyA\","
            + "\"material\":{"
            + "\"primaryKeyId\":1000000001,"
            + "\"key\":["
            + "{\"keyData\":"
            + "{\"typeUrl\":\"type.googleapis.com/google.crypto.tink.AesGcmKey\","
            + "\"value\":\"GhDRulECKAC8/19NMXDjeCjK\","
            + "\"keyMaterialType\":\"SYMMETRIC\"},"
            + "\"status\":\"ENABLED\","
            + "\"keyId\":1000000001,"
            + "\"outputPrefixType\":\"TINK\""
            + "}"
            + "]"
            + "}"
            + "},"
            + "{\"identifier\":\"keyB\","
            + "\"material\":{"
            + "\"primaryKeyId\":1000000002,"
            + "\"key\":["
            + "{\"keyData\":"
            + "{\"typeUrl\":\"type.googleapis.com/google.crypto.tink.AesGcmKey\","
            + "\"value\":\"GiBIZWxsbyFXb3JsZEZVQ0sxYWJjZGprbCQxMjM0NTY3OA==\","
            + "\"keyMaterialType\":\"SYMMETRIC\"},"
            + "\"status\":\"ENABLED\","
            + "\"keyId\":1000000002,"
            + "\"outputPrefixType\":\"TINK\""
            + "}"
            + "]"
            + "}"
            + "},"
            + "{\"identifier\":\"key9\","
            + "\"material\":{"
            + "\"primaryKeyId\":1000000003,"
            + "\"key\":["
            + "{\"keyData\":"
            + "{\"typeUrl\":\"type.googleapis.com/google.crypto.tink.AesSivKey\","
            + "\"value\":\"EkByiHi3H9shy2FO5UWgStNMmgqF629esenhnm0wZZArUkEU1/9l9J3ajJQI0GxDwzM1WFZK587W0xVB8KK4dqnz\","
            + "\"keyMaterialType\":\"SYMMETRIC\"},"
            + "\"status\":\"ENABLED\","
            + "\"keyId\":1000000003,"
            + "\"outputPrefixType\":\"TINK\""
            + "}"
            + "]"
            + "}"
            + "},"
            + "{\"identifier\":\"key8\","
            + "\"material\":{"
            + "\"primaryKeyId\":3015563227,"
            + "\"key\":["
            + "{\"keyData\":"
            + "{\"typeUrl\":\"type.googleapis.com/google.crypto.tink.AesSivKey\","
            + "\"value\":\"EkBWT3ZL7DmAN91erW3xAzMFDWMaQx34Su3VlaMiTWzjVDbKsH3IRr2HQFnaMvvVz2RH/+eYXn3zvAzWJbReCto/\","
            + "\"keyMaterialType\":\"SYMMETRIC\"},"
            + "\"status\":\"ENABLED\","
            + "\"keyId\":3015563227,"
            + "\"outputPrefixType\":\"TINK\""
            + "}"
            + "]"
            + "}"
            + "}"
            + "]";

    public static final String CIPHER_DATA_KEYS_CONFIG_ENCRYPTED = "["
            + "    {"
            + "        \"identifier\": \"keyX\","
            + "        \"material\": {"
            + "            \"encryptedKeyset\": \"CiQAxVFVnXQGci+bKTFoJwYENusDTbOmB+akfwJH3V9yRJXu8HwShQEAjEQQ+iL+3/Y8/Q7PcTgSJTAr8yUkNbFf7715soTCa9pfxFLxv78aZOwONmIktL1ntM8+uQfOt7Ka3nYxrrzitJORFSh8pIQBE7B1vcbhCJQk5+8mSnNcYcAgk90Es8qAiVeptfVaw0VLWok4/ejnsogaD0gLEOeR/4FJfKELj7LLUgLf\","
            + "            \"keysetInfo\": {"
            + "                \"primaryKeyId\": 1070658096,"
            + "                \"keyInfo\": ["
            + "                    {"
            + "                        \"typeUrl\": \"type.googleapis.com/google.crypto.tink.AesGcmKey\","
            + "                        \"status\": \"ENABLED\","
            + "                        \"keyId\": 1070658096,"
            + "                        \"outputPrefixType\": \"TINK\""
            + "                    }"
            + "                ]"
            + "            }"
            + "        }"
            + "    },"
            + "    {"
            + "        \"identifier\": \"keyY\","
            + "        \"material\": {"
            + "            \"encryptedKeyset\": \"CiQAxVFVnYb69VZimvSnRRsxEhFMbHHTW4BaGHVMLKTZrXViaPwSlAEAjEQQ+iDiddqY3C/jHIjAsU5Ph+gQULl4Xi6mmKusbjTiBzQkIwuXg+nE3Y1C0GFSl7LEqtBQuyb7L0w5CsjGRBoRLhyqJUfil92AAb1yC7j+ArxvcV+T970KPyVG9QdDcJ2fiYqNqwLf8dwqPP0n+nAHksF0DpQf6yg3vslox0GIVxauojPdbq9pFuQUTZyGVs/a\","
            + "            \"keysetInfo\": {"
            + "                \"primaryKeyId\": 1053599701,"
            + "                \"keyInfo\": ["
            + "                    {"
            + "                        \"typeUrl\": \"type.googleapis.com/google.crypto.tink.AesGcmKey\","
            + "                        \"status\": \"ENABLED\","
            + "                        \"keyId\": 1053599701,"
            + "                        \"outputPrefixType\": \"TINK\""
            + "                    }"
            + "                ]"
            + "            }"
            + "        }"
            + "    },"
            + "    {"
            + "        \"identifier\": \"key1\","
            + "        \"material\": {"
            + "            \"encryptedKeyset\": \"CiQAxVFVnfzb8jhDAfGwquh5lxU0R+blpz7DP/00cF8aq4gLtuIStwEAjEQQ+vGbPfFxa07XkaMHEP7TU9PGsd0l38St3CckCrgVnzYidrX3H4XtN58VUFN5eTXcIq3Rx2gsx/RaSpe85o+MP33woGM9Va4s/INyjeeCQVsJnoWU1EqLchfU8BnL0dAXwajj3Bj5X3oL8k22TNome2ywDKjrXz4AU75QYNwta000SmRxlY7UbmR1Mv38Nrs2qvy5P8B6fOYPusamtFJkJWG/dxJpoS+4URWcCc2yfrCY4yg=\","
            + "            \"keysetInfo\": {"
            + "                \"primaryKeyId\": 1932849140,"
            + "                \"keyInfo\": ["
            + "                    {"
            + "                        \"typeUrl\": \"type.googleapis.com/google.crypto.tink.AesSivKey\","
            + "                        \"status\": \"ENABLED\","
            + "                        \"keyId\": 1932849140,"
            + "                        \"outputPrefixType\": \"TINK\""
            + "                    }"
            + "                ]"
            + "            }"
            + "        }"
            + "    },"
            + "    {"
            + "        \"identifier\": \"key0\","
            + "        \"material\": {"
            + "            \"encryptedKeyset\": \"CiQAxVFVnUUw/pZSdQXtve5M+wgVBlGqPJwuf4X9SmWB4B1u4OQStQEAjEQQ+iXK6u/gbul2QpS0mIO2wqUwiOBHz5C+MZ2JKyjKlzMA8yGlyqoN54qhRJA5IazFUIJVWNigXBDUU0km1Bm1oFDdzb6pMVZY5HDH26AiyJZOQSjglLAz+SoYR3DjHapkWNDv2QGacP/5qCwC7zOCc89pZxEDtT+eJvVsJqUHV6VGJYnIVYQBwxBAzy3XsPWm6IARj5VHtLwOTuM3UNP96Bwk/jzR6Ot+izXASRTeHomP\","
            + "            \"keysetInfo\": {"
            + "                \"primaryKeyId\": 151824924,"
            + "                \"keyInfo\": ["
            + "                    {"
            + "                        \"typeUrl\": \"type.googleapis.com/google.crypto.tink.AesSivKey\","
            + "                        \"status\": \"ENABLED\","
            + "                        \"keyId\": 151824924,"
            + "                        \"outputPrefixType\": \"TINK\""
            + "                    }"
            + "                ]"
            + "            }"
            + "        }"
            + "    }"
            + "]";

    static Map<String, Object> TEST_OBJ_MAP_1;
    static Schema TEST_OBJ_SCHEMA_1;
    static Struct TEST_OBJ_STRUCT_1;

    static {
        TEST_OBJ_SCHEMA_1 = SchemaBuilder.struct()
          .field("HRCo", Schema.INT32_SCHEMA)
          .field("HRRef", Schema.INT32_SCHEMA)
          .field("EffectiveDate",Schema.INT64_SCHEMA) // 2011-11-01T00:00:00Z
          .field("Type",Schema.STRING_SCHEMA)
          .field("OldSalary",Schema.STRING_SCHEMA)
          .field("NewSalary",Schema.STRING_SCHEMA)
          .field("NewPositionCode",Schema.STRING_SCHEMA)
          //.field("NextDate",Schema.INT64_SCHEMA)
          .field("UpdatedYN",Schema.STRING_SCHEMA)
          .field("HistSeq",Schema.INT32_SCHEMA)
          .field("CalcYN",Schema.STRING_SCHEMA)
          .field("BatchId",Schema.INT32_SCHEMA)
          //.field("InUseBatchId",Schema.INT32_SCHEMA)
          //.field("InUseMth",Schema.INT64_SCHEMA)
          //.field("UniqueAttchID",Schema.STRING_SCHEMA)
          .field("KeyID",Schema.INT64_SCHEMA)
          .field("__op",Schema.STRING_SCHEMA)
          .field("__ts_ms", Schema.INT64_SCHEMA) // 2024-03-31T12:18:39.894Z
          .field("__deleted",Schema.STRING_SCHEMA)
          .field("EnterpriseId",Schema.STRING_SCHEMA)
          .build();

        TEST_OBJ_STRUCT_1 = new Struct(TEST_OBJ_SCHEMA_1)
          .put("HRCo",253)
          .put("HRRef",2)
          .put("EffectiveDate",1320105600000L) // 2011-11-01T00:00:00Z
          .put("Type","H")
          //.put("OldSalary","AA==")
          //.put("NewSalary","NWfg")
          .put("NewPositionCode","CARP")
          //.put("NextDate",null)
          .put("UpdatedYN","Y")
          .put("HistSeq",1)
          .put("CalcYN","N")
          .put("BatchId",3)
          //.put("InUseBatchId",null)
          //.put("InUseMth",null)
          //.put("UniqueAttchID",null)
          .put("KeyID",622L)
          .put("__op","r")
          .put("__ts_ms",1751305919894L) // 2024-03-31T12:18:39.894Z
          .put("__deleted","false")
          .put("EnterpriseId","5683");


          TEST_OBJ_MAP_1 = new LinkedHashMap<>();
          TEST_OBJ_MAP_1.put("HRCo",253);
          TEST_OBJ_MAP_1.put("HRRef",2);
          TEST_OBJ_MAP_1.put("EffectiveDate",1320105600000L); // 2011-11-01T00:00:00Z
          TEST_OBJ_MAP_1.put("Type","H");
          //TEST_OBJ_MAP_1.put("OldSalary","AA==");
          //TEST_OBJ_MAP_1.put("NewSalary","NWfg");
          TEST_OBJ_MAP_1.put("NewPositionCode","CARP");
          TEST_OBJ_MAP_1.put("NextDate",null);
          TEST_OBJ_MAP_1.put("UpdatedYN","Y");
          TEST_OBJ_MAP_1.put("HistSeq",1);
          TEST_OBJ_MAP_1.put("CalcYN","N");
          TEST_OBJ_MAP_1.put("BatchId",3);
          TEST_OBJ_MAP_1.put("InUseBatchId",null);
          TEST_OBJ_MAP_1.put("InUseMth",null);
          TEST_OBJ_MAP_1.put("UniqueAttchID",null);
          TEST_OBJ_MAP_1.put("KeyID",622L);
          TEST_OBJ_MAP_1.put("__op","r");
          TEST_OBJ_MAP_1.put("__ts_ms",1751305919894L); // 2024-03-31T12:18:39.894Z
          TEST_OBJ_MAP_1.put("__deleted","false");
          TEST_OBJ_MAP_1.put("EnterpriseId","5683");
    }
}
