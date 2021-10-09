package com.cntt2.nowfood.config.security;

import com.cntt2.nowfood.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * @author Vanh
 * @version 1.0
 * @date 10/9/2021 9:34 AM
 */
@Getter
@Setter
@NoArgsConstructor
public class UserPrincipal implements UserDetails {

    private Integer userId;
    private String username;
    private String password;
    private Collection authorities;

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
