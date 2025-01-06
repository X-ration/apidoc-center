package com.adam.apidoc_center.repository;

import com.adam.apidoc_center.domain.ProjectSharedUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectAllowedUserRepository extends JpaRepository<ProjectSharedUser, Long> {

}