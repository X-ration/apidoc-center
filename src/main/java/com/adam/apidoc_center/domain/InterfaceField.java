package com.adam.apidoc_center.domain;

import com.adam.apidoc_center.common.AbstractAuditable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = false)
@Entity
@Data
public class InterfaceField extends AbstractAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "interface_id")
    private long interfaceId;
    private String name;
    private String description;
    private Type type;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "interface_id", insertable = false, updatable = false)
    private ProjectInterface projectInterface;

    public enum Type {
        TEXT,FILE
    }
}
