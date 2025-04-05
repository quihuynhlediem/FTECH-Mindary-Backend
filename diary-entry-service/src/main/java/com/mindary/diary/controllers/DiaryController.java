package com.mindary.diary.controllers;

import com.mindary.diary.dto.AnalysisResultDto;
import com.mindary.diary.dto.DiaryDto;
import com.mindary.diary.mappers.Mapper;
import com.mindary.diary.models.DiaryEntity;
import com.mindary.diary.models.DiaryImage;
import com.mindary.diary.services.DiaryImageService;
import com.mindary.diary.services.DiaryService;
import com.mindary.diary.services.RabbitMQSender;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Diary APIs", description = "These APIs are used to handle diary entries.")
@RestController
@RequestMapping(path = "/api/v1/diaries")
@RequiredArgsConstructor
@Slf4j
public class DiaryController {
    private final DiaryService diaryService;
    private final Mapper<DiaryEntity, DiaryDto> diaryMapper;
    private final DiaryImageService diaryImageService;
    private final RabbitMQSender rabbitMQSender;

    @Operation(summary = "Get diaries by user ID", description = "Retrieve a paginated list of diaries for a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of diaries", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have access", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(schema = @Schema())})
    })
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

    @Operation(summary = "Get diary by ID", description = "Retrieve a specific diary by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of diary", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DiaryDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have access", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", description = "Diary not found", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(schema = @Schema())})
    })
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

    @Operation(summary = "Get diary by date", description = "Retrieve a diary for a specific user on a given date.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of diary", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DiaryDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have access", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", description = "Diary not found", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(schema = @Schema())})
    })
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

    @Operation(summary = "Create a new diary", description = "Create a new diary entry for a user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Diary created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DiaryDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have access", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "409", description = "Conflict - Diary already exists for the given date", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(schema = @Schema())})
    })
    @PreAuthorize("#userId.toString() == authentication.name")
    @PostMapping(path = "/user/{userId}")
    public ResponseEntity<AnalysisResultDto> createDiary(
            @PathVariable("userId") UUID userId,
            @RequestParam("diary") String diary,
            @RequestParam(value = "images", required = false) List<MultipartFile> photos,
            @RequestParam(value = "timezone") String timezone
    ) throws Exception {
        log.info("Creating diary");
        Optional<DiaryEntity> existingDiary = diaryService.findByUserIdAndDate(userId, timezone);

        if (existingDiary.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AnalysisResultDto());
        }
        DiaryEntity savedDiary = diaryService.create(userId, diary);
        Set<DiaryImage> savedImages = diaryImageService.uploadAndSaveImages(photos, savedDiary);
        savedDiary.setImages(savedImages);
        rabbitMQSender.sendDiary(savedDiary);

        AnalysisResultDto analysisResultDto = diaryService.analyze(savedDiary);

        return ResponseEntity.status(HttpStatus.CREATED).body(analysisResultDto);
    }

    @Operation(summary = "Create a diary on a target date", description = "Create a diary entry for a user on a specific date.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Diary created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DiaryDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Target date is in the future", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have access", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "409", description = "Conflict - Diary already exists for the given date", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(schema = @Schema())})
    })
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

        AnalysisResultDto analysisResultDto = diaryService.analyze(savedDiary);

        return ResponseEntity.status(HttpStatus.CREATED).body(diaryMapper.mapTo(savedDiary));
    }

    @Operation(summary = "Update a diary", description = "Partially update an existing diary entry.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Diary updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DiaryDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have access", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", description = "Diary not found", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(schema = @Schema())})
    })
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
