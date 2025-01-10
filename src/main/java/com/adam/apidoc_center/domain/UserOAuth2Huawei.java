package com.adam.apidoc_center.domain;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "user_oauth2_huawei")
public class UserOAuth2Huawei {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id")
    private long userId;
    private String displayName;
    private String headPictureUrl;
    private String unionId;
    private String openId;
    private Integer displayNameFlag;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;

}
