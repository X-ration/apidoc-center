package com.adam.apidoc_center.repository;

import com.adam.apidoc_center.domain.UserOAuth2Huawei;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOAuth2HuaweiRepository extends JpaRepository<UserOAuth2Huawei, Long> {

    UserOAuth2Huawei findByUnionId(String unionId);
    boolean existsByUnionId(String unionId);

}
