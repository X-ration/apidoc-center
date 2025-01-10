package com.adam.apidoc_center.domain;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "user_oauth2_github")
public class UserOAuth2Github {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id")
    private long userId;
    private Integer githubId;
    private String username;
    private String realName;
    private String avatarUrl;
    private String email;
    private String bio;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;

}
