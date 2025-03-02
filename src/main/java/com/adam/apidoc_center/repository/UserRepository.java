package com.adam.apidoc_center.repository;

import com.adam.apidoc_center.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);
    User findById(long userId);
    User findByEmail(String email);
    int countByEmail(String email);
    int countByUsername(String username);

}
