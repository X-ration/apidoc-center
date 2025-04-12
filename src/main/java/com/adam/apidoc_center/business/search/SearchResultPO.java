package com.adam.apidoc_center.business.search;

import com.adam.apidoc_center.common.StringConstants;
import lombok.Data;
import org.apache.lucene.document.Document;

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

    public SearchResultPO(Document document) {
        Objects.requireNonNull(document);
        this.id = Long.parseLong(document.get(StringConstants.SEARCH_FIELD_ID));
        this.name = document.get(StringConstants.SEARCH_FIELD_NAME);
        this.description = document.get(StringConstants.SEARCH_FIELD_DESCRIPTION);
        this.type = document.get(StringConstants.SEARCH_FIELD_CLASS).toUpperCase();
    }

}
