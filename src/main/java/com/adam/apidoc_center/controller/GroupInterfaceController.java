package com.adam.apidoc_center.controller;

import com.adam.apidoc_center.dto.GroupInterfaceDetailDisplayDTO;
import com.adam.apidoc_center.service.GroupInterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/interface/{interfaceId}")
public class GroupInterfaceController {

    @Autowired
    private GroupInterfaceService groupInterfaceService;

    @GetMapping("/view")
    public String viewInterface(@PathVariable long interfaceId, Model model) {
        GroupInterfaceDetailDisplayDTO groupInterfaceDetailDisplayDTO = groupInterfaceService.getInterfaceDetail(interfaceId);
        if(groupInterfaceDetailDisplayDTO == null) {
            return "redirect:/error/404";
        } else {
            model.addAttribute("interface", groupInterfaceDetailDisplayDTO);
            return "project/viewInterface";
        }
    }

}
