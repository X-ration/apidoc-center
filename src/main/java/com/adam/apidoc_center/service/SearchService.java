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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SearchService {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectGroupRepository projectGroupRepository;
    @Autowired
    private GroupInterfaceRepository groupInterfaceRepository;
    @Autowired
    private LuceneService luceneService;

    private final Comparator<SearchResultPO> searchResultPODBComparator = (po1, po2) -> {
        if(po1.getType().equals(po2.getType())) {
            return -1 * Long.compare(po1.getId(), po2.getId());
        } else {
            SearchResultDTO.Type type1 = SearchResultDTO.Type.valueOf(po1.getType()),
                    type2 = SearchResultDTO.Type.valueOf(po2.getType());
            return Integer.compare(type1.ordinal(), type2.ordinal());
        }
    };

    private final Comparator<SearchResultPO> searchResultPOLuceneComparator = (po1, po2) -> {
        if(po1.getType().equals(po2.getType())) {
            return 0;
        } else {
            SearchResultDTO.Type type1 = SearchResultDTO.Type.valueOf(po1.getType()),
                    type2 = SearchResultDTO.Type.valueOf(po2.getType());
            return Integer.compare(type1.ordinal(), type2.ordinal());
        }
    };

    public List<String> searchSuggestion(String param, SearchType searchType) {
        AssertUtil.requireNonNull(param, searchType);
        if(StringUtils.isBlank(param)) {
            return new ArrayList<>();
        } else {
            return luceneService.searchSuggestion(param.toLowerCase(), searchType, SystemConstants.SEARCH_SUGGESTION_MAX_SIZE);
        }
    }

    /**
     * 搜索Lucene索引
     * @param searchParam
     * @param searchType
     * @param pageNum 0-based
     * @param pageSize
     * @return
     */
    public Response<PagedData<SearchResultDTO>> searchLucene(String searchParam, SearchType searchType, int pageNum, int pageSize) {
        AssertUtil.requireNonNull(searchParam, searchType);
        try {
            PagedData<SearchResultPO> searchResultPOPagedData = luceneService.search(searchParam, searchType, pageNum, pageSize);
            searchResultPOPagedData.sort(searchResultPOLuceneComparator);
            PagedData<SearchResultDTO> searchResultDTOPagedData = searchResultPOPagedData.map(SearchResultDTO::mapFrom);
            return Response.success(searchResultDTOPagedData);
        } catch (Exception e) {
            log.error("searchLucene异常", e);
            return Response.fail("搜索失败");
        }
    }

    /**
     * 搜索数据库
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
                searchResultPOPagedData.sort(searchResultPODBComparator);
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
