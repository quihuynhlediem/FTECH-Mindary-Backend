package com.mindary.identity.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.time.LocalTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "customers", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id"})})
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class CustomerEntity extends User {
    @Column(name = "age")
    private String age;

    @Column(name = "gender")
    private String gender;

    @Column(name = "reminder_time")
    private LocalTime reminderTime;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "token_expire")
    private LocalDateTime tokenExpire;

    @Column(name = "is_token_validated")
    private Boolean tokenValidated;
}
