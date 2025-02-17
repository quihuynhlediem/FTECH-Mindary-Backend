package com.mindary.diary.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "diary_images", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryImage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private UUID id;

    @ManyToOne(targetEntity = DiaryEntity.class)
    @JoinColumn(name = "diary_id", nullable = false)
    private DiaryEntity diary;

    @Column(name = "url")
    private String url;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;
}
