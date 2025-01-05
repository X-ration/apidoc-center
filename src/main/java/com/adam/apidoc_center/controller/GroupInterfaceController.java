package com.adam.apidoc_center.controller;

import com.adam.apidoc_center.common.Response;
import com.adam.apidoc_center.common.StringConstants;
import com.adam.apidoc_center.dto.GroupInterfaceDTO;
import com.adam.apidoc_center.dto.GroupInterfaceDetailDisplayDTO;
import com.adam.apidoc_center.service.GroupInterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/modify")
    @ResponseBody
    public Response<?> modifyInterface(@PathVariable long interfaceId, @RequestBody GroupInterfaceDTO groupInterfaceDTO) {
        if(groupInterfaceDTO == null) {
            return Response.fail(StringConstants.REQUEST_PARAM_IS_NULL);
        }
        return groupInterfaceService.checkAndModify(interfaceId, groupInterfaceDTO);
    }

    @PostMapping("/delete")
    @ResponseBody
    public Response<Void> deleteInterface(@PathVariable long interfaceId) {
        return groupInterfaceService.deleteInterface(interfaceId);
    }

}
