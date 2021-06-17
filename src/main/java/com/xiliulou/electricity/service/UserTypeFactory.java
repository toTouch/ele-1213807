package com.xiliulou.electricity.service;
import com.xiliulou.electricity.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: lxc
 * @Date: 2021/3/30 14:08
 * @Description:
 */
@Service
public class UserTypeFactory {
	@Autowired
	private Map<String, UserTypeService> map = new HashMap<>();

	public static final Map<Integer, String> USER_TYPE = new HashMap<>();
	public static final String TYPE_USER_SUPER = "typeUserSuperService";
	public static final String TYPE_USER_OPERATE = "typeUserOperateService";
	public static final String TYPE_USER_FRANCHISEE = "typeUserFranchiseeService";
	public static final String TYPE_USER_STORE = "typeUserStoreService";
	static {
		USER_TYPE.put(User.TYPE_USER_SUPER,TYPE_USER_SUPER);
		USER_TYPE.put(User.TYPE_USER_OPERATE,TYPE_USER_OPERATE);
		USER_TYPE.put(User.TYPE_USER_FRANCHISEE,TYPE_USER_FRANCHISEE);
		USER_TYPE.put(User.TYPE_USER_STORE,TYPE_USER_STORE);
	}

	public UserTypeService getInstance(Integer type) {
		return map.get(USER_TYPE.get(type));
	}


}
