package com.mindary.diary.mappers.impl;

import com.mindary.diary.dto.DiaryDto;
import com.mindary.diary.mappers.Mapper;
import com.mindary.diary.models.DiaryEntity;
import lombok.RequiredArgsConstructor;
import org.hibernate.engine.spi.Managed;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiaryMapperImpl implements Mapper<DiaryEntity, DiaryDto> {

    private final ModelMapper modelMapper;

    @Override
    public DiaryDto mapTo(DiaryEntity diaryEntity) {
        return modelMapper.map(diaryEntity, DiaryDto.class);
    }

    @Override
    public DiaryEntity mapFrom(DiaryDto diaryDto) {
        return modelMapper.map(diaryDto, DiaryEntity.class);
    }
}
