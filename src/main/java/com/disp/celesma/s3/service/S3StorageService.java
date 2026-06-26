package com.disp.celesma.s3.service;

import com.disp.celesma.s3.service.interfaces.IStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3StorageService implements IStorageService {

    private final S3Client s3Client;

    @Value("${storage.bucket-name}")
    private String bucket;

    @Value("${storage.endpoint}")
    private String endpoint;

    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;   // 5MB
    private static final long MAX_FILE_SIZE   = 20 * 1024 * 1024;  // 20MB

    private static final String AVATAR_KEY      = "avatars/avatar_%s%s";
    private static final String ATTACHMENT_KEY  = "projects/%d/attachments/%s%s";

    private static final Set<String> ALLOWED_FILE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain"
    );

    // ─── Публичные методы ────────────────────────────────────────────

    @Override
    public String uploadAvatar(MultipartFile file, String username) {
        validateImage(file);
        String key = AVATAR_KEY.formatted(username, getExtension(file));
        return upload(file, key);
    }


    @Override
    public String uploadProjectAttachment(MultipartFile file, Long projectId) {
        validateFile(file);
        String key = ATTACHMENT_KEY.formatted(projectId, UUID.randomUUID(), getExtension(file));
        return upload(file, key);
    }

    @Override
    public void deleteAvatar(String avatarUrl) {
        delete(avatarUrl);
    }

    @Override
    public void deleteFile(String fileUrl) {
        delete(fileUrl);
    }

    // ─── Приватные методы ────────────────────────────────────────────

    private String upload(MultipartFile file, String key) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .acl(ObjectCannedACL.PUBLIC_READ)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + key, e);
        }
        return endpoint + "/" + bucket + "/" + key;
    }

    private void delete(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains(bucket)) return;
        String key = fileUrl.substring(fileUrl.indexOf(bucket) + bucket.length() + 1);
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("File is empty");
        if (file.getSize() > S3StorageService.MAX_AVATAR_SIZE) throw new IllegalArgumentException("File size must not exceed 5MB");
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("File is empty");
        if (file.getSize() > S3StorageService.MAX_FILE_SIZE) throw new IllegalArgumentException("File size must not exceed 20MB");
        String contentType = file.getContentType();
        if (contentType == null || !S3StorageService.ALLOWED_FILE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("File type not allowed: " + contentType);
        }
    }

    private String getExtension(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            return original.substring(original.lastIndexOf("."));
        }
        return ".bin";
    }
}
