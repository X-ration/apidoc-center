package com.adam.apidoc_center.controller;

import com.adam.apidoc_center.common.Response;
import com.adam.apidoc_center.common.StringConstants;
import com.adam.apidoc_center.dto.ProjectGroupDTO;
import com.adam.apidoc_center.service.ProjectGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/group")
public class ProjectGroupController {

    @Autowired
    private ProjectGroupService projectGroupService;

}