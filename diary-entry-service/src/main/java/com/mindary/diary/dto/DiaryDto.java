package com.mindary.diary.dto;

import com.mindary.diary.models.DiaryImage;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DiaryDto {
    private UUID id;

    private String content;

    private UUID userId;

    private Set<DiaryImageDto> images;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}