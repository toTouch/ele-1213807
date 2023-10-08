package com.xiliulou.electricity.controller.admin;


import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.ElectricityBatteryDataQuery;
import com.xiliulou.electricity.service.ElectricityBatteryDataService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
     */
    @GetMapping(value = "/admin/batteryData/allBattery/page")
    public R getAllBatteryPageData(@RequestParam("offset") long offset, @RequestParam("size") long size, @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
            @RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "physicsStatus", required = false) Integer physicsStatus,
            @RequestParam(value = "businessStatus", required = false) Integer businessStatus) {
        if (size < 0 || size > 50) {
            size = 10;
        }
        
        if (offset < 0) {
            offset = 0;
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
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(tenantId).tenant(tenant).sn(sn).franchiseeId(franchiseeId)
                .franchiseeIds(franchiseeIds).electricityCabinetId(electricityCabinetId).uid(uid).size(size).offset(offset).queryType(ElectricityBatteryDataQuery.QUERY_TYPE_ALL)
                .businessStatus(businessStatus).physicsStatus(physicsStatus).build();
        return electricityBatteryDataService.selectAllBatteryPageData(electricityBatteryQuery);
    }
    
    /**
     * 获取全部电池的数据总数
     */
    @GetMapping(value = "/admin/batteryData/allBattery/count")
    public R getAllBatteryDataCount(@RequestParam(value = "sn", required = false) String sn, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "physicsStatus", required = false) Integer physicsStatus, @RequestParam(value = "businessStatus", required = false) Integer businessStatus) {
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
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(TenantContextHolder.getTenantId()).sn(sn).franchiseeId(franchiseeId)
                .franchiseeIds(franchiseeIds).electricityCabinetId(electricityCabinetId).uid(uid).queryType(ElectricityBatteryDataQuery.QUERY_TYPE_ALL)
                .businessStatus(businessStatus).physicsStatus(physicsStatus).build();
        return electricityBatteryDataService.selectAllBatteryDataCount(electricityBatteryQuery);
    }
    
    /**
     * 获取在柜电池的分页数据
     */
    @GetMapping(value = "/admin/batteryData/inCabinetBattery/page")
    public R getInCabinetBatteryPageData(@RequestParam("offset") long offset, @RequestParam("size") long size, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "sn", required = false) String sn, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
            @RequestParam(value = "physicsStatus", required = false) Integer physicsStatus, @RequestParam(value = "businessStatus", required = false) Integer businessStatus) {
        if (size < 0 || size > 50) {
            size = 10;
        }
        
        if (offset < 0) {
            offset = 0;
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
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(tenantId).tenant(tenant).sn(sn).uid(uid).size(size).offset(offset)
                .franchiseeId(franchiseeId).franchiseeIds(franchiseeIds).electricityCabinetId(electricityCabinetId).queryType(ElectricityBatteryDataQuery.QUERY_TYPE_INCABINET)
                .businessStatus(businessStatus).physicsStatus(physicsStatus).build();
        return electricityBatteryDataService.selectInCabinetBatteryPageData(electricityBatteryQuery);
    }
    
    /**
     * 获取在柜电池的数据总数
     */
    @GetMapping(value = "/admin/batteryData/inCabinetBattery/count")
    public R getInCabinetBatteryDataCount(@RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
            @RequestParam(value = "physicsStatus", required = false) Integer physicsStatus, @RequestParam(value = "businessStatus", required = false) Integer businessStatus) {
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
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(TenantContextHolder.getTenantId()).sn(sn).uid(uid)
                .franchiseeId(franchiseeId).franchiseeIds(franchiseeIds).electricityCabinetId(electricityCabinetId).queryType(ElectricityBatteryDataQuery.QUERY_TYPE_INCABINET)
                .businessStatus(businessStatus).physicsStatus(physicsStatus).build();
        return electricityBatteryDataService.selectInCabinetBatteryDataCount(electricityBatteryQuery);
    }
    
    
    /**
     * 获取待租电池的分页数据
     */
    @GetMapping(value = "/admin/batteryData/pendingRentalBattery/page")
    public R getPendingRentalBatteryPageData(@RequestParam("offset") long offset, @RequestParam("size") long size, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "sn", required = false) String sn, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
            @RequestParam(value = "physicsStatus", required = false) Integer physicsStatus, @RequestParam(value = "businessStatus", required = false) Integer businessStatus) {
        if (size < 0 || size > 50) {
            size = 10;
        }
        
        if (offset < 0) {
            offset = 0;
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
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(tenantId).tenant(tenant).sn(sn).uid(uid).size(size).offset(offset)
                .electricityCabinetId(electricityCabinetId).franchiseeId(franchiseeId).franchiseeIds(franchiseeIds).queryType(ElectricityBatteryDataQuery.QUERY_TYPE_PENDINGRENTAL)
                .businessStatus(businessStatus).physicsStatus(physicsStatus).build();
        return electricityBatteryDataService.selectPendingRentalBatteryPageData(electricityBatteryQuery);
    }
    
    /**
     * 获取待租电池的数据总数
     */
    @GetMapping(value = "/admin/batteryData/pendingRentalBattery/count")
    public R getPendingRentalBatteryDataCount(@RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
            @RequestParam(value = "physicsStatus", required = false) Integer physicsStatus, @RequestParam(value = "businessStatus", required = false) Integer businessStatus) {
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
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(TenantContextHolder.getTenantId()).sn(sn).uid(uid)
                .electricityCabinetId(electricityCabinetId).franchiseeId(franchiseeId).franchiseeIds(franchiseeIds).queryType(ElectricityBatteryDataQuery.QUERY_TYPE_PENDINGRENTAL)
                .businessStatus(businessStatus).physicsStatus(physicsStatus).build();
        return electricityBatteryDataService.selectPendingRentalBatteryDataCount(electricityBatteryQuery);
    }
    
    /**
     * 获取已租电池的分页数据
     */
    @GetMapping(value = "/admin/batteryData/leasedBattery/page")
    public R getLeasedBatteryPageData(@RequestParam("offset") long offset, @RequestParam("size") long size, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "sn", required = false) String sn, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
            @RequestParam(value = "physicsStatus", required = false) Integer physicsStatus, @RequestParam(value = "businessStatus", required = false) Integer businessStatus) {
        if (size < 0 || size > 50) {
            size = 10;
        }
        
        if (offset < 0) {
            offset = 0;
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
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(tenantId).tenant(tenant).sn(sn).uid(uid).size(size).offset(offset)
                .electricityCabinetId(electricityCabinetId).franchiseeId(franchiseeId).franchiseeIds(franchiseeIds).queryType(ElectricityBatteryDataQuery.QUERY_TYPE_LEASED)
                .businessStatus(businessStatus).physicsStatus(physicsStatus).build();
        return electricityBatteryDataService.selectLeasedBatteryPageData(electricityBatteryQuery);
    }
    
    /**
     * 获取已租电池的数据总数
     */
    @GetMapping(value = "/admin/batteryData/leasedBattery/count")
    public R getLeasedBatteryDataCount(@RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
            @RequestParam(value = "physicsStatus", required = false) Integer physicsStatus, @RequestParam(value = "businessStatus", required = false) Integer businessStatus) {
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
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(TenantContextHolder.getTenantId()).sn(sn).uid(uid)
                .electricityCabinetId(electricityCabinetId).franchiseeId(franchiseeId).franchiseeIds(franchiseeIds).queryType(ElectricityBatteryDataQuery.QUERY_TYPE_LEASED)
                .businessStatus(businessStatus).physicsStatus(physicsStatus).build();
        return electricityBatteryDataService.selectLeasedBatteryDataCount(electricityBatteryQuery);
    }
    
    /**
     * 获取游离电池的分页数据
     */
    @GetMapping(value = "/admin/batteryData/strayBattery/page")
    public R getStrayBatteryPageData(@RequestParam("offset") long offset, @RequestParam("size") long size, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "sn", required = false) String sn, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
            @RequestParam(value = "physicsStatus", required = false) Integer physicsStatus, @RequestParam(value = "businessStatus", required = false) Integer businessStatus) {
        if (size < 0 || size > 50) {
            size = 10;
        }
        
        if (offset < 0) {
            offset = 0;
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
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(tenantId).tenant(tenant).sn(sn).uid(uid).size(size).offset(offset)
                .electricityCabinetId(electricityCabinetId).franchiseeId(franchiseeId).franchiseeIds(franchiseeIds).queryType(ElectricityBatteryDataQuery.QUERY_TYPE_STRAY)
                .businessStatus(businessStatus).physicsStatus(physicsStatus).build();
        return electricityBatteryDataService.selectStrayBatteryPageData(electricityBatteryQuery);
    }
    
    /**
     * 获取游离电池的数据总数
     */
    @GetMapping(value = "/admin/batteryData/strayBattery/count")
    public R getStrayBatteryDataCount(@RequestParam(value = "sn", required = false) String sn, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
            @RequestParam(value = "physicsStatus", required = false) Integer physicsStatus, @RequestParam(value = "businessStatus", required = false) Integer businessStatus) {
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
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(TenantContextHolder.getTenantId()).sn(sn).uid(uid)
                .electricityCabinetId(electricityCabinetId).franchiseeId(franchiseeId).franchiseeIds(franchiseeIds).queryType(ElectricityBatteryDataQuery.QUERY_TYPE_STRAY)
                .businessStatus(businessStatus).physicsStatus(physicsStatus).build();
        return electricityBatteryDataService.selectStrayBatteryDataCount(electricityBatteryQuery);
    }
    
    
    /**
     * 获取逾期电池的分页数据
     */
    @GetMapping(value = "/admin/batteryData/overdueBattery/page")
    public R getOverdueBatteryPageData(@RequestParam("offset") long offset, @RequestParam("size") long size, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "sn", required = false) String sn, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
            @RequestParam(value = "physicsStatus", required = false) Integer physicsStatus, @RequestParam(value = "businessStatus", required = false) Integer businessStatus) {
        if (size < 0 || size > 50) {
            size = 10;
        }
        
        if (offset < 0) {
            offset = 0;
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
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(tenantId).tenant(tenant).sn(sn).uid(uid).size(size).offset(offset)
                .electricityCabinetId(electricityCabinetId).franchiseeId(franchiseeId).franchiseeIds(franchiseeIds).queryType(ElectricityBatteryDataQuery.QUERY_TYPE_OVERDUE)
                .currentTimeMillis(System.currentTimeMillis()).businessStatus(businessStatus).physicsStatus(physicsStatus).build();
        return electricityBatteryDataService.selectOverdueBatteryPageData(electricityBatteryQuery);
    }
    
    /**
     * 获取逾期电池的数据总数
     */
    @GetMapping(value = "/admin/batteryData/overdueBattery/count")
    public R getOverdueBatteryDataCount(@RequestParam(value = "sn", required = false) String sn, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
            @RequestParam(value = "physicsStatus", required = false) Integer physicsStatus, @RequestParam(value = "businessStatus", required = false) Integer businessStatus) {
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
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(TenantContextHolder.getTenantId()).sn(sn).uid(uid)
                .electricityCabinetId(electricityCabinetId).franchiseeId(franchiseeId).franchiseeIds(franchiseeIds).queryType(ElectricityBatteryDataQuery.QUERY_TYPE_OVERDUE)
                .currentTimeMillis(System.currentTimeMillis()).businessStatus(businessStatus).physicsStatus(physicsStatus).build();
        return electricityBatteryDataService.selectOverdueBatteryDataCount(electricityBatteryQuery);
    }
    
    /**
     * 获取逾期电池的分页数据(车电一体)
     */
    @GetMapping(value = "/admin/batteryData/overdueCarBattery/page")
    public R getOverdueCarBatteryPageData(@RequestParam("offset") long offset, @RequestParam("size") long size, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "sn", required = false) String sn, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
            @RequestParam(value = "physicsStatus", required = false) Integer physicsStatus, @RequestParam(value = "businessStatus", required = false) Integer businessStatus) {
        if (size < 0 || size > 50) {
            size = 10;
        }
        
        if (offset < 0) {
            offset = 0;
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
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(tenantId).tenant(tenant).sn(sn).uid(uid).size(size).offset(offset)
                .electricityCabinetId(electricityCabinetId).franchiseeId(franchiseeId).franchiseeIds(franchiseeIds).queryType(ElectricityBatteryDataQuery.QUERY_TYPE_OVERDUE)
                .currentTimeMillis(System.currentTimeMillis()).businessStatus(businessStatus).physicsStatus(physicsStatus).build();
        return electricityBatteryDataService.selectOverdueCarBatteryPageData(electricityBatteryQuery);
    }
    
    /**
     * 获取逾期电池的数据总数(车电一体)
     */
    @GetMapping(value = "/admin/batteryData/overdueCarBattery/count")
    public R getOverdueCarBatteryDataCount(@RequestParam(value = "sn", required = false) String sn, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
            @RequestParam(value = "physicsStatus", required = false) Integer physicsStatus, @RequestParam(value = "businessStatus", required = false) Integer businessStatus) {
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
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(TenantContextHolder.getTenantId()).sn(sn).uid(uid)
                .electricityCabinetId(electricityCabinetId).franchiseeId(franchiseeId).franchiseeIds(franchiseeIds).queryType(ElectricityBatteryDataQuery.QUERY_TYPE_OVERDUE)
                .currentTimeMillis(System.currentTimeMillis()).businessStatus(businessStatus).physicsStatus(physicsStatus).build();
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
    @GetMapping(value = "/admin/batteryData/stockBattery/page")
    public R getStockBatteryPageDate(@RequestParam("offset") long offset, @RequestParam("size") long size, @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
            @RequestParam(value = "physicsStatus", required = false) Integer physicsStatus, @RequestParam(value = "businessStatus", required = false) Integer businessStatus,
            @RequestParam(value = "uid", required = false) Long uid) {
        if (size < 0 || size > 50) {
            size = 10;
        }
        if (offset < 0) {
            offset = 0;
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
    
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(tenantId).tenant(tenant).sn(sn).uid(uid).size(size).offset(offset)
                .franchiseeId(franchiseeId).franchiseeIds(franchiseeIds).electricityCabinetId(electricityCabinetId)
                .businessStatus(businessStatus).physicsStatus(physicsStatus).build();
        return electricityBatteryDataService.queryStockBatteryPageData(electricityBatteryQuery);
    }
    
    /**
     * 获取库存电池的数据总数
     */
    @GetMapping(value = "/admin/batteryData/stockBattery/count")
    public R getStockBatteryDataCount(@RequestParam(value = "sn", required = false) String sn, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
            @RequestParam(value = "physicsStatus", required = false) Integer physicsStatus, @RequestParam(value = "businessStatus", required = false) Integer businessStatus,
            @RequestParam(value = "uid", required = false) Long uid) {
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
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder().tenantId(TenantContextHolder.getTenantId()).sn(sn).uid(uid)
                .electricityCabinetId(electricityCabinetId).franchiseeId(franchiseeId).franchiseeIds(franchiseeIds)
                .businessStatus(businessStatus).physicsStatus(physicsStatus).build();
        return electricityBatteryDataService.queryStockBatteryPageDataCount(electricityBatteryQuery);
    }
    
    
}
