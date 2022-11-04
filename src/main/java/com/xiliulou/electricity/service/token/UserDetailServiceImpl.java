package com.xiliulou.electricity.service.token;

import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.security.authentication.authorization.AuthorizationService;
import com.xiliulou.security.bean.SecurityUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;

/**
 * @author: eclair
 * @Date: 2020/11/27 10:58
 * @Description:
 */
@Service
public class UserDetailServiceImpl implements UserDetailsService {
	@Autowired
	UserService userService;
	@Autowired
	AuthorizationService authorizationService;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		//查询用户
		User user = userService.queryByUserName(username);
		if (Objects.isNull(user) || Objects.equals(user.getUserType(), User.TYPE_USER_NORMAL_WX_PRO)) {
			throw new UsernameNotFoundException("用户名或者密码错误!");
		}
		Collection<? extends GrantedAuthority> authorities = authorizationService.acquireAllAuthorities(user.getUid(), user.getUserType());

		return new SecurityUser(user.getName(), user.getPhone(), user.getUid(), user.getUserType(), user.getDataType(),user.getLoginPwd(), user.isLock(), authorities,user.getTenantId());
	}
}
