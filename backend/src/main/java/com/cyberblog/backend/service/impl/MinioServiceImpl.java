package com.cyberblog.backend.service.impl;

import com.cyberblog.backend.common.BusinessException;
import com.cyberblog.backend.service.MinioService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Override
    public String upload(MultipartFile file, String prefix) {
        try {
            String ext = "";
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }
            String objectName = prefix + UUID.randomUUID() + ext;
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return endpoint + "/" + bucketName + "/" + objectName;
        } catch (Exception e) {
            log.error("MinIO upload failed", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void delete(String fileUrl) {
        try {
            String objectName = fileUrl.substring(fileUrl.indexOf(bucketName) + bucketName.length() + 1);
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.warn("MinIO delete failed: {}", e.getMessage());
        }
    }
}
