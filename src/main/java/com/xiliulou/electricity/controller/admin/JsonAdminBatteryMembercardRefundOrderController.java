package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.BatteryMembercardRefundOrderQuery;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 电池退租
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-12-15:48
 */

@Slf4j
@RestController
public class JsonAdminBatteryMembercardRefundOrderController extends BaseController {

    @Autowired
    private BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;

    @Autowired
    UserDataScopeService userDataScopeService;

    @GetMapping("/admin/battery/membercard/refund/page")
    public R getElectricityMemberCardPage(@RequestParam("size") long size,
                                          @RequestParam("offset") long offset,
                                          @RequestParam(value = "uid", required = false) Long uid,
                                          @RequestParam(value = "phone", required = false) String phone,
                                          @RequestParam(value = "orderId", required = false) String orderId,
                                          @RequestParam(value = "memberCardType", required = false) Integer cardType,
                                          @RequestParam(value = "mid", required = false) Long mid,
                                          @RequestParam(value = "status", required = false) Integer status,
                                          @RequestParam(value = "startTime", required = false) Long startTime,
                                          @RequestParam(value = "endTime", required = false) Long endTime,
                                          @RequestParam(value = "payType", required = false) Integer payType) {

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

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.springframework.util.CollectionUtils.isEmpty(storeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        BatteryMembercardRefundOrderQuery query = BatteryMembercardRefundOrderQuery.builder()
                .uid(uid)
                .phone(phone)
                .refundOrderNo(orderId)
                .startTime(startTime)
                .endTime(endTime)
                .offset(offset)
                .size(size)
                .tenantId(TenantContextHolder.getTenantId())
                .status(status)
                .franchiseeIds(franchiseeIds)
                .storeIds(storeIds)
                .mid(mid)
                .build();

        return R.ok(batteryMembercardRefundOrderService.selectByPage(query));
    }

    @GetMapping("/admin/battery/membercard/refund/queryCount")
    public R queryCount(@RequestParam(value = "uid", required = false) Long uid,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "orderId", required = false) String orderId,
                        @RequestParam(value = "memberCardType", required = false) Integer cardType,
                        @RequestParam(value = "mid", required = false) Long mid,
                        @RequestParam(value = "status", required = false) Integer status,
                        @RequestParam(value = "startTime", required = false) Long startTime,
                        @RequestParam(value = "endTime", required = false) Long endTime,
                        @RequestParam(value = "payType", required = false) Integer payType) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.springframework.util.CollectionUtils.isEmpty(storeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        BatteryMembercardRefundOrderQuery query = BatteryMembercardRefundOrderQuery.builder()
                .uid(uid)
                .phone(phone)
                .refundOrderNo(orderId)
                .startTime(startTime)
                .endTime(endTime)
                .tenantId(TenantContextHolder.getTenantId())
                .status(status)
                .franchiseeIds(franchiseeIds)
                .storeIds(storeIds)
                .mid(mid)
                .build();

        return R.ok(batteryMembercardRefundOrderService.selectPageCount(query));
    }


    @PostMapping("/admin/battery/membercard/refund/audit")
    @Log(title = "电池租金退款审核")
    public R batteryMembercardRefundAudit(@RequestParam("refundOrderNo") String refundOrderNo, @RequestParam("status") Integer status,
                                          @RequestParam(value = "errMsg", required = false) String errMsg, HttpServletRequest request) {


        return returnTripleResult(batteryMembercardRefundOrderService.batteryMembercardRefundAudit(refundOrderNo, errMsg, status, request));
    }

    /**
     * 可退租订单详情
     */
    @GetMapping("/admin/batteryMembercardRefund")
    public R batteryMembercardRefundOrderDetail(@RequestParam("orderNo") String orderNo, @RequestParam(value = "confirm", required = false) Integer confirm) {
        return returnTripleResult(batteryMembercardRefundOrderService.batteryMembercardRefundOrderDetail(orderNo,confirm));
    }

    @PostMapping("/admin/battery/membercard/refund")
    @Log(title = "后台电池租金退款")
    public R batteryMembercardRefund(@RequestParam("orderNo") String orderNo, HttpServletRequest request) {


        return returnTripleResult(batteryMembercardRefundOrderService.batteryMembercardRefundForAdmin(orderNo, request));
    }


}
