package com.adam.apidoc_center.domain;

import com.adam.apidoc_center.common.AbstractAuditable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.http.HttpMethod;

import javax.persistence.*;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Entity
@Data
public class GroupInterface extends AbstractAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "group_id")
    private long groupId;
    private String name;
    private String description;
    private String relativePath;
    @Enumerated(EnumType.STRING)
    private HttpMethod method;
    @Enumerated(EnumType.STRING)
    private Type type;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private ProjectGroup projectGroup;
    @JsonIgnoreProperties("groupInterface")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "groupInterface")
    private List<InterfaceField> interfaceFieldList;
    @JsonIgnoreProperties("groupInterface")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "groupInterface")
    private List<InterfaceHeader> interfaceHeaderList;

    @Getter
    @AllArgsConstructor
    public enum Type {
        FORM_URLENCODED("application/x-www-form-urlencoded"),
        FORM_DATA("multipart/form-data"),
        JSON("application/json"),
        NO_BODY(null),
        ;
        private final String contentType;
    }
}
