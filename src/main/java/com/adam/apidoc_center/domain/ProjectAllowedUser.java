package com.adam.apidoc_center.domain;

import com.adam.apidoc_center.common.AbstractAuditable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = false)
@Entity
@Data
@NoArgsConstructor
public class ProjectAllowedUser extends AbstractAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name= "project_id")
    private long projectId;
    private long userId;
    private boolean isAllow;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    public ProjectAllowedUser(long projectId, long userId) {
        this.projectId = projectId;
        this.userId = userId;
        this.isAllow = true;
    }

}