package com.repsy.storage.objectstorage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageStrategy {
    void init();
    String store(MultipartFile file);
    byte[] load(String filename);
    void delete(String filename);
    boolean exists(String packageName, String version, String fileName);
    void store(String packageName, String version, String fileName, byte[] fileContent);
    byte[] load(String packageName, String version, String fileName);
}
