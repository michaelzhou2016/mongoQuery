package com.example.mongo.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;


/**
 * <pre>
 * Statements
 * </pre>
 *
 * @author JJLEE Sptek
 * @version 1.0
 * @ClassName : JsonUtil.java
 * @Description : JSON 관련 유틸
 * @Modification Information
 * <pre>
 *     since          author              description
 *  ===========    =============    ===========================
 *  2017. 8. 16.     JJLEE Sptek     	최초 생성
 * </pre>
 * @see
 * @since 2017. 8. 16.
 */
@Service
public class JsonUtil {
    private static final Gson GSON = new Gson();

    public static <T> T jsonToObject(String jsonStr, Class<T> tClass) throws JsonSyntaxException {
        return GSON.fromJson(jsonStr, tClass);
    }

    public static <T> T jsonToObject(String jsonStr, Type typeOfT) throws JsonSyntaxException {
        return GSON.fromJson(jsonStr, typeOfT);
    }

    public static String toJsonStr(Object vo) {
        return GSON.toJson(vo);
    }

    public static JsonElement toJsonTree(Object src) {
        return GSON.toJsonTree(src);
    }
}
