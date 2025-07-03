package com.github.hpgrahsl.kafka.connect.transforms.kryptonite.messagepack;

import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.hpgrahsl.kryptonite.EncryptedField;
import com.github.hpgrahsl.kryptonite.serdes.SerdeProcessor;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

import org.apache.kafka.connect.data.Struct;
import org.msgpack.jackson.dataformat.MessagePackMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagePackSerdesProcessor implements SerdeProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessagePackSerdesProcessor.class);

    private static ObjectMapper objectMapper;

    public ObjectMapper getInstance() {
        if(objectMapper == null) {
            objectMapper = new MessagePackMapper().handleBigIntegerAndBigDecimalAsString();
            SimpleModule module = new SimpleModule();
            module.addSerializer(Struct.class, new StructSerializer());
            module.addDeserializer(Struct.class, new StructDeserializer());
            objectMapper.registerModule(module);
        }
        return objectMapper;
    }

    @Override
    public byte[] objectToBytes(Object object, Class<?> clazz) {
        var output = new Output(new ByteArrayOutputStream());
        try {
            var objectTypeId = MessagePackInstance.getId(clazz);
            // Convert integer to 5-byte array
            byte[] typeIdBytes = padObjectTypeId(objectTypeId);
            output.writeBytes(typeIdBytes); // Write type ID first
            output.writeBytes(getInstance().writeValueAsBytes(object));
            return output.toBytes();
        } catch (Exception e) {
            throw new RuntimeException("Serialization failed", e);
        }
        finally {
            output.close();
        }
    }

    @Override
    public byte[] objectToBytes(Object object) {
        return objectToBytes(object, object != null ? object.getClass() : Object.class);
    }

    @Override
    public Object bytesToObject(byte[] bytes, Class<?> clazz) {
        try {
            // Ignore the first 5 bytes since the class was provided
            byte[] objectBytes = new byte[bytes.length - 5];
            System.arraycopy(bytes, 5, objectBytes, 0, objectBytes.length);
           
            return getInstance().readValue(objectBytes, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    @Override
    public Object bytesToObject(byte[] bytes) {
        try {
            // Extract type ID from the first 5 bytes
            int objectTypeId = extractObjectTypeId(bytes);
            LOGGER.debug("Extracted object type ID: {}", objectTypeId);
            
            // Skip the first 5 bytes (type ID) and read the rest
            byte[] objectBytes = new byte[bytes.length - 5];
            System.arraycopy(bytes, 5, objectBytes, 0, objectBytes.length);
            LOGGER.debug("Object bytes length: {}", objectBytes.length);
            var registration = MessagePackInstance.getRegistration(objectTypeId);
            // If registration is null, fallback to Object class
            if (registration == null) {
                registration = new ClassRegistration(Object.class, -1);
            }

            // Since we don't have a lookup method, just deserialize to Object
            var className = registration.getClassName();
            return getInstance().readValue(objectBytes, Class.forName(className));
        } catch (Exception e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    @Override
    public String encodeField(EncryptedField object) {
        try {
            var encodedBytes = getInstance().writeValueAsBytes(object);
            var encodedField = Base64.getEncoder().encodeToString(encodedBytes);
            LOGGER.debug("encodedField: {}", encodedField);
            return encodedField;
        } catch (Exception e) {
            throw new RuntimeException("Serialization to JSON failed", e);
        }
    }

    @Override
    public EncryptedField decodeField(String encodedField){
        try {
            var decodedBytes = Base64.getDecoder().decode((String)encodedField);
            var decodedField = getInstance().readValue(decodedBytes, EncryptedField.class);
           LOGGER.debug("decodedField: {}", decodedField);
            return decodedField;
        } catch (Exception e) {
            throw new RuntimeException("Deserialization from JSON failed", e);
        }
    }

    private byte[] padObjectTypeId(Integer objectTypeId) {
        byte[] typeIdBytes = new byte[5];
        typeIdBytes[0] = (byte) ((objectTypeId >> 32) & 0xFF);
        typeIdBytes[1] = (byte) ((objectTypeId >> 24) & 0xFF);
        typeIdBytes[2] = (byte) ((objectTypeId >> 16) & 0xFF);
        typeIdBytes[3] = (byte) ((objectTypeId >> 8) & 0xFF);
        typeIdBytes[4] = (byte) (objectTypeId & 0xFF);
        return typeIdBytes;
    }

    private int extractObjectTypeId(byte[] bytes) {
        if (bytes.length < 5) {
            throw new IllegalArgumentException("Byte array must be at least 5 bytes long");
        }
        
        return ((bytes[0] & 0xFF) << 32) | 
               ((bytes[1] & 0xFF) << 24) | 
               ((bytes[2] & 0xFF) << 16) | 
               ((bytes[3] & 0xFF) << 8) | 
               (bytes[4] & 0xFF);
    }
}
