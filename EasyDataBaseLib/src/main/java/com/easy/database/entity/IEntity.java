package com.easy.database.entity;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 实现表基类，子类必须有一个空的实例方法
 * Time: 2019/8/20 0020
 * Author: zoulong
 */

public abstract class IEntity {

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<String,Object>();
        try {
            Class<?> clazz = getClass();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object value = field.get(this);
                if(value != null)
                map.put(fieldName, value);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return map;
    }
}
