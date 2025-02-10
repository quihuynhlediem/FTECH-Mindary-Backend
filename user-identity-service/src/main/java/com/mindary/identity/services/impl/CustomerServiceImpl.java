package com.mindary.identity.services.impl;

import com.mindary.identity.models.CustomerEntity;
import com.mindary.identity.repositories.CustomerRepository;
import com.mindary.identity.services.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public CustomerEntity save(CustomerEntity customer) {
        return customerRepository.save(customer);
    }

    @Override
    public Page<CustomerEntity> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    @Override
    public Optional<CustomerEntity> findOne(UUID id) {
        return customerRepository.findById(id);
    }

    @Override
    public boolean isExist(UUID id) {
        return customerRepository.existsById(id);
    }

    @Override
    public void delete(UUID id) {
        customerRepository.deleteById(id);
    }

    @Override
    public CustomerEntity partialUpdate(UUID id, CustomerEntity hostEntity) {
        return null;
    }
}
