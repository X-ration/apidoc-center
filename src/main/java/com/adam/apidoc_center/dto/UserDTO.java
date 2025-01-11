package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.config.WebConfig;
import com.adam.apidoc_center.domain.User;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String oAuth2HuaweiUsername;
    private String oAuth2GithubUsername;

    public UserDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.avatarUrl = user.getAvatarUrl();
        this.description = user.getDescription();
        this.userType = user.getUserTypeFullDesc();
        this.createTime = user.getCreateTime().format(WebConfig.DATE_TIME_FORMATTER);
        if(user.getUserTypeList().contains(User.UserType.OAUTH2_HUAWEI)) {
            this.oAuth2HuaweiUsername = user.getUserOAuth2Huawei().getDisplayName();
        }
        if(user.getUserTypeList().contains(User.UserType.OAUTH2_GITHUB)) {
            this.oAuth2GithubUsername = user.getUserOAuth2Github().getUsername();
        }
    }

}
