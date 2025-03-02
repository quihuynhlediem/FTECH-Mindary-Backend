package com.mindary.aichat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateTitleRequest {

    @NotBlank(message = "Title cannot be empty")
    private String title;
}
