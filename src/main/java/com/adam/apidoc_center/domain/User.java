package com.adam.apidoc_center.domain;

import com.adam.apidoc_center.config.WebConfig;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Proxy;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
@Data
@Proxy(lazy = false)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String email;
    private String username;
    private String password;
    private String avatarUrl;
    private String description;
    private String userType;
    private boolean isEnabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    //@JsonIgnoreProperties注解避免了循环引用对象循环序列化StackOverflowException
    @JsonIgnoreProperties("user")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user")
    private List<UserAuthority> userAuthorities;
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "user")
    private UserOAuth2Huawei userOAuth2Huawei;
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "user")
    private UserOAuth2Github userOAuth2Github;

    public String getCreateTimeDesc() {
        return createTime.format(WebConfig.DATE_TIME_FORMATTER);
    }

    public List<UserType> getUserTypeList() {
        return Arrays.stream(userType.split(","))
                .map(UserType::valueOf)
                .collect(Collectors.toList());
    }

    public String getUserTypeFullDesc() {
        List<String> fullDescList = Arrays.stream(userType.split(","))
                .map(UserType::valueOf)
                .map(UserType::getFullDesc)
                .collect(Collectors.toList());
        return StringUtils.join(fullDescList, ",");
    }

    public void setUserTypeList(List<UserType> userTypeList) {
        this.userType = StringUtils.join(userTypeList, ",");
    }

    public enum UserType {
        NORMAL("普通"), OAUTH2_GITHUB("GitHub"), OAUTH2_HUAWEI("华为");
        private String desc;
        private static final Map<String, UserType> registrationIdToUserTypeMap = new HashMap<>();
        static {
            registrationIdToUserTypeMap.put("huawei", OAUTH2_HUAWEI);
            registrationIdToUserTypeMap.put("github", OAUTH2_GITHUB);
        }
        UserType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }

        public String getFullDesc() {
            return desc + "用户";
        }

        public static UserType of(String registrationId) {
            Assert.notNull(registrationId, "of registrationId null");
            for(Map.Entry<String,UserType> entry: registrationIdToUserTypeMap.entrySet()) {
                if(entry.getKey().equalsIgnoreCase(registrationId)) {
                    return entry.getValue();
                }
            }
            return null;
        }
    }

}