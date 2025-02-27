package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.EleCabinetUsedRecordQuery;
import com.xiliulou.electricity.query.RentBatteryOrderQuery;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserTypeFactory;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.RentBatteryOrderVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 租车记录(TRentCarOrder)表控制层
 *
 * @author makejava
 * @since 2020-12-08 15:09:08
 */
@RestController
@Slf4j
public class JsonAdminRentBatteryOrderController {
    
    /**
     * 服务对象
     */
    @Autowired
    private RentBatteryOrderService rentBatteryOrderService;
    
    @Autowired
    UserTypeFactory userTypeFactory;
    
    @Autowired
    UserDataScopeService userDataScopeService;
    
    /**
     * 列表查询
     * @param isFreeze:1-冻结退电 0-正常退电
     */
    @GetMapping(value = "/admin/rentBatteryOrder/list")
    public R queryList(@RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "type", required = false) Integer type, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "orderId", required = false) String orderId, @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "electricityBatterySn", required = false) String electricityBatterySn,
            @RequestParam(value = "channel", required = false) String channel, @RequestParam(value = "isFreeze", required = false) Integer isFreeze) {
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
            // 保证数据安全，防止修改参数查询数据
            if (Objects.nonNull(franchiseeId)) {
                return R.ok(Collections.EMPTY_LIST);
            }
            
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            // 保证数据安全，防止修改参数查询数据
            if (Objects.nonNull(franchiseeId)) {
                return R.ok(Collections.EMPTY_LIST);
            }
            
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.springframework.util.CollectionUtils.isEmpty(storeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        RentBatteryOrderQuery rentBatteryOrderQuery = RentBatteryOrderQuery.builder().offset(offset).size(size).name(name).phone(phone).uid(uid).beginTime(beginTime)
                .endTime(endTime).status(status).orderId(orderId).type(type).franchiseeIds(franchiseeIds).storeIds(storeIds).tenantId(TenantContextHolder.getTenantId())
                .electricityCabinetId(electricityCabinetId).franchiseeId(franchiseeId).electricityBatterySn(electricityBatterySn).channel(channel).isFreeze(isFreeze).build();
        
        return rentBatteryOrderService.queryList(rentBatteryOrderQuery);
    }
    
    /**
     * 列表总数查询
     * @param isFreeze:1-冻结退电 0-正常退电
     */
    @GetMapping(value = "/admin/rentBatteryOrder/list/super")
    public R querySuperList(@RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "type", required = false) Integer type, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "isFreeze", required = false) Integer isFreeze) {
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
        
        if (user.getTenantId() != 1) {
            return R.fail("权限不足");
        }
        
        RentBatteryOrderQuery rentBatteryOrderQuery = RentBatteryOrderQuery.builder().offset(offset).size(size).name(name).phone(phone).beginTime(beginTime).endTime(endTime)
                .status(status).orderId(orderId).type(type).eleIdList(null).tenantId(null).isFreeze(isFreeze).build();
        
        return rentBatteryOrderService.queryList(rentBatteryOrderQuery);
    }
    
    /**
     * 列表总数查询
     * @param isFreeze:1-冻结退电 0-正常退电
     */
    @GetMapping(value = "/admin/rentBatteryOrder/queryCount/super")
    public R querySuperCount(@RequestParam(value = "status", required = false) String status, @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "name", required = false) String name, @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "orderId", required = false) String orderId, @RequestParam(value = "isFreeze", required = false) Integer isFreeze) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (user.getTenantId() != 1) {
            return R.fail("权限不足");
        }
    
        RentBatteryOrderQuery rentBatteryOrderQuery = RentBatteryOrderQuery.builder().name(name).phone(phone).beginTime(beginTime).endTime(endTime).status(status).orderId(orderId)
                .type(type).eleIdList(null).tenantId(null).isFreeze(isFreeze).build();
        
        return rentBatteryOrderService.queryCount(rentBatteryOrderQuery);
    }
    
    /**
     * 列表总数查询
     * @param isFreeze:1-冻结退电 0-正常退电
     */
    @GetMapping(value = "/admin/rentBatteryOrder/queryCount")
    public R queryCount(@RequestParam(value = "status", required = false) String status, @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "name", required = false) String name, @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
            @RequestParam(value = "electricityBatterySn", required = false) String electricityBatterySn,@RequestParam(value = "channel", required = false) String channel,
            @RequestParam(value = "isFreeze", required = false) Integer isFreeze) {
        
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
    
        RentBatteryOrderQuery rentBatteryOrderQuery = RentBatteryOrderQuery.builder().name(name).phone(phone).uid(uid).beginTime(beginTime).endTime(endTime).status(status)
                .orderId(orderId).type(type).franchiseeIds(franchiseeIds).storeIds(storeIds).tenantId(TenantContextHolder.getTenantId()).franchiseeId(franchiseeId)
                .electricityCabinetId(electricityCabinetId).electricityBatterySn(electricityBatterySn).channel(channel).isFreeze(isFreeze).build();
        
        return rentBatteryOrderService.queryCount(rentBatteryOrderQuery);
    }
    
    //租电池订单导出报表
    @GetMapping("/admin/rentBatteryOrder/exportExcel")
    public void exportExcel(@RequestParam(value = "status", required = false) String status, @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "name", required = false) String name, @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "orderId", required = false) String orderId, HttpServletResponse response) {
        
        Double days = (Double.valueOf(endTime - beginTime)) / 1000 / 3600 / 24;
        if (days > 33) {
            throw new CustomBusinessException("搜索日期不能大于33天");
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            throw new CustomBusinessException("查不到订单");
        }
        
        List<Integer> eleIdList = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE) || Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userDataType:{}", user.getDataType());
                throw new CustomBusinessException("查不到订单");
            }
            
            eleIdList = userTypeService.getEleIdListByUserType(user);
            if (CollectionUtils.isEmpty(eleIdList)) {
                throw new CustomBusinessException("查不到订单");
            }
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
        }
        
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
        }
        
        RentBatteryOrderQuery rentBatteryOrderQuery = RentBatteryOrderQuery.builder().name(name).phone(phone).beginTime(beginTime).endTime(endTime).status(status).orderId(orderId)
                .type(type).storeIds(storeIds).franchiseeIds(franchiseeIds).tenantId(TenantContextHolder.getTenantId()).build();
        
        rentBatteryOrderService.exportExcel(rentBatteryOrderQuery, response);
    }
    
    /**
     * 根据订单号查询订单
     *
     * @param orderId
     * @return
     */
    @GetMapping("/admin/rentBatteryOrder/queryOneByOrderId")
    public R queryOneByOrderId(@RequestParam(value = "orderId", required = true) String orderId) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderService.queryByOrderId(orderId);
        
        // 进行权限校验
        if (Objects.isNull(rentBatteryOrder) || !Objects.equals(rentBatteryOrder.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("100221", "未找到订单");
        }
        
        RentBatteryOrderVO rentBatteryOrderVO = new RentBatteryOrderVO();
        BeanUtils.copyProperties(rentBatteryOrder, rentBatteryOrderVO);
        
        return R.ok(rentBatteryOrderVO);
    }
    
    /**
     * 根据租/退电订单，或者换电订单的用户id判断对应的 单电套餐或者车店一体套餐是否是限制次数的
     *
     * @param uid
     * @return 0：不限制次数，1：限制次数
     */
    @GetMapping(value = "/admin/rentBatteryOrder/queryRentBatteryOrderLimitCountByUid")
    public R queryRentBatteryOrderLimitCountByUid(@RequestParam(value = "uid", required = true) Long uid) {
        return rentBatteryOrderService.queryRentBatteryOrderLimitCountByUid(uid);
    }
    
    //结束异常订单
    @PostMapping(value = "/admin/rentBatteryOrder/endOrder")
    public R endOrder(@RequestParam("orderId") String orderId) {
        return rentBatteryOrderService.endOrder(orderId);
    }
    
    /**
     * 电柜使用记录，（租，换，退）电池订单列表信息查询
     *
     * @param size
     * @param offset
     * @param uid
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/admin/rentBatteryOrder/usedRecords")
    public R usedRecords(@RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam(value = "id", required = true) Long id,
            @RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        EleCabinetUsedRecordQuery eleCabinetUsedRecordQuery = EleCabinetUsedRecordQuery.builder().offset(offset).size(size).id(id).uid(uid).beginTime(beginTime).endTime(endTime)
                .build();
        return R.ok(rentBatteryOrderService.findEleCabinetUsedRecords(eleCabinetUsedRecordQuery));
    }
    
    /**
     * 电柜使用记录，（租，换，退）电池订单列表总数
     *
     * @param uid
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/admin/rentBatteryOrder/usedRecordsTotalCount")
    public R usedRecordsTotalCount(@RequestParam(value = "id", required = true) Long id, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime) {
        EleCabinetUsedRecordQuery eleCabinetUsedRecordQuery = EleCabinetUsedRecordQuery.builder().id(id).uid(uid).beginTime(beginTime).endTime(endTime).build();
        return R.ok(rentBatteryOrderService.findUsedRecordsTotalCount(eleCabinetUsedRecordQuery));
    }
    
}
