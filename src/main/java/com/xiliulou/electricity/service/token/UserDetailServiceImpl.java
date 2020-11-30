package com.xiliulou.electricity.service.token;

import com.google.common.collect.Lists;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.security.bean.SecurityUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		//查询用户
		User user = userService.queryByUserName(username);
		if (Objects.isNull(user)) {
			throw new UsernameNotFoundException("用户名或者密码错误!");
		}
		//获取权限
		ArrayList<String> dbAuthsSet = Lists.newArrayList();
		Collection<? extends GrantedAuthority> authorities = AuthorityUtils
				.createAuthorityList(dbAuthsSet.toArray(new String[0]));

		return new SecurityUser(user.getName(), user.getPhone(), user.getUid(), user.getUserType(), user.getLoginPwd(), user.isLock(), authorities);
	}
}
