package com.lanf.system.utils;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import com.lanf.system.model.dto.CompanyRegisterDTO;
import com.lanf.system.model.entiry.SysI18nDO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonListEach {
    public static <T> String convertListToJson(List<T> list) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String toJsonString(Object object){

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static <T> List<T> convertJsonToList(String json, Class<T> elementType) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, elementType));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 国际化语言SysI18n数据转换为前端接收的格式
     *
     * @param list
     * @return
     */
    public static JsonObject convertSysI18nToJson(List<SysI18nDO> list) {
        JsonObject jsonObject = new JsonObject();
        for (SysI18nDO sys : list) {
            String key = sys.getName();
            String value = sys.getVal();
            if (key.contains(".")) {
                String[] nameParts = key.split("\\.");
                JsonObject currentObject = jsonObject;
                for (int i = 0; i < nameParts.length - 1; i++) {
                    if (!currentObject.has(nameParts[i])) {
                        JsonObject newObject = new JsonObject();
                        currentObject.add(nameParts[i], newObject);
                        currentObject = newObject;
                    } else {
                        JsonElement element = currentObject.get(nameParts[i]);
                        if (element.isJsonObject()) {
                            currentObject = element.getAsJsonObject();
                        } else {
                            JsonObject newObject = new JsonObject();
                            currentObject.add(nameParts[i], newObject);
                            currentObject = newObject;
                        }
                    }
                }
                currentObject.addProperty(nameParts[nameParts.length - 1], value);
            } else {
                jsonObject.addProperty(key, value);
            }
        }
        return jsonObject;
    }

    private static String convertToJsonString(JsonObject jsonObject) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(jsonObject);
    }

    public static List<SysI18nDO> convertJsonToList(String json) {
        Gson gson = new Gson();
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(json);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        List<SysI18nDO> list = new ArrayList<>();
        convertJsonObjectToList(jsonObject, "", list);
        return list;
    }

    private static void convertJsonObjectToList(JsonObject jsonObject, String prefix, List<SysI18nDO> list) {
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (value.isJsonObject()) {
                convertJsonObjectToList(value.getAsJsonObject(), prefix + key + ".", list);
            } else {
                SysI18nDO sys = new SysI18nDO();
                sys.setName(prefix + key);
                sys.setVal(value.getAsString());
                list.add(sys);
            }
        }
    }

    public static void main(String[] args) {

        CompanyRegisterDTO companyRegisterDTO = new CompanyRegisterDTO();
        
        System.out.println(toJsonString(companyRegisterDTO));

    }
}
