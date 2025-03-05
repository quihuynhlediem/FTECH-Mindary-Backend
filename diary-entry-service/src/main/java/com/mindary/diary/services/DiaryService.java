package com.mindary.diary.services;

import com.mindary.diary.dto.AnalysisResultDto;
import com.mindary.diary.models.DiaryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public interface DiaryService {

    DiaryEntity create(UUID userId, String diary);

    AnalysisResultDto analyze (DiaryEntity savedDiary);

    DiaryEntity save(DiaryEntity diary);

    boolean isExist(UUID diaryId);

    Optional<DiaryEntity> findOne(UUID diaryId);

    DiaryEntity partialUpdate(UUID diaryId,DiaryEntity diaryEntity);

    Page<DiaryEntity> findByUserId(UUID userId, Pageable pageable);

    Optional<DiaryEntity> findByUserIdAndDate(UUID userId, String timezone);

    Optional<DiaryEntity> findByUserIdAndDate(UUID userId, LocalDate targetDate);
}
