package com.xiliulou.electricity.controller.admin;


import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.enums.battery.BatteryLabelEnum;
import com.xiliulou.electricity.query.ElectricityBatteryDataQuery;
import com.xiliulou.electricity.service.ElectricityBatteryDataService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RestController
@Slf4j
public class JsonAdminElectricityBatteryDataController extends BaseController {
    
    @Autowired
    private ElectricityBatteryDataService electricityBatteryDataService;
    
    @Autowired
    UserDataScopeService userDataScopeService;
    
    @Autowired
    TenantService tenantService;
    
    /**
     * 获取全部电池的分页数据
     * todo 改post 请求 参数改sns list
     */
    @PostMapping(value = "/admin/batteryData/allBattery/page")
    public R getAllBatteryPageData(@RequestBody ElectricityBatteryDataQuery electricityBatteryRequest) {
        if (electricityBatteryRequest.getSize() < 0 || electricityBatteryRequest.getSize() > 50) {
            electricityBatteryRequest.setSize(10L);
        }
        
        if (electricityBatteryRequest.getOffset() < 0) {
            electricityBatteryRequest.setOffset(0L);
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            // 门店登录按领用条件查询
            electricityBatteryRequest.setLabel(List.of(BatteryLabelEnum.RECEIVED_ADMINISTRATORS.getCode()));
            electricityBatteryRequest.setReceiverId(SecurityUtils.getUid());
        }
        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("TENANT ERROR! tenantEntity not exists! id={}", tenantId);
            return R.ok(Collections.EMPTY_LIST);
        }
        if (CollectionUtils.isNotEmpty(electricityBatteryRequest.getSns()) && electricityBatteryRequest.getSns().size() == 1) {
            electricityBatteryRequest.setSn(electricityBatteryRequest.getSns().get(0));
            electricityBatteryRequest.setSns(Collections.EMPTY_LIST);
        }
        
        List<Integer> labels = electricityBatteryRequest.getLabel();
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(tenantId).tenant(tenant).sn(electricityBatteryRequest.getSn())
                .sns(electricityBatteryRequest.getSns()).franchiseeId(electricityBatteryRequest.getFranchiseeId()).franchiseeIds(franchiseeIds)
                .electricityCabinetId(electricityBatteryRequest.getElectricityCabinetId()).uid(electricityBatteryRequest.getUid()).size(electricityBatteryRequest.getSize())
                .offset(electricityBatteryRequest.getOffset()).queryType(ElectricityBatteryDataQuery.QUERY_TYPE_ALL).businessStatus(electricityBatteryRequest.getBusinessStatus())
                .physicsStatus(electricityBatteryRequest.getPhysicsStatus()).label(CollectionUtils.isEmpty(labels) ? null : labels)
                .receiverId(electricityBatteryRequest.getReceiverId()).build();
        return electricityBatteryDataService.selectAllBatteryPageData(electricityBatteryQuery);
    }
    
    /**
     * 获取全部电池的数据总数
     */
    @PostMapping(value = "/admin/batteryData/allBattery/count")
    public R getAllBatteryDataCount(@RequestBody ElectricityBatteryDataQuery electricityBatteryRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(0);
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            // 门店登录按领用条件查询
            electricityBatteryRequest.setLabel(List.of(BatteryLabelEnum.RECEIVED_ADMINISTRATORS.getCode()));
            electricityBatteryRequest.setReceiverId(SecurityUtils.getUid());
        }
        if (CollectionUtils.isNotEmpty(electricityBatteryRequest.getSns()) && electricityBatteryRequest.getSns().size() == 1) {
            electricityBatteryRequest.setSn(electricityBatteryRequest.getSns().get(0));
            electricityBatteryRequest.setSns(Collections.EMPTY_LIST);
        }
        List<Integer> labels = electricityBatteryRequest.getLabel();
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(TenantContextHolder.getTenantId())
                .sn(electricityBatteryRequest.getSn()).sns(electricityBatteryRequest.getSns()).franchiseeId(electricityBatteryRequest.getFranchiseeId())
                .franchiseeIds(franchiseeIds).electricityCabinetId(electricityBatteryRequest.getElectricityCabinetId()).uid(electricityBatteryRequest.getUid())
                .queryType(ElectricityBatteryDataQuery.QUERY_TYPE_ALL).businessStatus(electricityBatteryRequest.getBusinessStatus())
                .physicsStatus(electricityBatteryRequest.getPhysicsStatus()).label(CollectionUtils.isEmpty(labels) ? null : labels)
                .receiverId(electricityBatteryRequest.getReceiverId()).build();
        return electricityBatteryDataService.selectAllBatteryDataCount(electricityBatteryQuery);
    }
    
    /**
     * 清除异常交换电池用户
     *
     * @param id
     * @return
     */
    @PutMapping(value = "/admin/batteryData/guessUser/clear")
    @Log(title = "清除异常交换电池用户")
    public R clearGuessUser(@RequestParam("id") Long id) {
        return electricityBatteryDataService.updateGuessUserInfo(id);
    }
    
    /**
     * 获取在柜电池的分页数据
     */
    @PostMapping(value = "/admin/batteryData/inCabinetBattery/page")
    public R getInCabinetBatteryPageData(@RequestBody ElectricityBatteryDataQuery electricityBatteryRequest) {
        if (electricityBatteryRequest.getSize() < 0 || electricityBatteryRequest.getSize() > 50) {
            electricityBatteryRequest.setSize(10L);
        }
        
        if (electricityBatteryRequest.getOffset() < 0) {
            electricityBatteryRequest.setOffset(0L);
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }
        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("TENANT ERROR! tenantEntity not exists! id={}", tenantId);
            return R.ok(Collections.EMPTY_LIST);
        }
        
        if (CollectionUtils.isNotEmpty(electricityBatteryRequest.getSns()) && electricityBatteryRequest.getSns().size() == 1) {
            electricityBatteryRequest.setSn(electricityBatteryRequest.getSns().get(0));
            electricityBatteryRequest.setSns(Collections.EMPTY_LIST);
        }
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(tenantId).tenant(tenant).sn(electricityBatteryRequest.getSn())
                .sns(electricityBatteryRequest.getSns()).uid(electricityBatteryRequest.getUid()).size(electricityBatteryRequest.getSize())
                .offset(electricityBatteryRequest.getOffset()).franchiseeId(electricityBatteryRequest.getFranchiseeId()).franchiseeIds(franchiseeIds)
                .electricityCabinetId(electricityBatteryRequest.getElectricityCabinetId()).queryType(ElectricityBatteryDataQuery.QUERY_TYPE_INCABINET)
                .businessStatus(electricityBatteryRequest.getBusinessStatus()).physicsStatus(electricityBatteryRequest.getPhysicsStatus())
                .label(electricityBatteryRequest.getLabel()).receiverId(electricityBatteryRequest.getReceiverId()).build();
        return electricityBatteryDataService.selectInCabinetBatteryPageData(electricityBatteryQuery);
    }
    
    /**
     * 获取在柜电池的数据总数
     */
    @PostMapping(value = "/admin/batteryData/inCabinetBattery/count")
    public R getInCabinetBatteryDataCount(@RequestBody ElectricityBatteryDataQuery electricityBatteryRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(0);
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(0);
        }
        if (CollectionUtils.isNotEmpty(electricityBatteryRequest.getSns()) && electricityBatteryRequest.getSns().size() == 1) {
            electricityBatteryRequest.setSn(electricityBatteryRequest.getSns().get(0));
            electricityBatteryRequest.setSns(Collections.EMPTY_LIST);
        }
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(TenantContextHolder.getTenantId())
                .sn(electricityBatteryRequest.getSn()).sns(electricityBatteryRequest.getSns()).uid(electricityBatteryRequest.getUid())
                .franchiseeId(electricityBatteryRequest.getFranchiseeId()).franchiseeIds(franchiseeIds).electricityCabinetId(electricityBatteryRequest.getElectricityCabinetId())
                .queryType(ElectricityBatteryDataQuery.QUERY_TYPE_INCABINET).businessStatus(electricityBatteryRequest.getBusinessStatus())
                .physicsStatus(electricityBatteryRequest.getPhysicsStatus()).label(electricityBatteryRequest.getLabel()).receiverId(electricityBatteryRequest.getReceiverId())
                .build();
        return electricityBatteryDataService.selectInCabinetBatteryDataCount(electricityBatteryQuery);
    }
    
    
    /**
     * 获取待租电池的分页数据
     */
    @PostMapping(value = "/admin/batteryData/pendingRentalBattery/page")
    public R getPendingRentalBatteryPageData(@RequestBody ElectricityBatteryDataQuery electricityBatteryRequest) {
        if (electricityBatteryRequest.getSize() < 0 || electricityBatteryRequest.getSize() > 50) {
            electricityBatteryRequest.setSize(10L);
        }
        
        if (electricityBatteryRequest.getOffset() < 0) {
            electricityBatteryRequest.setOffset(0L);
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }
        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("TENANT ERROR! tenantEntity not exists! id={}", tenantId);
            return R.ok(Collections.EMPTY_LIST);
        }
        
        if (CollectionUtils.isNotEmpty(electricityBatteryRequest.getSns()) && electricityBatteryRequest.getSns().size() == 1) {
            electricityBatteryRequest.setSn(electricityBatteryRequest.getSns().get(0));
            electricityBatteryRequest.setSns(Collections.EMPTY_LIST);
        }
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(tenantId).tenant(tenant).sn(electricityBatteryRequest.getSn())
                .sns(electricityBatteryRequest.getSns()).uid(electricityBatteryRequest.getUid()).size(electricityBatteryRequest.getSize())
                .offset(electricityBatteryRequest.getOffset()).electricityCabinetId(electricityBatteryRequest.getElectricityCabinetId())
                .franchiseeId(electricityBatteryRequest.getFranchiseeId()).franchiseeIds(franchiseeIds).queryType(ElectricityBatteryDataQuery.QUERY_TYPE_PENDINGRENTAL)
                .businessStatus(electricityBatteryRequest.getBusinessStatus()).physicsStatus(electricityBatteryRequest.getPhysicsStatus())
                .label(electricityBatteryRequest.getLabel()).receiverId(electricityBatteryRequest.getReceiverId()).build();
        return electricityBatteryDataService.selectPendingRentalBatteryPageData(electricityBatteryQuery);
    }
    
    /**
     * 获取待租电池的数据总数
     */
    @PostMapping(value = "/admin/batteryData/pendingRentalBattery/count")
    public R getPendingRentalBatteryDataCount(@RequestBody ElectricityBatteryDataQuery electricityBatteryRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(0);
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(0);
        }
        if (CollectionUtils.isNotEmpty(electricityBatteryRequest.getSns()) && electricityBatteryRequest.getSns().size() == 1) {
            electricityBatteryRequest.setSn(electricityBatteryRequest.getSns().get(0));
            electricityBatteryRequest.setSns(Collections.EMPTY_LIST);
        }
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(TenantContextHolder.getTenantId())
                .sn(electricityBatteryRequest.getSn()).sns(electricityBatteryRequest.getSns()).uid(electricityBatteryRequest.getUid())
                .electricityCabinetId(electricityBatteryRequest.getElectricityCabinetId()).franchiseeId(electricityBatteryRequest.getFranchiseeId()).franchiseeIds(franchiseeIds)
                .queryType(ElectricityBatteryDataQuery.QUERY_TYPE_PENDINGRENTAL).businessStatus(electricityBatteryRequest.getBusinessStatus())
                .physicsStatus(electricityBatteryRequest.getPhysicsStatus()).label(electricityBatteryRequest.getLabel()).receiverId(electricityBatteryRequest.getReceiverId())
                .build();
        return electricityBatteryDataService.selectPendingRentalBatteryDataCount(electricityBatteryQuery);
    }
    
    /**
     * 获取已租电池的分页数据
     */
    @PostMapping(value = "/admin/batteryData/leasedBattery/page")
    public R getLeasedBatteryPageData(@RequestBody ElectricityBatteryDataQuery electricityBatteryRequest) {
        if (electricityBatteryRequest.getSize() < 0 || electricityBatteryRequest.getSize() > 50) {
            electricityBatteryRequest.setSize(10L);
        }
        
        if (electricityBatteryRequest.getOffset() < 0) {
            electricityBatteryRequest.setOffset(0L);
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }
        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("TENANT ERROR! tenantEntity not exists! id={}", tenantId);
            return R.ok(Collections.EMPTY_LIST);
        }
        
        if (CollectionUtils.isNotEmpty(electricityBatteryRequest.getSns()) && electricityBatteryRequest.getSns().size() == 1) {
            electricityBatteryRequest.setSn(electricityBatteryRequest.getSns().get(0));
            electricityBatteryRequest.setSns(Collections.EMPTY_LIST);
        }
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(tenantId).tenant(tenant).sn(electricityBatteryRequest.getSn())
                .sns(electricityBatteryRequest.getSns()).uid(electricityBatteryRequest.getUid()).size(electricityBatteryRequest.getSize())
                .offset(electricityBatteryRequest.getOffset()).electricityCabinetId(electricityBatteryRequest.getElectricityCabinetId())
                .franchiseeId(electricityBatteryRequest.getFranchiseeId()).franchiseeIds(franchiseeIds).queryType(ElectricityBatteryDataQuery.QUERY_TYPE_LEASED)
                .businessStatus(electricityBatteryRequest.getBusinessStatus()).physicsStatus(electricityBatteryRequest.getPhysicsStatus())
                .label(electricityBatteryRequest.getLabel()).receiverId(electricityBatteryRequest.getReceiverId()).build();
        return electricityBatteryDataService.selectLeasedBatteryPageData(electricityBatteryQuery);
    }
    
    /**
     * 获取已租电池的数据总数
     */
    @PostMapping(value = "/admin/batteryData/leasedBattery/count")
    public R getLeasedBatteryDataCount(@RequestBody ElectricityBatteryDataQuery electricityBatteryRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(0);
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(0);
        }
        
        if (CollectionUtils.isNotEmpty(electricityBatteryRequest.getSns()) && electricityBatteryRequest.getSns().size() == 1) {
            electricityBatteryRequest.setSn(electricityBatteryRequest.getSns().get(0));
            electricityBatteryRequest.setSns(Collections.EMPTY_LIST);
        }
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(TenantContextHolder.getTenantId())
                .sn(electricityBatteryRequest.getSn()).sns(electricityBatteryRequest.getSns()).uid(electricityBatteryRequest.getUid())
                .electricityCabinetId(electricityBatteryRequest.getElectricityCabinetId()).franchiseeId(electricityBatteryRequest.getFranchiseeId()).franchiseeIds(franchiseeIds)
                .queryType(ElectricityBatteryDataQuery.QUERY_TYPE_LEASED).businessStatus(electricityBatteryRequest.getBusinessStatus())
                .physicsStatus(electricityBatteryRequest.getPhysicsStatus()).label(electricityBatteryRequest.getLabel()).receiverId(electricityBatteryRequest.getReceiverId())
                .build();
        
        return electricityBatteryDataService.selectLeasedBatteryDataCount(electricityBatteryQuery);
    }
    
    /**
     * 获取游离电池的分页数据
     */
    @PostMapping(value = "/admin/batteryData/strayBattery/page")
    public R getStrayBatteryPageData(@RequestBody ElectricityBatteryDataQuery electricityBatteryRequest) {
        if (electricityBatteryRequest.getSize() < 0 || electricityBatteryRequest.getSize() > 50) {
            electricityBatteryRequest.setSize(10L);
        }
        
        if (electricityBatteryRequest.getOffset() < 0) {
            electricityBatteryRequest.setOffset(0L);
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }
        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("TENANT ERROR! tenantEntity not exists! id={}", tenantId);
            return R.ok(Collections.EMPTY_LIST);
        }
        
        if (CollectionUtils.isNotEmpty(electricityBatteryRequest.getSns()) && electricityBatteryRequest.getSns().size() == 1) {
            electricityBatteryRequest.setSn(electricityBatteryRequest.getSns().get(0));
            electricityBatteryRequest.setSns(Collections.EMPTY_LIST);
        }
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(tenantId).tenant(tenant).sn(electricityBatteryRequest.getSn())
                .sns(electricityBatteryRequest.getSns()).uid(electricityBatteryRequest.getUid()).size(electricityBatteryRequest.getSize())
                .offset(electricityBatteryRequest.getOffset()).electricityCabinetId(electricityBatteryRequest.getElectricityCabinetId())
                .franchiseeId(electricityBatteryRequest.getFranchiseeId()).franchiseeIds(franchiseeIds).queryType(ElectricityBatteryDataQuery.QUERY_TYPE_STRAY)
                .businessStatus(electricityBatteryRequest.getBusinessStatus()).physicsStatus(electricityBatteryRequest.getPhysicsStatus())
                .label(electricityBatteryRequest.getLabel()).receiverId(electricityBatteryRequest.getReceiverId()).build();
        return electricityBatteryDataService.selectStrayBatteryPageData(electricityBatteryQuery);
    }
    
    /**
     * 获取游离电池的数据总数
     */
    @PostMapping(value = "/admin/batteryData/strayBattery/count")
    public R getStrayBatteryDataCount(@RequestBody ElectricityBatteryDataQuery electricityBatteryRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(0);
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(0);
        }
        
        if (CollectionUtils.isNotEmpty(electricityBatteryRequest.getSns()) && electricityBatteryRequest.getSns().size() == 1) {
            electricityBatteryRequest.setSn(electricityBatteryRequest.getSns().get(0));
            electricityBatteryRequest.setSns(Collections.EMPTY_LIST);
        }
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(TenantContextHolder.getTenantId())
                .sn(electricityBatteryRequest.getSn()).sns(electricityBatteryRequest.getSns()).uid(electricityBatteryRequest.getUid())
                .electricityCabinetId(electricityBatteryRequest.getElectricityCabinetId()).franchiseeId(electricityBatteryRequest.getFranchiseeId()).franchiseeIds(franchiseeIds)
                .queryType(ElectricityBatteryDataQuery.QUERY_TYPE_STRAY).businessStatus(electricityBatteryRequest.getBusinessStatus())
                .physicsStatus(electricityBatteryRequest.getPhysicsStatus()).label(electricityBatteryRequest.getLabel()).receiverId(electricityBatteryRequest.getReceiverId())
                .build();
        return electricityBatteryDataService.selectStrayBatteryDataCount(electricityBatteryQuery);
    }
    
    
    /**
     * 获取逾期电池的分页数据
     */
    @PostMapping(value = "/admin/batteryData/overdueBattery/page")
    public R getOverdueBatteryPageData(@RequestBody ElectricityBatteryDataQuery electricityBatteryRequest) {
        if (electricityBatteryRequest.getSize() < 0 || electricityBatteryRequest.getSize() > 50) {
            electricityBatteryRequest.setSize(10L);
        }
        
        if (electricityBatteryRequest.getOffset() < 0) {
            electricityBatteryRequest.setOffset(0L);
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }
        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("TENANT ERROR! tenantEntity not exists! id={}", tenantId);
            return R.ok(Collections.EMPTY_LIST);
        }
        
        if (CollectionUtils.isNotEmpty(electricityBatteryRequest.getSns()) && electricityBatteryRequest.getSns().size() == 1) {
            electricityBatteryRequest.setSn(electricityBatteryRequest.getSns().get(0));
            electricityBatteryRequest.setSns(Collections.EMPTY_LIST);
        }
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(tenantId).tenant(tenant).sn(electricityBatteryRequest.getSn())
                .sns(electricityBatteryRequest.getSns()).uid(electricityBatteryRequest.getUid()).size(electricityBatteryRequest.getSize())
                .offset(electricityBatteryRequest.getOffset()).electricityCabinetId(electricityBatteryRequest.getElectricityCabinetId())
                .franchiseeId(electricityBatteryRequest.getFranchiseeId()).franchiseeIds(franchiseeIds).queryType(ElectricityBatteryDataQuery.QUERY_TYPE_OVERDUE)
                .currentTimeMillis(System.currentTimeMillis()).businessStatus(electricityBatteryRequest.getBusinessStatus())
                .physicsStatus(electricityBatteryRequest.getPhysicsStatus()).sort(electricityBatteryRequest.getSort()).label(electricityBatteryRequest.getLabel())
                .receiverId(electricityBatteryRequest.getReceiverId()).build();
        return electricityBatteryDataService.selectOverdueBatteryPageData(electricityBatteryQuery);
    }
    
    /**
     * 获取逾期电池的数据总数
     */
    @PostMapping(value = "/admin/batteryData/overdueBattery/count")
    public R getOverdueBatteryDataCount(@RequestBody ElectricityBatteryDataQuery electricityBatteryRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(0);
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(0);
        }
        
        if (CollectionUtils.isNotEmpty(electricityBatteryRequest.getSns()) && electricityBatteryRequest.getSns().size() == 1) {
            electricityBatteryRequest.setSn(electricityBatteryRequest.getSns().get(0));
            electricityBatteryRequest.setSns(Collections.EMPTY_LIST);
        }
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(TenantContextHolder.getTenantId())
                .sn(electricityBatteryRequest.getSn()).sns(electricityBatteryRequest.getSns()).uid(electricityBatteryRequest.getUid())
                .electricityCabinetId(electricityBatteryRequest.getElectricityCabinetId()).franchiseeId(electricityBatteryRequest.getFranchiseeId()).franchiseeIds(franchiseeIds)
                .queryType(ElectricityBatteryDataQuery.QUERY_TYPE_OVERDUE).currentTimeMillis(System.currentTimeMillis())
                .businessStatus(electricityBatteryRequest.getBusinessStatus()).physicsStatus(electricityBatteryRequest.getPhysicsStatus())
                .label(electricityBatteryRequest.getLabel()).receiverId(electricityBatteryRequest.getReceiverId()).build();
        return electricityBatteryDataService.selectOverdueBatteryDataCount(electricityBatteryQuery);
    }
    
    /**
     * 获取逾期电池的分页数据(车电一体)
     */
    @PostMapping(value = "/admin/batteryData/overdueCarBattery/page")
    public R getOverdueCarBatteryPageData(@RequestBody ElectricityBatteryDataQuery electricityBatteryRequest) {
        if (electricityBatteryRequest.getSize() < 0 || electricityBatteryRequest.getSize() > 50) {
            electricityBatteryRequest.setSize(10L);
        }
        
        if (electricityBatteryRequest.getOffset() < 0) {
            electricityBatteryRequest.setOffset(0L);
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }
        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("TENANT ERROR! tenantEntity not exists! id={}", tenantId);
            return R.ok(Collections.EMPTY_LIST);
        }
        
        if (CollectionUtils.isNotEmpty(electricityBatteryRequest.getSns()) && electricityBatteryRequest.getSns().size() == 1) {
            electricityBatteryRequest.setSn(electricityBatteryRequest.getSns().get(0));
            electricityBatteryRequest.setSns(Collections.EMPTY_LIST);
        }
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(tenantId).tenant(tenant).sn(electricityBatteryRequest.getSn())
                .sns(electricityBatteryRequest.getSns()).uid(electricityBatteryRequest.getUid()).size(electricityBatteryRequest.getSize())
                .offset(electricityBatteryRequest.getOffset()).electricityCabinetId(electricityBatteryRequest.getElectricityCabinetId())
                .franchiseeId(electricityBatteryRequest.getFranchiseeId()).franchiseeIds(franchiseeIds).queryType(ElectricityBatteryDataQuery.QUERY_TYPE_OVERDUE)
                .currentTimeMillis(System.currentTimeMillis()).sort(electricityBatteryRequest.getSort()).businessStatus(electricityBatteryRequest.getBusinessStatus())
                .physicsStatus(electricityBatteryRequest.getPhysicsStatus()).build();
        return electricityBatteryDataService.selectOverdueCarBatteryPageData(electricityBatteryQuery);
    }
    
    /**
     * 获取逾期电池的数据总数(车电一体)
     */
    @PostMapping(value = "/admin/batteryData/overdueCarBattery/count")
    public R getOverdueCarBatteryDataCount(@RequestBody ElectricityBatteryDataQuery electricityBatteryRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(0);
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(0);
        }
        
        if (CollectionUtils.isNotEmpty(electricityBatteryRequest.getSns()) && electricityBatteryRequest.getSns().size() == 1) {
            electricityBatteryRequest.setSn(electricityBatteryRequest.getSns().get(0));
            electricityBatteryRequest.setSns(Collections.EMPTY_LIST);
        }
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(TenantContextHolder.getTenantId())
                .sn(electricityBatteryRequest.getSn()).sns(electricityBatteryRequest.getSns()).franchiseeId(electricityBatteryRequest.getFranchiseeId())
                .franchiseeIds(franchiseeIds).electricityCabinetId(electricityBatteryRequest.getElectricityCabinetId())
                .currentTimeMillis(System.currentTimeMillis()).uid(electricityBatteryRequest.getUid()).queryType(ElectricityBatteryDataQuery.QUERY_TYPE_OVERDUE)
                .businessStatus(electricityBatteryRequest.getBusinessStatus()).physicsStatus(electricityBatteryRequest.getPhysicsStatus()).build();
        return electricityBatteryDataService.selectOverdueCarBatteryDataCount(electricityBatteryQuery);
    }
    
    /**
     * 获取库存电池的分页数据
     *
     * @param offset               启示页
     * @param size                 每页大小
     * @param sn                   电池编码
     * @param franchiseeId
     * @param electricityCabinetId
     * @return
     */
    @PostMapping(value = "/admin/batteryData/stockBattery/page")
    public R getStockBatteryPageDate(@RequestBody ElectricityBatteryDataQuery electricityBatteryRequest) {
        if (electricityBatteryRequest.getSize() < 0 || electricityBatteryRequest.getSize() > 50) {
            electricityBatteryRequest.setSize(10L);
        }
        
        if (electricityBatteryRequest.getOffset() < 0) {
            electricityBatteryRequest.setOffset(0L);
        }
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }
        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("TENANT ERROR! tenantEntity not exists! id={}", tenantId);
            return R.ok(Collections.EMPTY_LIST);
        }
        
        if (CollectionUtils.isNotEmpty(electricityBatteryRequest.getSns()) && electricityBatteryRequest.getSns().size() == 1) {
            electricityBatteryRequest.setSn(electricityBatteryRequest.getSns().get(0));
            electricityBatteryRequest.setSns(Collections.EMPTY_LIST);
        }
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(tenantId).tenant(tenant).sn(electricityBatteryRequest.getSn())
                .sns(electricityBatteryRequest.getSns()).uid(electricityBatteryRequest.getUid()).size(electricityBatteryRequest.getSize())
                .offset(electricityBatteryRequest.getOffset()).franchiseeId(electricityBatteryRequest.getFranchiseeId()).franchiseeIds(franchiseeIds)
                .electricityCabinetId(electricityBatteryRequest.getElectricityCabinetId()).businessStatus(electricityBatteryRequest.getBusinessStatus())
                .physicsStatus(electricityBatteryRequest.getPhysicsStatus()).label(electricityBatteryRequest.getLabel()).receiverId(electricityBatteryRequest.getReceiverId())
                .build();
        return electricityBatteryDataService.queryStockBatteryPageData(electricityBatteryQuery);
    }
    
    /**
     * 获取库存电池的数据总数
     */
    @PostMapping(value = "/admin/batteryData/stockBattery/count")
    public R getStockBatteryDataCount(@RequestBody ElectricityBatteryDataQuery electricityBatteryRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(0);
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(0);
        }
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(TenantContextHolder.getTenantId())
                .sn(electricityBatteryRequest.getSn()).sns(electricityBatteryRequest.getSns()).uid(electricityBatteryRequest.getUid())
                .franchiseeId(electricityBatteryRequest.getFranchiseeId()).franchiseeIds(franchiseeIds).electricityCabinetId(electricityBatteryRequest.getElectricityCabinetId())
                .businessStatus(electricityBatteryRequest.getBusinessStatus()).physicsStatus(electricityBatteryRequest.getPhysicsStatus())
                .label(electricityBatteryRequest.getLabel()).receiverId(electricityBatteryRequest.getReceiverId()).build();
        return electricityBatteryDataService.queryStockBatteryPageDataCount(electricityBatteryQuery);
    }
}
