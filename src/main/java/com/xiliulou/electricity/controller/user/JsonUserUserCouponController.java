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

    //用户优惠券列表查询
    @GetMapping(value = "/admin/coupon/list")
    public R queryList(@RequestParam(value = "size", required = false) Long size,
                       @RequestParam(value = "offset", required = false) Long offset,
                       @RequestParam(value = "couponId", required = false) Integer couponId,
                       @RequestParam(value = "uid", required = false) Long uid,
                       @RequestParam(value = "phone", required = false) String phone) {
        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        UserCouponQuery userCouponQuery = UserCouponQuery.builder()
                .offset(offset)
                .size(size)
                .couponId(couponId)
                .uid(uid)
                .phone(phone).build();
        return userCouponService.queryMyCoupon(userCouponQuery);
    }

    //批量发放优惠券
    @PostMapping(value = "/admin/coupon/batchRelease")
    public R batchRelease(@RequestParam("id") Integer id,@RequestParam("uid") String uid) {
        Long[] uids = (Long[])
                JSONUtil.parseArray(uid).toArray(Long[].class);
        return userCouponService.batchRelease(id,uids);
    }


}
