package com.adam.apidoc_center.service;

import com.adam.apidoc_center.common.Response;
import com.adam.apidoc_center.common.StringConstants;
import com.adam.apidoc_center.domain.GroupInterface;
import com.adam.apidoc_center.domain.InterfaceField;
import com.adam.apidoc_center.domain.InterfaceHeader;
import com.adam.apidoc_center.domain.User;
import com.adam.apidoc_center.dto.*;
import com.adam.apidoc_center.repository.GroupInterfaceRepository;
import com.adam.apidoc_center.repository.InterfaceFieldRepository;
import com.adam.apidoc_center.repository.InterfaceHeaderRepository;
import com.adam.apidoc_center.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GroupInterfaceService {

    @Autowired
    private GroupInterfaceRepository groupInterfaceRepository;
    @Autowired
    private InterfaceHeaderRepository interfaceHeaderRepository;
    @Autowired
    private InterfaceFieldRepository interfaceFieldRepository;
    @Autowired
    private ProjectGroupService projectGroupService;
    @Autowired
    private UserService userService;

    public GroupInterfaceDetailDisplayDTO getInterfaceDetail(long interfaceId) {
        if(interfaceId <= 0) {
            return null;
        }
        Optional<GroupInterface> groupInterfaceOptional = groupInterfaceRepository.findById(interfaceId);
        if(groupInterfaceOptional.isEmpty()) {
            return null;
        }
        GroupInterface groupInterface = groupInterfaceOptional.get();
        GroupInterfaceDetailDisplayDTO groupInterfaceDetailDisplayDTO = new GroupInterfaceDetailDisplayDTO(groupInterface);
        processCreatorAndUpdater(groupInterfaceDetailDisplayDTO);
        return groupInterfaceDetailDisplayDTO;
    }

    private void processCreatorAndUpdater(GroupInterfaceDetailDisplayDTO groupInterfaceDetailDisplayDTO) {
        List<Long> userIdList = new LinkedList<>();
        userIdList.add(groupInterfaceDetailDisplayDTO.getCreateUserId());
        userIdList.add(groupInterfaceDetailDisplayDTO.getUpdateUserId());
        Map<Long, User> userMap = userService.queryUserMap(userIdList);
        if(userMap.containsKey(groupInterfaceDetailDisplayDTO.getCreateUserId())) {
            User user = userMap.get(groupInterfaceDetailDisplayDTO.getCreateUserId());
            UserCoreDTO userCoreDTO = new UserCoreDTO(user);
            groupInterfaceDetailDisplayDTO.setCreator(userCoreDTO);
        }
        if(userMap.containsKey(groupInterfaceDetailDisplayDTO.getUpdateUserId())) {
            User user = userMap.get(groupInterfaceDetailDisplayDTO.getUpdateUserId());
            UserCoreDTO userCoreDTO = new UserCoreDTO(user);
            groupInterfaceDetailDisplayDTO.setUpdater(userCoreDTO);
        }
    }

    public Response<Void> deleteInterface(long interfaceId) {
        if(interfaceId <= 0) {
            return Response.fail(StringConstants.GROUP_INTERFACE_ID_INVALID);
        }
        Optional<GroupInterface> groupInterfaceOptional = groupInterfaceRepository.findById(interfaceId);
        if(groupInterfaceOptional.isEmpty()) {
            return Response.fail(StringConstants.GROUP_INTERFACE_ID_INVALID);
        }
        GroupInterface groupInterface = groupInterfaceOptional.get();
        try {
            groupInterfaceRepository.delete(groupInterface);
            return Response.success();
        } catch (Exception e) {
            log.error("deleteInterface error", e);
            return Response.fail(StringConstants.GROUP_INTERFACE_DELETE_FAIL);
        }
    }

    @Transactional
    public Response<?> checkAndModify(long interfaceId, GroupInterfaceDTO groupInterfaceDTO) {
        if(interfaceId <= 0) {
            return Response.fail(StringConstants.GROUP_INTERFACE_ID_INVALID);
        }
        Optional<GroupInterface> groupInterfaceOptional = groupInterfaceRepository.findById(interfaceId);
        if(groupInterfaceOptional.isEmpty()) {
            return Response.fail(StringConstants.GROUP_INTERFACE_ID_INVALID);
        }
        GroupInterfaceErrorMsg groupInterfaceErrorMsg = checkCreateParams(groupInterfaceDTO);
        if(groupInterfaceErrorMsg.hasError()) {
            return Response.fail(StringConstants.GROUP_INTERFACE_MODIFY_FAIL_CHECK_INPUT, groupInterfaceErrorMsg);
        }
        GroupInterface groupInterface = groupInterfaceOptional.get();
        groupInterface.setName(groupInterfaceDTO.getName());
        if(StringUtils.isNotEmpty(groupInterfaceDTO.getDescription())) {
            groupInterface.setDescription(groupInterface.getDescription());
        }
        groupInterface.setRelativePath(groupInterfaceDTO.getRelativePath());
        groupInterface.setMethod(groupInterfaceDTO.getMethod());
        groupInterface.setType(groupInterfaceDTO.getType());
        //修改接口
        if(!CollectionUtils.isEmpty(groupInterface.getInterfaceHeaderList())) {
            interfaceHeaderRepository.deleteAll(groupInterface.getInterfaceHeaderList());
            groupInterface.setInterfaceHeaderList(null);
        }
        if(!CollectionUtils.isEmpty(groupInterface.getInterfaceFieldList())) {
            interfaceFieldRepository.deleteAll(groupInterface.getInterfaceFieldList());
            groupInterface.setInterfaceFieldList(null);
        }
        if(!CollectionUtils.isEmpty(groupInterfaceDTO.getHeaderList())) {
            groupInterface.setInterfaceHeaderList(groupInterfaceDTO.getHeaderList().stream()
                    .map(interfaceHeaderDTO -> new InterfaceHeader(groupInterface.getId(), interfaceHeaderDTO))
                    .collect(Collectors.toList())
            );
        }
        if(!CollectionUtils.isEmpty(groupInterfaceDTO.getFieldList())) {
            groupInterface.setInterfaceFieldList(groupInterfaceDTO.getFieldList().stream()
                    .map(interfaceFieldDTO -> new InterfaceField(groupInterface.getId(), interfaceFieldDTO))
                    .collect(Collectors.toList())
            );
        }
        groupInterfaceRepository.save(groupInterface);
        return Response.success();
    }

    @Transactional
    public Response<?> checkAndCreate(long groupId, GroupInterfaceDTO groupInterfaceDTO) {
        if(groupId <= 0) {
            return Response.fail(StringConstants.PROJECT_GROUP_ID_INVALID);
        }
        if(!projectGroupService.exists(groupId)) {
            return Response.fail(StringConstants.PROJECT_GROUP_ID_INVALID);
        }
        GroupInterfaceErrorMsg groupInterfaceErrorMsg = checkCreateParams(groupInterfaceDTO);
        if(groupInterfaceErrorMsg.hasError()) {
            return Response.fail(StringConstants.GROUP_INTERFACE_CREATE_FAIL_CHECK_INPUT, groupInterfaceErrorMsg);
        }
        //创建接口
        GroupInterface groupInterface = new GroupInterface();
        groupInterface.setName(groupInterfaceDTO.getName());
        groupInterface.setGroupId(groupId);
        groupInterface.setDescription(groupInterfaceDTO.getDescription());
        groupInterface.setRelativePath(groupInterfaceDTO.getRelativePath());
        groupInterface.setMethod(groupInterfaceDTO.getMethod());
        groupInterface.setType(groupInterfaceDTO.getType());
        groupInterfaceRepository.save(groupInterface);
        if(!CollectionUtils.isEmpty(groupInterfaceDTO.getHeaderList())) {
            List<InterfaceHeader> interfaceHeaderList = groupInterfaceDTO.getHeaderList().stream()
                    .map(interfaceHeaderDTO -> {
                        InterfaceHeader interfaceHeader = new InterfaceHeader();
                        interfaceHeader.setInterfaceId(groupInterface.getId());
                        interfaceHeader.setName(interfaceHeaderDTO.getName());
                        interfaceHeader.setDescription(interfaceHeaderDTO.getDescription());
                        interfaceHeader.setRequired(interfaceHeaderDTO.getRequired());
                        return interfaceHeader;
                    })
                    .collect(Collectors.toList());
            interfaceHeaderRepository.saveAll(interfaceHeaderList);
        }
        if(!CollectionUtils.isEmpty(groupInterfaceDTO.getFieldList())) {
            List<InterfaceField> interfaceFieldList = groupInterfaceDTO.getFieldList().stream()
                    .map(interfaceFieldDTO -> {
                        InterfaceField interfaceField = new InterfaceField();
                        interfaceField.setInterfaceId(groupInterface.getId());
                        interfaceField.setName(interfaceFieldDTO.getName());
                        interfaceField.setType(interfaceFieldDTO.getType());
                        interfaceField.setDescription(interfaceFieldDTO.getDescription());
                        interfaceField.setRequired(interfaceFieldDTO.getRequired());
                        return interfaceField;
                    })
                    .collect(Collectors.toList());
            interfaceFieldRepository.saveAll(interfaceFieldList);
        }
        return Response.success();
    }

    private GroupInterfaceErrorMsg checkCreateParams(GroupInterfaceDTO groupInterfaceDTO) {
        GroupInterfaceErrorMsg errorMsg = new GroupInterfaceErrorMsg();
        if(StringUtils.isBlank(groupInterfaceDTO.getName())) {
            errorMsg.setName(StringConstants.GROUP_INTERFACE_NAME_BLANK);
        } else if(groupInterfaceDTO.getName().length() > 32) {
            errorMsg.setName(StringConstants.GROUP_INTERFACE_NAME_LENGTH_EXCEEDED);
        }
        if(StringUtils.isNotEmpty(groupInterfaceDTO.getDescription())) {
            if(groupInterfaceDTO.getDescription().length() > 100) {
                errorMsg.setDescription(StringConstants.GROUP_INTERFACE_DESCRIPTION_LENGTH_EXCEEDED);
            }
        }
        if(StringUtils.isBlank(groupInterfaceDTO.getRelativePath())) {
            errorMsg.setRelativePath(StringConstants.GROUP_INTERFACE_RELATIVE_PATH_BLANK);
        } else if(groupInterfaceDTO.getRelativePath().length() > 256) {
            errorMsg.setRelativePath(StringConstants.GROUP_INTERFACE_RELATIVE_PATH_LENGTH_EXCEEDED);
        } else if(!StringUtil.isRelativePath(groupInterfaceDTO.getRelativePath())) {
            errorMsg.setRelativePath(StringConstants.GROUP_INTERFACE_RELATIVE_PATH_INVALID);
        }
        if(groupInterfaceDTO.getMethod() == null) {
            errorMsg.setMethod(StringConstants.GROUP_INTERFACE_METHOD_NULL);
        }
        if(groupInterfaceDTO.getType() == null) {
            errorMsg.setType(StringConstants.GROUP_INTERFACE_TYPE_NULL);
        }
        if(!CollectionUtils.isEmpty(groupInterfaceDTO.getHeaderList())) {
            errorMsg.setHeaderList(groupInterfaceDTO.getHeaderList().stream()
                    .map(interfaceHeaderDTO -> {
                        GroupInterfaceErrorMsg.InterfaceHeaderErrorMsg interfaceHeaderErrorMsg =
                                new GroupInterfaceErrorMsg.InterfaceHeaderErrorMsg();
                        if(StringUtils.isBlank(interfaceHeaderDTO.getName())) {
                            interfaceHeaderErrorMsg.setName(StringConstants.INTERFACE_HEADER_NAME_BLANK);
                        } else if(interfaceHeaderDTO.getName().length() > 32) {
                            interfaceHeaderErrorMsg.setName(StringConstants.INTERFACE_HEADER_NAME_LENGTH_EXCEEDED);
                        }
                        if(StringUtils.isNotEmpty(interfaceHeaderDTO.getDescription())) {
                            if(interfaceHeaderDTO.getDescription().length() > 100) {
                                interfaceHeaderErrorMsg.setDescription(StringConstants.INTERFACE_HEADER_DESCRIPTION_LENGTH_EXCEEDED);
                            }
                        }
                        if(interfaceHeaderDTO.getRequired() == null) {
                            interfaceHeaderErrorMsg.setRequired(StringConstants.INTERFACE_HEADER_REQUIRED_NULL);
                        }
                        return interfaceHeaderErrorMsg;
                    })
                    .collect(Collectors.toList())
            );
        }
        if(!CollectionUtils.isEmpty(groupInterfaceDTO.getFieldList())) {
            errorMsg.setFieldList(groupInterfaceDTO.getFieldList().stream()
                    .map(interfaceFieldDTO -> {
                        GroupInterfaceErrorMsg.InterfaceFieldErrorMsg interfaceFieldErrorMsg =
                                new GroupInterfaceErrorMsg.InterfaceFieldErrorMsg();
                        if(StringUtils.isBlank(interfaceFieldDTO.getName())) {
                            interfaceFieldErrorMsg.setName(StringConstants.INTERFACE_FIELD_NAME_BLANK);
                        } else if(interfaceFieldDTO.getName().length() > 32) {
                            interfaceFieldErrorMsg.setName(StringConstants.INTERFACE_FIELD_NAME_LENGTH_EXCEEDED);
                        }
                        if(interfaceFieldDTO.getType() == null) {
                            interfaceFieldErrorMsg.setType(StringConstants.INTERFACE_FIELD_TYPE_NULL);
                        }
                        if(StringUtils.isNotEmpty(interfaceFieldDTO.getDescription())) {
                            if(interfaceFieldDTO.getDescription().length() > 100) {
                                interfaceFieldErrorMsg.setDescription(StringConstants.INTERFACE_FIELD_DESCRIPTION_LENGTH_EXCEEDED);
                            }
                        }
                        if(interfaceFieldDTO.getRequired() == null) {
                            interfaceFieldErrorMsg.setRequired(StringConstants.INTERFACE_FIELD_REQUIRED_NULL);
                        }
                        return interfaceFieldErrorMsg;
                    })
                    .collect(Collectors.toList())
            );
        }
        return errorMsg;
    }

}