package com.xiliulou.electricity.utils;

import com.xiliulou.security.bean.TokenUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

/**
 * @author: eclair
 * @Date: 2020/11/30 16:09
 * @Description:
 */
public class SecurityUtils {
    public static Long getUid() {
        TokenUser userInfo = getUserInfo();
        if (Objects.isNull(userInfo)) {
            return null;
        }
        return userInfo.getUid();
    }

    public static TokenUser getUserInfo() {
        Authentication authentication = null;
        TokenUser user = null;
        try {
            authentication = SecurityContextHolder.getContext().getAuthentication();

            if (null == authentication) {
                return null;
            }
            user = (TokenUser) authentication.getPrincipal();
        } catch (Exception e) {

        }
        if (Objects.isNull(user)) {
            return null;
        }
        return user;
    }

    public static boolean isAdmin() {
        TokenUser user = getUserInfo();
        if (Objects.isNull(user)) {
            return false;
        }

        return Objects.equals(user.getUid(), 1);
    }
}
