package com.xiliulou.electricity.service.token;

import com.google.common.collect.Lists;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.electricity.entity.PermissionResource;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.service.PermissionResourceService;
import com.xiliulou.electricity.service.RoleService;
import com.xiliulou.electricity.service.UserRoleService;
import com.xiliulou.security.authentication.authorization.AuthorizationService;
import com.xiliulou.security.bean.UrlGrantedAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * @author: eclair
 * @Date: 2020/12/9 16:04
 * @Description:
 */
@Service
public class AuthorizationServiceImpl implements AuthorizationService {
	@Autowired
	UserRoleService userRoleService;
	@Autowired
	RoleService roleService;
	@Autowired
	PermissionResourceService permissionResourceService;

	@Override
	public Collection<? extends GrantedAuthority> acquireAllAuthorities(long uid, int type) {
		if (type == User.TYPE_USER_NORMAL_WX_PRO) {
			return Lists.newArrayList();
		}

		HashSet<GrantedAuthority> grantedAuthorities = new HashSet<>();
		List<Long> roleIds = roleService.queryRidsByUid(uid);
		if (!DataUtil.collectionIsUsable(roleIds)) {
			return grantedAuthorities;
		}

		for (Long roleId : roleIds) {
			List<PermissionResource> permissionResources = permissionResourceService.queryPermissionsByRole(roleId);
			if (DataUtil.collectionIsUsable(permissionResources)) {
				for (PermissionResource p : permissionResources) {
					//页面不需要校验
					if (p.getType().equals(PermissionResource.TYPE_PAGE)) {
						continue;
					}
					GrantedAuthority t = new UrlGrantedAuthority(p.getMethod(), p.getUri());
					grantedAuthorities.add(t);
				}
			}
		}

		return grantedAuthorities;

	}
}
