package com.mindary.diary.controllers;

import com.mindary.diary.dto.DiaryDto;
import com.mindary.diary.mappers.Mapper;
import com.mindary.diary.models.DiaryEntity;
import com.mindary.diary.models.DiaryImage;
import com.mindary.diary.services.DiaryImageService;
import com.mindary.diary.services.DiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/diaries")
@RequiredArgsConstructor
@Slf4j
public class DiaryController {
    private final DiaryService diaryService;
    private final Mapper<DiaryEntity, DiaryDto> diaryMapper;
    private final DiaryImageService diaryImageService;

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
            @PathVariable("diaryId") UUID diaryId
    ) {
        Optional<DiaryEntity> foundDiary = diaryService.findOne(diaryId);
        return foundDiary.map(diary -> {
            DiaryDto diaryDto = diaryMapper.mapTo(diary);
            return new ResponseEntity<>(diaryDto, HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PreAuthorize("#userId.toString() == authentication.name")
    @GetMapping(path = "/user/{userId}/{date}")
    public ResponseEntity<DiaryDto> getDiaryByTimezone(
            @PathVariable("userId") UUID userId,
            @PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate currentDate
    ) {

        Optional<DiaryEntity> foundDiary = diaryService.findByUserIdAndDate(userId, currentDate);

        if (foundDiary.isPresent()) {
            DiaryDto diaryDto = diaryMapper.mapTo(foundDiary.get());
            return new ResponseEntity<>(diaryDto, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PreAuthorize("#userId.toString() == authentication.name")
    @PostMapping(path = "/user/{userId}")
    public ResponseEntity<DiaryDto> createDiary(
            @PathVariable("userId") UUID userId,
            @RequestParam("diary") String diary,
            @RequestParam(value = "images", required = false) List<MultipartFile> photos,
            @RequestParam(value = "timezone") String timezone
    ) throws Exception {
        log.info("Creating diary");

        Optional<DiaryEntity> existingDiary = diaryService.findByUserIdAndDate(userId, timezone);

        if (existingDiary.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(diaryMapper.mapTo(existingDiary.get()));
        }

        DiaryEntity savedDiary = diaryService.create(userId, diary);

        Set<DiaryImage> savedImages = diaryImageService.uploadAndSaveImages(photos, savedDiary);

        savedDiary.setImages(savedImages);

        return ResponseEntity.status(HttpStatus.CREATED).body(diaryMapper.mapTo(savedDiary));
    }

    @PreAuthorize("#userId.toString() == authentication.name")
    @PostMapping(path = "/user/{userId}/{date}")
    public ResponseEntity<DiaryDto> createDiaryOnTargetDate(
            @PathVariable("userId") UUID userId,
            @PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate targetDate,
            @RequestParam("diary") String diary,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "timezone") String timezone
    ) throws Exception {
        log.info("Creating diary");
        ZoneId zone = ZoneId.of(timezone);
        LocalDate currentDate = LocalDate.now(zone);

        if (targetDate.isAfter(currentDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(DiaryDto.builder().content("Target date is in the future.").build());
        }

        Optional<DiaryEntity> existingDiary = diaryService.findByUserIdAndDate(userId, targetDate);

        if (existingDiary.isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        DiaryEntity savedDiary = diaryService.create(userId, diary);

        Set<DiaryImage> savedDiaryImages = diaryImageService.uploadAndSaveImages(images, savedDiary);
        savedDiary.setImages(savedDiaryImages);

        return ResponseEntity.status(HttpStatus.CREATED).body(diaryMapper.mapTo(savedDiary));
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
