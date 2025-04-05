package com.mindary.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SymptomDto {
    private String name;
    private String risk;
    private String description;
    private String suggestions;
}
