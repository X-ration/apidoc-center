package com.adam.apidoc_center.common;

import com.adam.apidoc_center.domain.User;
import com.adam.apidoc_center.security.SecurityUtil;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<Long> {
    @Override
    public Optional<Long> getCurrentAuditor() {
        User user = SecurityUtil.getUser();
        long userId = user.getId();
        return Optional.of(userId);
    }
}