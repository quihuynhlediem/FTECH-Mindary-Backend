package com.mindary.diary.repositories;

import com.mindary.diary.models.DiaryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DiaryRepository extends JpaRepository<DiaryEntity, UUID> {
    Page<DiaryEntity> findByUserId(UUID userId, Pageable pageable);
}
