package com.mindary.diary.dto;

import com.mindary.diary.models.DiaryEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DiaryImageDto {
    private UUID id;

    private String url;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
