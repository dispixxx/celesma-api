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

@Service
@RequiredArgsConstructor
public class S3StorageService implements IStorageService {

    private final S3Client s3Client;

    @Value("${storage.bucket-name}")
    private String bucket;

    @Value("${storage.endpoint}")
    private String endpoint;

    public String uploadAvatar(MultipartFile file, String username) {
        validateImageFile(file);

        String key = "avatars/" + "avatar_" + username + getExtension(file);

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
            throw new RuntimeException("Failed to upload avatar", e);
        }

        return endpoint + "/" + bucket + "/" + key;
    }

    public void deleteAvatar(String avatarUrl) {
        if (avatarUrl == null || !avatarUrl.contains(bucket)) return;

        // Вытаскиваем key из URL
        String key = avatarUrl.substring(avatarUrl.indexOf("avatars/"));
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("File is empty");

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // 5MB лимит
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must not exceed 5MB");
        }
    }

    private String getExtension(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            return original.substring(original.lastIndexOf("."));
        }
        return ".jpg";
    }
}
