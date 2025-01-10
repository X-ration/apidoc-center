package com.adam.apidoc_center.repository;

import com.adam.apidoc_center.domain.UserOAuth2Github;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOAuth2GithubRepository extends JpaRepository<UserOAuth2Github, Long> {

    UserOAuth2Github findByGithubId(Integer githubId);
    boolean existsByGithubId(Integer githubId);

}
