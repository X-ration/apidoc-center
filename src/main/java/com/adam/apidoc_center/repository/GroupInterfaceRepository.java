package com.adam.apidoc_center.repository;

import com.adam.apidoc_center.domain.GroupInterface;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupInterfaceRepository extends JpaRepository<GroupInterface, Long> {

    Page<GroupInterface> findGroupInterfacesByNameLikeOrDescriptionLike(String name, String description, Pageable pageable);
    long countGroupInterfacesByNameLikeOrDescriptionLike(String name, String description);

}