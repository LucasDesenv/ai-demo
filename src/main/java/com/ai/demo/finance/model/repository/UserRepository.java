package com.ai.demo.finance.model.repository;

import com.ai.demo.finance.model.User;
import com.ai.demo.finance.model.enums.Country;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Page<User> findAllByCountry(Country country, Pageable pageable);
}