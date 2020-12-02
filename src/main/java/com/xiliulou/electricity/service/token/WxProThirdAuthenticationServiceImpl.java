package com.xiliulou.electricity.service.token;

import com.xiliulou.security.authentication.thirdauth.ThirdAuthenticationService;
import com.xiliulou.security.bean.SecurityUser;
import com.xiliulou.security.constant.TokenConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author: eclair
 * @Date: 2020/12/2 15:29
 * @Description:
 */
@Service
public class WxProThirdAuthenticationServiceImpl implements ThirdAuthenticationService {
//	@Autowired
//	ElectricityPayParamsService electricityPayParamsService;

	@Override
	public SecurityUser registerUserAndLoadUser(HashMap<String, Object> authMap) {
		String code = (String) authMap.get("code");
		String iv = (String) authMap.get("iv");
		String phone = (String) authMap.get("phone");
		String data = (String) authMap.get("data");

		return null;
	}
}
