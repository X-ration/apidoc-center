package com.adam.apidoc_center.domain;

import com.adam.apidoc_center.common.AbstractAuditable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = false)
@Entity
@Data
public class InterfaceHeader extends AbstractAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "interface_id")
    private long interfaceId;
    private String name;
    private String description;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "interface_id", insertable = false, updatable = false)
    private GroupInterface groupInterface;

}
