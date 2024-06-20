package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.BatteryDepositAdd;
import com.xiliulou.electricity.query.EleDepositOrderQuery;
import com.xiliulou.electricity.query.RentCarDepositAdd;
import com.xiliulou.electricity.query.RentCarDepositQuery;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 缴纳押金订单表(TEleDepositOrder)表控制层
 *
 * @author makejava
 * @since 2021-03-02 10:16:44
 */
@RestController
@Slf4j
public class JsonAdminEleDepositOrderController extends BaseController {
    
    /**
     * 服务对象
     */
    @Autowired
    EleDepositOrderService eleDepositOrderService;
    
    @Autowired
    FranchiseeService franchiseeService;
    
    @Autowired
    StoreService storeService;
    
    @Autowired
    UserDataScopeService userDataScopeService;
    
    //列表查询
    @GetMapping(value = "/admin/eleDepositOrder/list")
    public R queryList(@RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam(value = "franchiseeName", required = false) String franchiseeName,
            @RequestParam(value = "status", required = false) Integer status, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "orderId", required = false) String orderId, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "depositType", required = false) Integer depositType,
            @RequestParam(value = "carModel", required = false) String carModel, @RequestParam(value = "payType", required = false) Integer payType,
            @RequestParam(value = "storeName", required = false) String storeName, @RequestParam(value = "orderType", required = false) Integer orderType) {
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
            if (CollectionUtils.isEmpty(storeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        EleDepositOrderQuery eleDepositOrderQuery = EleDepositOrderQuery.builder().offset(offset).size(size).name(name).phone(phone).uid(uid).beginTime(beginTime).endTime(endTime)
                .status(status).orderId(orderId).storeIds(storeIds).tenantId(TenantContextHolder.getTenantId()).carModel(carModel).franchiseeName(franchiseeName)
                .depositType(depositType).payType(payType).storeName(storeName).franchiseeIds(franchiseeIds).orderType(orderType).build();
        return eleDepositOrderService.queryList(eleDepositOrderQuery);
    }
    
    //列表查询
    @GetMapping(value = "/admin/eleDepositOrder/queryCount")
    public R queryCount(@RequestParam(value = "status", required = false) Integer status, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "orderId", required = false) String orderId, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "depositType", required = false) Integer depositType,
            @RequestParam(value = "carModel", required = false) String carModel, @RequestParam(value = "franchiseeName", required = false) String franchiseeName,
            @RequestParam(value = "payType", required = false) Integer payType, @RequestParam(value = "storeName", required = false) String storeName,
            @RequestParam(value = "orderType", required = false) Integer orderType) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(storeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        EleDepositOrderQuery eleDepositOrderQuery = EleDepositOrderQuery.builder().name(name).phone(phone).uid(uid).beginTime(beginTime).endTime(endTime).status(status)
                .orderId(orderId).storeIds(storeIds).carModel(carModel).depositType(depositType).payType(payType).storeName(storeName).tenantId(TenantContextHolder.getTenantId())
                .franchiseeName(franchiseeName).franchiseeIds(franchiseeIds).orderType(orderType).build();
        
        return eleDepositOrderService.queryCount(eleDepositOrderQuery);
    }
    
    /**
     * 查询用户押金是否可退，以及保险信息
     *
     * @param orderId
     * @return
     */
    @GetMapping(value = "/admin/eleDepositOrder/queryDepositDetail/{orderId}")
    public R queryDepositAndInsuranceDetail(@PathVariable("orderId") String orderId) {
        
        return returnTripleResult(eleDepositOrderService.queryDepositAndInsuranceDetail(orderId));
    }
    
    /**
     * 校验订单所用支付参数是否存在
     * @param orderId 押金订单号
     * @return 1校验通过 0校验失败
     */
    @GetMapping("/admin/eleDepositOrder/checkPayParamsDetails")
    public R checkPayParamsDetails(@RequestParam("orderId") String orderId) {
        return eleDepositOrderService.checkPayParamsDetails(orderId);
    }
}
