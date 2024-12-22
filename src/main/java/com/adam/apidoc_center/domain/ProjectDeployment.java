package com.adam.apidoc_center.domain;

import com.adam.apidoc_center.common.AbstractAuditable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = false)
@Entity
@Data
public class ProjectDeployment extends AbstractAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "project_id")
    private long projectId;
    @Enumerated(EnumType.STRING)
    private Environment environment;
    private String deploymentUrl;
    private boolean isEnabled;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    @Getter
    public enum Environment {
        DEV("开发"), TEST("测试"), PROD("生产"), CUSTOM("自定义");
        private String desc;
        Environment(String desc) {
            this.desc = desc;
        }
        public String getFullDesc() {
            return desc + "环境";
        }
    }
}