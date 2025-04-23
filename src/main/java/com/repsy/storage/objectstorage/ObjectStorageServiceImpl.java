package com.repsy.storage.objectstorage;

import com.repsy.storage.StorageStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.MinioException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;

@Slf4j
@Service
public class ObjectStorageServiceImpl implements StorageStrategy {

    private final MinioClient minioClient;
    private final String bucket;

    public ObjectStorageServiceImpl(
            @Value("${minio.endpoint:http://localhost:9000}") String endpoint,
            @Value("${minio.accessKey:minioadmin}") String accessKey,
            @Value("${minio.secretKey:minioadmin}") String secretKey,
            @Value("${minio.bucket:repsy}") String bucket) {
        this.bucket = bucket;
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)