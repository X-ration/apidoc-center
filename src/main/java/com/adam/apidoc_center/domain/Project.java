package com.adam.apidoc_center.domain;

import com.adam.apidoc_center.common.AbstractAuditable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.persistence.*;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Entity
@Data
public class Project extends AbstractAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private AccessMode accessMode;
    @JsonIgnoreProperties("project")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "project")
    private List<ProjectDeployment> projectDeploymentList;
    @JsonIgnoreProperties("project")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "project")
    private List<ProjectSharedUser> projectSharedUserList;
    @JsonIgnoreProperties("project")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "project")
    private List<ProjectGroup> projectGroupList;

    @Getter
    public enum AccessMode {
        PUBLIC("公开"), PRIVATE("私有");
        private String desc;
        AccessMode(String desc) {
            this.desc = desc;
        }
    }
}