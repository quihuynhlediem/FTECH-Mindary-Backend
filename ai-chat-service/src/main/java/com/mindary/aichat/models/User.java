package com.mindary.aichat.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class User {
    private UUID id;

    private String username;

    private String password;

    private String email;

    private String profileImage;

    private UserRole role;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public enum UserRole {
        CUSTOMER,
    }
}
