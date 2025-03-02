package com.adam.apidoc_center.common;

import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/**
 * 所有带有审计功能的实体的父类
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public class AbstractAuditable {

    @CreatedDate
    private LocalDateTime createTime;
    @LastModifiedDate
    private LocalDateTime updateTime;
    @CreatedBy
    private long createUserId;
    @LastModifiedBy
    private long updateUserId;

}