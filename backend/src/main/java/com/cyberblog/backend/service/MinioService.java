package com.cyberblog.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface MinioService {
    String upload(MultipartFile file, String prefix);
    void delete(String fileUrl);
}
