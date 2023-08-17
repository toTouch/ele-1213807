package com.xiliulou.electricity.service;

import com.xiliulou.electricity.query.UserInfoDataQuery;
import com.xiliulou.electricity.vo.car.PageDataResult;

public interface UserInfoDataService {
    PageDataResult queryUserInfoData(UserInfoDataQuery userInfoDataQuery);
}
