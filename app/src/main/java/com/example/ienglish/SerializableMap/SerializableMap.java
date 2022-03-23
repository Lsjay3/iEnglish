package com.example.ienglish.SerializableMap;

import java.io.Serializable;
import java.util.Map;

/**
 * 没用上
 */

public class SerializableMap implements Serializable {

    private Map<String,Object> map;

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }
}
