package com.adam.apidoc_center.service;

import com.adam.apidoc_center.common.PagedData;
import com.adam.apidoc_center.domain.Project;
import com.adam.apidoc_center.dto.ProjectDTO;
import com.adam.apidoc_center.repository.ProjectAllowedUserRepository;
import com.adam.apidoc_center.repository.ProjectDeploymentRepository;
import com.adam.apidoc_center.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectDeploymentRepository projectDeploymentRepository;
    @Autowired
    private ProjectAllowedUserRepository projectAllowedUserRepository;

    public PagedData<ProjectDTO> getProjectsPaged(int pageNum, int pageSize) {
        Assert.isTrue(pageNum >= 0 && pageSize > 0, "getProjectsPaged param invalid");
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Project> page = projectRepository.findAll(pageRequest);
        PagedData<Project> pagedData = PagedData.convert(page, pageRequest);
        return pagedData.map(ProjectDTO::convert);
    }

}