package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.UserInfoDataQuery;
import com.xiliulou.electricity.service.UserInfoDataService;
import com.xiliulou.electricity.vo.car.PageDataResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户运营数据
 */
@RestController
public class JsonAdminUserInfoDataController {

    @Autowired
    private UserInfoDataService userInfoDataService;

    /**
     *
     */
    @PostMapping("/admin/user/all/info")
    public R userActivityInfo(@RequestBody UserInfoDataQuery userInfoDataQuery) {
        PageDataResult pageDataResult = userInfoDataService.queryUserInfoData(userInfoDataQuery);
        return R.ok(pageDataResult);

    }



}
