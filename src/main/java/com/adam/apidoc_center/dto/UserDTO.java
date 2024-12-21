package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.config.WebConfig;
import com.adam.apidoc_center.domain.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UserDTO {

    private long id;
    private String email;
    private String username;
    private String password;
    private String verifyPassword;
    private String avatarUrl;
    private String description;
    private String userType;
    private String createTime;

    public UserDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.avatarUrl = user.getAvatarUrl();
        this.description = user.getDescription();
        this.userType = user.getUserType().getFullDesc();
        this.createTime = user.getCreateTime().format(WebConfig.DATE_TIME_FORMATTER);
    }

}
