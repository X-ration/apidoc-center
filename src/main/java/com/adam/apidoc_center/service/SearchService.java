package com.adam.apidoc_center.service;

import com.adam.apidoc_center.business.search.SearchResultPO;
import com.adam.apidoc_center.business.search.SearchType;
import com.adam.apidoc_center.common.PagedData;
import com.adam.apidoc_center.common.Response;
import com.adam.apidoc_center.common.SystemConstants;
import com.adam.apidoc_center.domain.GroupInterface;
import com.adam.apidoc_center.domain.Project;
import com.adam.apidoc_center.domain.ProjectGroup;
import com.adam.apidoc_center.dto.SearchResultDTO;
import com.adam.apidoc_center.repository.GroupInterfaceRepository;
import com.adam.apidoc_center.repository.ProjectGroupRepository;
import com.adam.apidoc_center.repository.ProjectRepository;
import com.adam.apidoc_center.util.AssertUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectGroupRepository projectGroupRepository;
    @Autowired
    private GroupInterfaceRepository groupInterfaceRepository;
    @Autowired
    private LuceneService luceneService;

    public List<String> searchSuggestion(String param, SearchType searchType) {
        AssertUtil.requireNonNull(param, searchType);
        if(StringUtils.isBlank(param)) {
            return new ArrayList<>();
        } else {
            return luceneService.searchSuggestion(param.toLowerCase(), searchType, SystemConstants.SEARCH_SUGGESTION_MAX_SIZE);
        }
    }

    /**
     * 搜索方法
     * @param searchParam
     * @param searchType
     * @param pageNum 0-based
     * @param pageSize
     * @return
     */
    public Response<PagedData<SearchResultDTO>> searchDB(String searchParam, SearchType searchType, int pageNum, int pageSize) {
        AssertUtil.requireNonNull(searchParam, searchType);
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        PagedData<SearchResultDTO> searchResultDTOPagedData = null;
        searchParam = '%' + searchParam + '%';
        switch (searchType) {
            case ALL:
                Page<Map<String,Object>> searchResultPOPage = projectRepository.findAllSearchResult(searchParam, PageRequest.of(pageNum, pageSize));
                PagedData<SearchResultPO> searchResultPOPagedData = PagedData.convert(searchResultPOPage, pageRequest)
                        .map(SearchResultPO::new);
                searchResultPOPagedData.sort((po1, po2) -> {
                    if(po1.getType().equals(po2.getType())) {
                        return -1 * Long.compare(po1.getId(), po2.getId());
                    } else {
                        SearchResultDTO.Type type1 = SearchResultDTO.Type.valueOf(po1.getType()),
                                type2 = SearchResultDTO.Type.valueOf(po2.getType());
                        return Integer.compare(type1.ordinal(), type2.ordinal());
                    }
                });
                searchResultDTOPagedData = searchResultPOPagedData.map(SearchResultDTO::mapFrom);
                return Response.success(searchResultDTOPagedData);
            case PROJECT:
                Page<Project> projectPage = projectRepository.findProjectsByNameLikeOrDescriptionLike(searchParam, searchParam, pageRequest);
                PagedData<Project> projectPagedData = PagedData.convert(projectPage, pageRequest);
                searchResultDTOPagedData = projectPagedData.map(SearchResultDTO::mapFrom);
                return Response.success(searchResultDTOPagedData);
            case GROUP:
                Page<ProjectGroup> projectGroupPage = projectGroupRepository.findProjectGroupsByNameLike(searchParam, pageRequest);
                PagedData<ProjectGroup> projectGroupPagedData = PagedData.convert(projectGroupPage, pageRequest);
                searchResultDTOPagedData = projectGroupPagedData.map(SearchResultDTO::mapFrom);
                return Response.success(searchResultDTOPagedData);
            case INTERFACE:
                Page<GroupInterface> groupInterfacePage = groupInterfaceRepository.findGroupInterfacesByNameLikeOrDescriptionLike(searchParam, searchParam, pageRequest);
                PagedData<GroupInterface> groupInterfacePagedData = PagedData.convert(groupInterfacePage, pageRequest);
                searchResultDTOPagedData = groupInterfacePagedData.map(SearchResultDTO::mapFrom);
                return Response.success(searchResultDTOPagedData);
        }
        return Response.fail("Invalid state");
    }

}
