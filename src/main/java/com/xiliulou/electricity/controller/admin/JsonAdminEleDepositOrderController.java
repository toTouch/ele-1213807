package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.BatteryDepositAdd;
import com.xiliulou.electricity.query.EleDepositOrderQuery;
import com.xiliulou.electricity.query.RentCarDepositAdd;
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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
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
public class JsonAdminEleDepositOrderController {
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
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "franchiseeName", required = false) String franchiseeName,
                       @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "orderId", required = false) String orderId,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime,
                       @RequestParam(value = "depositType", required = false) Integer depositType,
                       @RequestParam(value = "carModel", required = false) String carModel,
        @RequestParam(value = "payType", required = false) Integer payType,
        @RequestParam(value = "storeName", required = false) String storeName) {
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

        //隔离门店租车数据
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            depositType = EleDepositOrder.RENT_CAR_DEPOSIT;
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
        }

        EleDepositOrderQuery eleDepositOrderQuery = EleDepositOrderQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .phone(phone)
                .beginTime(beginTime)
                .endTime(endTime)
                .status(status)
                .orderId(orderId)
                .storeIds(storeIds)
                .tenantId(TenantContextHolder.getTenantId())
                .carModel(carModel)
                .franchiseeName(franchiseeName)
                .depositType(depositType)
                .payType(payType)
                .franchiseeIds(franchiseeIds).build();
        return eleDepositOrderService.queryList(eleDepositOrderQuery);
    }

    //列表查询
    @GetMapping(value = "/admin/eleDepositOrder/queryCount")
    public R queryCount(@RequestParam(value = "status", required = false) Integer status,
                        @RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "orderId", required = false) String orderId,
                        @RequestParam(value = "beginTime", required = false) Long beginTime,
                        @RequestParam(value = "endTime", required = false) Long endTime,
                        @RequestParam(value = "depositType", required = false) Integer depositType,
                        @RequestParam(value = "carModel", required = false) String carModel,
                        @RequestParam(value = "franchiseeName", required = false) String franchiseeName,
        @RequestParam(value = "payType", required = false) Integer payType,
        @RequestParam(value = "storeName", required = false) String storeName) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

//        //隔离门店租车数据
//        Long storeId = null;
//        if (Objects.equals(user.getType(), User.TYPE_USER_STORE)) {
//            depositType = EleDepositOrder.RENT_CAR_DEPOSIT;
//            Store store = storeService.queryByUid(user.getUid());
//            if (Objects.nonNull(store)) {
//                storeId = store.getId();
//            }
//        }
//
//        Long franchiseeId = null;
//        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
//                && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)
//                && !Objects.equals(user.getType(),User.TYPE_USER_STORE)) {
//            //加盟商
//            Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
//            if (Objects.nonNull(franchisee)) {
//                franchiseeId = franchisee.getId();
//            }
//        }
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            depositType = EleDepositOrder.RENT_CAR_DEPOSIT;
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {

            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
        }

        EleDepositOrderQuery eleDepositOrderQuery = EleDepositOrderQuery.builder()
                .name(name)
                .phone(phone)
                .beginTime(beginTime)
                .endTime(endTime)
                .status(status)
                .orderId(orderId)
                .storeIds(storeIds)
                .carModel(carModel)
                .depositType(depositType)
                .payType(payType)
                .tenantId(TenantContextHolder.getTenantId())
                .franchiseeName(franchiseeName)
                .franchiseeIds(franchiseeIds).build();

        return eleDepositOrderService.queryCount(eleDepositOrderQuery);
    }

    //押金订单导出报表
    @GetMapping("/admin/eleDepositOrder/exportExcel")
    public void exportExcel(@RequestParam(value = "status", required = false) Integer status,
                            @RequestParam(value = "name", required = false) String name,
                            @RequestParam(value = "phone", required = false) String phone,
                            @RequestParam(value = "orderId", required = false) String orderId,
                            @RequestParam(value = "beginTime", required = false) Long beginTime,
                            @RequestParam(value = "endTime", required = false) Long endTime, HttpServletResponse response) {

        Double days = (Double.valueOf(endTime - beginTime)) / 1000 / 3600 / 24;
        if (days > 33) {
            throw new CustomBusinessException("搜索日期不能大于33天");
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            throw new CustomBusinessException("查不到订单");
        }
    
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
        
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
        }
        
        EleDepositOrderQuery eleDepositOrderQuery = EleDepositOrderQuery.builder()
                .name(name)
                .phone(phone)
                .beginTime(beginTime)
                .endTime(endTime)
                .status(status)
                .orderId(orderId)
                .storeIds(storeIds)
                .tenantId(TenantContextHolder.getTenantId())
                .franchiseeIds(franchiseeIds).build();
        eleDepositOrderService.exportExcel(eleDepositOrderQuery, response);
    }

    //缴纳电池押金
    @PostMapping(value = "/admin/eleDepositOrder/batteryDeposit")
    public R batteryDeposit(@RequestBody @Validated(value = CreateGroup.class) BatteryDepositAdd batteryDepositAdd) {
        return eleDepositOrderService.adminPayBatteryDeposit(batteryDepositAdd);
    }

    //缴纳租车押金
    @PostMapping(value = "/admin/eleDepositOrder/rentCarDeposit")
    public R rentCarDeposit(@RequestBody @Validated(value = CreateGroup.class) RentCarDepositAdd rentCarDepositAdd) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        rentCarDepositAdd.setTenantId(tenantId);

        return eleDepositOrderService.adminPayRentCarDeposit(rentCarDepositAdd);

    }


}
