package com.adam.apidoc_center.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String email;
    private String username;
    private String password;
    private String avatarUrl;
    @Enumerated(EnumType.STRING)
    private UserType userType;
    private boolean isEnabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    //@JsonIgoreProperties注解避免了循环引用对象循环序列化StackOverflowException
    @JsonIgnoreProperties("user")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user")
    private List<UserAuthority> userAuthorities;

    public enum UserType {
        NORMAL, OAUTH2_GITHUB, OAUTH2_HUAWEI
    }

}