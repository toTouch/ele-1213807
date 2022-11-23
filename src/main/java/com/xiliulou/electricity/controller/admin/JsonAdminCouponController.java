package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.CouponQuery;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 优惠券规则表(TCoupon)表控制层
 *
 * @author makejava
 * @since 2021-04-14 09:28:22
 */
@RestController
@Slf4j
public class JsonAdminCouponController {
    /**
     * 服务对象
     */
    @Autowired
    private CouponService couponService;

    @Autowired
    UserDataScopeService userDataScopeService;

    //新增
    @PostMapping(value = "/admin/coupon")
    public R save(@RequestBody @Validated(value = CreateGroup.class) Coupon coupon) {
        return couponService.insert(coupon);
    }

    //修改--暂时无此功能
    @PutMapping(value = "/admin/coupon")
    @Log(title = "修改优惠券")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) Coupon coupon) {
        return couponService.update(coupon);
    }

    //列表查询
    @GetMapping(value = "/admin/coupon/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "discountType", required = false) Integer discountType,
                       @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "applyType", required = false) Integer applyType) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
//        if(Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)){
//            return R.ok(Collections.EMPTY_LIST);
//        }
//
//        List<Long> franchiseeIds = null;
//        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
//            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
//            if (CollectionUtils.isEmpty(franchiseeIds)) {
//                return R.ok();
//            }
//        }

        CouponQuery couponQuery = CouponQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .discountType(discountType)
                .franchiseeId(franchiseeId)
//                .franchiseeIds(franchiseeIds)
                .applyType(applyType)
                .tenantId(TenantContextHolder.getTenantId()).build();
        return couponService.queryList(couponQuery);
    }

    //列表查询
    @GetMapping(value = "/admin/coupon/count")
    public R queryCount(@RequestParam(value = "discountType", required = false) Integer discountType,
                        @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                        @RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "applyType", required = false) Integer applyType) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
//        if(Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)){
//            return R.ok(Collections.EMPTY_LIST);
//        }
//
//        List<Long> franchiseeIds = null;
//        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
//            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
//            if (CollectionUtils.isEmpty(franchiseeIds)) {
//                return R.ok();
//            }
//        }

        CouponQuery couponQuery = CouponQuery.builder()
                .name(name)
                .discountType(discountType)
                .franchiseeId(franchiseeId)
//                .franchiseeIds(franchiseeIds)
                .applyType(applyType)
                .tenantId(tenantId).build();
        return couponService.queryCount(couponQuery);
    }

}
