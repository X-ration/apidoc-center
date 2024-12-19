package com.adam.apidoc_center.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
public class RememberMeToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String username;
    private String series;
    private String token;
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date lastUsed;

}