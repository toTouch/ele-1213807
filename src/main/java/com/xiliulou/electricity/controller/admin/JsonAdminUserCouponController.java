package com.xiliulou.electricity.controller.admin;

import cn.hutool.json.JSONUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.CouponBatchSendWithPhonesRequest;
import com.xiliulou.electricity.query.CouponBatchSendWithUidsRequest;
import com.xiliulou.electricity.query.UserCouponQuery;
import com.xiliulou.electricity.service.UserCouponService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

    @Autowired
    UserDataScopeService userDataScopeService;

    //用户优惠券列表查询
    @GetMapping(value = "/admin/userCoupon/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "couponId", required = false) Integer couponId,
                       @RequestParam(value = "userName", required = false) String userName,
                       @RequestParam(value = "orderId", required = false) String orderId,
                       @RequestParam(value = "uid", required = false) Long uid,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime,
                       @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "discountType", required = false) Integer discountType,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "superposition", required = false) Integer superposition,
                       @RequestParam(value = "verifiedUid", required = false) Long verifiedUid) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        UserCouponQuery userCouponQuery = UserCouponQuery.builder()
                .offset(offset)
                .size(size)
                .beginTime(beginTime)
                .endTime(endTime)
                .couponId(couponId)
                .uid(uid)
                .orderId(orderId)
                .status(status)
                .superposition(superposition)
                .userName(userName)
                .phone(phone)
                .discountType(discountType)
                .storeIds(storeIds)
                .franchiseeIds(franchiseeIds)
                .verifiedUid(verifiedUid)
                .tenantId(TenantContextHolder.getTenantId()).build();
        return userCouponService.queryList(userCouponQuery);
    }

    //用户优惠券列表查询
    @GetMapping(value = "/admin/userCoupon/queryCount")
    public R queryCount(@RequestParam(value = "couponId", required = false) Integer couponId,
                        @RequestParam(value = "uid", required = false) Long uid,
                        @RequestParam(value = "beginTime", required = false) Long beginTime,
                        @RequestParam(value = "endTime", required = false) Long endTime,
                        @RequestParam(value = "userName", required = false) String userName,
                        @RequestParam(value = "orderId", required = false) String orderId,
                        @RequestParam(value = "status", required = false) Integer status,
                        @RequestParam(value = "discountType", required = false) Integer discountType,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "superposition", required = false) Integer superposition) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        UserCouponQuery userCouponQuery = UserCouponQuery.builder()
                .couponId(couponId)
                .uid(uid)
                .beginTime(beginTime)
                .endTime(endTime)
                .orderId(orderId)
                .userName(userName)
                .discountType(discountType)
                .status(status)
                .superposition(superposition)
                .phone(phone)
                .storeIds(storeIds)
                .franchiseeIds(franchiseeIds)
                .tenantId(TenantContextHolder.getTenantId()).build();
        return userCouponService.queryCount(userCouponQuery);
    }

    //批量发放优惠券
    @PostMapping(value = "/admin/userCoupon/batchRelease")
    @Log(title = "批量发放优惠券")
    public R batchRelease(@RequestBody @Validated CouponBatchSendWithUidsRequest request) {
        Long[] uids = (Long[]) JSONUtil.parseArray(request.getJsonUids()).toArray(Long[].class);
        return userCouponService.adminBatchRelease(request.getCouponId(), uids);
    }

    //批量发放优惠券V2
    @PostMapping(value = "/admin/userCoupon/batchReleaseV2")
    @Log(title = "批量发放优惠券V2")
    public R batchReleaseV2(@RequestBody @Validated CouponBatchSendWithPhonesRequest request) {
        return userCouponService.adminBatchReleaseV2(request);
    }

    @GetMapping(value = "/admin/userCoupon/check/send/finish")
    public R checkFinish(@RequestParam("sessionId") String sessionId) {
        return userCouponService.checkSendFinish(sessionId);
    }

    //核销优惠券
    @Log(title = "核销优惠券")
    @PostMapping(value = "/admin/userCoupon/destruction")
    public R destruction(@RequestParam("couponId") String couponId) {
        Long[] couponIds = (Long[]) JSONUtil.parseArray(couponId).toArray(Long[].class);
        return userCouponService.destruction(couponIds);
    }

}
