package com.mindary.identity.controllers;

import com.mindary.identity.dto.CustomerDto;
import com.mindary.identity.mappers.impl.CustomerMapper;
import com.mindary.identity.models.CustomerEntity;
import com.mindary.identity.services.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@Tag(name = "Customer CRUD APIs", description = "These APIs are used to handle CRUD for Customer")
@RestController
@RequestMapping(path = "/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

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
    @GetMapping(path = "/{id}")
    public ResponseEntity<CustomerDto> getCustomer(@PathVariable("id") UUID id) {
        Optional<CustomerEntity> foundCustomer = customerService.findOne(id);
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
    @PutMapping(path = "/{id}")
    public ResponseEntity<CustomerDto> fullUpdateHost(
            @PathVariable("id") UUID id,
            @Validated @RequestBody CustomerDto customerDto
    ) {
        if (!customerService.isExist(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        customerDto.setId(id);
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
    @PatchMapping(path = "/{id}")
    public ResponseEntity<CustomerDto> partialUpdateHost(
            @PathVariable("id") UUID id,
            @Validated @RequestBody CustomerDto customerDto
    ) {
        if (!customerService.isExist(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        CustomerEntity hostEntity = customerMapper.mapFrom(customerDto);
        CustomerEntity updateHost = customerService.partialUpdate(id, hostEntity);

        return new ResponseEntity<>(
                customerMapper.mapTo(updateHost),
                HttpStatus.OK
        );
    }

//    @PostMapping(path = "/hosts/{id}/address")
//    public ResponseEntity<HostDto> addHostAddress(
//            @PathVariable("id") long id,
//            @RequestBody AddressDto addressDto
//    ) {
//        if (!hostService.isExists(id)) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//
//        Address addressEntity = addressMapper.mapFrom(addressDto);
//        Host hostEntity = hostService.saveAddress(id, addressEntity);
//        return new ResponseEntity<>(hostMapper.mapTo(hostEntity), HttpStatus.OK);
//    }

    @Operation(summary = "Delete a customer", description = "Delete a customer by their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Customer deleted successfully", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(schema = @Schema())})
    })
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<CustomerDto> deleteHost(@PathVariable("id") UUID id) {
        if (!customerService.isExist(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        customerService.delete(id);

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
