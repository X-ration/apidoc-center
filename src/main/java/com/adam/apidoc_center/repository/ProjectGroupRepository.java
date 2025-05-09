package com.adam.apidoc_center.repository;

import com.adam.apidoc_center.domain.ProjectGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectGroupRepository extends JpaRepository<ProjectGroup, Long> {

    Page<ProjectGroup> findProjectGroupsByNameLike(String name, Pageable pageable);
    long countProjectGroupsByNameLike(String name);

}