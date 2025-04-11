package com.adam.apidoc_center.controller;

import com.adam.apidoc_center.business.search.SearchType;
import com.adam.apidoc_center.common.PagedData;
import com.adam.apidoc_center.common.Response;
import com.adam.apidoc_center.common.StringConstants;
import com.adam.apidoc_center.dto.SearchResultDTO;
import com.adam.apidoc_center.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @RequestMapping("")
    public String search(@RequestParam(required = false, defaultValue = "") String searchParam,
                         @RequestParam(required = false, defaultValue = "ALL") SearchType searchType,
                         @RequestParam(required = false, defaultValue = "0") int pageNum,
                         @RequestParam(required = false, defaultValue = "10") int pageSize,
                         Model model) {
        model.addAttribute("searchParam", searchParam);
        model.addAttribute("searchType", searchType);
        model.addAttribute("searchTypeDesc", searchType.getDesc());
        if(StringUtils.isBlank(searchParam)) {
            model.addAttribute("error", StringConstants.SEARCH_FAIL_PREFIX + StringConstants.SEARCH_PARAM_BLANK);
        } else {
            Response<PagedData<SearchResultDTO>> response = searchService.searchDB(searchParam, searchType, pageNum, pageSize);
            if(response.isSuccess()) {
                model.addAttribute("data", response.getData());
            } else {
                model.addAttribute("error", StringConstants.SEARCH_FAIL_PREFIX + response.getMessage());
            }
        }
        return "project/search";
    }

}
