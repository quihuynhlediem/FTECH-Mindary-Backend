package com.mindary.identity.controllers;

import com.mindary.identity.dto.CustomerDto;
import com.mindary.identity.mappers.impl.CustomerMapper;
import com.mindary.identity.models.CustomerEntity;
import com.mindary.identity.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/customers")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:3000")
public class CustomerController {
    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    @GetMapping()
    public Page<CustomerDto> listCustomers(Pageable pageable) {
        Page<CustomerEntity> customers = customerService.findAll(pageable);
        return customers.map(customerMapper::mapTo);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<CustomerDto> getCustomer(@PathVariable("id") UUID id) {
        Optional<CustomerEntity> foundCustomer = customerService.findOne(id);
        return foundCustomer.map(host -> {
            CustomerDto customerDto = customerMapper.mapTo(host);
            return new ResponseEntity<>(customerDto, HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping()
    public ResponseEntity<CustomerDto> createCustomer(
            @Validated @RequestBody CustomerDto hostDto
    ) {
        CustomerEntity customerEntity = customerMapper.mapFrom(hostDto);
        CustomerEntity savedCustomerEntity = customerService.save(customerEntity);
        return new ResponseEntity<>(customerMapper.mapTo(savedCustomerEntity), HttpStatus.CREATED);
    }

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

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<CustomerDto> deleteHost(@PathVariable("id") UUID id) {
        if (!customerService.isExist(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        customerService.delete(id);

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
