# Repsy Storage — Object Storage

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-6DB33F?style=flat-square&logo=spring-boot)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk)
![MinIO](https://img.shields.io/badge/MinIO-S3%20Compatible-C72C48?style=flat-square&logo=minio)

MinIO/S3-compatible object storage strategy implementation for the [Repsy Package Manager](https://github.com/sametyilmaztemel/repsy-package-manager). This module provides a pluggable storage backend that stores packages as objects in an S3-compatible bucket using the [MinIO Java SDK](https://min.io/docs/minio/linux/developers/java/minio-java.html).

## Overview

This library implements the `StorageStrategy` interface and provides two production-ready implementations:

- **`ObjectStorageStrategy`** — Full MinIO-backed implementation with streaming support, MultipartFile handling, and automatic bucket initialization
- **`ObjectStorageServiceImpl`** — Lightweight MinIO implementation focused on package-centric operations

A third class, `ObjectStorageService`, provides an in-memory reference implementation for testing.

## Architecture

```
┌──────────────────────────────────────────┐
│         Repsy Package Manager             │
│                                           │
│   PackageService ──► StorageStrategy      │
│                          │                │
│              ┌───────────▼────────────┐   │
│              │  ObjectStorageStrategy │   │
│              │  (MinIO / S3)          │   │
│              └───────────┬────────────┘   │
│                          │                │
│              ┌───────────▼────────────┐   │
│              │     MinIO Bucket       │   │
│              │  ┌─ package/           │   │
│              │  │  └─ version/file    │   │
│              │  └─ ...                │   │
│              └────────────────────────┘   │
└──────────────────────────────────────────┘
```

## Storage Interface

```java
public interface StorageStrategy {
    void init();
    String store(MultipartFile file);
    byte[] load(String filename);
    void delete(String filename);
    boolean exists(String packageName, String version, String fileName);
    void store(String packageName, String version, String fileName, byte[] fileContent);
    byte[] load(String packageName, String version, String fileName);
}
```

## Configuration

Add the following properties to your `application.properties` or `application.yml`:

```properties
# MinIO Configuration
minio.endpoint=http://localhost:9000
minio.accessKey=minioadmin
minio.secretKey=minioadmin
minio.bucket=repsy
```

**Property Reference**

- `minio.endpoint` — MinIO server URL (default: `http://localhost:9000`)
- `minio.accessKey` — Access key credential (default: `minioadmin`)
- `minio.secretKey` — Secret key credential (default: `minioadmin`)
- `minio.bucket` — Bucket name for package storage (default: `repsy`)

## Usage

### Maven Dependency

```xml
<dependency>
    <groupId>com.repsy</groupId>
    <artifactId>repsy-storage-object-storage</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Spring Boot Auto-Configuration

The `@Service` annotation on `ObjectStorageStrategy` enables automatic component scanning. Simply ensure the package is scanned and the MinIO properties are configured.

```java
@Autowired
private StorageStrategy storageStrategy;

// Store a package
storageStrategy.store("mylib", "1.0.0", "package.rep", fileBytes);

// Load a package
byte[] content = storageStrategy.load("mylib", "1.0.0", "package.rep");

// Check existence
boolean exists = storageStrategy.exists("mylib", "1.0.0", "package.rep");
```

### Object Key Structure

Packages are stored in the MinIO bucket with the following key pattern:

```
{packageName}/{version}/{fileName}
```

For example: `mylib/1.0.0/package.rep`

## Features

- **Automatic Bucket Creation** — Creates the configured bucket on startup if it doesn't exist
- **Streaming Upload/Download** — Efficient handling of large files via MinIO streaming API
- **MultipartFile Support** — Direct integration with Spring's `MultipartFile` for web uploads
- **Content Type Handling** — Sets `application/octet-stream` for proper binary handling
- **S3 Compatible** — Works with any S3-compatible object storage (MinIO, AWS S3, etc.)

## Requirements

- Java 17+
- Spring Boot 3.x
- MinIO server (or any S3-compatible object storage)

## Related

- [Repsy Package Manager](https://github.com/sametyilmaztemel/repsy-package-manager) — Main application
- [Repsy Storage — File System](https://github.com/sametyilmaztemel/repsy-storage-file-system) — Alternative filesystem storage backend

## License

This project is available under the MIT License.
