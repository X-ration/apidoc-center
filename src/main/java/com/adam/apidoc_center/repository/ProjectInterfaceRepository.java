package com.adam.apidoc_center.repository;

import com.adam.apidoc_center.domain.GroupInterface;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectInterfaceRepository extends JpaRepository<GroupInterface, Long> {

}