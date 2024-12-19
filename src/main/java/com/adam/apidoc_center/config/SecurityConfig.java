package com.adam.apidoc_center.config;

import com.adam.apidoc_center.security.ImprovedSavedRequestAwareAuthenticationSuccessHandler;
import com.adam.apidoc_center.security.PersistentTokenRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, PersistentTokenRepositoryImpl persistentTokenRepository) throws Exception {
        http.authorizeHttpRequests()
                .antMatchers("/user/login/**", "/user/logout", "/user/register", "/error/**").permitAll()
                .antMatchers("/resources/**").permitAll()
                .anyRequest().authenticated();

        http.formLogin(form -> form
                        .loginPage("/user/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(new ImprovedSavedRequestAwareAuthenticationSuccessHandler())
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

        http.csrf(Customizer.withDefaults());
        http.rememberMe(rememberMe -> rememberMe
                        .key("privateKey")
                        .tokenRepository(persistentTokenRepository)
                        .tokenValiditySeconds(60 * 60 * 24 * 7)
        );
        return http.build();
    }

}
