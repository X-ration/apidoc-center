package com.adam.apidoc_center.common;

import com.adam.apidoc_center.security.ExtendedUser;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<Long> {
    @Override
    public Optional<Long> getCurrentAuditor() {
        ExtendedUser extendedUser = (ExtendedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long userId = extendedUser.getUser().getId();
        return Optional.of(userId);
    }
}