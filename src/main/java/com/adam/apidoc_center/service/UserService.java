package com.adam.apidoc_center.service;

import com.adam.apidoc_center.common.CacheConstants;
import com.adam.apidoc_center.common.Response;
import com.adam.apidoc_center.common.StringConstants;
import com.adam.apidoc_center.common.SystemConstants;
import com.adam.apidoc_center.domain.User;
import com.adam.apidoc_center.domain.UserAuthority;
import com.adam.apidoc_center.domain.UserOAuth2Github;
import com.adam.apidoc_center.domain.UserOAuth2Huawei;
import com.adam.apidoc_center.dto.*;
import com.adam.apidoc_center.repository.UserAuthorityRepository;
import com.adam.apidoc_center.repository.UserOAuth2GithubRepository;
import com.adam.apidoc_center.repository.UserOAuth2HuaweiRepository;
import com.adam.apidoc_center.repository.UserRepository;
import com.adam.apidoc_center.security.LoginType;
import com.adam.apidoc_center.security.SecurityUtil;
import com.adam.apidoc_center.security.oauth2.ExtendedOAuth2User;
import com.adam.apidoc_center.security.oauth2.OAuth2Provider;
import com.adam.apidoc_center.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserAuthorityRepository userAuthorityRepository;
    @Autowired
    private UserOAuth2HuaweiRepository userOAuth2HuaweiRepository;
    @Autowired
    private UserOAuth2GithubRepository userOAuth2GithubRepository;
    @Autowired
    //解决循环依赖
    @Lazy
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MailService mailService;
    @Autowired
    private MemoryCacheService memoryCacheService;

    @Transactional
    public Response<OAuth2BindSuccessData> oauth2BindLogin(String username, String password) {
        String registrationId = SecurityUtil.getOAuth2RegistrationId();
        OAuth2Provider oAuth2Provider = OAuth2Provider.of(registrationId);
        ExtendedOAuth2User extendedOAuth2User = SecurityUtil.getExtendedOAuth2User();
        if(oAuth2Provider == null) {
            return Response.fail(StringConstants.OAUTH2_UNKNOWN_PROVIDER);
        }
        if(extendedOAuth2User == null) {
            throw new RuntimeException("Invalid state");
        }
        User user = userRepository.findByUsername(username);
        if(user == null) {
            return Response.fail(StringConstants.USERNAME_OR_PASSWORD_WRONG);
        }
        if(!passwordEncoder.matches(password, user.getPassword())) {
            return Response.fail(StringConstants.USERNAME_OR_PASSWORD_WRONG);
        }
        //执行绑定
        LocalDateTime now = LocalDateTime.now();
        List<User.UserType> userTypeList = user.getUserTypeList();
        switch (oAuth2Provider) {
            case HUAWEI:
                String unionId = extendedOAuth2User.getAttribute("unionID");
                //因为在OAuth2UserService中执行了创建，这里直接find就行
                UserOAuth2Huawei userOAuth2Huawei = findUserHuawei(unionId);
                updateHuaweiUserProperties(userOAuth2Huawei, extendedOAuth2User, user.getId(), now, false);
                userOAuth2HuaweiRepository.save(userOAuth2Huawei);
                if(!userTypeList.contains(User.UserType.OAUTH2_HUAWEI)) {
                    userTypeList.add(User.UserType.OAUTH2_HUAWEI);
                    user.setUserTypeList(userTypeList);
                }
                user.setUserOAuth2Huawei(userOAuth2Huawei);
                break;
            case GITHUB:
                Integer githubId = extendedOAuth2User.getAttribute("id");
                //因为在OAuth2UserService中执行了创建，这里直接find就行
                UserOAuth2Github userOAuth2Github = findUserGithub(githubId);
                updateGitHubUserProperties(userOAuth2Github, extendedOAuth2User, user.getId(), now, false);
                userOAuth2GithubRepository.save(userOAuth2Github);
                if(!userTypeList.contains(User.UserType.OAUTH2_GITHUB)) {
                    userTypeList.add(User.UserType.OAUTH2_GITHUB);
                    user.setUserTypeList(userTypeList);
                }
                user.setUserOAuth2Github(userOAuth2Github);
                break;
        }
        user.setUpdateTime(now);
        userRepository.save(user);
        extendedOAuth2User.setUser(user);

        OAuth2BindSuccessData oAuth2BindSuccessData = generateOAuth2BindSuccessData(extendedOAuth2User, oAuth2Provider, user);
        return Response.success(oAuth2BindSuccessData);
    }

    private void updateHuaweiUserProperties(UserOAuth2Huawei user, OAuth2User oAuth2User, long userId, LocalDateTime now, boolean create) {
        user.setUserId(userId);
        user.setDisplayName(oAuth2User.getAttribute("displayName"));
        user.setHeadPictureUrl(oAuth2User.getAttribute("headPictureURL"));
        user.setUnionId(oAuth2User.getAttribute("unionID"));
        user.setOpenId(oAuth2User.getAttribute("openID"));
        user.setDisplayNameFlag(oAuth2User.getAttribute("displayNameFlag"));
        if(create) {
            user.setCreateTime(now);
        }
        user.setUpdateTime(now);
    }

    private void updateGitHubUserProperties(UserOAuth2Github user, OAuth2User oAuth2User, long userId, LocalDateTime now, boolean create) {
        user.setUserId(userId);
        user.setGithubId(oAuth2User.getAttribute("id"));
        user.setUsername(oAuth2User.getAttribute("login"));
        user.setAvatarUrl(oAuth2User.getAttribute("avatar_url"));
        user.setRealName(oAuth2User.getAttribute("name"));
        user.setEmail(oAuth2User.getAttribute("email"));
        user.setBio(oAuth2User.getAttribute("bio"));
        if(create) {
            user.setCreateTime(now);
        }
        user.setUpdateTime(now);
    }

    public User findHuaweiBindUser(String unionId) {
        Objects.requireNonNull(unionId);
        UserOAuth2Huawei userOAuth2Huawei = userOAuth2HuaweiRepository.findByUnionId(unionId);
        if (userOAuth2Huawei == null) {
            return null;
        } else {
            return userOAuth2Huawei.getUser();
        }
    }

    public UserOAuth2Huawei findUserHuawei(String unionId) {
        Objects.requireNonNull(unionId);
        return userOAuth2HuaweiRepository.findByUnionId(unionId);
    }

    public void saveOAuth2User(Object userOAuth2, OAuth2User oAuth2User, User.UserType userType, long userId) {
        Objects.requireNonNull(oAuth2User);
        Objects.requireNonNull(userType);
        Assert.isTrue(userType != User.UserType.NORMAL, "saveOAuth2User userType should not be NORMAL");
        boolean create;
        switch (userType) {
            case OAUTH2_HUAWEI:
                UserOAuth2Huawei userOAuth2Huawei = (UserOAuth2Huawei) userOAuth2;
                create = userOAuth2Huawei.getId() == 0;
                updateHuaweiUserProperties(userOAuth2Huawei, oAuth2User, userId, LocalDateTime.now(), create);
                userOAuth2HuaweiRepository.save(userOAuth2Huawei);
                break;
            case OAUTH2_GITHUB:
                UserOAuth2Github userOAuth2Github = (UserOAuth2Github) userOAuth2;
                create = userOAuth2Github.getId() == 0;
                updateGitHubUserProperties(userOAuth2Github, oAuth2User, userId, LocalDateTime.now(), create);
                userOAuth2GithubRepository.save(userOAuth2Github);
                break;
        }
    }

    public User findGithubBindUser(Integer githubId) {
        Objects.requireNonNull(githubId);
        UserOAuth2Github userOAuth2Github = userOAuth2GithubRepository.findByGithubId(githubId);
        if(userOAuth2Github == null) {
            return null;
        } else {
            return userOAuth2Github.getUser();
        }
    }

    public UserOAuth2Github findUserGithub(Integer githubId) {
        Objects.requireNonNull(githubId);
        return userOAuth2GithubRepository.findByGithubId(githubId);
    }

    public Map<Long, User> queryUserMap(List<Long> userIdList) {
        Assert.notNull(userIdList, "queryUserMap userIdList null");
        if(CollectionUtils.isEmpty(userIdList)) {
            return new HashMap<>();
        }
        Set<Long> userIdSet = new HashSet<>(userIdList);
        List<User> userList = userRepository.findAllById(userIdSet);
        if(CollectionUtils.isEmpty(userList)) {
            return new HashMap<>();
        } else {
            return userList.stream()
                    .collect(Collectors.toMap(User::getId, Function.identity()));
        }
    }

    public Response<?> checkAndModify(UserDTO userDTO) {
        Assert.notNull(userDTO, "checkAndModify userDTO null");
        log.debug("checkAndModify userDTO={}", userDTO);
        ProfileErrorMsg errorMsg = checkModifyParams(userDTO);
        if(errorMsg.hasError()) {
            return Response.fail(StringConstants.MODIFY_FAIL_CHECK_INPUTS, errorMsg);
        }
        //进行修改
        try {
            User user = SecurityUtil.getUser();
            user.setUsername(userDTO.getUsername());
            if(StringUtils.isNotEmpty(userDTO.getPassword())) {
                user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            }
            if(StringUtils.isNotEmpty(userDTO.getAvatarUrl())) {
                user.setAvatarUrl(userDTO.getAvatarUrl());
            }
            if(StringUtils.isNotEmpty(userDTO.getDescription())) {
                user.setDescription(userDTO.getDescription());
            }
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
            return Response.success();
        } catch (Exception e) {
            log.error("modify fail", e);
            return Response.fail(StringConstants.MODIFY_FAIL_SERVER_ERROR);
        }
    }

    @Transactional
    public Response<?> oauth2BindRegister(RegisterForm registerForm) {
        Assert.notNull(registerForm, "checkAndRegister registerForm null");
        log.debug("checkAndRegister registerForm={}", registerForm);
        ProfileErrorMsg errorMsg = checkRegisterParams(registerForm);
        if(errorMsg.hasError()) {
            return Response.fail(StringConstants.REGISTER_FAIL_CHECK_INPUTS, errorMsg);
        }

        String registrationId = SecurityUtil.getOAuth2RegistrationId();
        OAuth2Provider oAuth2Provider = OAuth2Provider.of(registrationId);
        ExtendedOAuth2User extendedOAuth2User = SecurityUtil.getExtendedOAuth2User();
        if(oAuth2Provider == null) {
            return Response.fail(StringConstants.OAUTH2_UNKNOWN_PROVIDER);
        }
        if(extendedOAuth2User == null) {
            throw new RuntimeException("Invalid state");
        }
        List<User.UserType> userTypeList = new LinkedList<>();
        userTypeList.add(User.UserType.NORMAL);
        switch (oAuth2Provider) {
            case HUAWEI:
                userTypeList.add(User.UserType.OAUTH2_HUAWEI);
                break;
            case GITHUB:
                userTypeList.add(User.UserType.OAUTH2_GITHUB);
                break;
        }

        LocalDateTime now = LocalDateTime.now();
        User user = createUserAndAuthority(registerForm, userTypeList, now);
        extendedOAuth2User.setUser(user);
        switch (oAuth2Provider) {
            case HUAWEI:
                String unionId = extendedOAuth2User.getAttribute("unionID");
                UserOAuth2Huawei userOAuth2Huawei = findUserHuawei(unionId);
                updateHuaweiUserProperties(userOAuth2Huawei, extendedOAuth2User, user.getId(), now, false);
                userOAuth2HuaweiRepository.save(userOAuth2Huawei);
                user.setUserOAuth2Huawei(userOAuth2Huawei);
                break;
            case GITHUB:
                Integer githubId = extendedOAuth2User.getAttribute("id");
                UserOAuth2Github userOAuth2Github = findUserGithub(githubId);
                updateGitHubUserProperties(userOAuth2Github, extendedOAuth2User, user.getId(), now, false);
                userOAuth2GithubRepository.save(userOAuth2Github);
                user.setUserOAuth2Github(userOAuth2Github);
                break;
        }

        OAuth2BindSuccessData oAuth2BindSuccessData = generateOAuth2BindSuccessData(extendedOAuth2User, oAuth2Provider, user);
        return Response.success(oAuth2BindSuccessData);
    }

    private OAuth2BindSuccessData generateOAuth2BindSuccessData(ExtendedOAuth2User extendedOAuth2User, OAuth2Provider oAuth2Provider, User user) {
        OAuth2BindSuccessData oAuth2BindSuccessData = new OAuth2BindSuccessData();
        oAuth2BindSuccessData.setOAuth2User(extendedOAuth2User);
        oAuth2BindSuccessData.setProvider(oAuth2Provider);
        oAuth2BindSuccessData.setUserId(user.getId());
        oAuth2BindSuccessData.setUsername(user.getUsername());
        oAuth2BindSuccessData.setEmail(user.getEmail());
        return oAuth2BindSuccessData;
    }

    public Response<Void> unbindOAuth2User(User.UserType userType) {
        Objects.requireNonNull(userType);
        Assert.isTrue(userType != User.UserType.NORMAL, "unbindOAuth2User userType should not be NORMAL");
        User user = SecurityUtil.getUser();
        List<User.UserType> userTypeList = user.getUserTypeList();
        LoginType loginType = SecurityUtil.getLoginType();
        switch (userType) {
            case OAUTH2_GITHUB:
                if(!userTypeList.contains(User.UserType.OAUTH2_GITHUB)) {
                    return Response.fail(StringConstants.OAUTH2_UNBIND_FAIL_NO_BINDING_GITHUB);
                } else {
                    userTypeList.remove(User.UserType.OAUTH2_GITHUB);
                    user.setUserTypeList(userTypeList);
                }
                UserOAuth2Github userOAuth2Github = user.getUserOAuth2Github();
                userOAuth2Github.setUserId(SystemConstants.INVALID_USER_ID);
                userOAuth2Github.setUser(null);
                user.setUserOAuth2Github(null);
                userOAuth2GithubRepository.save(userOAuth2Github);
                userRepository.save(user);
                if(loginType == LoginType.OAUTH2_GITHUB) {
                    SecurityUtil.getSecurityUser().clearUser();
                }
                break;
            case OAUTH2_HUAWEI:
                if(!userTypeList.contains(User.UserType.OAUTH2_HUAWEI)) {
                    return Response.fail(StringConstants.OAUTH2_UNBIND_FAIL_NO_BINDING_HUAWEI);
                } else {
                    userTypeList.remove(User.UserType.OAUTH2_HUAWEI);
                    user.setUserTypeList(userTypeList);
                }
                UserOAuth2Huawei userOAuth2Huawei = user.getUserOAuth2Huawei();
                userOAuth2Huawei.setUserId(SystemConstants.INVALID_USER_ID);
                userOAuth2Huawei.setUser(null);
                user.setUserOAuth2Huawei(null);
                userOAuth2HuaweiRepository.save(userOAuth2Huawei);
                userRepository.save(user);
                if(loginType == LoginType.OAUTH2_HUAWEI) {
                    SecurityUtil.getSecurityUser().clearUser();
                }
                break;
        }
        return Response.success();
    }

    public Response<?> checkAndRegister(RegisterForm registerForm) {
        Assert.notNull(registerForm, "checkAndRegister registerForm null");
        log.debug("checkAndRegister registerForm={}", registerForm);
        ProfileErrorMsg errorMsg = checkRegisterParams(registerForm);
        if(errorMsg.hasError()) {
            return Response.fail(StringConstants.REGISTER_FAIL_CHECK_INPUTS, errorMsg);
        }
        //进行注册
        try {
            User user = createUserAndAuthority(registerForm, List.of(User.UserType.NORMAL), LocalDateTime.now());
            RegisterSuccessData registerSuccessData = new RegisterSuccessData();
            registerSuccessData.setUserId(user.getId());
            registerSuccessData.setUsername(user.getUsername());
            registerSuccessData.setEmail(user.getEmail());
            return Response.success(registerSuccessData);
        } catch (Exception e) {
            log.error("register fail", e);
            return Response.fail(StringConstants.REGISTER_FAIL_SERVER_ERROR);
        }
    }

    public Response<EmailCodeErrorMsg> sendEmailCode(EmailCodeRequestDTO requestDTO) {
        if(requestDTO == null) {
            return Response.fail(StringConstants.SEND_EMAIL_CODE_PARAM_INVALID);
        }
        EmailCodeErrorMsg errorMsg = checkEmailCodeParam(requestDTO);
        if(errorMsg.hasError()) {
            return Response.fail(StringConstants.SEND_EMAIL_CODE_PARAM_INVALID, errorMsg);
        }
        String cacheKey = CacheConstants.EMAIL_CODE_PREFIX + requestDTO.getEmail();
        String code = StringUtil.randomString(SystemConstants.EMAIL_CODE_NUM_OF_DIGITS);
        memoryCacheService.setValueExpire(cacheKey, code, CacheConstants.EMAIL_CODE_EXPIRE);
        String mailText = StringConstants.SEND_EMAIL_CODE_TEXT.replace("{}", code);
        try {
            mailService.sendTextMail(requestDTO.getEmail(), StringConstants.SEND_EMAIL_CODE_SUBJECT, mailText);
            return Response.success();
        } catch (MessagingException e) {
            log.error("sendEmailCode error email={}", requestDTO.getEmail());
            errorMsg.setCode(StringConstants.SEND_EMAIL_CODE_ERROR);
            return Response.fail(StringConstants.SEND_EMAIL_CODE_ERROR, errorMsg);
        }
    }

    private EmailCodeErrorMsg checkEmailCodeParam(EmailCodeRequestDTO requestDTO) {
        String email = requestDTO.getEmail();
        EmailCodeErrorMsg errorMsg = new EmailCodeErrorMsg();
        if(StringUtils.isBlank(email)) {
            errorMsg.setEmail(StringConstants.EMAIL_INPUT_BLANK);
        } else if(email.length() > 256) {
            errorMsg.setEmail(StringConstants.EMAIL_LENGTH_EXCEEDED);
        } else if(!StringUtil.isEmail(email)) {
            errorMsg.setEmail(StringConstants.EMAIL_INVALID);
        } else if(userExistsByEmail(email)) {
            errorMsg.setEmail(StringConstants.EMAIL_ALREADY_IN_USE);
        }
        return errorMsg;
    }

    private User createUserAndAuthority(RegisterForm registerForm, List<User.UserType> userTypeList, LocalDateTime now) {
        User user = new User();
        user.setUsername(registerForm.getUsername());
        user.setEmail(registerForm.getEmail());
        user.setPassword(passwordEncoder.encode(registerForm.getPassword()));
        user.setAvatarUrl(SystemConstants.DEFAULT_AVATAR_URL);
        if(StringUtils.isNotEmpty(registerForm.getDescription())) {
            user.setDescription(registerForm.getDescription());
        }
        user.setUserTypeList(userTypeList);
        user.setEnabled(true);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        userRepository.save(user);
        UserAuthority userAuthority = new UserAuthority();
        userAuthority.setUserId(user.getId());
        userAuthority.setAuthority(UserAuthority.Authority.ROLE_USER);
        userAuthority.setCreateTime(now);
        userAuthorityRepository.save(userAuthority);
        return user;
    }

    public boolean userExistsByEmail(String email) {
        Assert.notNull(email, "userExistsByEmail email null");
        return userRepository.countByEmail(email) > 0;
    }

    public boolean userExistsByUsername(String username) {
        Assert.notNull(username, "userExistsByUsername username null");
        return userRepository.countByUsername(username) > 0;
    }

    public UserCoreDTO queryUserCore(String queryParam) {
        Assert.isTrue(StringUtils.isNotBlank(queryParam), "queryUserCore queryParam blank");
        User user;
        if(StringUtil.isNumber(queryParam)) {
            long userId = Long.parseLong(queryParam);
            user = userRepository.findById(userId);
        } else if(StringUtil.isEmail(queryParam)) {
            user = userRepository.findByEmail(queryParam);
        } else {
            user = userRepository.findByUsername(queryParam);
        }
        if(user == null) {
            return null;
        } else {
            return new UserCoreDTO(user);
        }
    }

    private ProfileErrorMsg checkModifyParams(UserDTO userDTO) {
        ProfileErrorMsg errorMsg = new ProfileErrorMsg();
        User user = SecurityUtil.getUser();
        String originalUsername = user.getUsername();
        if(StringUtils.isBlank(userDTO.getUsername())) {
            errorMsg.setUsername(StringConstants.USERNAME_INPUT_BLANK);
        } else if(userDTO.getUsername().length() > 32) {
            errorMsg.setUsername(StringConstants.USERNAME_LENGTH_EXCEEDED);
        } else if(StringUtil.isNumber(userDTO.getUsername())) {
            errorMsg.setUsername(StringConstants.USERNAME_NUMBER_NOT_ALLOWED);
        } else if(!StringUtils.equals(originalUsername, userDTO.getUsername()) && userExistsByUsername(userDTO.getUsername())) {
            errorMsg.setUsername(StringConstants.USERNAME_ALREADY_IN_USE);
        }
        if(StringUtils.isNotEmpty(userDTO.getPassword()) || StringUtils.isNotEmpty(userDTO.getVerifyPassword())) {
            if (StringUtils.isBlank(userDTO.getPassword())) {
                errorMsg.setPassword(StringConstants.PASSWORD_INPUT_BLANK);
            } else if (userDTO.getPassword().length() > 32) {
                errorMsg.setPassword(StringConstants.PASSWORD_LENGTH_EXCEEDED);
            } else if (!StringUtil.isPassword(userDTO.getPassword())) {
                errorMsg.setPassword(StringConstants.PASSWORD_INVALID);
            }
            if (StringUtils.isBlank(userDTO.getVerifyPassword())) {
                errorMsg.setVerifyPassword(StringConstants.VERIFY_PASSWORD_INPUT_BLANK);
            } else if (!StringUtils.equals(userDTO.getPassword(), userDTO.getVerifyPassword())) {
                errorMsg.setVerifyPassword(StringConstants.VERIFY_PASSWORD_NOT_EQUAL);
            }
        }
        if(StringUtils.isNotEmpty(userDTO.getDescription())) {
            if(userDTO.getDescription().length() > 100) {
                errorMsg.setDescription(StringConstants.DESCRIPTION_LENGTH_EXCEEDED);
            }
        }
        return errorMsg;
    }

    private ProfileErrorMsg checkRegisterParams(RegisterForm registerForm) {
        ProfileErrorMsg errorMsg = new ProfileErrorMsg();
        if(StringUtils.isBlank(registerForm.getUsername())) {
            errorMsg.setUsername(StringConstants.USERNAME_INPUT_BLANK);
        } else if(registerForm.getUsername().length() > 32) {
            errorMsg.setUsername(StringConstants.USERNAME_LENGTH_EXCEEDED);
        } else if(StringUtil.isNumber(registerForm.getUsername())) {
            errorMsg.setUsername(StringConstants.USERNAME_NUMBER_NOT_ALLOWED);
        } else if(userExistsByUsername(registerForm.getUsername())) {
            errorMsg.setUsername(StringConstants.USERNAME_ALREADY_IN_USE);
        }
        String email = null;
        if(StringUtils.isBlank(registerForm.getEmail())) {
            errorMsg.setEmail(StringConstants.EMAIL_INPUT_BLANK);
        } else if(registerForm.getEmail().length() > 256) {
            errorMsg.setEmail(StringConstants.EMAIL_LENGTH_EXCEEDED);
        } else if(!StringUtil.isEmail(registerForm.getEmail())) {
            errorMsg.setEmail(StringConstants.EMAIL_INVALID);
        } else if(userExistsByEmail(registerForm.getEmail())) {
            errorMsg.setEmail(StringConstants.EMAIL_ALREADY_IN_USE);
        } else {
            email = registerForm.getEmail();
        }
        if(StringUtils.isBlank(registerForm.getEmailCode())) {
            errorMsg.setEmailCode(StringConstants.EMAIL_CODE_INPUT_BLANK);
        } else {
            if(email != null) {
                String cacheKey = CacheConstants.EMAIL_CODE_PREFIX + email;
                String code = memoryCacheService.getValue(cacheKey);
                if(!registerForm.getEmailCode().equalsIgnoreCase(code)) {
                    errorMsg.setEmailCode(StringConstants.EMAIL_CODE_NOT_MATCH);
                }
            }
        }
        if(StringUtils.isBlank(registerForm.getPassword())) {
            errorMsg.setPassword(StringConstants.PASSWORD_INPUT_BLANK);
        } else if(registerForm.getPassword().length() > 32) {
            errorMsg.setPassword(StringConstants.PASSWORD_LENGTH_EXCEEDED);
        } else if(!StringUtil.isPassword(registerForm.getPassword())) {
            errorMsg.setPassword(StringConstants.PASSWORD_INVALID);
        }
        if(StringUtils.isBlank(registerForm.getVerifyPassword())) {
            errorMsg.setVerifyPassword(StringConstants.VERIFY_PASSWORD_INPUT_BLANK);
        } else if(!StringUtils.equals(registerForm.getPassword(), registerForm.getVerifyPassword())) {
            errorMsg.setVerifyPassword(StringConstants.VERIFY_PASSWORD_NOT_EQUAL);
        }
        if(StringUtils.isNotEmpty(registerForm.getDescription())) {
            if(registerForm.getDescription().length() > 100) {
                errorMsg.setDescription(StringConstants.DESCRIPTION_LENGTH_EXCEEDED);
            }
        }
        return errorMsg;
    }

}
