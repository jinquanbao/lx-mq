package com.laoxin.mq.client.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JSONUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        //反序列化时候遇到不匹配的属性并不抛出异常
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //序列化时候遇到空对象不抛出异常
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //反序列化的时候如果是无效子类型,不抛出异常
        objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        //不使用默认的dateTime进行序列化,
        objectMapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //使用JSR310提供的序列化类,里面包含了大量的JDK8时间序列化类
        objectMapper.registerModule(new JavaTimeModule());

    }

    public static <T> T fromJson(byte[] json, Class<T> classOfT){
        if(json == null || json.length == 0){
            return null;
        }
        try {
            return objectMapper.readValue(json,classOfT);
        } catch (JsonParseException e) {
            log.error("json parse error {}",e.getMessage());
            throw new RuntimeException("json解析异常",e);
        } catch (JsonMappingException e) {
            log.error("json parse error {}",e.getMessage());
            throw new RuntimeException("json解析异常",e);
        } catch (IOException e) {
            log.error("json parse error {}",e.getMessage());
            throw new RuntimeException("json解析异常",e);
        }
    }

    public static <T> T fromJson(String json, Class<T> classOfT){
        if(StringUtils.isEmpty(json)){
            return null;
        }
        try {
            return objectMapper.readValue(json,classOfT);
        } catch (JsonParseException e) {
            log.error("json parse error {}",e.getMessage());
            throw new RuntimeException("json解析异常",e);
        } catch (JsonMappingException e) {
            log.error("json parse error {}",e.getMessage());
            throw new RuntimeException("json解析异常",e);
        } catch (IOException e) {
            log.error("json parse error {}",e.getMessage());
            throw new RuntimeException("json解析异常",e);
        }
    }

    public static <T> T fromJson(String json, ParameterizedTypeReference reference){
        return fromJson(json,reference.getType());
    }

    public static <T> T fromJson(String json, Type type){
        if(StringUtils.isEmpty(json)){
            return null;
        }
        try {
            return objectMapper.readValue(json,objectMapper.constructType(type));
        } catch (JsonParseException e) {
            log.error("json parse error {}",e.getMessage());
            throw new RuntimeException("json解析异常",e);
        } catch (JsonMappingException e) {
            log.error("json parse error {}",e.getMessage());
            throw new RuntimeException("json解析异常",e);
        } catch (IOException e) {
            log.error("json parse error {}",e.getMessage());
            throw new RuntimeException("json解析异常",e);
        }
    }

    public static <T> List<T> fromJsonArray(String json, Class<?> classOfT){
        if(StringUtils.isEmpty(json)){
            return null;
        }
        try {
            JavaType jt = objectMapper.getTypeFactory().constructParametricType(ArrayList.class, classOfT);
            return objectMapper.readValue(json,jt);
        } catch (JsonParseException e) {
            log.error("json parse error {}",e.getMessage());
            throw new RuntimeException("json解析异常",e);
        } catch (JsonMappingException e) {
            log.error("json parse error {}",e.getMessage());
            throw new RuntimeException("json解析异常",e);
        } catch (IOException e) {
            log.error("json parse error {}",e.getMessage());
            throw new RuntimeException("json解析异常",e);
        }
    }



    public static String toJson(Object obj){
        if(null == obj){
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("json processing error {}",e.getMessage());
            throw new RuntimeException("json读取异常",e);
        }
    }
}
