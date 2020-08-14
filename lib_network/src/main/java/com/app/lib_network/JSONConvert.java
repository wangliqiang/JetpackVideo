package com.app.lib_network;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Type;

public class JSONConvert implements Convert {

    @Override
    public Object convert(String response, Type type) {
        JSONObject jsonObject = JSON.parseObject(response);
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null) {
            Object obj = data.get("data");
            return JSON.parseObject(obj.toString(), type);
        }
        return null;
    }

    @Override
    public Object convert(String response, Class clazz) {
        JSONObject jsonObject = JSON.parseObject(response);
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null) {
            Object obj = data.get("data");
            return JSON.parseObject(obj.toString(), clazz);
        }
        return null;
    }
}
