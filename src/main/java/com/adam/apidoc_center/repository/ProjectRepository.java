package com.adam.apidoc_center.repository;

import com.adam.apidoc_center.domain.Project;
import com.adam.apidoc_center.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

}
