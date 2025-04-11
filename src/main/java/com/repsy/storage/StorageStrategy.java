package com.repsy.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageStrategy {
    String store(MultipartFile file);
    byte[] load(String filename);
    void delete(String filename);
} 