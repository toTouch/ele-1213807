package com.xiliulou.electricity.controller.admin.supper;

import cn.hutool.core.lang.Pair;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.query.MemberCardOrderQuery;
import com.xiliulou.electricity.query.supper.DelBatteryReq;
import com.xiliulou.electricity.query.supper.UserGrantSourceReq;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.supper.AdminSupperService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.supper.DelBatteryVo;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * @author: Ant
 * @Date 2024/4/22
 * @Description:
 **/
@Slf4j
@RestController
@RequestMapping("/super/admin")
public class JsonAdminSupperController {
    
    @Resource
    private AdminSupperService adminSupperService;
    
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    
    
    /**
     * 根据电池SN删除电池
     * @param delBatteryReq 删除电池请求体
     * @return R<DelBatteryVo>
     */
    @PostMapping("/delBatterys")
    public R<DelBatteryVo> delBatterys(@RequestBody DelBatteryReq delBatteryReq) {
        Pair<List<String>, List<String>> pair = adminSupperService.delBatteryBySnList(delBatteryReq.getTenantId(), delBatteryReq.getBatterySnList(), delBatteryReq.getViolentDel());
        DelBatteryVo delBatteryVo = new DelBatteryVo();
        delBatteryVo.setSuccessSnList(pair.getKey());
        delBatteryVo.setFailedSnList(pair.getValue());
        return R.ok(delBatteryVo);
    }
    
    @PostMapping("/grantPermission")
    public R<?> grantPermission(@RequestBody UserGrantSourceReq userGrantSourceReq) {
        adminSupperService.grantPermission(userGrantSourceReq);
        return R.ok();
    }
    
    /**
     * 套餐购买记录查询列表
     */
    @GetMapping("/super/admin/electricityMemberCardOrder/page")
    public R getElectricityMemberCardPage(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "orderId", required = false) String orderId, @RequestParam(value = "memberCardType", required = false) Integer cardType,
            @RequestParam(value = "memberCardModel", required = false) Integer memberCardModel, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "useStatus", required = false) Integer useStatus, @RequestParam(value = "source", required = false) Integer source,
            @RequestParam(value = "payCount", required = false) Integer payCount, @RequestParam(value = "refId", required = false) Long refId,
            @RequestParam(value = "queryStartTime", required = false) Long queryStartTime, @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "queryEndTime", required = false) Long queryEndTime,
            @RequestParam(value = "payType", required = false) Integer payType, @RequestParam(value = "cardId", required = false) Long cardId,
            @RequestParam(value = "tenantId", required = false) Integer tenantId) {
        
        if (Objects.isNull(size) || size < 0 || size > 50) {
            size = 10L;
        }
        
        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!SecurityUtils.isAdmin()) {
            return R.ok(Collections.emptyList());
        }
        
        MemberCardOrderQuery memberCardOrderQuery = MemberCardOrderQuery.builder().payType(payType).phone(phone).orderId(orderId).cardType(cardType).queryStartTime(queryStartTime)
                .queryEndTime(queryEndTime).offset(offset).size(size).tenantId(tenantId).status(status).uid(uid).useStatus(useStatus).source(source)
                .payType(payType).refId(refId).cardModel(memberCardModel).franchiseeId(franchiseeId).franchiseeIds(null).storeIds(null).cardPayCount(payCount)
                .userName(userName).payType(payType).cardId(cardId).build();
        
        return electricityMemberCardOrderService.listSuperAdminPage(memberCardOrderQuery);
    }
    
    /**
     * 套餐购买记录查询列表数
     */
    @GetMapping("/super/admin/electricityMemberCardOrder/queryCount")
    public R queryCount(@RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "memberCardType", required = false) Integer cardType, @RequestParam(value = "memberCardModel", required = false) Integer memberCardModel,
            @RequestParam(value = "status", required = false) Integer status, @RequestParam(value = "useStatus", required = false) Integer useStatus,
            @RequestParam(value = "payCount", required = false) Integer payCount, @RequestParam(value = "source", required = false) Integer source,
            @RequestParam(value = "refId", required = false) Long refId, @RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
            @RequestParam(value = "queryEndTime", required = false) Long queryEndTime, @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "payType", required = false) Integer payType, @RequestParam(value = "cardId", required = false) Long cardId,
            @RequestParam(value = "tenantId", required = false) Integer tenantId) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!SecurityUtils.isAdmin()) {
            return R.ok(NumberConstant.ZERO);
        }
        
        MemberCardOrderQuery memberCardOrderQuery = MemberCardOrderQuery.builder().payType(payType).phone(phone).orderId(orderId).cardType(cardType).queryStartTime(queryStartTime)
                .queryEndTime(queryEndTime).tenantId(TenantContextHolder.getTenantId()).status(status).uid(uid).useStatus(useStatus).source(source).payType(payType).refId(refId)
                .cardModel(memberCardModel).franchiseeId(franchiseeId).franchiseeIds(null).storeIds(null).cardPayCount(payCount).userName(userName).payType(payType).tenantId(tenantId)
                .cardId(cardId).build();
        
        return electricityMemberCardOrderService.queryCount(memberCardOrderQuery);
    }
    
    /**
     * 电柜列表查询
     */
    @GetMapping(value = "/super/admin/electricityCabinet/list")
    public R queryListSuper(@RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "address", required = false) String address, @RequestParam(value = "usableStatus", required = false) Integer usableStatus,
            @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus, @RequestParam(value = "stockStatus", required = false) Integer stockStatus,
            @RequestParam(value = "warehouseId", required = false) Long warehouseId, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "areaId", required = false) Long areaId, @RequestParam(value = "tenantId", required = false) Integer tenantId) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!SecurityUtils.isAdmin()) {
            return R.ok(Collections.emptyList());
        }
        
        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder().offset(offset).size(size).areaId(areaId).name(name).address(address).tenantId(tenantId)
                .usableStatus(usableStatus).stockStatus(stockStatus).warehouseId(warehouseId).onlineStatus(onlineStatus).beginTime(beginTime).endTime(endTime).eleIdList(null)
                .id(id).tenantId(null).build();
        
        return electricityCabinetService.listSuperAdminPage(electricityCabinetQuery);
    }
    
    //电柜列表数量查询
    @GetMapping(value = "/super/admin/electricityCabinet/queryCount")
    public R queryCountSuper(@RequestParam(value = "name", required = false) String name, @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "usableStatus", required = false) Integer usableStatus, @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus,
            @RequestParam(value = "stockStatus", required = false) Integer stockStatus, @RequestParam(value = "warehouseId", required = false) Long warehouseId,
            @RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "areaId", required = false) Long areaId, @RequestParam(value = "tenantId", required = false) Integer tenantId) {
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!SecurityUtils.isAdmin()) {
            return R.ok(NumberConstant.ZERO);
        }
        
        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder().name(name).address(address).areaId(areaId).usableStatus(usableStatus).tenantId(tenantId)
                .onlineStatus(onlineStatus).stockStatus(stockStatus).warehouseId(warehouseId).beginTime(beginTime).endTime(endTime).eleIdList(null).build();
        
        return electricityCabinetService.queryCount(electricityCabinetQuery);
    }
    
    
}
