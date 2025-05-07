package com.mindary.identity.controllers;

import com.mindary.identity.dto.CustomerDto;
import com.mindary.identity.dto.response.AuthResponse;
import com.mindary.identity.mappers.impl.CustomerMapper;
import com.mindary.identity.models.CustomerEntity;
import com.mindary.identity.models.User;
import com.mindary.identity.services.AuthenticationService;
import com.mindary.identity.services.CustomerService;
import com.mindary.identity.services.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Tag(name = "Customer CRUD APIs", description = "These APIs are used to handle CRUD for Customer")
@RestController
@RequestMapping(path = "/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {
    private final CustomerService customerService;
    private final CustomerMapper customerMapper;
    private final AuthenticationService authenticationService;
    private final EmailService emailService;

    @Operation(summary = "List all customers", description = "Retrieve a paginated list of customers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of customers", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(schema = @Schema())})
    })
    @GetMapping()
    public Page<CustomerDto> listCustomers(Pageable pageable) {
        Page<CustomerEntity> customers = customerService.findAll(pageable);
        return customers.map(customerMapper::mapTo);
    }

    @Operation(summary = "Get customer by ID", description = "Retrieve a customer by their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of customer", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerDto.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(schema = @Schema())})
    })
    @PreAuthorize("#userId == principal.id")
    @GetMapping(path = "/{id}")
    public ResponseEntity<CustomerDto> getCustomer(@PathVariable("id") UUID userId) {
        Optional<CustomerEntity> foundCustomer = customerService.findOne(userId);
        return foundCustomer.map(host -> {
            CustomerDto customerDto = customerMapper.mapTo(host);
            return new ResponseEntity<>(customerDto, HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Create a new customer", description = "Create a new customer record.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid input", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(schema = @Schema())})
    })
    @PostMapping()
    public ResponseEntity<CustomerDto> createCustomer(
            @Validated @RequestBody CustomerDto hostDto
    ) {
        CustomerEntity customerEntity = customerMapper.mapFrom(hostDto);
        customerEntity.setRole(User.UserRole.CUSTOMER);
        CustomerEntity savedCustomerEntity = customerService.save(customerEntity);
        return new ResponseEntity<>(customerMapper.mapTo(savedCustomerEntity), HttpStatus.CREATED);
    }

    @Operation(summary = "Update customer details", description = "Update the details of an existing customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid input", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(schema = @Schema())})
    })
    @PreAuthorize("#userId == principal.id")
    @PutMapping(path = "/{id}")
    public ResponseEntity<CustomerDto> fullUpdateHost(
            @PathVariable("id") UUID userId,
            @Validated @RequestBody CustomerDto customerDto
    ) {
        if (!customerService.isExist(userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        customerDto.setId(userId);
        CustomerEntity hostEntity = customerMapper.mapFrom(customerDto);
        CustomerEntity savedHostEntity = customerService.save(hostEntity);
        return new ResponseEntity<>(
                customerMapper.mapTo(savedHostEntity),
                HttpStatus.OK
        );
    }

    @Operation(summary = "Partially update customer details", description = "Partially update the details of an existing customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer partially updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid input", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(schema = @Schema())})
    })
    @PreAuthorize("#userId == principal.id")
    @PatchMapping(path = "/{id}")
    public ResponseEntity<CustomerDto> partialUpdateHost(
            @PathVariable("id") UUID userId,
            @Validated @RequestBody CustomerDto customerDto
    ) {
        if (!customerService.isExist(userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

//        CustomerEntity hostEntity = customerMapper.mapFrom(customerDto);
        CustomerEntity updateHost = customerService.partialUpdate(userId, customerDto);

        return new ResponseEntity<>(
                customerMapper.mapTo(updateHost),
                HttpStatus.OK
        );
    }

    @PostMapping(path = "/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @RequestBody CustomerDto customerDto
    ) throws MessagingException, UnsupportedEncodingException {
        String email = customerDto.getEmail();
        Optional<CustomerEntity> findCustomer = customerService.findByEmail(email);
        if (findCustomer.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        CustomerEntity customerEntity = findCustomer.get();

        String otp = authenticationService.generateOTP();
        LocalDateTime expiryDateTime = authenticationService.generateExpiryDateTime();

        customerEntity.setResetToken(otp);
        customerEntity.setTokenExpire(expiryDateTime);
        customerEntity.setTokenValidated(false);
        customerService.save(customerEntity);

        emailService.sendOtp(customerEntity.getEmail(), customerEntity.getUsername(), otp);

        return new ResponseEntity<>("OTP has been sent to email " + email, HttpStatus.OK);
    }

    @PostMapping(path = "/validate-otp")
    public ResponseEntity<String> validateOTP(
            @RequestBody CustomerDto customerDto
    ) {
        String email = customerDto.getEmail();
        String otp = customerDto.getOtp();

        Optional<CustomerEntity> findCustomer = customerService.findByEmail(email);
        if (findCustomer.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        CustomerEntity customerEntity = findCustomer.get();

        if (customerEntity.getTokenExpire().isBefore(LocalDateTime.now())) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }

        if (!customerEntity.getResetToken().equals(otp)) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }

        if (customerEntity.getTokenValidated()) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }

        customerEntity.setResetToken(null);
        customerEntity.setTokenExpire(null);
        customerEntity.setTokenValidated(true);
        customerService.save(customerEntity);

        return new ResponseEntity<>("OTP Validation Successful", HttpStatus.OK);
    }

    @PostMapping(path = "/new-password")
    public ResponseEntity<String> setNewPassword(
            @RequestBody CustomerDto customerDto
    ) {
        String email = customerDto.getEmail();
        String newPassword = customerDto.getPassword();

        Optional<CustomerEntity> findCustomer = customerService.findByEmail(email);
        if (findCustomer.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        CustomerEntity customerEntity = findCustomer.get();

        if (!customerEntity.getTokenValidated()) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }

        customerService.resetPassword(customerEntity, newPassword);

        return new ResponseEntity<>("Password Reset Successful", HttpStatus.OK);
    }



    @Operation(summary = "Delete a customer", description = "Delete a customer by their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Customer deleted successfully", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(schema = @Schema())})
    })
    @PreAuthorize("#userId == principal.id")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<CustomerDto> deleteHost(@PathVariable("id") UUID userId) {
        if (!customerService.isExist(userId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        customerService.delete(userId);

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
