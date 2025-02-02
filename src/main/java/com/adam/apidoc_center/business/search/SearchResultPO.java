package com.adam.apidoc_center.business.search;

import lombok.Data;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

@Data
public class SearchResultPO {

    private long id;
    private String name;
    private String description;
    private String type;

    public SearchResultPO(Map<String,Object> map) {
        Objects.requireNonNull(map);
        this.id = ((BigInteger) map.get("id")).longValue();
        this.name = (String) map.get("name");
        this.description = (String) map.get("description");
        this.type = (String) map.get("type");
    }

}
