package com.adam.apidoc_center.domain;

import com.adam.apidoc_center.common.AbstractAuditable;
import com.adam.apidoc_center.dto.InterfaceHeaderDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = false)
@Entity
@Data
@NoArgsConstructor
public class InterfaceHeader extends AbstractAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "interface_id")
    private long interfaceId;
    private String name;
    private String description;
    private boolean required;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "interface_id", insertable = false, updatable = false)
    private GroupInterface groupInterface;

    public InterfaceHeader(long interfaceId, InterfaceHeaderDTO interfaceHeaderDTO) {
        this.interfaceId = interfaceId;
        this.name = interfaceHeaderDTO.getName();
        this.description = interfaceHeaderDTO.getDescription();
        this.required = interfaceHeaderDTO.getRequired();
    }

}
