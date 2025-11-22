package com.esgbank.greenbond.core.util;

import com.esgbank.greenbond.core.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    private TestObject testObject;

    @BeforeEach
    void setUp() {
        TestMdcUtils.setupTestMdc();
        testObject = new TestObject("test", 123, LocalDateTime.now());
    }

    @Test
    void shouldSerializeAndDeserializeObject() {
        // When
        String json = JsonUtils.toJson(testObject);
        TestObject deserialized = JsonUtils.fromJson(json, TestObject.class);

        // Then
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.getName()).isEqualTo(testObject.getName());
        assertThat(deserialized.getValue()).isEqualTo(testObject.getValue());
    }

    @Test
    void shouldHandleApiResponseSerialization() {
        // Given
        ApiResponse<String> response = ApiResponse.success("test data", "Operation successful");

        // When
        String json = JsonUtils.toJson(response);
        ApiResponse<?> deserialized = JsonUtils.fromJson(json, ApiResponse.class);

        // Then
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.getStatus()).isEqualTo("success");
        assertThat(deserialized.getMessage()).isEqualTo("Operation successful");
    }

    @Test
    void shouldConvertToAndFromMap() {
        // When
        Map<String, Object> map = JsonUtils.toMap(testObject);
        TestObject fromMap = JsonUtils.fromMap(map, TestObject.class);

        // Then
        assertThat(fromMap).isNotNull();
        assertThat(fromMap.getName()).isEqualTo(testObject.getName());
    }

    @Test
    void shouldValidateJson() {
        // Given
        String validJson = "{\"name\":\"test\",\"value\":123}";
        String invalidJson = "{invalid json}";

        // When & Then
        assertTrue(JsonUtils.isValidJson(validJson));
        assertFalse(JsonUtils.isValidJson(invalidJson));
    }

    @Test
    void shouldHandleNullValues() {
        // When & Then
        assertThat(JsonUtils.toJson(null)).isEqualTo("null");
        assertThat(JsonUtils.fromJson("null", TestObject.class)).isNull();
    }

    // Test record for serialization
    record TestObject(String name, int value, LocalDateTime timestamp) {}
}