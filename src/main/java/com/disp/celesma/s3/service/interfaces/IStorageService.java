package com.disp.celesma.s3.service.interfaces;

import org.springframework.web.multipart.MultipartFile;


public interface IStorageService {
    String uploadAvatar(MultipartFile file, String username);
    void deleteAvatar(String avatarUrl);

    String uploadProjectAttachment(MultipartFile file, Long projectId);
    void deleteFile(String fileUrl);
}
