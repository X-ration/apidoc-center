package com.adam.apidoc_center.repository;

import com.adam.apidoc_center.domain.ProjectDeployment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectDeploymentRepository extends JpaRepository<ProjectDeployment, Long> {

}
