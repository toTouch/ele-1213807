package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.FreeDepositOrderQuery;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-24-9:53
 */
@RestController
@Slf4j
public class JsonAdminFreeDepositOrderController extends BaseController {

    @Autowired
    private FreeDepositOrderService freeDepositOrderService;

    @Autowired
    UserDataScopeService userDataScopeService;

    /**
     * 分页
     */
    @GetMapping("/admin/freeDepositOrder/page")
    public R page(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                  @RequestParam(value = "authStatus", required = false) Integer authStatus,
                  @RequestParam(value = "payStatus", required = false) Integer payStatus,
                  @RequestParam(value = "depositType", required = false) Integer depositType,
                  @RequestParam(value = "orderId", required = false) String orderId,
                  @RequestParam(value = "phone", required = false) String phone,
                  @RequestParam(value = "realName", required = false) String realName,
                  @RequestParam(value = "uid", required = false) Long uid,
                  @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                  @RequestParam(value = "startTime", required = false) Long startTime,
                  @RequestParam(value = "endTime", required = false) Long endTime) {
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
            if(org.springframework.util.CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        FreeDepositOrderQuery query = FreeDepositOrderQuery.builder()
                .size(size)
                .offset(offset)
                .authStatus(authStatus)
                .payStatus(payStatus)
                .depositType(depositType)
                .orderId(orderId)
                .phone(phone)
                .realName(realName)
                .uid(uid)
                .storeIds(storeIds)
                .franchiseeId(franchiseeId)
                .franchiseeIds(franchiseeIds)
                .tenantId(TenantContextHolder.getTenantId())
                .startTime(startTime)
                .endTime(endTime)
                .build();

        return R.ok(this.freeDepositOrderService.selectByPage(query));
    }

    /**
     * 分页总记录数
     */
    @GetMapping("/admin/freeDepositOrder/queryCount")
    public R pageCount(@RequestParam(value = "authStatus", required = false) Integer authStatus,
                       @RequestParam(value = "payStatus", required = false) Integer payStatus,
                       @RequestParam(value = "depositType", required = false) Integer depositType,
                       @RequestParam(value = "orderId", required = false) String orderId,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "realName", required = false) String realName,
                       @RequestParam(value = "uid", required = false) Long uid,
                       @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                       @RequestParam(value = "startTime", required = false) Long startTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        FreeDepositOrderQuery query = FreeDepositOrderQuery.builder()
                .authStatus(authStatus)
                .payStatus(payStatus)
                .depositType(depositType)
                .orderId(orderId)
                .phone(phone)
                .realName(realName)
                .uid(uid)
                .storeIds(storeIds)
                .franchiseeId(franchiseeId)
                .franchiseeIds(franchiseeIds)
                .tenantId(TenantContextHolder.getTenantId())
                .startTime(startTime)
                .endTime(endTime)
                .build();

        return R.ok(this.freeDepositOrderService.selectByPageCount(query));
    }

    /**
     * 同步免押状态
     */
    @GetMapping("/admin/freeDepositOrder/synchroniz/status")
    public R synchronizFreeDepositOrderStatus(@RequestParam(value = "orderId") String orderId) {

        Triple<Boolean, String, Object> verifyPermissionResult = verifyPermission();
        if (Boolean.FALSE.equals(verifyPermissionResult.getLeft())) {
            return returnTripleResult(verifyPermissionResult);
        }

        return returnTripleResult(this.freeDepositOrderService.synchronizFreeDepositOrderStatus(orderId));
    }


    /**
     * 查询免押订单状态
     */
    @GetMapping("/admin/freeDepositOrder/order/status")
    public R selectFreeDepositOrderStatus(@RequestParam(value = "orderId") String orderId) {

        Triple<Boolean, String, Object> verifyPermissionResult = verifyPermission();
        if (Boolean.FALSE.equals(verifyPermissionResult.getLeft())) {
            return returnTripleResult(verifyPermissionResult);
        }

        return returnTripleResult(this.freeDepositOrderService.selectFreeDepositOrderStatus(orderId));
    }


    /**
     * 授权转支付
     */
    @PutMapping("/admin/freeDepositOrder/AuthToPay")
    public R freeDepositAuthToPay(@RequestParam(value = "orderId") String orderId,
            @RequestParam(value = "payTransAmt", required = false) BigDecimal payTransAmt,
            @RequestParam(value = "remark", required = false) String remark) {
    
        Triple<Boolean, String, Object> verifyPermissionResult = verifyPermission();
        if (Boolean.FALSE.equals(verifyPermissionResult.getLeft())) {
            return returnTripleResult(verifyPermissionResult);
        }
    
        return returnTripleResult(this.freeDepositOrderService.freeDepositAuthToPay(orderId, payTransAmt, remark));
    }
    
    /**
     * 查询授权支付结果
     */
    @GetMapping("/admin/freeDepositOrder/AuthToPay/result")
    public R selectFreeDepositAuthToPay(@RequestParam(value = "orderId") String orderId) {
        
        Triple<Boolean, String, Object> verifyPermissionResult = verifyPermission();
        if (Boolean.FALSE.equals(verifyPermissionResult.getLeft())) {
            return returnTripleResult(verifyPermissionResult);
        }
        
        return returnTripleResult(this.freeDepositOrderService.selectFreeDepositAuthToPay(orderId));
    }
    
    
    private Triple<Boolean, String, Object> verifyPermission() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户!");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return Triple.of(false, "ELECTRICITY.0066", "用户权限不足");
        }
        
        return Triple.of(true, "", null);
    }
    
    
    
    /**
     * 代扣
     */
    @PutMapping("/admin/freeDepositOrder/trilateralPay")
    public R freeDepositTrilateralPay(@RequestParam(value = "orderId") String orderId,
            @RequestParam(value = "payTransAmt") BigDecimal payTransAmt,
            @RequestParam(value = "remark", required = false) String remark) {
        
        Triple<Boolean, String, Object> verifyPermissionResult = verifyPermission();
        if (Boolean.FALSE.equals(verifyPermissionResult.getLeft())) {
            return returnTripleResult(verifyPermissionResult);
        }
        
        return returnTripleResult(this.freeDepositOrderService.freeDepositTrilateralPay(orderId, payTransAmt, remark));
    }
    
    
    /**
     * 代扣同步状态
     */
    @GetMapping("/admin/freeDepositOrder/sync/authPay/status")
    public R syncAuthPayStatus(@RequestParam(value = "orderId") String orderId, @RequestParam(value = "authPayOrderId") String authPayOrderId) {
        Triple<Boolean, String, Object> verifyPermissionResult = verifyPermission();
        if (Boolean.FALSE.equals(verifyPermissionResult.getLeft())) {
            return returnTripleResult(verifyPermissionResult);
        }
        return returnTripleResult(this.freeDepositOrderService.syncAuthPayStatus(orderId, authPayOrderId));
    }

}
