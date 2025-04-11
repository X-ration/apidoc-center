package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.business.search.SearchType;
import lombok.Data;

@Data
public class SearchSuggestionRequestDTO {

    private String param;
    private SearchType searchType;

}
