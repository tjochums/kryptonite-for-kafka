package com.github.hpgrahsl.kafka.connect.transforms.kryptonite.messagepack;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.kafka.connect.data.Struct;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StructSerializer extends JsonSerializer<Struct> {
    @Override
    public void serialize(Struct struct, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (struct == null) {
            gen.writeNull();
            return;
        }
        Map<String, Object> map = new HashMap<>();
        struct.schema().fields().forEach(field -> map.put(field.name(), struct.get(field)));
        gen.writeObject(map);
    }
}