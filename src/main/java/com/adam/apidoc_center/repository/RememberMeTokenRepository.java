package com.adam.apidoc_center.repository;

import com.adam.apidoc_center.domain.RememberMeToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

public interface RememberMeTokenRepository extends JpaRepository<RememberMeToken, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE RememberMeToken SET token=:token,lastUsed=:lastUsed " +
            "WHERE series=:series")
    int updateTokenAndLastUsedBySeries(@Param("token")String token, @Param("lastUsed") Date lastUsed, @Param("series")String series);

    RememberMeToken queryBySeries(String series);

    @Transactional
    int deleteByUsername(String username);

}