package com.xiliulou.electricity.controller.admin;

import cn.hutool.json.JSONUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.UserCouponQuery;
import com.xiliulou.electricity.service.UserCouponService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 优惠券表(TCoupon)表控制层
 *
 * @author makejava
 * @since 2021-04-14 09:27:59
 */
@RestController
public class JsonAdminUserCouponController {
    /**
     * 服务对象
     */
    @Autowired
    private UserCouponService userCouponService;

    //用户优惠券列表查询
    @GetMapping(value = "/admin/userCoupon/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "couponId", required = false) Integer couponId,
                       @RequestParam(value = "uid", required = false) Long uid,
                       @RequestParam(value = "phone", required = false) String phone) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        UserCouponQuery userCouponQuery = UserCouponQuery.builder()
                .offset(offset)
                .size(size)
                .couponId(couponId)
                .uid(uid)
                .phone(phone)
                .tenantId(TenantContextHolder.getTenantId()).build();
        return userCouponService.queryList(userCouponQuery);
    }

    //用户优惠券列表查询
    @GetMapping(value = "/admin/userCoupon/queryCount")
    public R queryCount(@RequestParam(value = "couponId", required = false) Integer couponId,
                        @RequestParam(value = "uid", required = false) Long uid,
                        @RequestParam(value = "phone", required = false) String phone) {

        UserCouponQuery userCouponQuery = UserCouponQuery.builder()
                .couponId(couponId)
                .uid(uid)
                .phone(phone)
                .tenantId(TenantContextHolder.getTenantId()).build();
        return userCouponService.queryCount(userCouponQuery);
    }

	//批量发放优惠券
	@PostMapping(value = "/admin/userCoupon/batchRelease")
	public R batchRelease(@RequestParam("id") Integer id, @RequestParam("uid") String uid) {
		Long[] uids = (Long[])
				JSONUtil.parseArray(uid).toArray(Long[].class);
		return userCouponService.adminBatchRelease(id, uids);
	}

}
