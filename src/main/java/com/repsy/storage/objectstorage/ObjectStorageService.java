package com.repsy.storage.objectstorage;

import com.repsy.storage.StorageStrategy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ObjectStorageService implements StorageStrategy {
    private final Map<String, byte[]> storage = new HashMap<>();

    @Override
    public void store(String packageName, String version, String fileName, byte[] fileContent) {
        String key = getStorageKey(packageName, version, fileName);
        storage.put(key, fileContent);
    }

    @Override
    public byte[] load(String packageName, String version, String fileName) {
        String key = getStorageKey(packageName, version, fileName);
        byte[] content = storage.get(key);
        if (content == null) {
            throw new RuntimeException("File not found: " + fileName + " in " + packageName + "/" + version);
        }
        return content;
    }

    @Override
    public boolean exists(String packageName, String version, String fileName) {
        String key = getStorageKey(packageName, version, fileName);
        return storage.containsKey(key);
    }
    
    private String getStorageKey(String packageName, String version, String fileName) {
        return packageName + "/" + version + "/" + fileName;
    }
} 