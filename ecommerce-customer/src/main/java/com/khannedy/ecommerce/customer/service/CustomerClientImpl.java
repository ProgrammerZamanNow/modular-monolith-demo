package com.khannedy.ecommerce.customer.service;

import com.khannedy.ecommerce.customer.client.CustomerClient;
import com.khannedy.ecommerce.customer.client.CustomerClientResponse;
import com.khannedy.ecommerce.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomerClientImpl implements CustomerClient {

    private final CustomerRepository customerRepository;

    public CustomerClientImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CustomerClientResponse> getCustomer(String id) {
        return customerRepository.findById(id)
                .map(customer -> new CustomerClientResponse(
                        customer.getId(),
                        customer.getName(),
                        customer.getEmail(),
                        customer.getPhone()
                ));
    }
}
