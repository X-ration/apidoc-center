package com.adam.apidoc_center.domain;

import com.adam.apidoc_center.common.AbstractAuditable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Entity
@Data
public class ProjectGroup extends AbstractAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "project_id")
    private long projectId;
    private String name;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;
    @JsonIgnoreProperties("projectGroup")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "projectGroup")
    private List<ProjectInterface> projectInterfaceList;

}