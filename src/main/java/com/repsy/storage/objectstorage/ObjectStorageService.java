package com.repsy.storage.objectstorage;

import com.repsy.storage.StorageStrategy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ObjectStorageService implements StorageStrategy {
    private final Map<String, byte[]> storage = new HashMap<>();
