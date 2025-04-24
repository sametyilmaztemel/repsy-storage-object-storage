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
                .build();
        init();
    }

    public void init() {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                System.out.println("Created bucket: " + bucket);
            }
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            System.err.println("Could not initialize object storage: " + e.getMessage());
            throw new RuntimeException("Could not initialize object storage", e);
        }
    }

    @Override
    public void store(String packageName, String version, String fileName, byte[] fileContent) {
        try {
            String objectName = getObjectName(packageName, version, fileName);
            try (ByteArrayInputStream bais = new ByteArrayInputStream(fileContent)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(objectName)
                                .stream(bais, fileContent.length, -1)
                                .contentType("application/octet-stream")
                                .build());
                System.out.println("Stored file: " + fileName + " in " + packageName + "/" + version);
            }
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            System.err.println("Failed to store file: " + fileName + ": " + e.getMessage());
            throw new RuntimeException("Failed to store file: " + fileName, e);
        }
    }

    @Override
    public byte[] load(String packageName, String version, String fileName) {
        try {
            String objectName = getObjectName(packageName, version, fileName);
            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build())) {
                return stream.readAllBytes();
            }
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            System.err.println("Failed to load file: " + fileName + ": " + e.getMessage());
            throw new RuntimeException("Failed to load file: " + fileName, e);
        }
    }

    @Override
    public boolean exists(String packageName, String version, String fileName) {
        try {
            String objectName = getObjectName(packageName, version, fileName);
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getObjectName(String packageName, String version, String fileName) {
        return packageName + "/" + version + "/" + fileName;
    }
}
