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
                log.info("Stored file: {} in {}/{}", fileName, packageName, version);
            }
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            log.error("Failed to store file: {}", fileName, e);
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
            log.error("Failed to load file: {}", fileName, e);
            throw new RuntimeException("Failed to load file: " + fileName, e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID().toString();
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(fileName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build());
                log.info("Stored file: {}", fileName);
                return fileName;
            }
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            log.error("Failed to store file", e);
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public byte[] load(String filename) {
        try {
            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(filename)
                            .build())) {
                return stream.readAllBytes();
            }
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            log.error("Failed to load file: {}", filename, e);
            throw new RuntimeException("Failed to load file: " + filename, e);
        }
    }

    @Override
    public void delete(String filename) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(filename)
                            .build());
            log.info("Deleted file: {}", filename);
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            log.error("Failed to delete file: {}", filename, e);
            throw new RuntimeException("Failed to delete file: " + filename, e);
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
