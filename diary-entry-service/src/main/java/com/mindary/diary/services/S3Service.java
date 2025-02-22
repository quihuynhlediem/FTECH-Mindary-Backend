package com.mindary.diary.services;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    public String uploadFile(MultipartFile file) throws Exception;
}
