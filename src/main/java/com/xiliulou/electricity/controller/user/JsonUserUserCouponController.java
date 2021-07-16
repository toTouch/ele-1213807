package com.xiliulou.electricity.controller.user;

import cn.hutool.json.JSONUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.UserCouponQuery;
import com.xiliulou.electricity.service.UserCouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shaded.org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 优惠券表(TCoupon)表控制层
 *
 * @author makejava
 * @since 2021-04-14 09:27:59
 */
@RestController
public class JsonUserUserCouponController {
    /**
     * 服务对象
     */
    @Autowired
    private UserCouponService userCouponService;

    //我的优惠券查询
    //status 1--未使用  2--已使用  3--已过期
    //type  1--减免券，2--打折券，3-体验劵
    @GetMapping(value = "/user/userCoupon/queryMyCoupon")
    public R queryMyCoupon(@RequestParam(value = "status", required = false) String status, @RequestParam(value = "type", required = false) String type) {
        List<Integer> typeList = null;
        if (StringUtils.isNotEmpty(type)) {
            Integer[] types = (Integer[])
                    JSONUtil.parseArray(type).toArray(Integer[].class);

            typeList = Arrays.asList(types);
        }

        List<Integer> statusList = null;
        if (StringUtils.isNotEmpty(status)) {
            Integer[] statuses = (Integer[])
                    JSONUtil.parseArray(status).toArray(Integer[].class);

            statusList = Arrays.asList(statuses);
        }
        return userCouponService.queryMyCoupon(statusList, typeList);
    }


    //领劵
    @PostMapping(value = "/user/userCoupon/getCoupon")
    public R getCoupon( @RequestParam("activityId") Integer activityId,@RequestParam("couponId") Integer couponId) {

        return userCouponService.getShareCoupon(activityId,couponId);
    }

}
