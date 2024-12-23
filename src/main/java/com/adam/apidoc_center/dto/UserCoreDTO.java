package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.domain.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserCoreDTO {

    private long id;
    private String email;
    private String username;

    public UserCoreDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.username = user.getUsername();
    }

}
