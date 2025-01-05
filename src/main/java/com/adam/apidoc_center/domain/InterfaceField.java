package com.adam.apidoc_center.domain;

import com.adam.apidoc_center.common.AbstractAuditable;
import com.adam.apidoc_center.dto.InterfaceFieldDTO;
import lombok.*;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = false)
@Entity
@Data
@NoArgsConstructor
public class InterfaceField extends AbstractAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "interface_id")
    private long interfaceId;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private Type type;
    private boolean required;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "interface_id", insertable = false, updatable = false)
    private GroupInterface groupInterface;

    public InterfaceField(long interfaceId, InterfaceFieldDTO interfaceFieldDTO) {
        this.interfaceId = interfaceId;
        this.name = interfaceFieldDTO.getName();
        this.description = interfaceFieldDTO.getDescription();
        this.type = interfaceFieldDTO.getType();
        this.required = interfaceFieldDTO.getRequired();
    }

    @Getter
    @AllArgsConstructor
    public enum Type {
        TEXT("值"),FILE("文件");
        private String desc;
    }
}
