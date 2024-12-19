package com.adam.apidoc_center.repository;

import com.adam.apidoc_center.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

}
