package com.mindary.diary.repositories;

import com.mindary.diary.models.DiaryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiaryRepository extends JpaRepository<DiaryEntity, UUID> {
    Page<DiaryEntity> findByUserId(UUID userId, Pageable pageable);

    Optional<DiaryEntity> findByUserIdAndCreatedAtBetween(UUID userId, LocalDateTime start, LocalDateTime end);
}
