package com.adam.apidoc_center.controller;

import com.adam.apidoc_center.common.NameValuePair;
import com.adam.apidoc_center.common.Response;
import com.adam.apidoc_center.common.StringConstants;
import com.adam.apidoc_center.dto.CallInterfaceRequestDTO;
import com.adam.apidoc_center.dto.CallInterfaceResponseDTO;
import com.adam.apidoc_center.dto.GroupInterfaceDTO;
import com.adam.apidoc_center.dto.GroupInterfaceDetailDisplayDTO;
import com.adam.apidoc_center.service.GroupInterfaceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/interface/{interfaceId}")
@Slf4j
public class GroupInterfaceController {

    @Autowired
    private GroupInterfaceService groupInterfaceService;
    @Autowired
    private ObjectMapper objectMapper;

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

    @PostMapping("/call")
    @ResponseBody
    public Response<CallInterfaceResponseDTO> callInterface(@PathVariable long interfaceId, @RequestBody CallInterfaceRequestDTO requestDTO,
                                                            HttpServletResponse httpServletResponse
    ) {
        log.debug("callInterface interfaceId={} requestDTO={}", interfaceId, requestDTO);
        requestDTO.setInterfaceId(interfaceId);
        return groupInterfaceService.callInterface(requestDTO, httpServletResponse);
    }

    @PostMapping(value = "/callForm", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public Response<CallInterfaceResponseDTO> callFormInterface(@PathVariable long interfaceId, @RequestParam Map<String,String> paramMap,
                                     @RequestParam Map<String, MultipartFile> fileMap, HttpServletResponse httpServletResponse) {
        log.debug("callFormInterface interfaceId={} paramMap={} fileMap={}", interfaceId, paramMap, fileMap);
        CallInterfaceRequestDTO requestDTO = convertRequestDTO(interfaceId, paramMap, fileMap);
        if(requestDTO == null) {
            return Response.fail("参数不完整或格式不正确");
        } else {
            return groupInterfaceService.callInterface(requestDTO, httpServletResponse);
        }
    }

    private CallInterfaceRequestDTO convertRequestDTO(long interfaceId, Map<String,String> paramMap, Map<String,MultipartFile> fileMap) {
        CallInterfaceRequestDTO requestDTO = new CallInterfaceRequestDTO();
        requestDTO.setInterfaceId(interfaceId);
        List<NameValuePair<String,Object>> fieldList = new LinkedList<>();
        for(Map.Entry<String,String> entry:paramMap.entrySet()) {
            if(entry.getKey().equals("deploymentId")) {
                String deploymentIdString = entry.getValue();
                if (deploymentIdString == null) {
                    return null;
                } else {
                    try {
                        long deploymentId = Long.parseLong(deploymentIdString);
                        requestDTO.setDeploymentId(deploymentId);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            } else if(entry.getKey().equals("callStack")) {
                String callStackString = entry.getValue();
                if (callStackString == null) {
                    return null;
                } else {
                    try {
                        CallInterfaceRequestDTO.CallStack callStack = CallInterfaceRequestDTO.CallStack.valueOf(callStackString);
                        requestDTO.setCallStack(callStack);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                }
            } else if(entry.getKey().equals("headerList")) {
                String headerListJson = entry.getValue();
                if (headerListJson == null) {
                    return null;
                } else {
                    try {
                        List<NameValuePair<String, String>> headerList = objectMapper.readValue(headerListJson, new TypeReference<>() {
                        });
                        requestDTO.setHeaderList(headerList);
                    } catch (JsonProcessingException e) {
                        return null;
                    }
                }
            } else {
                NameValuePair<String,Object> nameValuePair = new NameValuePair<>();
                nameValuePair.setName(entry.getKey());
                nameValuePair.setValue(entry.getValue());
                fieldList.add(nameValuePair);
            }
        }
        for(Map.Entry<String,MultipartFile> entry: fileMap.entrySet()) {
            NameValuePair<String,Object> nameValuePair = new NameValuePair<>();
            nameValuePair.setName(entry.getKey());
            nameValuePair.setValue(entry.getValue());
            fieldList.add(nameValuePair);
        }
        requestDTO.setFieldList(fieldList);
        return requestDTO;
    }

}
