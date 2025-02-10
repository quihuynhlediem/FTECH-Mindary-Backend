package com.mindary.identity.mappers.impl;

import com.mindary.identity.dto.CustomerDto;
import com.mindary.identity.mappers.Mapper;
import com.mindary.identity.models.CustomerEntity;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerMapper implements Mapper<CustomerEntity, CustomerDto> {

    private final ModelMapper modelMapper;

    @Override
    public CustomerDto mapTo(CustomerEntity customerEntity) {
        return modelMapper.map(customerEntity, CustomerDto.class);
    }

    @Override
    public CustomerEntity mapFrom(CustomerDto customerDto) {
        return modelMapper.map(customerDto, CustomerEntity.class);
    }
}
