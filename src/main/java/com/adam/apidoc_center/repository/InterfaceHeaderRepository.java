package com.adam.apidoc_center.repository;

import com.adam.apidoc_center.domain.InterfaceField;
import com.adam.apidoc_center.domain.InterfaceHeader;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterfaceHeaderRepository extends JpaRepository<InterfaceHeader, Long> {

}