package com.github.hpgrahsl.kafka.connect.transforms.kryptonite.messagepack;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import org.apache.kafka.connect.data.ConnectSchema;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Schema.Type;
import org.apache.kafka.connect.data.Struct;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class StructDeserializer extends JsonDeserializer<Struct> {
    public StructDeserializer() {
        
    }

    @Override
    public Struct deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = p.readValueAs(Map.class);
        Schema schema = extractSchema(map);
        Struct struct = new Struct(schema);
        
        // Populate the struct with values from the map
        for (Field field : schema.fields()) {
            Object value = map.get(field.name());
            // Handle type conversions if needed
            value = convertToSchemaType(value, field.schema());
            struct.put(field.name(), value);
        }

        return struct;
    }
    
    private Object convertToSchemaType(Object value, Schema schema) {
        if (value == null) {
            return null;
        }
        
        // Convert values to match the schema type
        switch (schema.type()) {
            case INT8:
                if (value instanceof Number) {
                    return (byte)((Number) value).intValue();
                }
                break;
            case INT16:
                if (value instanceof Number) {
                    return (short)((Number) value).intValue();
                }
                break;
            case INT32:
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                }
                break;
            case INT64:
                if (value instanceof Number) {
                    return ((Number) value).longValue();
                }
                break;
            case FLOAT32:
                if (value instanceof Number) {
                    return ((Number) value).floatValue();
                }
                break;
            case FLOAT64:
                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                }
                break;
            case BOOLEAN:
                if (value instanceof Boolean) {
                    return value;
                }
                break;
            case STRING:
                return value.toString();
            case BYTES:
                if (value instanceof byte[]) {
                    return value;
                } else if (value instanceof String) {
                    // Assume Base64 encoding for string to bytes conversion
                    return java.util.Base64.getDecoder().decode((String) value);
                }
                break;
            case STRUCT:
                if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> valueMap = (Map<String, Object>) value;
                    Struct nestedStruct = new Struct(schema);
                    for (Field field : schema.fields()) {
                        Object fieldValue = valueMap.get(field.name());
                        if (fieldValue != null) {
                            nestedStruct.put(field.name(), convertToSchemaType(fieldValue, field.schema()));
                        }
                    }
                    return nestedStruct;
                }
                break;
            case ARRAY:
                if (value instanceof ArrayList) {
                    @SuppressWarnings("unchecked")
                    ArrayList<Object> list = (ArrayList<Object>) value;
                    ArrayList<Object> convertedList = new ArrayList<>(list.size());
                    for (Object item : list) {
                        convertedList.add(convertToSchemaType(item, schema.valueSchema()));
                    }
                    return convertedList;
                }
                break;
            case MAP:
                if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<Object, Object> valueMap = (Map<Object, Object>) value;
                    Map<Object, Object> convertedMap = new java.util.HashMap<>();
                    for (Map.Entry<Object, Object> entry : valueMap.entrySet()) {
                        Object convertedKey = convertToSchemaType(entry.getKey(), schema.keySchema());
                        Object convertedValue = convertToSchemaType(entry.getValue(), schema.valueSchema());
                        convertedMap.put(convertedKey, convertedValue);
                    }
                    return convertedMap;
                }
                break;
        }
        
        return value; // Return original value if no conversion performed
    }
    
    private Schema extractSchema(Map<String, Object> map) {
        // Check if the map contains schema metadata
        if (map.containsKey("schema") && map.get("schema") instanceof Map) {
            // If there's explicit schema information, use it
            @SuppressWarnings("unchecked")
            Map<String, Object> schemaMap = (Map<String, Object>) map.get("schema");
            return readSchema(schemaMap);
        } else {
            // Otherwise infer schema from the data
            return inferSchema(map);
        }
    }

    public Schema readSchema(Map<String, Object> schemaMap) {
        // Extract schema properties if available
        String name = schemaMap.containsKey("name") ? (String)schemaMap.get("name") : null;
        Boolean isOptional = schemaMap.containsKey("optional") ? (Boolean)schemaMap.get("optional") : false;
        Object defaultValue = schemaMap.containsKey("default") ? schemaMap.get("default") : null;
        Integer version = schemaMap.containsKey("version") ? (Integer)schemaMap.get("version") : null;
        String doc = schemaMap.containsKey("doc") ? (String)schemaMap.get("doc") : null;
        
        // If we have explicit field definitions
        if (schemaMap.containsKey("fields") && schemaMap.get("fields") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> fieldsMap = (Map<String, Object>)schemaMap.get("fields");
            return createStructSchema(fieldsMap, name, isOptional, defaultValue, version, doc);
        } else {
            // If there are no explicit field definitions, use the schema map itself as the fields
            return createStructSchema(schemaMap, name, isOptional, defaultValue, version, doc);
        }
    }
    
    private Schema determineSchema(Object value) {
        if (value == null) {
            return Schema.OPTIONAL_STRING_SCHEMA; // Default to optional string for null values
        } else if (value instanceof String) {
            return Schema.STRING_SCHEMA;
        } else if (value instanceof Integer) {
            return Schema.INT32_SCHEMA;
        } else if (value instanceof Long) {
            return Schema.INT64_SCHEMA;
        } else if (value instanceof Float) {
            return Schema.FLOAT32_SCHEMA;
        } else if (value instanceof Double) {
            return Schema.FLOAT64_SCHEMA;
        } else if (value instanceof Boolean) {
            return Schema.BOOLEAN_SCHEMA;
        } else if (value instanceof byte[]) {
            return Schema.BYTES_SCHEMA;
        } else if (value instanceof Map) {
            // For nested objects, create a struct schema
            @SuppressWarnings("unchecked")
            Map<String, Object> nestedMap = (Map<String, Object>) value;
            return inferSchema(nestedMap);
        } else if (value instanceof ArrayList) {
            // For arrays, determine schema of first element (if available)
            @SuppressWarnings("unchecked")
            ArrayList<Object> list = (ArrayList<Object>) value;
            if (!list.isEmpty()) {
                Schema elementSchema = determineSchema(list.get(0));
                return SchemaBuilder.array(elementSchema).build();
            } else {
                // Empty array, default to string array
                return SchemaBuilder.array(Schema.STRING_SCHEMA).build();
            }
        } else {
            // Default to string schema for unknown types
            return Schema.STRING_SCHEMA;
        }
    }
    
    private Schema createStructSchema(Map<String, Object> fieldsMap, String name, boolean isOptional, 
                                    Object defaultValue, Integer version, String doc) {
        var fields = new ArrayList<Field>();
        int fieldIndex = 0;

        for (String fieldName : fieldsMap.keySet()) {
            Object fieldValue = fieldsMap.get(fieldName);
            Schema fieldSchema = determineSchema(fieldValue);
            fields.add(new Field(fieldName, fieldIndex++, fieldSchema));
        }
        
        return new ConnectSchema(Type.STRUCT, isOptional, defaultValue, name, version, doc, null, fields, null, null);
    }
    
    private Schema inferSchema(Map<String, Object> dataMap) {
        var fields = new ArrayList<Field>();
        int fieldIndex = 0;

        for (String fieldName : dataMap.keySet()) {
            Object fieldValue = dataMap.get(fieldName);
            Schema fieldSchema = determineSchema(fieldValue);
            fields.add(new Field(fieldName, fieldIndex++, fieldSchema));
        }
        
        return new ConnectSchema(Type.STRUCT, false, null, null, null, null, null, fields, null, null);
    }
}