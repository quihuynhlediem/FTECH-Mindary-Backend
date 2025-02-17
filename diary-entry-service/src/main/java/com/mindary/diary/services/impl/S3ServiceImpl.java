package com.mindary.diary.services.impl;

import com.mindary.diary.services.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucketname:application.properties}")
    private String bucketName;

    @Value("${aws.region:application.properties}")
    private String region;

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = createFileName(file);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .metadata(metadata)
                .build();

        RequestBody requestBody = RequestBody.fromInputStream(file.getInputStream(), file.getSize());

        s3Client.putObject(putObjectRequest, requestBody);

        return generateS3Url(fileName);
    }

    private String createFileName(MultipartFile file) {
        String timestamp = String.valueOf(Instant.now().toEpochMilli()); // Milliseconds since epoch
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return timestamp; // Handle cases where original filename is not available
        }

        int dotIndex = originalFilename.lastIndexOf(".");
        String baseName = (dotIndex == -1) ? originalFilename : originalFilename.substring(0, dotIndex);
        String extension = (dotIndex == -1 || dotIndex == originalFilename.length() - 1) ? "" : originalFilename.substring(dotIndex);

        return baseName + "_" + timestamp + extension;
    }


    private String generateS3Url(String fileName) {
        return "https://" + bucketName + ".s3." +  region + ".amazonaws.com/" + fileName;
    }
}
