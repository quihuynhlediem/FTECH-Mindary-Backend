package com.mindary.aichat.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatRequest {

    @NotNull
    private UUID userId;

    @NotBlank
    private String message;
}
