package com.mindary.identity.services.impl;

import com.mindary.identity.dto.CustomerDto;
import com.mindary.identity.models.CustomerEntity;
import com.mindary.identity.repositories.CustomerRepository;
import com.mindary.identity.services.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

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
    public Optional<CustomerEntity> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    @Override
    public void resetPassword(CustomerEntity customerEntity, String newPassword) {
        customerEntity.setPassword(passwordEncoder.encode(newPassword));
        customerEntity.setTokenValidated(null);
        customerRepository.save(customerEntity);
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
    public CustomerEntity partialUpdate(UUID id, CustomerDto customerDto) {
        return customerRepository.findById(id).map(existingCustomer -> {
            Optional.ofNullable(customerDto.getEmail()).ifPresent(existingCustomer::setEmail);
            Optional.ofNullable(customerDto.getProfileImage()).ifPresent(existingCustomer::setProfileImage);
            Optional.ofNullable(customerDto.getNickName()).ifPresent(existingCustomer::setUsername);
            Optional.ofNullable(customerDto.getGender()).ifPresent(existingCustomer::setGender);
            Optional.ofNullable(customerDto.getAge()).ifPresent(existingCustomer::setAge);

            int hour = customerDto.getHour();
            int minute = customerDto.getMinute();
            String ampm = customerDto.getAmpm();

            try {
                if (ampm.equals("PM") && hour != 12) {
                    hour += 12;
                } else if (ampm.equals("AM") && hour == 12) {
                    hour = 0;
                }

                LocalTime reminderTime = LocalTime.of(hour, minute);
                existingCustomer.setReminderTime(reminderTime);
            } catch (DateTimeException e) {
                throw new IllegalArgumentException("Invalid reminder time format", e);
            }

            return customerRepository.save(existingCustomer);
        }).orElseThrow(() -> new RuntimeException("Customer does not exist"));
    }
}
