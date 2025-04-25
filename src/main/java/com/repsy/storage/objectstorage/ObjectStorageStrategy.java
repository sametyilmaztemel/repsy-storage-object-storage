package com.repsy.storage.objectstorage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.MinioException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class ObjectStorageStrategy implements StorageStrategy {

    private final MinioClient minioClient;
    private final String bucket;

    public ObjectStorageStrategy(
            @Value("${minio.endpoint:http://localhost:9000}") String endpoint,
            @Value("${minio.accessKey:minioadmin}") String accessKey,
            @Value("${minio.secretKey:minioadmin}") String secretKey,
            @Value("${minio.bucket:repsy}") String bucket) {
        this.bucket = bucket;
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        init();
    }

    @Override
    public void init() {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Created bucket: {}", bucket);
            }
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            log.error("Could not initialize object storage", e);
            throw new RuntimeException("Could not initialize object storage", e);
        }