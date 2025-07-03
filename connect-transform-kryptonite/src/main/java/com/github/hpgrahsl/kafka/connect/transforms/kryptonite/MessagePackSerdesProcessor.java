package com.github.hpgrahsl.kafka.connect.transforms.kryptonite;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.msgpack.jackson.dataformat.MessagePackMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagePackSerdesProcessor implements ExpandedSerdesProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessagePackSerdesProcessor.class);

    private static final ObjectMapper objectMapper = new MessagePackMapper().handleBigIntegerAndBigDecimalAsString();

    @Override
    public byte[] objectToBytes(Object object, Class<?> clazz) {
        try {
            return objectMapper.writeValueAsBytes(object);
            //return json.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Serialization failed", e);
        }
    }

    @Override
    public byte[] objectToBytes(Object object) {
        return objectToBytes(object, object != null ? object.getClass() : Object.class);
    }

    @Override
    public Object bytesToObject(byte[] bytes, Class<?> clazz) {
        try {
            String json = new String(bytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    @Override
    public Object bytesToObject(byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, Object.class);
        } catch (Exception e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    @Override
    public String doTheThing(Object object) {
        try {
            var encodedBytes = objectMapper.writeValueAsBytes(object);
            var encodedField = Base64.getEncoder().encodeToString(encodedBytes);
            LOGGER.debug("encodedField: {}", encodedField);
            return encodedField;
        } catch (Exception e) {
            throw new RuntimeException("Serialization to JSON failed", e);
        }
    }

    @Override
    public <T> T undoTheThing(String encodedField, Class<T> type) {
        try {
            var decodedBytes = Base64.getDecoder().decode((String)encodedField);
            var decodedField = objectMapper.readValue(decodedBytes, type);
           LOGGER.debug("decodedField: {}", decodedField);
            return decodedField;
        } catch (Exception e) {
            throw new RuntimeException("Deserialization from JSON failed", e);
        }
    }
}
