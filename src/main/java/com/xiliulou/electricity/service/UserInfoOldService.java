package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfoOld;

/**
 * @author: Miss.Li
 * @Date: 2021/9/27 10:14
 * @Description:
 */
public interface UserInfoOldService {


	UserInfoOld queryByPhone(String phone);
}
