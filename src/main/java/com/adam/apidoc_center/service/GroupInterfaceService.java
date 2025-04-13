package com.adam.apidoc_center.service;

import com.adam.apidoc_center.common.NameValuePair;
import com.adam.apidoc_center.common.Response;
import com.adam.apidoc_center.common.StringConstants;
import com.adam.apidoc_center.common.SystemConstants;
import com.adam.apidoc_center.config.WebConfig;
import com.adam.apidoc_center.domain.*;
import com.adam.apidoc_center.dto.*;
import com.adam.apidoc_center.repository.GroupInterfaceRepository;
import com.adam.apidoc_center.repository.InterfaceFieldRepository;
import com.adam.apidoc_center.repository.InterfaceHeaderRepository;
import com.adam.apidoc_center.util.AssertUtil;
import com.adam.apidoc_center.util.LocalDateTimeUtil;
import com.adam.apidoc_center.util.StringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
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
    @Autowired
    private ProjectService projectService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private OkHttpClient okHttpClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private LuceneService luceneService;

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

    public Long getProjectId(long interfaceId) {
        Assert.isTrue(interfaceId > 0, "getProjectId interfaceId<=0");
        Optional<GroupInterface> groupInterfaceOptional = groupInterfaceRepository.findById(interfaceId);
        if(groupInterfaceOptional.isEmpty()) {
            return null;
        }
        GroupInterface groupInterface = groupInterfaceOptional.get();
        return groupInterface.getProjectGroup().getProjectId();
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
            luceneService.deleteInterface(groupInterface);
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
        groupInterface.setResponseType(groupInterfaceDTO.getResponseType());
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
        luceneService.updateInterface(groupInterface);
        return Response.success();
    }

    @Transactional
    public Response<?> checkAndCreate(long groupId, GroupInterfaceDTO groupInterfaceDTO) {
        if(groupId <= 0) {
            return Response.fail(StringConstants.PROJECT_GROUP_ID_INVALID);
        }
        ProjectGroup projectGroup = projectGroupService.findById(groupId);
        if(projectGroup == null) {
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
        groupInterface.setResponseType(groupInterfaceDTO.getResponseType());
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
        groupInterface.setProjectGroup(projectGroup);
        luceneService.createInterface(groupInterface);
        return Response.success();
    }

    public Response<CallInterfaceResponseDTO> callInterface(CallInterfaceRequestDTO requestDTO, HttpServletResponse httpServletResponse) {
        Objects.requireNonNull(requestDTO);
        //check interface id
        long interfaceId = requestDTO.getInterfaceId();
        if(interfaceId < 0) {
            return Response.fail(callInterfaceErrorMsg(StringConstants.GROUP_INTERFACE_ID_INVALID));
        }
        Optional<GroupInterface> groupInterfaceOptional = groupInterfaceRepository.findById(interfaceId);
        if(groupInterfaceOptional.isEmpty()) {
            return Response.fail(callInterfaceErrorMsg(StringConstants.GROUP_INTERFACE_ID_INVALID));
        }
        GroupInterface groupInterface = groupInterfaceOptional.get();
        GroupInterface.Type interfaceType = groupInterface.getType();
        //check required header
        if(groupInterface.getInterfaceHeaderList() != null) {
            Map<String,String> headerMap;
            if(CollectionUtils.isEmpty(requestDTO.getHeaderList())) {
                headerMap = new HashMap<>();
            } else {
                headerMap = requestDTO.getHeaderList().stream()
                        .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
            }
            List<String> requiredHeaderNameList = groupInterface.getInterfaceHeaderList().stream()
                    .filter(InterfaceHeader::isRequired)
                    .map(InterfaceHeader::getName)
                    .collect(Collectors.toList());
            for(String requiredHeaderName: requiredHeaderNameList) {
                String value = headerMap.get(requiredHeaderName);
                if(value == null) {
                    String errorMsg = StringConstants.GROUP_INTERFACE_CALL_HEADER_MISSING.replace("{}", requiredHeaderName);
                    return Response.fail(callInterfaceErrorMsg(errorMsg));
                }
            }
        }
        //check required field and field type
        if(groupInterface.getInterfaceFieldList() != null) {
            Map<String,Object> fieldMap;
            if(CollectionUtils.isEmpty(requestDTO.getFieldList())) {
                fieldMap = new HashMap<>();
            } else {
                fieldMap = requestDTO.getFieldList().stream()
                        .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
            }
            Map<String, InterfaceField.Type> requiredFieldNameTypeMap = groupInterface.getInterfaceFieldList().stream()
                    .filter(InterfaceField::isRequired)
                    .collect(Collectors.toMap(InterfaceField::getName, InterfaceField::getType));
            for(Map.Entry<String, InterfaceField.Type> entry: requiredFieldNameTypeMap.entrySet()) {
                String fieldName = entry.getKey();
                InterfaceField.Type fieldType = entry.getValue();
                Object value = fieldMap.get(fieldName);
                if(value == null) {
                    String errorMsg = StringConstants.GROUP_INTERFACE_CALL_FIELD_MISSING.replace("{}", fieldName);
                    return Response.fail(callInterfaceErrorMsg(errorMsg));
                } else if(fieldType == InterfaceField.Type.TEXT && !(value instanceof String)) {
                    String errorMsg = StringConstants.GROUP_INTERFACE_CALL_TEXT_PARAM_INVALID.replace("{}", fieldName);
                    return Response.fail(callInterfaceErrorMsg(errorMsg));
                } else if(fieldType == InterfaceField.Type.FILE && !(value instanceof MultipartFile)) {
                    String errorMsg = StringConstants.GROUP_INTERFACE_CALL_FILE_PARAM_INVALID.replace("{}", fieldName);
                    return Response.fail(callInterfaceErrorMsg(errorMsg));
                }
            }
            if(interfaceType != GroupInterface.Type.FORM_DATA) {
                for (NameValuePair<String, Object> nameValuePair : requestDTO.getFieldList()) {
                    if(nameValuePair.getValue() instanceof MultipartFile) {
                        return Response.fail(callInterfaceErrorMsg(StringConstants.GROUP_INTERFACE_CALL_FILE_PARAM_NOT_ALLOWED));
                    }
                }
            }
        }
        //check project deployment id
        long projectDeploymentId = requestDTO.getDeploymentId();;
        if(projectDeploymentId <= 0) {
            return Response.fail(callInterfaceErrorMsg(StringConstants.PROJECT_DEPLOYMENT_ID_INVALID));
        }
        ProjectDeployment projectDeployment = projectService.findDeploymentById(projectDeploymentId);
        if(projectDeployment == null) {
            return Response.fail(callInterfaceErrorMsg(StringConstants.PROJECT_DEPLOYMENT_ID_INVALID));
        }

        //准备调用
        String relativePath = groupInterface.getRelativePath();
        GroupInterface.ResponseType responseType = groupInterface.getResponseType();
        HttpMethod httpMethod = groupInterface.getMethod();
        String projectPath = projectDeployment.getDeploymentUrl();
        while(projectPath.length() > 0 && projectPath.charAt(projectPath.length() - 1) == '/') {
            projectPath = projectPath.substring(0, projectPath.length() - 1);
        }
        String callUrl = projectPath + relativePath;
        CallInterfaceRequestDTO.CallStack callStack = requestDTO.getCallStack();
        List<NameValuePair<String,String>> headerList = requestDTO.getHeaderList();
        if(headerList == null) {
            headerList = new LinkedList<>();
        }
        List<NameValuePair<String,Object>> fieldList = requestDTO.getFieldList();
        if(fieldList == null) {
            fieldList = new LinkedList<>();
        }

        //调用
        return callInterface(callUrl, httpMethod, interfaceType, responseType, callStack, headerList, fieldList, httpServletResponse);
    }

    private Response<CallInterfaceResponseDTO> callInterface(String callUrl, HttpMethod httpMethod, GroupInterface.Type interfaceType,
                                                   GroupInterface.ResponseType responseType,
                                                   CallInterfaceRequestDTO.CallStack callStack,
                                                   List<NameValuePair<String,String>> headerList,
                                                   List<NameValuePair<String,Object>> fieldList,
                                                             HttpServletResponse httpServletResponse
    ) {
        AssertUtil.requireNonNull(callUrl, httpMethod, interfaceType, callStack, headerList, fieldList);
        String bodyJson = null;
        if(interfaceType == GroupInterface.Type.FORM_URLENCODED) {
            StringBuilder stringBuilder = new StringBuilder(callUrl);
            stringBuilder.append("?");
            for(NameValuePair<String,Object> nameValuePair: fieldList) {
                String name = nameValuePair.getName(),
                        value = (String) nameValuePair.getValue();
                String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8),
                        encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);
                stringBuilder.append(encodedName).append("=").append(encodedValue).append("&");
            }
            if(fieldList.size() > 0) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
            String originalCallUrl = callUrl;
            callUrl = stringBuilder.toString();
            log.debug("form-urlencoded callUrl updated from {} to {}", originalCallUrl, callUrl);
        } else if(interfaceType == GroupInterface.Type.JSON) {
            Map<String,String> map = new HashMap<>();
            for(NameValuePair<String,Object> nameValuePair: fieldList) {
                String name = nameValuePair.getName(),
                        value = (String) nameValuePair.getValue();
                map.put(name, value);
            }
            try {
                bodyJson = objectMapper.writeValueAsString(map);
            } catch (JsonProcessingException e) {
                log.error("callInterface JSON stringify error", e);
                return wrapException(e);
            }
        }

        switch (callStack) {
            case RestTemplate:
                return callInterfaceByRestTemplate(callUrl, httpMethod, interfaceType, responseType, headerList, fieldList, bodyJson, httpServletResponse);
            case OkHttp:
                return callInterfaceByOkHttp(callUrl, httpMethod, interfaceType, responseType, headerList, fieldList, bodyJson, httpServletResponse);
            default:
                return null;
        }
    }

    private Response<CallInterfaceResponseDTO> callInterfaceByRestTemplate(String callUrl, HttpMethod httpMethod, GroupInterface.Type interfaceType,
                                                                 GroupInterface.ResponseType responseType,
                                                                 List<NameValuePair<String,String>> headerList,
                                                                 List<NameValuePair<String,Object>> fieldList,
                                                                 String bodyJson, HttpServletResponse httpServletResponse) {
        HttpHeaders httpHeaders = new HttpHeaders();
        for(NameValuePair<String,String> nameValuePair: headerList) {
            httpHeaders.add(nameValuePair.getName(), nameValuePair.getValue());
        }
        HttpEntity<?> httpEntity = null;
        switch (interfaceType) {
            case FORM_URLENCODED:
            case NO_BODY:
                httpEntity = new HttpEntity<>(null, httpHeaders);
                break;
            case FORM_DATA:
                httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
                MultiValueMap<String,Object> formData = new LinkedMultiValueMap<>();
                for(NameValuePair<String,Object> nameValuePair: fieldList) {
                    if(!(nameValuePair.getValue() instanceof MultipartFile)) {
                        formData.add(nameValuePair.getName(), nameValuePair.getValue());
                    } else {
                        String fieldName = nameValuePair.getName();
                        MultipartFile multipartFile = (MultipartFile) nameValuePair.getValue();
                        try {
                            ByteArrayInputStream in = new ByteArrayInputStream(multipartFile.getBytes());
                            InputStreamResource resource = new InputStreamResource(in) {
                                @Override
                                public long contentLength() throws IOException {
                                    return in.available();
                                }
                                @Override
                                public String getFilename() {
                                    return multipartFile.getOriginalFilename();
                                }
                            };
                            formData.add(fieldName, resource);
                        } catch (IOException e) {
                            log.error("callInterfaceByRestTemplate multipart-form file {} exception", fieldName, e);
                            return wrapException(e);
                        }
                    }
                }
                httpEntity = new HttpEntity<>(formData, httpHeaders);
                break;
            case JSON:
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                httpEntity = new HttpEntity<>(bodyJson, httpHeaders);
                break;
        }
        try {
            if(responseType == GroupInterface.ResponseType.TEXT) {
                ResponseEntity<String> responseEntity = restTemplate.exchange(callUrl, httpMethod, httpEntity, String.class);
                String responseBody = responseEntity.getBody();
                String time = LocalDateTimeUtil.friendlyNowDateTime();
                HttpStatus status = responseEntity.getStatusCode();
                if(status.is2xxSuccessful()) {
                    String contentType = StringConstants.GROUP_INTERFACE_CALL_DEFAULT_CONTENT_TYPE;
                    if (responseEntity.getHeaders().getContentType() != null) {
                        MediaType mediaType = responseEntity.getHeaders().getContentType();
                        contentType = mediaType.getType() + "/" + mediaType.getSubtype();
                    }
                    CallInterfaceResponseDTO responseDTO = new CallInterfaceResponseDTO();
                    responseDTO.setTime(time);
                    responseDTO.setStatus(status.value());
                    responseDTO.setContentType(contentType);
                    responseDTO.setBody(responseBody);
                    return Response.success(responseDTO);
                } else {
                    throw new RuntimeException("调用接口异常，状态码：" + status.value());
                }
            } else if(responseType == GroupInterface.ResponseType.FILE) {
                ResponseEntity<byte[]> responseEntity = restTemplate.exchange(callUrl, httpMethod, httpEntity, byte[].class);
                HttpStatus status = responseEntity.getStatusCode();
                if(status.is2xxSuccessful()) {
                    byte[] bodyBytes = responseEntity.getBody();
                    ContentDisposition contentDisposition = responseEntity.getHeaders().getContentDisposition();
                    httpServletResponse.setHeader("Content-Disposition", contentDisposition.toString());
                    httpServletResponse.setHeader("Content-Type", "application/octet-stream");
                    httpServletResponse.setHeader("ApiDocCenter-FileName", contentDisposition.getFilename());
                    StreamUtils.copy(bodyBytes != null ? bodyBytes : new byte[0], httpServletResponse.getOutputStream());
                    return null;
                } else {
                    throw new RuntimeException("调用接口异常，状态码：" + status.value());
                }
            } else {
                throw new RuntimeException("Invalid state");
            }
        } catch (Exception e) {
            log.error("callInterface RestTemplate exchange exception", e);
            return wrapException(e);
        }
    }

    private Response<CallInterfaceResponseDTO> callInterfaceByOkHttp(String callUrl, HttpMethod httpMethod, GroupInterface.Type interfaceType,
                                                                           GroupInterface.ResponseType responseType,
                                                                           List<NameValuePair<String,String>> headerList,
                                                                           List<NameValuePair<String,Object>> fieldList,
                                                                           String bodyJson, HttpServletResponse httpServletResponse) {
        Request.Builder requestBuilder = new Request.Builder()
                .url(callUrl);
        for(NameValuePair<String,String> nameValuePair: headerList) {
            requestBuilder.addHeader(nameValuePair.getName(), nameValuePair.getValue());
        }
        RequestBody requestBody = null;
        switch (interfaceType) {
            case FORM_URLENCODED:
            case NO_BODY:
                break;
            case FORM_DATA:
                MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder();
                for(NameValuePair<String,Object> nameValuePair: fieldList) {
                    if (!(nameValuePair.getValue() instanceof MultipartFile)) {
                        requestBodyBuilder.addFormDataPart(nameValuePair.getName(), nameValuePair.getValue().toString());
                    } else {
                        String fieldName = nameValuePair.getName();
                        MultipartFile multipartFile = (MultipartFile) nameValuePair.getValue();
                        try {
                            byte[] multipartFileBytes = multipartFile.getBytes();
                            requestBodyBuilder.addFormDataPart(fieldName, multipartFile.getOriginalFilename(),
                                    RequestBody.create(SystemConstants.OKHTTP_MEDIA_TYPE_OCTET_STREAM, multipartFileBytes));
                        } catch (IOException e) {
                            log.error("callInterfaceByOkHttp multipart-form file {} exception", fieldName, e);
                            return wrapException(e);
                        }
                    }
                }
                requestBody = requestBodyBuilder.build();
                requestBuilder.method(httpMethod.name(), requestBody);
                break;
            case JSON:
                requestBody = RequestBody.create(SystemConstants.OKHTTP_MEDIA_TYPE_JSON_UTF8, bodyJson);
                requestBuilder.method(httpMethod.name(), requestBody);
                break;
        }
        Request request = requestBuilder.build();
        try {
            okhttp3.Response response = okHttpClient.newCall(request).execute();
            try(ResponseBody responseBody = response.body()) {
                if (response.isSuccessful()) {
                    if (responseType == GroupInterface.ResponseType.TEXT) {
                        String contentType = response.header("Content-Type", StringConstants.GROUP_INTERFACE_CALL_DEFAULT_CONTENT_TYPE);
                        CallInterfaceResponseDTO responseDTO = new CallInterfaceResponseDTO();
                        responseDTO.setTime(LocalDateTimeUtil.friendlyNowDateTime());
                        responseDTO.setStatus(response.code());
                        responseDTO.setContentType(contentType);
                        responseDTO.setBody(responseBody.string());
                        return Response.success(responseDTO);
                    } else if(responseType == GroupInterface.ResponseType.FILE) {
                        String contentDisposition = response.header("Content-Disposition");
                        ContentDisposition contentDispositionObject = ContentDisposition.parse(contentDisposition);
                        httpServletResponse.setHeader("Content-Disposition", contentDisposition);
                        httpServletResponse.setHeader("Content-Type", "application/octet-stream");
                        httpServletResponse.setHeader("ApiDocCenter-FileName", contentDispositionObject.getFilename());
                        StreamUtils.copy(responseBody.bytes(), httpServletResponse.getOutputStream());
                        return null;
                    } else {
                        throw new RuntimeException("Invalid state");
                    }
                } else {
                    log.warn("callInterface OkHttp unsuccessful code={} body={}", response.code(), responseBody.string());
                    throw new RuntimeException("调用接口异常，状态码：" + response.code());
                }
            }
        } catch (Exception e) {
            log.error("callInterface OkHttp call exception", e);
            return wrapException(e);
        }
    }

    private Response<CallInterfaceResponseDTO> wrapException(Exception e) {
        CallInterfaceResponseDTO responseDTO = new CallInterfaceResponseDTO();
        responseDTO.setTime(WebConfig.DATE_TIME_FORMATTER.format(LocalDateTime.now()));
        String exceptionMessage = e.getClass().getName() + ":" + e.getMessage();
        responseDTO.setException(exceptionMessage);
        return Response.fail(StringConstants.GROUP_INTERFACE_CALL_EXCEPTION, responseDTO);
    }

    private String callInterfaceErrorMsg(String errorMsg) {
        return StringConstants.GROUP_INTERFACE_CALL_PARAM_INVALID + errorMsg;
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
        if(groupInterfaceDTO.getResponseType() == null) {
            errorMsg.setResponseType(StringConstants.GROUP_INTERFACE_RESPONSE_TYPE_NULL);
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
                        } else if(groupInterfaceDTO.getType() == GroupInterface.Type.FORM_DATA
                                && SystemConstants.FORM_DATA_PROHIBIT_FIELD_NAME_LIST.contains(interfaceFieldDTO.getName())) {
                            interfaceFieldErrorMsg.setName(StringConstants.INTERFACE_FIELD_NAME_PROHIBITED);
                        }
                        if(interfaceFieldDTO.getType() == null) {
                            interfaceFieldErrorMsg.setType(StringConstants.INTERFACE_FIELD_TYPE_NULL);
                        } else if(groupInterfaceDTO.getType() == GroupInterface.Type.NO_BODY) {
                            interfaceFieldErrorMsg.setType(StringConstants.INTERFACE_FIELD_SHOULD_NOT_EXIST);
                        } else if(groupInterfaceDTO.getType() != GroupInterface.Type.FORM_DATA && interfaceFieldDTO.getType() == InterfaceField.Type.FILE) {
                            interfaceFieldErrorMsg.setType(StringConstants.INTERFACE_FIELD_TYPE_SHOULD_NOT_BE_FILE);
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