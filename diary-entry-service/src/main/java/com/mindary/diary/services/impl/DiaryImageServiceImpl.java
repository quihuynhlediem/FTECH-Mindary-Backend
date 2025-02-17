package com.mindary.diary.services.impl;

import com.mindary.diary.models.DiaryEntity;
import com.mindary.diary.models.DiaryImage;
import com.mindary.diary.repositories.DiaryImageRepository;
import com.mindary.diary.services.DiaryImageService;
import com.mindary.diary.services.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryImageServiceImpl implements DiaryImageService {

    private final DiaryImageRepository diaryImageRepository;
    private final S3Service s3Service;

    @Override
    public DiaryImage save(DiaryImage diaryImage) {
        return diaryImageRepository.save(diaryImage);
    }

    @Override
    public Set<DiaryImage> uploadAndSaveImages(List<MultipartFile> images, DiaryEntity diaryEntity) {
        if (images == null || images.isEmpty()) {
            return Set.of();
        }

        return images.stream()
                .filter(image -> !image.isEmpty())
                .map(image -> {
                    try {
                        String s3Url = s3Service.uploadFile(image);
                        DiaryImage diaryImage = DiaryImage.builder()
                                .url(s3Url)
                                .diary(diaryEntity)
                                .build();
                        return diaryImageRepository.save(diaryImage);
                    } catch (Exception e) {
                        log.error("Error uploading or saving image: ", e);
                        throw new RuntimeException("Error processing images", e);
                    }
                })
                .collect(Collectors.toSet());
    }
}
