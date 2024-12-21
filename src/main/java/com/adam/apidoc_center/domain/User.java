package com.adam.apidoc_center.domain;

import com.adam.apidoc_center.config.WebConfig;
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
    private String description;
    @Enumerated(EnumType.STRING)
    private UserType userType;
    private boolean isEnabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    //@JsonIgoreProperties注解避免了循环引用对象循环序列化StackOverflowException
    @JsonIgnoreProperties("user")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user")
    private List<UserAuthority> userAuthorities;

    public String getCreateTimeDesc() {
        return createTime.format(WebConfig.DATE_TIME_FORMATTER);
    }

    public enum UserType {
        NORMAL("普通"), OAUTH2_GITHUB("GitHub"), OAUTH2_HUAWEI("华为");
        private String desc;
        UserType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }

        public String getFullDesc() {
            return desc + "用户";
        }
    }

}