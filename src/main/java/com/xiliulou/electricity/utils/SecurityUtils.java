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
		if(Objects.isNull(userInfo)) {
			return null;
		}
		return userInfo.getUid();
	}

	public static TokenUser getUserInfo() {
		Authentication authentication = null;
		try {
			authentication = SecurityContextHolder.getContext().getAuthentication();
		}catch (Exception e) {

		}
		if (null == authentication) {
			return null;
		}
		TokenUser user = (TokenUser) authentication.getPrincipal();
		if (Objects.isNull(user)) {
			return null;
		}
		return user;
	}
}
