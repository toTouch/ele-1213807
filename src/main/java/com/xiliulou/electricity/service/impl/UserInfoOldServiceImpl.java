package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfoOld;
import com.xiliulou.electricity.mapper.UserInfoMapper;
import com.xiliulou.electricity.mapper.UserInfoOldMapper;
import com.xiliulou.electricity.service.UserInfoOldService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: Miss.Li
 * @Date: 2021/9/27 10:14
 * @Description:
 */
@Service("userInfoOldService")
public class UserInfoOldServiceImpl implements UserInfoOldService {

	@Resource
	private UserInfoOldMapper userInfoOldMapper;


	@Override
	public UserInfoOld queryByPhone(String phone) {
		return userInfoOldMapper.selectOne(new LambdaQueryWrapper<UserInfoOld>().eq(UserInfoOld::getPhone, phone)
				.eq(UserInfoOld::getDelFlag, UserInfoOld.DEL_NORMAL));
	}
}
