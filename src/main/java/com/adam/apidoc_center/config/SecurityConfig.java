package com.adam.apidoc_center.config;

import com.adam.apidoc_center.security.ImprovedSavedRequestAwareAuthenticationSuccessHandler;
import com.adam.apidoc_center.security.OAuth2BindUserFilter;
import com.adam.apidoc_center.security.PersistentTokenRepositoryImpl;
import com.adam.apidoc_center.security.ProjectAuthorizationManager;
import com.adam.apidoc_center.security.oauth2.MyDefaultOAuth2AuthorizationRequestResolver;
import com.adam.apidoc_center.security.oauth2.MyOAuth2LoginAuthenticationFilter;
import com.adam.apidoc_center.security.oauth2.MyOAuth2UserRequestEntityConverter;
import com.adam.apidoc_center.security.oauth2.MyOAuth2UserService;
import com.adam.apidoc_center.util.ReflectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

import javax.servlet.Filter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private ProjectAuthorizationManager projectAuthorizationManager;
    @Value("${security.csrf.enable:false}")
    private boolean csrfEnable;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ImprovedSavedRequestAwareAuthenticationSuccessHandler authenticationSuccessHandler,
                                                   PersistentTokenRepositoryImpl persistentTokenRepository,
                                                   ClientRegistrationRepository clientRegistrationRepository,
                                                   OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
                                                   MyOAuth2UserService oAuth2UserService
    ) throws Exception {
        http.authorizeHttpRequests()
                .antMatchers("/user/login/**", "/user/logout", "/user/register", "/user/sendEmailCode", "/error/**").permitAll()
                .antMatchers("/resources/**").permitAll()
                .antMatchers("/restHello/**").permitAll()
                .antMatchers("/project/**","/group/**","/interface/**").access(projectAuthorizationManager)
                .anyRequest().authenticated();

        http.formLogin(form -> form
                        .loginPage("/user/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(authenticationSuccessHandler)
                        .failureUrl("/user/login?error")
        );

        http.logout(logout -> logout
                .logoutUrl("/user/logout")
//                .addLogoutHandler(new MyLogoutHandler())
                .logoutSuccessUrl("/user/login?logout")
        );

        http.exceptionHandling(exception ->
                exception.accessDeniedPage("/error/403")
        );

        if(csrfEnable) {
            http.csrf(Customizer.withDefaults());
        } else {
            http.csrf(AbstractHttpConfigurer::disable);
        }
        http.rememberMe(rememberMe -> rememberMe
                        .key("privateKey")
                        .tokenRepository(persistentTokenRepository)
                        .tokenValiditySeconds(60 * 60 * 24 * 7)
        );
        //开启oauth2会自动注册两个Filter到SecurityFilterChain: OAuth2AuthorizationRequestRedirectFilter, OAuth2LoginAuthenticationFilter
        //在UsernamePasswordAuthenticationFilter之前
        http.oauth2Login(oauth2 -> oauth2
                        .loginPage("/user/login")
                        //用户信息端点，定义从OAuth2UserRequest到OAuth2User的获取过程
                        //默认给予ROLE_USER和以SCOPE_开头的两个权限，可以通过自定义OAuth2UserService定制权限列表
                        .userInfoEndpoint(userInfo -> userInfo
                                        .userService(oAuth2UserService(oAuth2UserService))
//                        .userAuthoritiesMapper(grantedAuthoritiesMapper)
                        )
                        //授权端点，定义页面中a.href跳转的连接的基础uri，处理Github登录请求，将请求重定向到github.com
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/user/login/oauth2/authorization")
                        )
                        //重定向端点，在Github配置的callback url的基础uri，处理Github回调请求，带上code请求access_token，然后带上token请求用户信息
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/user/login/oauth2/callback/*")
                        )
        );
        MyOAuth2LoginAuthenticationFilter myOAuth2LoginAuthenticationFilter = new MyOAuth2LoginAuthenticationFilter(
                clientRegistrationRepository, oAuth2AuthorizedClientService,
                "/user/login/oauth2/callback/*");
        http.addFilterBefore(myOAuth2LoginAuthenticationFilter, OAuth2LoginAuthenticationFilter.class);
        OAuth2BindUserFilter oAuth2BindUserFilter = new OAuth2BindUserFilter("/user/login/**", "/user/logout",
                "/user/register", "/error/**", "/resources/**", "/restHello/**");
        http.addFilterAfter(oAuth2BindUserFilter, AuthorizationFilter.class);
        SecurityFilterChain securityFilterChain = http.build();
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManagerBuilder.class).getObject();
        myOAuth2LoginAuthenticationFilter.setAuthenticationManager(authenticationManager);
        injectMyDefaultOAuth2AuthorizationRequestResolver(securityFilterChain, "/user/login/oauth2/authorization");
        return securityFilterChain;
    }

    private DefaultOAuth2UserService oAuth2UserService(MyOAuth2UserService oAuth2UserService) {
        oAuth2UserService.setRequestEntityConverter(new MyOAuth2UserRequestEntityConverter());
        return oAuth2UserService;
    }

    private void injectMyDefaultOAuth2AuthorizationRequestResolver(SecurityFilterChain securityFilterChain, String authorizationBaseUrl) {
        OAuth2AuthorizationRequestRedirectFilter filter = getFilterOfType(securityFilterChain, OAuth2AuthorizationRequestRedirectFilter.class);
        try {
            DefaultOAuth2AuthorizationRequestResolver internalResolver = ReflectUtils.getPrivateField(filter, "authorizationRequestResolver", DefaultOAuth2AuthorizationRequestResolver.class);
            MyDefaultOAuth2AuthorizationRequestResolver resolver = new MyDefaultOAuth2AuthorizationRequestResolver(internalResolver, authorizationBaseUrl);
            ReflectUtils.setPrivateField(filter, "authorizationRequestResolver", resolver);
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {

        }
    }

    private <T> T getFilterOfType(SecurityFilterChain securityFilterChain, Class<T> clazz) {
        for(Filter filter: securityFilterChain.getFilters()) {
            if(clazz.isInstance(filter)) {
                return (T) filter;
            }
        }
        return null;
    }

}
