package com.xiliulou.electricity.controller.admin;

import com.alibaba.excel.EasyExcel;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.BatteryExcelQuery;
import com.xiliulou.electricity.query.BatteryExcelV3Query;
import com.xiliulou.electricity.query.BindElectricityBatteryQuery;
import com.xiliulou.electricity.query.EleBatteryQuery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.retrofit.BatteryPlatRetrofitService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.BatteryExcelListener;
import com.xiliulou.electricity.utils.BatteryExcelListenerV2;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description: 电池 controller
 * @author: Mr.YG
 * @create: 2020-11-27 14:08
 **/
@RestController
@Slf4j
public class JsonAdminElectricityCabinetBatteryController extends BaseController {
    
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    
    @Autowired
    FranchiseeService franchiseeService;
    
    @Autowired
    UserDataScopeService userDataScopeService;
    
    @Autowired
    BatteryGeoService batteryGeoService;
    @Autowired
    BatteryPlatRetrofitService batteryPlatRetrofitService;
    @Autowired
    TenantService tenantService;
    
    /**
     * 新增电池
     *
     * @param
     * @return
     */
    @PostMapping(value = "/admin/battery")
    public R save(@RequestBody @Validated EleBatteryQuery electricityBattery) {
        
        return electricityBatteryService.saveElectricityBattery(electricityBattery);
    }
    
    /**
     * 修改电池
     *
     * @param
     * @return
     */
    @PutMapping(value = "/admin/battery")
    @Log(title = "修改电池")
    public R update(@RequestBody @Validated EleBatteryQuery query) {
        if (Objects.isNull(query.getId())) {
            return R.fail("请求参数错误!");
        }
        
        return electricityBatteryService.updateForAdmin(query);
    }
    
    /**
     * 删除电池
     *
     * @param
     * @return
     */
    @DeleteMapping(value = "/admin/battery/{id}")
    @Log(title = "删除电池")
    public R delete(@PathVariable("id") Long id, @RequestParam(value = "isNeedSync", required = false) Integer isNeedSync) {
        return electricityBatteryService.deleteElectricityBattery(id, isNeedSync);
    }
    
    @GetMapping("/admin/battery/info")
    public R queryBatteryInfo(@RequestParam("sn") String sn) {
        if (StringUtils.isEmpty(sn)) {
            return R.ok();
        }
        
        return returnTripleResult(electricityBatteryService.queryBatteryInfoBySn(sn));
    }
    
    
    /**
     * 电池分页
     *
     * @param
     * @return
     */
    @GetMapping(value = "/admin/battery/page")
    public R getElectricityBatteryPage(@RequestParam(value = "offset") Long offset,
            @RequestParam(value = "size") Long size,
            @RequestParam(value = "physicsStatus", required = false) Integer physicsStatus,
            @RequestParam(value = "businessStatus", required = false) Integer businessStatus,
            @RequestParam(value = "electricityCabinetName", required = false) String electricityCabinetName,
            @RequestParam(value = "chargeStatus", required = false) Integer chargeStatus,
            @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "model", required = false) String model,
            @RequestParam(value = "power", required = false) Double power,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "franchiseeName", required = false) String franchiseeName,
            @RequestParam(value = "bindStatus", required = false) Integer bindStatus) {
        
        if (Objects.isNull(size) || size < 0 || size > 50) {
            size = 10L;
        }
        
        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
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
        
        ElectricityBatteryQuery electricityBatteryQuery = new ElectricityBatteryQuery();
        electricityBatteryQuery.setPhysicsStatus(physicsStatus);
        electricityBatteryQuery.setBusinessStatus(businessStatus);
        electricityBatteryQuery.setSn(sn);
        electricityBatteryQuery.setModel(model);
        electricityBatteryQuery.setPower(power);
        electricityBatteryQuery.setTenantId(TenantContextHolder.getTenantId());
        electricityBatteryQuery.setChargeStatus(chargeStatus);
        electricityBatteryQuery.setFranchiseeId(franchiseeId);
        electricityBatteryQuery.setFranchiseeIds(franchiseeIds);
        electricityBatteryQuery.setElectricityCabinetName(electricityCabinetName);
        electricityBatteryQuery.setFranchiseeName(franchiseeName);
        
        //当运营商信息不存在的时候，才可以查看绑定与未绑定运营商的数据信息
        if(Objects.isNull(franchiseeId) && CollectionUtils.isEmpty(franchiseeIds)){
            electricityBatteryQuery.setBindStatus(bindStatus);
        }
        
        return electricityBatteryService.queryList(electricityBatteryQuery, offset, size);
    }
    
    /**
     * 获取当前加盟商的电池+未绑定加盟商的电池
     *
     * @param offset
     * @param size
     * @param franchiseeId
     * @return
     */
    @GetMapping(value = "/admin/battery/bind/page")
    public R batteryBindPage(@RequestParam(value = "offset") Long offset, @RequestParam(value = "size") Long size,
            @RequestParam(value = "franchiseeId") Long franchiseeId) {
        return electricityBatteryService.queryBindListByPage(offset, size, franchiseeId);
    }
    
    /**
     * 分配电池
     */
    
    /**
     * 电池分页数量
     *
     * @param
     * @return
     */
    @GetMapping(value = "/admin/battery/queryCount")
    public R queryCount(@RequestParam(value = "physicsStatus", required = false) Integer physicsStatus,
            @RequestParam(value = "businessStatus", required = false) Integer businessStatus,
            @RequestParam(value = "chargeStatus", required = false) Integer chargeStatus,
            @RequestParam(value = "electricityCabinetName", required = false) String electricityCabinetName,
            @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "model", required = false) String model,
            @RequestParam(value = "power", required = false) Double power,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "franchiseeName", required = false) String franchiseeName,
            @RequestParam(value = "bindStatus", required = false) Integer bindStatus) {
        
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
        
        ElectricityBatteryQuery electricityBatteryQuery = new ElectricityBatteryQuery();
        electricityBatteryQuery.setPhysicsStatus(physicsStatus);
        electricityBatteryQuery.setBusinessStatus(businessStatus);
        electricityBatteryQuery.setSn(sn);
        electricityBatteryQuery.setModel(model);
        electricityBatteryQuery.setPower(power);
        electricityBatteryQuery.setTenantId(TenantContextHolder.getTenantId());
        electricityBatteryQuery.setChargeStatus(chargeStatus);
        electricityBatteryQuery.setElectricityCabinetName(electricityCabinetName);
        electricityBatteryQuery.setFranchiseeId(franchiseeId);
        electricityBatteryQuery.setFranchiseeIds(franchiseeIds);
        electricityBatteryQuery.setFranchiseeName(franchiseeName);
        
        //当运营商信息不存在的时候，才可以查看绑定与未绑定运营商的数据信息
        if(Objects.isNull(franchiseeId) && CollectionUtils.isEmpty(franchiseeIds)){
            electricityBatteryQuery.setBindStatus(bindStatus);
        }
        return electricityBatteryService.queryCount(electricityBatteryQuery);
    }
    
    
    /**
     * 加盟商电池数量
     *
     * @param
     * @return
     */
    @GetMapping(value = "/admin/battery/pageByFranchisee")
    public R pageByFranchisee(@RequestParam(value = "offset") Long offset, @RequestParam(value = "size") Long size,
            @RequestParam(value = "physicsStatus", required = false) Integer physicsStatus,
            @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "chargeStatus", required = false) Integer chargeStatus) {
        
        //用户
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
        
        ElectricityBatteryQuery electricityBatteryQuery = new ElectricityBatteryQuery();
        electricityBatteryQuery.setPhysicsStatus(physicsStatus);
        electricityBatteryQuery.setSn(sn);
        electricityBatteryQuery.setFranchiseeIds(franchiseeIds);
        electricityBatteryQuery.setTenantId(TenantContextHolder.getTenantId());
        electricityBatteryQuery.setChargeStatus(chargeStatus);
        
        return electricityBatteryService.queryList(electricityBatteryQuery, offset, size);
    }
    
    
    /**
     * 加盟商电池
     *
     * @param
     * @return
     */
    @GetMapping(value = "/admin/battery/queryCountByFranchisee")
    public R queryCountByFranchisee(@RequestParam(value = "physicsStatus", required = false) Integer physicsStatus,
            @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "chargeStatus", required = false) Integer chargeStatus) {
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        //用户
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
        
        ElectricityBatteryQuery electricityBatteryQuery = new ElectricityBatteryQuery();
        electricityBatteryQuery.setPhysicsStatus(physicsStatus);
        electricityBatteryQuery.setSn(sn);
        electricityBatteryQuery.setFranchiseeIds(franchiseeIds);
        electricityBatteryQuery.setTenantId(tenantId);
        electricityBatteryQuery.setChargeStatus(chargeStatus);
        
        return electricityBatteryService.queryCount(electricityBatteryQuery);
    }
    
    
    /**
     * 获取单个电池
     *
     * @param
     * @return
     */
    @GetMapping(value = "/admin/battery/{id}")
    public R queryById(@PathVariable("id") Long id) {
        return electricityBatteryService.queryById(id);
    }
    
    /**
     * 电池绑定/解绑加盟商
     */
    @PostMapping(value = "/admin/franchisee/bindElectricityBattery")
    @Log(title = "电池绑定/解绑加盟商")
    public R bindElectricityBattery(
            @RequestBody @Validated(value = CreateGroup.class) BindElectricityBatteryQuery bindElectricityBatteryQuery) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }
        
        return electricityBatteryService.bindFranchiseeForBattery(bindElectricityBatteryQuery);
    }
    
    
    /**
     * 文件上传
     * <p>
     * 1. 创建excel对应的实体对象
     * <p>
     * 2. 由于默认一行行的读取excel，所以需要创建excel一行一行的回调监听器
     * <p>
     * 3. 直接读即可
     */
    @PostMapping("/admin/battery/excel")
    @Transactional(rollbackFor = Exception.class)
    public R upload(@RequestParam("file") MultipartFile file, @RequestParam("franchiseeId") Long franchiseeId) {
        try {
            EasyExcel.read(file.getInputStream(), BatteryExcelQuery.class,
                    new BatteryExcelListener(electricityBatteryService, franchiseeId)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.ok();
    }
    
    /**
     * 同步电池服务平台的接口
     *
     * @param file
     * @return
     */
    @Deprecated
    @PostMapping("/admin/battery/excel/v2")
    public R uploadV2(@RequestParam("file") MultipartFile file, @RequestParam("franchiseeId") Long franchiseeId)  {
        try {
            return uploadV2WithTransaction(file, franchiseeId);
        } catch (CustomBusinessException e) {
            return R.failMsg(e.getMessage());
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof CustomBusinessException) {
                CustomBusinessException customException = (CustomBusinessException) cause;
                return R.failMsg(customException.getMessage());
            }
            log.error("IMPORT BATTERY ERROR! ", e);
            return R.failMsg("导入失败");
        }
    }
    
    /***
     * @description 导入电池excel
     * @date 2023/10/18 10:18:31
     * @author HeYafeng
     */
    @PostMapping("/admin/battery/excel/v3")
    public R uploadV3(@RequestBody BatteryExcelV3Query batteryExcelV3Query) {
        if (ObjectUtils.isEmpty(batteryExcelV3Query)) {
            return R.fail("100602", "Excel模版中数据为空，请检查修改后再操作");
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user!");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return electricityBatteryService.saveBatchFromExcel(batteryExcelV3Query, user.getUid());
    }
    
    @Transactional(rollbackFor = Exception.class)
    public R uploadV2WithTransaction(MultipartFile file, Long franchiseeId) throws IOException {
        EasyExcel.read(file.getInputStream(), BatteryExcelQuery.class,
                new BatteryExcelListenerV2(electricityBatteryService, batteryPlatRetrofitService, tenantService.queryByIdFromCache(TenantContextHolder.getTenantId()).getCode(), franchiseeId)).sheet().doRead();
        return R.ok();
    }
    /**
     * 电池总览
     *
     * @param
     * @param sn
     * @return
     */
    @Deprecated
    @GetMapping("/admin/battery/queryBatteryOverview")
    public R queryBatteryOverview(@RequestParam(value = "businessStatus", required = false) Integer businessStatus,
            @RequestParam(value = "physicsStatus", required = false) Integer physicsStatus,
            @RequestParam(value = "sn", required = false) String sn) {
        
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
        
        ElectricityBatteryQuery electricityBatteryQuery = ElectricityBatteryQuery.builder().physicsStatus(physicsStatus)
                .businessStatus(businessStatus).sn(sn).franchiseeIds(franchiseeIds)
                .tenantId(TenantContextHolder.getTenantId()).build();
        return electricityBatteryService.queryBatteryOverview(electricityBatteryQuery);
    }
    
    @GetMapping("/admin/battery/location/map/list")
    public R queryBatteryMapLimit(@RequestParam("offset") Integer offset, @RequestParam("size") Integer size) {
        if (offset < 0) {
            offset = 0;
        }
        
        if (size < 0 || size > 100) {
            size = 10;
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
                return R.ok(Collections.emptyList());
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.emptyList());
        }
        
        return returnTripleResult(electricityBatteryService.queryBatteryMapList(offset, size, franchiseeIds));
        
    }
    
    @GetMapping("/admin/battery/location/map")
    public R queryBatteryMap(@RequestParam(value = "lat") Double lat, @RequestParam(value = "lon") Double lon,
            @RequestParam(value = "size", required = false, defaultValue = "10000") Long size,
            @RequestParam(value = "length", required = false, defaultValue = "5") Integer length) {
        return returnTripleResult(batteryGeoService.queryBatteryMap(lat, lon, size, length));
    }
    
    
    /**
     * 电池总览--电池统计
     *
     * @return
     */
    @GetMapping("/admin/battery/batteryStatistical")
    public R batteryStatistical() {
        
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
        
        ElectricityBatteryQuery electricityBatteryQuery = new ElectricityBatteryQuery();
        electricityBatteryQuery.setTenantId(TenantContextHolder.getTenantId());
        electricityBatteryQuery.setFranchiseeIds(franchiseeIds);
        
        return electricityBatteryService.batteryStatistical(electricityBatteryQuery);
    }
    
    /**
     * 根据电池名字获取电池详情
     * @return
     */
    @GetMapping("/admin/battery/selectByBatteryName")
    public R batteryInfo(@RequestParam(value = "offset") Long offset, @RequestParam(value = "size") Long size,
            @RequestParam(value = "name") String name) {
        ElectricityBatteryQuery batteryQuery = ElectricityBatteryQuery.builder().sn(name).offset(offset)
                .size(size).tenantId(TenantContextHolder.getTenantId()).build();
        
        return R.ok(electricityBatteryService.selectBatteryInfoByBatteryName(batteryQuery));
    }
    
    
    @GetMapping("/admin/battery/export")
    public void export(@RequestParam(value = "physicsStatus", required = false) Integer physicsStatus,
            @RequestParam(value = "businessStatus", required = false) Integer businessStatus,
            @RequestParam(value = "electricityCabinetName", required = false) String electricityCabinetName,
            @RequestParam(value = "chargeStatus", required = false) Integer chargeStatus,
            @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "power", required = false) Double power,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "franchiseeName", required = false) String franchiseeName, HttpServletResponse response){
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new CustomBusinessException("用户不存在");
        }
        
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            throw new CustomBusinessException("用户权限不足");
        }
        
        ElectricityBatteryQuery electricityBatteryQuery = new ElectricityBatteryQuery();
        electricityBatteryQuery.setOffset(0L);
        electricityBatteryQuery.setSize(Long.MAX_VALUE);
        electricityBatteryQuery.setPhysicsStatus(physicsStatus);
        electricityBatteryQuery.setBusinessStatus(businessStatus);
        electricityBatteryQuery.setSn(sn);
        electricityBatteryQuery.setPower(power);
        electricityBatteryQuery.setTenantId(TenantContextHolder.getTenantId());
        electricityBatteryQuery.setChargeStatus(chargeStatus);
        electricityBatteryQuery.setFranchiseeId(franchiseeId);
        electricityBatteryQuery.setElectricityCabinetName(electricityCabinetName);
        electricityBatteryQuery.setFranchiseeName(franchiseeName);
        
        electricityBatteryService.export(electricityBatteryQuery,response);
    }
}