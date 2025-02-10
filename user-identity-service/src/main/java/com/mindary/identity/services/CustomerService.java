package com.mindary.identity.services;

import com.mindary.identity.models.CustomerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface CustomerService {
    CustomerEntity save(CustomerEntity tenant);

    Page<CustomerEntity> findAll(Pageable pageable);

    Optional<CustomerEntity> findOne(UUID id);

    boolean isExist(UUID id);

    void delete(UUID id);

    CustomerEntity partialUpdate(UUID id, CustomerEntity hostEntity);
}
