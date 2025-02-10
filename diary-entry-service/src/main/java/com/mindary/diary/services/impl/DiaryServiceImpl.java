package com.mindary.diary.services.impl;

import com.mindary.diary.models.DiaryEntity;
import com.mindary.diary.repositories.DiaryRepository;
import com.mindary.diary.services.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiaryServiceImpl implements DiaryService {
    private final DiaryRepository diaryRepository;

    @Override
    public Page<DiaryEntity> findByUserId(UUID userId, Pageable pageable) {
        return diaryRepository.findByUserId(userId, pageable);
    }

    @Override
    public DiaryEntity save(DiaryEntity diary) {
        return diaryRepository.save(diary);
    }

    @Override
    public boolean isExist(UUID diaryId) {
        return diaryRepository.existsById(diaryId);
    }

    @Override
    public Optional<DiaryEntity> findOne(UUID diaryId) {
        return diaryRepository.findById(diaryId);
    }

    @Override
    public DiaryEntity partialUpdate(UUID diaryId,DiaryEntity diaryEntity) {
        return diaryRepository.findById(diaryId).map(existingDiary -> {
            Optional.ofNullable(existingDiary.getContent()).ifPresent(existingDiary::setContent);
            return diaryRepository.save(existingDiary);
        }).orElseThrow(() -> new RuntimeException("Diary does not exist"));
    }
}
