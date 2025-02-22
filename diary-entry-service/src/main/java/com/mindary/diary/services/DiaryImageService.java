package com.mindary.diary.services;

import com.mindary.diary.models.DiaryEntity;
import com.mindary.diary.models.DiaryImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface DiaryImageService {
    DiaryImage save(DiaryImage diaryImage);

    Set<DiaryImage> uploadAndSaveImages(List<MultipartFile> photos, DiaryEntity savedDiary);
}
