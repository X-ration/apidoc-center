package com.adam.apidoc_center.service;

import com.adam.apidoc_center.common.Response;
import com.adam.apidoc_center.common.StringConstants;
import com.adam.apidoc_center.common.SystemConstants;
import com.adam.apidoc_center.domain.User;
import com.adam.apidoc_center.domain.UserAuthority;
import com.adam.apidoc_center.dto.*;
import com.adam.apidoc_center.repository.UserAuthorityRepository;
import com.adam.apidoc_center.repository.UserRepository;
import com.adam.apidoc_center.security.ExtendedUser;
import com.adam.apidoc_center.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserAuthorityRepository userAuthorityRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Response<?> checkAndModify(UserDTO userDTO) {
        Assert.notNull(userDTO, "checkAndModify userDTO null");
        log.debug("checkAndModify userDTO={}", userDTO);
        ProfileErrorMsg errorMsg = checkModifyParams(userDTO);
        if(errorMsg.hasError()) {
            return Response.fail(StringConstants.MODIFY_FAIL_CHECK_INPUTS, errorMsg);
        }
        //进行修改
        try {
            ExtendedUser extendedUser = (ExtendedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = extendedUser.getUser();
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

    public Response<?> checkAndRegister(RegisterForm registerForm) {
        Assert.notNull(registerForm, "checkAndRegister registerForm null");
        log.debug("checkAndRegister registerForm={}", registerForm);
        ProfileErrorMsg errorMsg = checkRegisterParams(registerForm);
        if(errorMsg.hasError()) {
            return Response.fail(StringConstants.REGISTER_FAIL_CHECK_INPUTS, errorMsg);
        }
        //进行注册
        try {
            LocalDateTime now = LocalDateTime.now();
            User user = new User();
            user.setUsername(registerForm.getUsername());
            user.setEmail(registerForm.getEmail());
            user.setPassword(passwordEncoder.encode(registerForm.getPassword()));
            user.setAvatarUrl(SystemConstants.DEFAULT_AVATAR_URL);
            if(StringUtils.isNotEmpty(registerForm.getDescription())) {
                user.setDescription(registerForm.getDescription());
            }
            user.setUserType(User.UserType.NORMAL);
            user.setEnabled(true);
            user.setCreateTime(now);
            user.setUpdateTime(now);
            userRepository.save(user);
            UserAuthority userAuthority = new UserAuthority();
            userAuthority.setUserId(user.getId());
            userAuthority.setAuthority(UserAuthority.Authority.ROLE_USER);
            userAuthority.setCreateTime(now);
            userAuthorityRepository.save(userAuthority);
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
        ExtendedUser extendedUser = (ExtendedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String originalUsername = extendedUser.getUser().getUsername();
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
        if(StringUtils.isBlank(registerForm.getEmail())) {
            errorMsg.setEmail(StringConstants.EMAIL_INPUT_BLANK);
        } else if(registerForm.getEmail().length() > 256) {
            errorMsg.setEmail(StringConstants.EMAIL_LENGTH_EXCEEDED);
        } else if(!StringUtil.isEmail(registerForm.getEmail())) {
            errorMsg.setEmail(StringConstants.EMAIL_INVALID);
        } else if(userExistsByEmail(registerForm.getEmail())) {
            errorMsg.setEmail(StringConstants.EMAIL_ALREADY_IN_USE);
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
