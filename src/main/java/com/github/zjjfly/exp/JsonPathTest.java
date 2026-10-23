package com.github.zjjfly.exp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.JsonPath;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="https://github.com/zjjfly"/>zjjfly</a>
 */
public class JsonPathTest {
    public static void main(String[] args) throws JsonProcessingException {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> nested = new HashMap<>();
        nested.put("name", "Alice");
        data.put("user", nested);

        // 使用 JsonPath 访问嵌套字段
        List<String> result = JsonPath.read(List.of(data,data), "*.user.name");
        System.out.println(result); // 输出: Alice
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSS");
        LocalDateTime localDateTime = LocalDateTime.parse("2020-02-02 12:34:56.1111", dateTimeFormatter);
        System.out.println(localDateTime);
        Instant now = Instant.now();
        System.out.println(now);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        System.out.println(objectMapper.writeValueAsString(Map.of("dateTime", now)));
    }
}
