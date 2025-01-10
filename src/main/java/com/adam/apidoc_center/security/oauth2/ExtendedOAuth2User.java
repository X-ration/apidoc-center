package com.adam.apidoc_center.security.oauth2;

import com.adam.apidoc_center.domain.User;
import com.adam.apidoc_center.security.SecurityUser;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
@Setter
public class ExtendedOAuth2User extends DefaultOAuth2User implements SecurityUser {

    private User user;
    private OAuth2Provider oAuth2Provider;

    /**
     * Constructs a {@code DefaultOAuth2User} using the provided parameters.
     *
     * @param authorities      the authorities granted to the user
     * @param attributes       the attributes about the user
     * @param nameAttributeKey the key used to access the user's &quot;name&quot; from
     *                         {@link #getAttributes()}
     */
    public ExtendedOAuth2User(Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes,
                              String nameAttributeKey, OAuth2Provider oAuth2Provider, User user) {
        super(authorities, attributes, nameAttributeKey);
        this.user = user;
        this.oAuth2Provider = oAuth2Provider;
    }

}
