package com.mindary.identity.dto;

import com.mindary.identity.models.User;
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
public class UserDto {
    private UUID id;

    private String username;

    private String password;

    private String email;

    private String profileImage;

    private User.UserRole role;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
