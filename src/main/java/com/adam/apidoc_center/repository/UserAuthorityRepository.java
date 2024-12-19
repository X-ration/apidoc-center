package com.adam.apidoc_center.repository;

import com.adam.apidoc_center.domain.UserAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAuthorityRepository extends JpaRepository<UserAuthority, Long> {

    List<UserAuthority> findByUserId(long userId);

}
