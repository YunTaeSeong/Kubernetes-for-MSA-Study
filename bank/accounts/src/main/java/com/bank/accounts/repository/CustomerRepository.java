package com.bank.accounts.repository;

import com.bank.accounts.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByMobileNumber(String mobileNumber); // 대소문자는 틀려도 되지만 필드명은 정확히 엔티티랑 같아야 함
}
