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

package com.github.hpgrahsl.kafka.connect.transforms.kryptonite.messagepack;

import com.github.hpgrahsl.kryptonite.EncryptedField;
import com.github.hpgrahsl.kryptonite.FieldMetaData;
import com.github.hpgrahsl.kryptonite.PayloadMetaData;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

public class MessagePackInstance {
  /* Tries to simulate what Kryo was doing in order to handle the serialization of the class type along with the message pack
   * messages. This was really only necessary for the schema aware records because they were coming in as a struct. I'm hoping
   * that mirroring the types Kryo did should be enough to catch all the types we're likely to encounter, but it's possible we
   * won't, so if that happens, we'll likely see an error in the logs indicating that the object type id is null at some point,
   * and we'll just need to add the new type to this map.
   */
  private static final ThreadLocal<Map<Integer, ClassRegistration>> Instance = new ThreadLocal<Map<Integer, ClassRegistration>>() {
    protected Map<Integer, ClassRegistration> initialValue() {
      Map<Integer, ClassRegistration> instance = new HashMap<Integer, ClassRegistration>();
        instance.put(0, new ClassRegistration(FieldMetaData.class, 0));
        instance.put(1, new ClassRegistration(PayloadMetaData.class, 1));
        instance.put(2, new ClassRegistration(EncryptedField.class, 2));
        instance.put(3, new ClassRegistration(Struct.class, 3));
        instance.put(4, new ClassRegistration(Schema.class, 4));
        instance.put(5, new ClassRegistration(Schema.Type.class, 5));
        instance.put(6, new ClassRegistration(Object.class, 6));
        instance.put(7, new ClassRegistration(byte[].class, 7));
        instance.put(8, new ClassRegistration(BigDecimal.class, 8));
        instance.put(9, new ClassRegistration(List.class, 9));
        instance.put(10, new ClassRegistration(ArrayList.class, 10));
        instance.put(11, new ClassRegistration(LinkedList.class, 11));
        instance.put(12, new ClassRegistration(Map.class, 12));
        instance.put(13, new ClassRegistration(HashMap.class, 13));
        instance.put(14, new ClassRegistration(LinkedHashMap.class, 14));
        instance.put(15, new ClassRegistration(Set.class, 15));
        instance.put(16, new ClassRegistration(HashSet.class, 16));
        instance.put(17, new ClassRegistration(LinkedHashSet.class, 17));
        instance.put(18, new ClassRegistration(Date.class, 18));
        instance.put(19, new ClassRegistration(Time.class, 19));
        instance.put(20, new ClassRegistration(Timestamp.class, 20));
        instance.put(21, new ClassRegistration(String.class, 21));
        instance.put(22, new ClassRegistration(Integer.class, 22));
        instance.put(23, new ClassRegistration(Boolean.class, 23));
        instance.put(24, new ClassRegistration(Long.class, 24));
        instance.put(25, new ClassRegistration(Double.class, 25));
        instance.put(26, new ClassRegistration(Float.class, 26));

        try {
            instance.put(1000, new ClassRegistration(Class.forName("java.util.Arrays$ArrayList"), 1000));
            instance.put(1001, new ClassRegistration(Class.forName("java.util.ImmutableCollections$ListN"), 1001));
            instance.put(1002, new ClassRegistration(Class.forName("java.util.ImmutableCollections$SetN"), 1002));
            instance.put(1003, new ClassRegistration(Class.forName("java.util.ImmutableCollections$Map1"), 1003));
            instance.put(1004, new ClassRegistration(Class.forName("java.util.ImmutableCollections$MapN"), 1004));
            instance.put(1005, new ClassRegistration(Class.forName("java.util.ImmutableCollections$List12"), 1005));
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    return instance;
  };
};

  public static Map<Integer, ClassRegistration> get() {
    return Instance.get();
  }
  public static Integer getId(Class<?> clazz) {
    var values = Instance.get().values();
    var registration = values.stream()
        .filter(reg -> reg.getClassName().equals(clazz.getName()))
        .findFirst();
    return registration.map(ClassRegistration::getId).orElse(null);
  }

  public static ClassRegistration getRegistration(Integer id) {
    return Instance.get().get(id);
  }
}