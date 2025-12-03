package com.bank.accounts.controller;

import com.bank.accounts.dto.CustomerDetailsDto;
import com.bank.accounts.service.ICustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/api", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
@Validated
@Tag(
        name = "CRUD REST APIs for Customers in Bank",
        description = "REST APIs in Bank FETCH customer details"
)
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final ICustomerService iCustomerService;

    @GetMapping("/fetchCustomerDetails")
    public ResponseEntity<CustomerDetailsDto> fetchCustomerDetails(
            @RequestHeader("bank-correlation-id") String correlationId,
            @Pattern(regexp="(^$|[0-9]{10})", message = "Mobile number must be 10 digits")
            @RequestParam String mobileNumber)
    {
//        logger.debug("bank-correlation-id found: {}", correlationId);
        logger.debug("fetchCustomerDetails method start");
        CustomerDetailsDto customerDetailsDto = iCustomerService.fetchCustomerDetails(mobileNumber, correlationId);
        logger.debug("fetchCustomerDetails method end");
        return ResponseEntity
                .status(HttpStatus.SC_OK)
                .body(customerDetailsDto);
    }
}
