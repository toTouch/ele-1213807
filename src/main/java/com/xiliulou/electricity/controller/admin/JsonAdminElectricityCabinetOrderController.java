package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.ElectricityCabinetOrderQuery;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserTypeFactory;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityCabinetOrderVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 订单表(TElectricityCabinetOrder)表控制层
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
@RestController
@Slf4j
public class JsonAdminElectricityCabinetOrderController {
    
    /**
     * 服务对象
     */
    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;
    
    @Autowired
    UserTypeFactory userTypeFactory;
    
    @Autowired
    UserDataScopeService userDataScopeService;
    
    /**
     * 分页查询换电订单列表
     */
    @GetMapping("/admin/electricityCabinetOrder/list")
    public R queryList(@RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "eid", required = false) Long eid, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "source", required = false) Integer source,
            @RequestParam(value = "paymentMethod", required = false) Integer paymentMethod,
            @RequestParam(value = "electricityCabinetName", required = false) String electricityCabinetName, @RequestParam(value = "oldCellNo", required = false) Integer oldCellNo,
            @RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "batterySn", required = false) String batterySn, @RequestParam(value = "oldElectricityBatterySn", required = false) String oldElectricityBatterySn,
            @RequestParam(value = "newElectricityBatterySn", required = false) String newElectricityBatterySn,@RequestParam(value = "channel" , required = false) String channel) {
        
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
        
        // 加盟商后台账户会绑定加盟商个体作为数据类型，查询数据需要查询出账号绑定的加盟商个体，以加盟商个体ID查询
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
        
        // 门店后台账户会绑定门店个体作为数据类型，同上
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
        
        ElectricityCabinetOrderQuery electricityCabinetOrderQuery = ElectricityCabinetOrderQuery.builder().offset(offset).size(size).orderId(orderId).phone(phone).status(status)
                .eid(eid).beginTime(beginTime).endTime(endTime).paymentMethod(paymentMethod).franchiseeIds(franchiseeIds).storeIds(storeIds).source(source)
                .electricityCabinetName(electricityCabinetName).oldCellNo(oldCellNo).uid(uid).tenantId(TenantContextHolder.getTenantId())
                .batterySn(batterySn).oldElectricityBatterySn(oldElectricityBatterySn).newElectricityBatterySn(newElectricityBatterySn)
                .franchiseeId(franchiseeId).channel(channel).build();
        return electricityCabinetOrderService.queryList(electricityCabinetOrderQuery);
    }
    
    
    @GetMapping("/admin/electricityCabinetOrder/list/super")
    public R querySuperList(@RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "eid", required = false) Long eid, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "source", required = false) Integer source,
            @RequestParam(value = "paymentMethod", required = false) Integer paymentMethod,
            @RequestParam(value = "electricityCabinetName", required = false) String electricityCabinetName, @RequestParam(value = "oldCellNo", required = false) Integer oldCellNo,
            @RequestParam(value = "uid", required = false) Long uid) {
        
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (user.getTenantId() != 1) {
            return R.fail("权限不足");
        }
        
        ElectricityCabinetOrderQuery electricityCabinetOrderQuery = ElectricityCabinetOrderQuery.builder().offset(offset).size(size).orderId(orderId).phone(phone).status(status)
                .eid(eid).beginTime(beginTime).endTime(endTime).paymentMethod(paymentMethod).eleIdList(null).source(source).electricityCabinetName(electricityCabinetName)
                .oldCellNo(oldCellNo).uid(uid).tenantId(null).build();
        return electricityCabinetOrderService.queryList(electricityCabinetOrderQuery);
    }
    
    /**
     * 根据订单号查询订单
     *
     * @param orderId
     * @return
     */
    @GetMapping("/admin/electricityCabinetOrder/queryOneByOrderId")
    public R queryOneByOrderId(@RequestParam(value = "orderId", required = true) String orderId) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderService.queryByOrderId(orderId);
        
        // 进行权限校验
        if (Objects.isNull(electricityCabinetOrder) || !Objects.equals(electricityCabinetOrder.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("100221", "未找到订单");
        }
        
        ElectricityCabinetOrderVO electricityCabinetOrderVO = new ElectricityCabinetOrderVO();
        BeanUtils.copyProperties(electricityCabinetOrder, electricityCabinetOrderVO);
        
        return R.ok(electricityCabinetOrderVO);
    }
    
    //换电柜订单查询
    @GetMapping("/admin/electricityCabinetOrder/queryCount")
    public R queryCount(@RequestParam(value = "orderId", required = false) String orderId, @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "status", required = false) String status, @RequestParam(value = "eid", required = false) Long eid,
            @RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "source", required = false) Integer source, @RequestParam(value = "paymentMethod", required = false) Integer paymentMethod,
            @RequestParam(value = "electricityCabinetName", required = false) String electricityCabinetName, @RequestParam(value = "oldCellNo", required = false) Integer oldCellNo,
            @RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "batterySn", required = false) String batterySn, @RequestParam(value = "oldElectricityBatterySn", required = false) String oldElectricityBatterySn,
            @RequestParam(value = "newElectricityBatterySn", required = false) String newElectricityBatterySn,@RequestParam(value = "channel" , required = false) String channel) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
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
        
        ElectricityCabinetOrderQuery electricityCabinetOrderQuery = ElectricityCabinetOrderQuery.builder().orderId(orderId).phone(phone).status(status).eid(eid)
                .beginTime(beginTime).endTime(endTime).paymentMethod(paymentMethod).franchiseeIds(franchiseeIds).storeIds(storeIds).source(source)
                .electricityCabinetName(electricityCabinetName).oldCellNo(oldCellNo).uid(uid).tenantId(TenantContextHolder.getTenantId())
                .franchiseeId(franchiseeId).batterySn(batterySn).oldElectricityBatterySn(oldElectricityBatterySn).newElectricityBatterySn(newElectricityBatterySn)
                .channel(channel).build();
        return electricityCabinetOrderService.queryCount(electricityCabinetOrderQuery);
    }
    
    @GetMapping("/admin/electricityCabinetOrder/queryCount/super")
    public R querySuperCount(@RequestParam(value = "orderId", required = false) String orderId, @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "status", required = false) String status, @RequestParam(value = "eid", required = false) Long eid,
            @RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "source", required = false) Integer source, @RequestParam(value = "paymentMethod", required = false) Integer paymentMethod,
            @RequestParam(value = "electricityCabinetName", required = false) String electricityCabinetName, @RequestParam(value = "oldCellNo", required = false) Integer oldCellNo,
            @RequestParam(value = "uid", required = false) Long uid) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (user.getTenantId() != 1) {
            return R.fail("权限不足");
        }
        
        ElectricityCabinetOrderQuery electricityCabinetOrderQuery = ElectricityCabinetOrderQuery.builder().orderId(orderId).phone(phone).status(status).eid(eid)
                .beginTime(beginTime).endTime(endTime).paymentMethod(paymentMethod).eleIdList(null).source(source).electricityCabinetName(electricityCabinetName)
                .oldCellNo(oldCellNo).uid(uid).tenantId(null).build();
        return electricityCabinetOrderService.queryCount(electricityCabinetOrderQuery);
    }
    
    //结束异常订单
    @PutMapping(value = "/admin/electricityCabinetOrder/endOrder")
    public R endOrder(@RequestParam("orderId") String orderId) {
        return electricityCabinetOrderService.endOrder(orderId);
    }
}
