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
    private String environment;
    private String deploymentUrl;
    private boolean isEnabled;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

}