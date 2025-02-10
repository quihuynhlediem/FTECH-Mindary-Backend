package com.mindary.diary.controllers;

import com.mindary.diary.dto.DiaryDto;
import com.mindary.diary.mappers.Mapper;
import com.mindary.diary.models.DiaryEntity;
import com.mindary.diary.services.DiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/diaries")
@RequiredArgsConstructor
@Slf4j
public class DiaryController {
    private final DiaryService diaryService;
    private final Mapper<DiaryEntity, DiaryDto> diaryMapper;

    @PreAuthorize("#userId.toString() == authentication.name")
    @GetMapping(path = "/user/{userId}")
    public Page<DiaryDto> getDiariesByUserId(
            @PathVariable("userId") UUID userId,
            Pageable pageable
    ) {
        log.info("Getting diaries by userId: {}", userId);
        Page<DiaryEntity> foundDiaries = diaryService.findByUserId(userId, pageable);
        return foundDiaries.map(diaryMapper::mapTo);
    }


    @PreAuthorize("#userId.toString() == authentication.name")
    @GetMapping(path = "{diaryId}/user/{userId}")
    public ResponseEntity<DiaryDto> getDiaryById(
            @PathVariable("userId") UUID userId,
            @RequestParam("diaryId") UUID diaryId
    ) {
        Optional<DiaryEntity> foundDiary = diaryService.findOne(diaryId);
        return foundDiary.map(diary -> {
            DiaryDto diaryDto = diaryMapper.mapTo(diary);
            return new ResponseEntity<>(diaryDto, HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PreAuthorize("#userId.toString() == authentication.name")
    @PostMapping(path = "/user/{userId}")
    public ResponseEntity<DiaryDto> createDiary(
            @PathVariable("userId") UUID userId,
            @RequestBody DiaryDto diaryDto
    ) {
        DiaryEntity diary = diaryMapper.mapFrom(diaryDto);
        diary.setUserId(userId);
        DiaryEntity savedDiary = diaryService.save(diary);
        return ResponseEntity.ok(diaryMapper.mapTo(savedDiary));
    }

    @PreAuthorize("#userId.toString() == authentication.name")
    @PatchMapping(path = "/{diaryId}/user/{userId}")
    public ResponseEntity<DiaryDto> updateDiary(
            @PathVariable("userId") UUID userId,
            @RequestParam("diaryId") UUID diaryId,
            @RequestBody DiaryDto diaryDto
    ) {
        if (!diaryService.isExist(diaryId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        DiaryEntity diaryEntity = diaryMapper.mapFrom(diaryDto);
        DiaryEntity savedDiary = diaryService.partialUpdate(diaryId, diaryEntity);

        return new ResponseEntity<>(
                diaryMapper.mapTo(savedDiary),
                HttpStatus.OK
        );
    }
}
