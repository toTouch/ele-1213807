package com.xiliulou.electricity.controller.admin.supper;

import cn.hutool.core.lang.Pair;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.BatteryMembercardRefundOrderQuery;
import com.xiliulou.electricity.query.BatteryServiceFeeQuery;
import com.xiliulou.electricity.query.EleDepositOrderQuery;
import com.xiliulou.electricity.query.EleRefundQuery;
import com.xiliulou.electricity.query.ElectricityCabinetOrderQuery;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.query.ElectricityMemberCardRecordQuery;
import com.xiliulou.electricity.query.EnableMemberCardRecordQuery;
import com.xiliulou.electricity.query.MemberCardOrderQuery;
import com.xiliulou.electricity.query.RentBatteryOrderQuery;
import com.xiliulou.electricity.query.supper.DelBatteryReq;
import com.xiliulou.electricity.query.supper.UserGrantSourceReq;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.EleBatteryServiceFeeOrderService;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleDisableMemberCardRecordService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.EnableMemberCardRecordService;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import com.xiliulou.electricity.service.supper.AdminSupperService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.supper.DelBatteryVo;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/admin/inner/super")
public class JsonAdminSupperController {
    
    @Resource
    private AdminSupperService adminSupperService;
    
    @Resource
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Resource
    ElectricityCabinetService electricityCabinetService;
    
    @Resource
    private BatteryMemberCardService batteryMemberCardService;
    
    @Resource
    ElectricityCabinetOrderService electricityCabinetOrderService;
    
    @Resource
    private RentBatteryOrderService rentBatteryOrderService;
    
    @Resource
    private BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;
    
    @Resource
    private EleRefundOrderService eleRefundOrderService;
    
    @Resource
    private EleDisableMemberCardRecordService eleDisableMemberCardRecordService;
    
    @Resource
    private EnableMemberCardRecordService enableMemberCardRecordService;
    
    @Resource
    private EleDepositOrderService eleDepositOrderService;
    
    @Resource
    private EleBatteryServiceFeeOrderService eleBatteryServiceFeeOrderService;
    
    
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
    @GetMapping("/electricityMemberCardOrder/page")
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
    @GetMapping("/electricityMemberCardOrder/queryCount")
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
                .queryEndTime(queryEndTime).status(status).uid(uid).useStatus(useStatus).source(source).payType(payType).refId(refId)
                .cardModel(memberCardModel).franchiseeId(franchiseeId).franchiseeIds(null).storeIds(null).cardPayCount(payCount).userName(userName).payType(payType).tenantId(tenantId)
                .cardId(cardId).build();
        
        return electricityMemberCardOrderService.queryCount(memberCardOrderQuery);
    }
    
    /**
     * 电柜列表查询
     */
    @GetMapping(value = "/electricityCabinet/list")
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
                .id(id).build();
        
        return electricityCabinetService.listSuperAdminPage(electricityCabinetQuery);
    }
    
    /**
     * 电柜列表数量查询
     */
    @GetMapping(value = "/electricityCabinet/queryCount")
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
    
    /**
     * 套餐配置列表
     */
    @GetMapping("/battery/memberCard/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "mid", required = false) Long mid, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "rentType", required = false) Integer rentType, @RequestParam(value = "rentUnit", required = false) Integer rentUnit,
            @RequestParam(value = "businessType", required = false) Integer businessType, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "batteryModel", required = false) String batteryModel, @RequestParam(value = "userGroupId", required = false) Long userGroupId
            , @RequestParam(value = "tenantId", required = false) Integer tenantId) {
        
        if (Objects.nonNull(rentType) && Objects.nonNull(userGroupId)) {
            return R.ok(Collections.emptyList());
        }
        
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
    
        if (!SecurityUtils.isAdmin()) {
            return R.ok(Collections.emptyList());
        }
        
        BatteryMemberCardQuery query = BatteryMemberCardQuery.builder().size(size).offset(offset).tenantId(tenantId).id(mid).franchiseeId(franchiseeId)
                .status(status).businessType(businessType == null ? 0 : businessType).rentType(rentType).rentUnit(rentUnit).name(name).delFlag(BatteryMemberCard.DEL_NORMAL)
                .franchiseeIds(null).batteryModel(batteryModel).userInfoGroupId(Objects.nonNull(userGroupId) ? userGroupId.toString() : null).build();
        
        return R.ok(batteryMemberCardService.listSuperAdminPage(query));
    }
    
    /**
     * 套餐配置列表总数
     */
    @GetMapping("/battery/memberCard/queryCount")
    public R pageCount(@RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "rentType", required = false) Integer rentType, @RequestParam(value = "rentUnit", required = false) Integer rentUnit,
            @RequestParam(value = "businessType", required = false) Integer businessType, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "batteryModel", required = false) String batteryModel, @RequestParam(value = "userGroupId", required = false) Long userGroupId
            , @RequestParam(value = "tenantId", required = false) Integer tenantId) {
        
        if (Objects.nonNull(rentType) && Objects.nonNull(userGroupId)) {
            return R.ok(Collections.emptyList());
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!SecurityUtils.isAdmin()) {
            return R.ok(NumberConstant.ZERO);
        }
        
        BatteryMemberCardQuery query = BatteryMemberCardQuery.builder().franchiseeId(franchiseeId).status(status).businessType(businessType == null ? 0 : businessType)
                .rentType(rentType).rentUnit(rentUnit).name(name).tenantId(tenantId).delFlag(BatteryMemberCard.DEL_NORMAL).franchiseeIds(null)
                .batteryModel(batteryModel).userInfoGroupId(Objects.nonNull(userGroupId) ? userGroupId.toString() : null).build();
        
        return R.ok(batteryMemberCardService.selectByPageCount(query));
    }
    
    /**
     * 换电订单列表
     */
    @GetMapping("/electricityCabinetOrder/list")
    public R querySuperList(@RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "eid", required = false) Long eid, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "source", required = false) Integer source,
            @RequestParam(value = "paymentMethod", required = false) Integer paymentMethod,
            @RequestParam(value = "electricityCabinetName", required = false) String electricityCabinetName, @RequestParam(value = "oldCellNo", required = false) Integer oldCellNo,
            @RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "tenantId", required = false) Integer tenantId) {
        
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
    
        if (!SecurityUtils.isAdmin()) {
            return R.ok(Collections.emptyList());
        }
        
        ElectricityCabinetOrderQuery electricityCabinetOrderQuery = ElectricityCabinetOrderQuery.builder().offset(offset).size(size).orderId(orderId).phone(phone).status(status)
                .eid(eid).beginTime(beginTime).endTime(endTime).paymentMethod(paymentMethod).eleIdList(null).source(source).electricityCabinetName(electricityCabinetName)
                .oldCellNo(oldCellNo).uid(uid).tenantId(tenantId).build();
        return electricityCabinetOrderService.listSuperAdminPage(electricityCabinetOrderQuery);
    }
    
    /**
     * 换电订单列表总数
     */
    @GetMapping("/electricityCabinetOrder/queryCount")
    public R querySuperCount(@RequestParam(value = "orderId", required = false) String orderId, @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "status", required = false) String status, @RequestParam(value = "eid", required = false) Long eid,
            @RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "source", required = false) Integer source, @RequestParam(value = "paymentMethod", required = false) Integer paymentMethod,
            @RequestParam(value = "electricityCabinetName", required = false) String electricityCabinetName, @RequestParam(value = "oldCellNo", required = false) Integer oldCellNo,
            @RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "tenantId", required = false) Integer tenantId) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!SecurityUtils.isAdmin()) {
            return R.ok(NumberConstant.ZERO);
        }
        
        ElectricityCabinetOrderQuery electricityCabinetOrderQuery = ElectricityCabinetOrderQuery.builder().orderId(orderId).phone(phone).status(status).eid(eid)
                .beginTime(beginTime).endTime(endTime).paymentMethod(paymentMethod).eleIdList(null).source(source).electricityCabinetName(electricityCabinetName)
                .oldCellNo(oldCellNo).uid(uid).tenantId(tenantId).build();
        return electricityCabinetOrderService.queryCount(electricityCabinetOrderQuery);
    }
    
    /**
     * 租退电订单列表
     */
    @GetMapping(value = "/rentBatteryOrder/list")
    public R querySuperList(@RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "type", required = false) Integer type, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "tenantId", required = false) Integer tenantId) {
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
    
        if (!SecurityUtils.isAdmin()) {
            return R.ok(Collections.emptyList());
        }
        
        RentBatteryOrderQuery rentBatteryOrderQuery = RentBatteryOrderQuery.builder().offset(offset).size(size).name(name).phone(phone).beginTime(beginTime).endTime(endTime)
                .status(status).orderId(orderId).type(type).eleIdList(null).tenantId(tenantId).build();
        
        return rentBatteryOrderService.listSuperAdminPage(rentBatteryOrderQuery);
    }
    
    /**
     * 租退电订单列表
     */
    @GetMapping(value = "/rentBatteryOrder/queryCount")
    public R querySuperCount(@RequestParam(value = "status", required = false) String status, @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "name", required = false) String name, @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "orderId", required = false) String orderId, @RequestParam(value = "tenantId", required = false) Integer tenantId) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!SecurityUtils.isAdmin()) {
            return R.ok(NumberConstant.ZERO);
        }
        
        RentBatteryOrderQuery rentBatteryOrderQuery = RentBatteryOrderQuery.builder().name(name).phone(phone).beginTime(beginTime).endTime(endTime).status(status).orderId(orderId)
                .type(type).eleIdList(null).tenantId(tenantId).build();
        
        return rentBatteryOrderService.queryCount(rentBatteryOrderQuery);
    }
    
    /**
     * 租金退款审核列表
     */
    @GetMapping("/battery/membercard/refund/page")
    public R getElectricityMemberCardPage(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "refundOrderNo", required = false) String refundOrderNo,
            @RequestParam(value = "rentType", required = false) Integer rentType, @RequestParam(value = "mid", required = false) Long mid,
            @RequestParam(value = "status", required = false) Integer status, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "payType", required = false) Integer payType,
            @RequestParam(value = "tenantId", required = false) Integer tenantId) {
        
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
    
        if (!SecurityUtils.isAdmin()) {
            return R.ok(Collections.emptyList());
        }
        
        BatteryMembercardRefundOrderQuery query = BatteryMembercardRefundOrderQuery.builder().uid(uid).phone(phone).refundOrderNo(refundOrderNo).startTime(beginTime)
                .endTime(endTime).offset(offset).size(size).rentType(rentType).tenantId(tenantId).status(status).franchiseeIds(null)
                .storeIds(null).mid(mid).payType(payType).build();
        
        return R.ok(batteryMembercardRefundOrderService.listSuperAdminPage(query));
    }
    
    /**
     * 租金退款审核列表总数
     */
    @GetMapping("/battery/membercard/refund/queryCount")
    public R queryCount(@RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "refundOrderNo", required = false) String refundOrderNo, @RequestParam(value = "rentType", required = false) Integer rentType,
            @RequestParam(value = "mid", required = false) Long mid, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "payType", required = false) Integer payType, @RequestParam(value = "tenantId", required = false) Integer tenantId) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!SecurityUtils.isAdmin()) {
            return R.ok(NumberConstant.ZERO);
        }
        
        BatteryMembercardRefundOrderQuery query = BatteryMembercardRefundOrderQuery.builder().uid(uid).phone(phone).refundOrderNo(refundOrderNo).startTime(beginTime)
                .endTime(endTime).tenantId(tenantId).status(status).rentType(rentType).payType(payType).franchiseeIds(null).storeIds(null)
                .mid(mid).build();
        
        return R.ok(batteryMembercardRefundOrderService.selectPageCount(query));
    }
    
    /**
     * 退押审核列表
     */
    @GetMapping("/eleRefundOrder/queryList")
    public R queryList(@RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam(value = "franchiseeName", required = false) String franchiseeName,
            @RequestParam(value = "status", required = false) Integer status, @RequestParam(value = "payType", required = false) Integer payType,
            @RequestParam(value = "refundOrderType", required = false) Integer refundOrderType, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "orderId", required = false) String orderId, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "orderType", required = false) Integer orderType,
            @RequestParam(value = "refundOrderNo", required = false) String refundOrderNo, @RequestParam(value = "tenantId", required = false) Integer tenantId) {
        
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!SecurityUtils.isAdmin()) {
            return R.ok(Collections.emptyList());
        }
    
        EleRefundQuery eleRefundQuery = EleRefundQuery.builder().offset(offset).size(size).orderId(orderId).status(status).beginTime(beginTime).endTime(endTime)
                .tenantId(tenantId).storeIds(null).franchiseeIds(null).phone(phone).uid(uid).payType(payType).refundOrderType(refundOrderType)
                .name(name).orderType(orderType).refundOrderNo(refundOrderNo).build();
        
        
        return eleRefundOrderService.listSuperAdminPage(eleRefundQuery);
    }
    
    /**
     * 退押审核列表总数
     */
    @GetMapping("/eleRefundOrder/queryCount")
    public R queryCount(@RequestParam(value = "orderId", required = false) String orderId, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "payType", required = false) Integer payType, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "refundOrderType", required = false) Integer refundOrderType, @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "orderType", required = false) Integer orderType, @RequestParam(value = "refundOrderNo", required = false) String refundOrderNo,
            @RequestParam(value = "tenantId", required = false) Integer tenantId) {
        
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!SecurityUtils.isAdmin()) {
            return R.ok(NumberConstant.ZERO);
        }
        
        EleRefundQuery eleRefundQuery = EleRefundQuery.builder().orderId(orderId).status(status).storeIds(null).franchiseeIds(null).payType(payType)
                .refundOrderType(refundOrderType).beginTime(beginTime).endTime(endTime).tenantId(tenantId).phone(phone).uid(uid).orderType(orderType)
                .refundOrderNo(refundOrderNo).build();
        
        return eleRefundOrderService.queryCount(eleRefundQuery);
    }
    
    /**
     * 冻结套餐审核列表
     */
    @GetMapping(value = "/electricityMemberCard/disableMemberCard")
    public R getElectricityDisableMemberCardList(@RequestParam(value = "offset") Long offset, @RequestParam(value = "size") Long size,
            @RequestParam(value = "disableMemberCardNo", required = false) String disableMemberCardNo, @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "status", required = false) Integer status, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "tenantId", required = false) Integer tenantId) {
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
    
        if (!SecurityUtils.isAdmin()) {
            return R.ok(Collections.emptyList());
        }
        
        ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery = ElectricityMemberCardRecordQuery.builder().offset(offset).size(size).franchiseeIds(null)
                .storeIds(null).disableMemberCardNo(disableMemberCardNo).phone(phone).status(status).uid(uid).beginTime(beginTime).endTime(endTime)
                .tenantId(tenantId).build();
        
        return eleDisableMemberCardRecordService.listSuperAdminPage(electricityMemberCardRecordQuery);
    }
    
    /**
     * 冻结套餐审核列表总数
     */
    @GetMapping(value = "/electricityMemberCard/disableMemberCardCount")
    public R getElectricityDisableMemberCardCount(@RequestParam(value = "disableMemberCardNo", required = false) String disableMemberCardNo,
            @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "tenantId", required = false) Integer tenantId) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!SecurityUtils.isAdmin()) {
            return R.ok(NumberConstant.ZERO);
        }
        
        ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery = ElectricityMemberCardRecordQuery.builder().disableMemberCardNo(disableMemberCardNo).phone(phone)
                .status(status).uid(uid).franchiseeIds(null).storeIds(null).beginTime(beginTime).endTime(endTime).tenantId(tenantId).build();
        
        return eleDisableMemberCardRecordService.queryCount(electricityMemberCardRecordQuery);
    }
    
    /**
     * 套餐冻结记录列表
     */
    @GetMapping(value = "/enableMemberCardRecord/list")
    public R queryList(@RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "enableType", required = false) Integer enableType, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "disableMemberCardNo", required = false) String disableMemberCardNo,
            @RequestParam(value = "beginDisableTime", required = false) Long beginDisableTime, @RequestParam(value = "endDisableTime", required = false) Long endDisableTime,
            @RequestParam(value = "tenantId", required = false) Integer tenantId) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        // 用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!SecurityUtils.isAdmin()) {
            return R.ok(Collections.emptyList());
        }
        
        EnableMemberCardRecordQuery enableMemberCardRecordQuery = EnableMemberCardRecordQuery.builder().enableType(enableType).beginTime(beginTime).endTime(endTime).offset(offset)
                .size(size).phone(phone).franchiseeIds(null).storeIds(null).userName(userName).uid(uid).tenantId(tenantId).disableMemberCardNo(disableMemberCardNo)
                .beginDisableTime(beginDisableTime).endDisableTime(endDisableTime).build();
        
        return enableMemberCardRecordService.listSuperAdminPage(enableMemberCardRecordQuery);
    }
    
    /**
     * 套餐冻结记录列表总数
     */
    @GetMapping(value = "/enableMemberCardRecord/queryCount")
    public R queryCount(@RequestParam(value = "userName", required = false) String userName, @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "enableType", required = false) Integer enableType,
            @RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "id", required = false) Integer id, @RequestParam(value = "disableMemberCardNo", required = false) String disableMemberCardNo,
            @RequestParam(value = "beginDisableTime", required = false) Long beginDisableTime, @RequestParam(value = "endDisableTime", required = false) Long endDisableTime,
            @RequestParam(value = "tenantId", required = false) Integer tenantId) {
        
        // 用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!SecurityUtils.isAdmin()) {
            return R.ok(NumberConstant.ZERO);
        }
        
        EnableMemberCardRecordQuery enableMemberCardRecordQuery = EnableMemberCardRecordQuery.builder().enableType(enableType).beginTime(beginTime).endTime(endTime).phone(phone)
                .franchiseeIds(null).storeIds(null).userName(userName).uid(uid).tenantId(tenantId).disableMemberCardNo(disableMemberCardNo)
                .beginDisableTime(beginDisableTime).endDisableTime(endDisableTime).build();
        
        return enableMemberCardRecordService.queryCount(enableMemberCardRecordQuery);
    }
    
    /**
     * 押金缴纳列表
     */
    @GetMapping(value = "/eleDepositOrder/list")
    public R queryList(@RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam(value = "franchiseeName", required = false) String franchiseeName,
            @RequestParam(value = "status", required = false) Integer status, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "orderId", required = false) String orderId, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "depositType", required = false) Integer depositType,
            @RequestParam(value = "carModel", required = false) String carModel, @RequestParam(value = "payType", required = false) Integer payType,
            @RequestParam(value = "storeName", required = false) String storeName, @RequestParam(value = "orderType", required = false) Integer orderType,
            @RequestParam(value = "tenantId", required = false) Integer tenantId) {
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
    
        if (!SecurityUtils.isAdmin()) {
            return R.ok(Collections.emptyList());
        }
        
        EleDepositOrderQuery eleDepositOrderQuery = EleDepositOrderQuery.builder().offset(offset).size(size).name(name).phone(phone).uid(uid).beginTime(beginTime).endTime(endTime)
                .status(status).orderId(orderId).storeIds(null).tenantId(tenantId).carModel(carModel).franchiseeName(franchiseeName)
                .depositType(depositType).payType(payType).storeName(storeName).franchiseeIds(null).orderType(orderType).build();
        
        return eleDepositOrderService.listSuperAdminPage(eleDepositOrderQuery);
    }
    
    /**
     * 押金缴纳列表总数
     */
    @GetMapping(value = "/eleDepositOrder/queryCount")
    public R queryCount(@RequestParam(value = "status", required = false) Integer status, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "orderId", required = false) String orderId, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "depositType", required = false) Integer depositType,
            @RequestParam(value = "carModel", required = false) String carModel, @RequestParam(value = "franchiseeName", required = false) String franchiseeName,
            @RequestParam(value = "payType", required = false) Integer payType, @RequestParam(value = "storeName", required = false) String storeName,
            @RequestParam(value = "orderType", required = false) Integer orderType, @RequestParam(value = "tenantId", required = false) Integer tenantId) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!SecurityUtils.isAdmin()) {
            return R.ok(NumberConstant.ZERO);
        }
        
        EleDepositOrderQuery eleDepositOrderQuery = EleDepositOrderQuery.builder().name(name).phone(phone).uid(uid).beginTime(beginTime).endTime(endTime).status(status)
                .orderId(orderId).storeIds(null).carModel(carModel).depositType(depositType).payType(payType).storeName(storeName).tenantId(tenantId)
                .franchiseeName(franchiseeName).franchiseeIds(null).orderType(orderType).build();
        
        return eleDepositOrderService.queryCount(eleDepositOrderQuery);
    }
    
    /**
     * 滞纳金记录列表
     */
    @GetMapping("/batteryServiceFee/queryList")
    public R queryList(@RequestParam("offset") Long offset, @RequestParam("size") Long size,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "source", required = false) Integer source,
            @RequestParam(value = "payTimeBegin", required = false) Long payTimeBegin,
            @RequestParam(value = "payTimeEnd", required = false) Long payTimeEnd,
            @RequestParam(value = "orderByServiceFeeGenerateTime", required = false) Integer orderByServiceFeeGenerateTime,
            @RequestParam(value = "orderByPayTime", required = false) Integer orderByPayTime, @RequestParam(value = "tenantId", required = false) Integer tenantId) {
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
    
        if (!SecurityUtils.isAdmin()) {
            return R.ok(Collections.emptyList());
        }
        
        BatteryServiceFeeQuery batteryServiceFeeQuery = BatteryServiceFeeQuery.builder()
                .uid(uid)
                .beginTime(beginTime)
                .endTime(endTime)
                .offset(offset)
                .size(size)
                .name(name)
                .orderId(orderId)
                .status(status)
                .tenantId(tenantId)
                .franchiseeIds(null)
                .storeIds(null)
                .source(source)
                .phone(phone)
                .payTimeBegin(payTimeBegin)
                .payTimeEnd(payTimeEnd)
                .orderByServiceFeeGenerateTime(orderByServiceFeeGenerateTime)
                .orderByPayTime(orderByPayTime)
                .build();
        
        return eleBatteryServiceFeeOrderService.listSuperAdminPage(batteryServiceFeeQuery);
    }
    
    /**
     * 滞纳金记录列表总数
     */
    @GetMapping("/batteryServiceFee/queryCount")
    public R queryCount(@RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "source", required = false) Integer source,
            @RequestParam(value = "payTimeBegin", required = false) Long payTimeBegin,
            @RequestParam(value = "payTimeEnd", required = false) Long payTimeEnd,
            @RequestParam(value = "tenantId", required = false) Integer tenantId) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!SecurityUtils.isAdmin()) {
            return R.ok(NumberConstant.ZERO);
        }
        
        BatteryServiceFeeQuery batteryServiceFeeQuery = BatteryServiceFeeQuery.builder()
                .uid(uid)
                .beginTime(beginTime)
                .endTime(endTime)
                .status(status)
                .name(name)
                .orderId(orderId)
                .tenantId(tenantId)
                .franchiseeIds(null)
                .storeIds(null)
                .source(source)
                .phone(phone)
                .payTimeBegin(payTimeBegin)
                .payTimeEnd(payTimeEnd)
                .build();
        
        return eleBatteryServiceFeeOrderService.countTotalForSuperAdmin(batteryServiceFeeQuery);
    }
}
