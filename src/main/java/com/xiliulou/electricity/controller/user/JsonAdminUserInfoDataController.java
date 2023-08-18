package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.UserInfoDataQuery;
import com.xiliulou.electricity.service.UserInfoDataService;
import com.xiliulou.electricity.vo.car.PageDataResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 用户运营数据
 */
public class JsonAdminUserInfoDataController {

    @Autowired
    private UserInfoDataService userInfoDataService;

    /**
     * 用户数据信息
     */
    @GetMapping("/user/activity/info")
    public R userActivityInfo(@RequestBody UserInfoDataQuery userInfoDataQuery) {
        PageDataResult pageDataResult = userInfoDataService.queryUserInfoData(userInfoDataQuery);
        return R.ok(pageDataResult);

    }



}
