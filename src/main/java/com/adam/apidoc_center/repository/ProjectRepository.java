package com.adam.apidoc_center.repository;

import com.adam.apidoc_center.domain.Project;
import com.adam.apidoc_center.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Map;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM ((SELECT * FROM (SELECT id,name,description,'PROJECT' AS type FROM project WHERE name LIKE :param OR description LIKE :param) AS p) UNION " +
            "(SELECT * FROM (SELECT id,name,'' AS description,'GROUP' AS type FROM project_group WHERE name LIKE :param) AS g) UNION " +
            "(SELECT * FROM (SELECT id,name,description,'INTERFACE' AS type FROM group_interface WHERE name LIKE :param OR description LIKE :param) AS i)) AS dt",
            countQuery = "SELECT COUNT(*) FROM ((SELECT * FROM (SELECT id,name,description,'PROJECT' AS type FROM project WHERE name LIKE :param OR description LIKE :param) AS p) UNION " +
                    "(SELECT * FROM (SELECT id,name,'' AS description,'GROUP' AS type FROM project_group WHERE name LIKE :param) AS g) UNION " +
                    "(SELECT * FROM (SELECT id,name,description,'INTERFACE' AS type FROM group_interface WHERE name LIKE :param OR description LIKE :param) AS i)) AS dt"
    )
    Page<Map<String,Object>> findAllSearchResult(@Param("param") String searchParam, Pageable pageable);

    Page<Project> findProjectsByNameLikeOrDescriptionLike(String name, String description, Pageable pageable);
    long countProjectsByNameLikeOrDescriptionLike(String name, String description);

}
