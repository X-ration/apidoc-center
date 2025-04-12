package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.business.search.SearchResultPO;
import com.adam.apidoc_center.domain.GroupInterface;
import com.adam.apidoc_center.domain.Project;
import com.adam.apidoc_center.domain.ProjectGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResultDTO {

    private Type type;
    private String name;
    private String description;
    private String link;

    public enum Type {
        PROJECT,GROUP,INTERFACE
    }

    public static SearchResultDTO mapFrom(SearchResultPO searchResultPO) {
        SearchResultDTO.Type type = SearchResultDTO.Type.valueOf(searchResultPO.getType());
        Objects.requireNonNull(type);
        String link = link(type, searchResultPO.getId());
        return new SearchResultDTO(type, searchResultPO.getName(), searchResultPO.getDescription(), link);
    }

    private static String link(Type type, long id) {
        String link = null;
        switch (type) {
            case PROJECT:
                link = "/project/" + id + "/view";
                break;
            case GROUP:
                link = "/group/" + id + "/view";
                break;
            case INTERFACE:
                link = "/interface/" + id + "/view";
                break;
        }
        return link;
    }

    public static SearchResultDTO mapFrom(Project project) {
        String link = "/project/" + project.getId() + "/view";
        return new SearchResultDTO(Type.PROJECT, project.getName(), project.getDescription(), link);
    }

    public static SearchResultDTO mapFrom(ProjectGroup group) {
        String link = "/group/" + group.getId() + "/view";
        return new SearchResultDTO(Type.GROUP, group.getName(), "", link);
    }

    public static SearchResultDTO mapFrom(GroupInterface groupInterface) {
        String link = "/interface/" + groupInterface.getId() + "/view";
        return new SearchResultDTO(Type.INTERFACE, groupInterface.getName(), groupInterface.getDescription(), link);
    }

}