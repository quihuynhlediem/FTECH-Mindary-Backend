package com.mindary.identity.security;

import com.mindary.identity.models.CustomerEntity;
import com.mindary.identity.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

@RequiredArgsConstructor
public class SystemUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<CustomerEntity> customer = customerRepository.findByEmail(email);
        if (customer.isPresent()) {
            return new SystemUserDetails(customer.get());
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}