package com.mindary.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnalysisResultDto {
    private UUID senderId;
    private UUID diaryId;
    private EmotionDto emotion;
    private List<CorrelationDto> correlations;

    private List<SymptomDto> symptoms;
}
