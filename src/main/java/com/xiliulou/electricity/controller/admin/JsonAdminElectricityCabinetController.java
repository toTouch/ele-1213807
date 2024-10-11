package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.sms.SmsService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.dto.ElectricityCabinetOtherSetting;
import com.xiliulou.electricity.entity.EleCabinetCoreData;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.query.EleCabinetPatternQuery;
import com.xiliulou.electricity.query.EleOuterCommandQuery;
import com.xiliulou.electricity.query.ElectricityCabinetAddAndUpdate;
import com.xiliulou.electricity.query.ElectricityCabinetAddressQuery;
import com.xiliulou.electricity.query.ElectricityCabinetBatchEditRentReturnQuery;
import com.xiliulou.electricity.query.ElectricityCabinetImportQuery;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.query.ElectricityCabinetTransferQuery;
import com.xiliulou.electricity.query.HomepageBatteryFrequencyQuery;
import com.xiliulou.electricity.query.HomepageElectricityExchangeFrequencyQuery;
import com.xiliulou.electricity.request.asset.TransferCabinetModelRequest;
import com.xiliulou.electricity.service.EleCabinetCoreDataService;
import com.xiliulou.electricity.service.EleOnlineLogService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserTypeFactory;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.electricity.vo.HomepageBatteryVo;
import com.xiliulou.electricity.vo.HomepageElectricityExchangeFrequencyVo;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 换电柜表(TElectricityCabinet)表控制层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@RestController
@Slf4j
public class JsonAdminElectricityCabinetController extends BasicController {
    
    /**
     * 服务对象
     */
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    
    @Qualifier("alibabaSmsService")
    @Autowired
    SmsService smsService;
    
    @Autowired
    StoreService storeService;
    
    @Autowired
    FranchiseeService franchiseeService;
    
    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    UserTypeFactory userTypeFactory;
    
    @Autowired
    EleCabinetCoreDataService eleCabinetCoreDataService;
    
    @Autowired
    EleOnlineLogService eleOnlineLogService;
    
    @Autowired
    UserDataScopeService userDataScopeService;

    
    //修改换电柜
    @PutMapping(value = "/admin/electricityCabinet")
    @Log(title = "修改换电柜")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) ElectricityCabinetAddAndUpdate electricityCabinetAddAndUpdate) {
        return electricityCabinetService.edit(electricityCabinetAddAndUpdate);
    }
    
    @PutMapping(value = "/admin/electricityCabinet/updateAddress")
    @Log(title = "修改换电柜")
    public R updateAddress(@RequestBody @Validated ElectricityCabinetAddressQuery eleCabinetAddressQuery) {
        return returnTripleResult(electricityCabinetService.updateAddress(eleCabinetAddressQuery));
    }
    
    //删除换电柜
    @DeleteMapping(value = "/admin/electricityCabinet/{id}")
    @Log(title = "删除换电柜")
    public R delete(@PathVariable("id") Integer id) {
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return electricityCabinetService.delete(id);
    }
    
    /**
     * 获取换电柜扩展参数
     */
    @GetMapping(value = "/admin/electricityCabinet/extendData/{electricityCabinetId}")
    public R queryElectricityCabinetExtendData(@PathVariable("electricityCabinetId") Integer id) {
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return electricityCabinetService.queryElectricityCabinetExtendData(id);
    }
    
    //列表查询
    @GetMapping(value = "/admin/electricityCabinet/list")
    public R queryList(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "address", required = false) String address,
                       @RequestParam(value = "usableStatus", required = false) Integer usableStatus,
                       @RequestParam(value = "powerType", required = false) Integer powerType,
                       @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus,
                       @RequestParam(value = "stockStatus", required = false) Integer stockStatus,
                       @RequestParam(value = "warehouseId", required = false) Long warehouseId,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime,
                       @RequestParam(value = "id", required = false) Integer id,
                       @RequestParam(value = "idList", required = false) List<Integer> idList,
                       @RequestParam(value = "areaId", required = false) Long areaId,
            @RequestParam(value = "productKey", required = false) String productKey,
            @RequestParam(value = "deviceName", required = false) String deviceName,
            @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
        if (Objects.isNull(size) || size < 0 || size > 50) {
            size = 10L;
        }
        
        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }
        
        // 数据权校验
        Triple<List<Long>, List<Long>, Boolean> permissionTriple = checkPermission();
        if (!permissionTriple.getRight()) {
            return R.ok(Collections.emptyList());
        }
        
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Integer> eleIdList = null;
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getDataType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            
            eleIdList = userTypeService.getEleIdListByDataType(user);
            if (CollectionUtils.isEmpty(eleIdList)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .address(address)
                .powerType(powerType)
                .usableStatus(usableStatus)
                .onlineStatus(onlineStatus)
                .stockStatus(stockStatus)
                .warehouseId(warehouseId)
                .beginTime(beginTime)
                .endTime(endTime)
                .eleIdList(eleIdList)
                .id(id)
                .tenantId(TenantContextHolder.getTenantId())
                .franchiseeIdList(permissionTriple.getLeft())
                .storeIdList(permissionTriple.getMiddle())
                .areaId(areaId)
                .productKey(productKey)
                .deviceName(deviceName)
                .idList(idList)
                .sn(sn)
                .franchiseeId(franchiseeId)
                .build();

        return electricityCabinetService.queryList(electricityCabinetQuery);
    }
    
    
    //列表数量查询
    @GetMapping(value = "/admin/electricityCabinet/queryCount")
    public R queryCount(@RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "address", required = false) String address,
                        @RequestParam(value = "usableStatus", required = false) Integer usableStatus,
                        @RequestParam(value = "powerType", required = false) Integer powerType,
                        @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus,
                        @RequestParam(value = "stockStatus", required = false) Integer stockStatus,
                        @RequestParam(value = "warehouseId", required = false) Long warehouseId,
                        @RequestParam(value = "beginTime", required = false) Long beginTime,
                        @RequestParam(value = "endTime", required = false) Long endTime,
                        @RequestParam(value = "sn", required = false) String sn,
                        @RequestParam(value = "areaId", required = false) Long areaId,
                        @RequestParam(value = "idList", required = false) List<Integer> idList,
                        @RequestParam(value = "modelId", required = false) Integer modelId,
            @RequestParam(value = "productKey", required = false) String productKey,
            @RequestParam(value = "deviceName", required = false) String deviceName,
            @RequestParam(value = "version", required = false) String version,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {

        // 数据权校验
        Triple<List<Long>, List<Long>, Boolean> permissionTriple = checkPermission();
        if (!permissionTriple.getRight()) {
            return R.ok(Collections.emptyList());
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Integer> eleIdList = null;
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getDataType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            
            eleIdList = userTypeService.getEleIdListByDataType(user);
            if (CollectionUtils.isEmpty(eleIdList)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder()
                .name(name)
                .address(address)
                .powerType(powerType)
                .usableStatus(usableStatus)
                .onlineStatus(onlineStatus)
                .warehouseId(warehouseId)
                .beginTime(beginTime)
                .endTime(endTime)
                .eleIdList(eleIdList)
                .modelId(modelId)
                .sn(sn)
                .idList(idList)
                .areaId(areaId)
                .stockStatus(stockStatus)
                .tenantId(TenantContextHolder.getTenantId())
                .franchiseeIdList(permissionTriple.getLeft())
                .storeIdList(permissionTriple.getMiddle())
                .productKey(productKey)
                .deviceName(deviceName)
                .version(version)
                .franchiseeId(franchiseeId)
                .build();

        return electricityCabinetService.queryCount(electricityCabinetQuery);
    }
    
    //列表查询
    @GetMapping(value = "/admin/electricityCabinet/list/super")
    public R queryListSuper(@RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "address", required = false) String address, @RequestParam(value = "usableStatus", required = false) Integer usableStatus,
            @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus, @RequestParam(value = "stockStatus", required = false) Integer stockStatus,
            @RequestParam(value = "warehouseId", required = false) Long warehouseId, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "areaId", required = false) Long areaId) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (user.getTenantId() != 1) {
            return R.fail("权限不足");
        }
        
        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder().offset(offset).size(size).areaId(areaId).name(name).address(address)
                .usableStatus(usableStatus).stockStatus(stockStatus).warehouseId(warehouseId).onlineStatus(onlineStatus).beginTime(beginTime).endTime(endTime).eleIdList(null)
                .id(id).tenantId(null).build();
        
        return electricityCabinetService.queryList(electricityCabinetQuery);
    }
    
    //列表数量查询
    @GetMapping(value = "/admin/electricityCabinet/queryCount/super")
    public R queryCountSuper(@RequestParam(value = "name", required = false) String name, @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "usableStatus", required = false) Integer usableStatus, @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus,
            @RequestParam(value = "stockStatus", required = false) Integer stockStatus, @RequestParam(value = "warehouseId", required = false) Long warehouseId,
            @RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "areaId", required = false) Long areaId) {
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (user.getTenantId() != 1) {
            return R.fail("权限不足");
        }
        
        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder().name(name).address(address).areaId(areaId).usableStatus(usableStatus)
                .onlineStatus(onlineStatus).stockStatus(stockStatus).warehouseId(warehouseId).beginTime(beginTime).endTime(endTime).eleIdList(null).build();
        
        return electricityCabinetService.queryCount(electricityCabinetQuery);
    }
    
    @PutMapping("/admin/cabinet/onLineStatus/{id}")
    public R updateOnlineStatus(@PathVariable("id") Long id) {
        return returnTripleResult(electricityCabinetService.updateOnlineStatus(id));
    }
    
    /**
     * 查询空仓、有电池数量
     */
    @GetMapping("/admin/cabinet/battery/statistics/{id}")
    public R batteryStatistics(@PathVariable("id") Long id) {
        return R.ok(electricityCabinetService.batteryStatistics(id));
    }
    
    //禁启用换电柜
    @PutMapping(value = "/admin/electricityCabinet/updateStatus")
    @Log(title = "禁/启用换电柜")
    public R updateStatus(@RequestParam("id") Integer id, @RequestParam("usableStatus") Integer usableStatus) {
        return electricityCabinetService.updateStatus(id, usableStatus);
    }
    
    /**
     * @description 首页一：运营小程序 只响应 柜机总数、在线柜机数
     * @date 2023/12/7 15:13:34
     * @author HeYafeng
     */
    @GetMapping(value = "/admin/electricityCabinet/homeOne/v2")
    public R homeOneV2() {
        return electricityCabinetService.homeOneV2();
    }
    
    // TODO: 2022/10/15 危险接口
    //发送命令
    @PostMapping(value = "/admin/electricityCabinet/command")
    public R sendCommandToEleForOuterV2(@RequestBody EleOuterCommandQuery eleOuterCommandQuery) {
        return electricityCabinetService.sendCommandToEleForOuter(eleOuterCommandQuery);
        
    }
    
    @PostMapping(value = "/admin/electricityCabinet/command/super")
    public R sendCommandToEleForOuterV2Super(@RequestBody EleOuterCommandQuery eleOuterCommandQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (user.getTenantId() != 1) {
            return R.fail("权限不足");
        }
        return electricityCabinetService.sendCommandToEleForOuterSuper(eleOuterCommandQuery);
        
    }
    
    
    //查看开门命令
    @GetMapping("/admin/electricityCabinet/open/check")
    public R checkOpenSession(@RequestParam("sessionId") String sessionId) {
        if (StrUtil.isEmpty(sessionId)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return electricityCabinetService.checkOpenSessionId(sessionId);
    }
    
    //解锁电柜
    @PostMapping(value = "/admin/electricityCabinet/unlockCabinet")
    public R unlockCabinet(@RequestParam("id") Integer id) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        //限制解锁权限
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER) && !Objects.equals(user.getType(), User.TYPE_USER_NORMAL_ADMIN)) {
            log.info("USER TYPE ERROR! not found operate service! userType={}", user.getType());
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(id);
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        
        if (!Objects.equals(electricityCabinet.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }
        
        //发送命令
        HashMap<String, Object> dataMap = Maps.newHashMap();
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(UUID.randomUUID().toString().replace("-", "")).data(dataMap)
                .productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName()).command(ElectricityIotConstant.ELE_COMMAND_UNLOCK_CABINET).build();
        
        eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
        //删除缓存
        redisService.delete(CacheConstant.UNLOCK_CABINET_CACHE + electricityCabinet.getId());
        
        redisService.delete(CacheConstant.ORDER_ELE_ID + id);
        return R.ok();
    }
    
    //列表查询
    @GetMapping(value = "/admin/electricityCabinet/queryNameList")
    public R queryNameList(@RequestParam("size") Long size, @RequestParam("offset") Long offset) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Integer> eleIdList = null;
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            
            eleIdList = userTypeService.getEleIdListByDataType(user);
            if (CollectionUtils.isEmpty(eleIdList)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        return R.ok(electricityCabinetService.queryNameList(size, offset, eleIdList, TenantContextHolder.getTenantId()));
    }
    
    /**
     * 读取柜机配置信息
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/admin/electricityCabinet/queryConfig")
    public R queryConfig(@RequestParam("id") Integer id) {
        
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(id);
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        
        if (!Objects.equals(electricityCabinet.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }

        ElectricityCabinetOtherSetting otherSetting = redisService.getWithHash(CacheConstant.OTHER_CONFIG_CACHE_V_2 + electricityCabinet.getId(),
                ElectricityCabinetOtherSetting.class);
        
        return R.ok(otherSetting);
    }
    
    //列表查询
    @GetMapping(value = "/admin/electricityCabinet/{id}")
    public R queryById(@PathVariable("id") Integer id) {
        return electricityCabinetService.queryById(id);
    }
    
    /**
     * 查询换电柜所属加盟商
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/admin/cabinetBelongFranchisee/{id}")
    public R queryCabinetBelongFranchisee(@PathVariable("id") Integer id) {
        return electricityCabinetService.queryCabinetBelongFranchisee(id);
    }
    
    /**
     * admin查看所有换电柜
     *
     * @return
     */
    @GetMapping(value = "/admin/electricityCabinet/allCabinet")
    public R queryAllElectricityCabinet(@RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam(value = "name", required = false) String name) {
        
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
            log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getType());
            return R.fail("ELECTRICITY.0066", "用户权限不足");
            
        }
        
        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder().size(size).offset(offset).name(name).build();
        
        return electricityCabinetService.queryAllElectricityCabinet(electricityCabinetQuery);
    }
    
    //核心板上报数据详情
    @GetMapping(value = "/admin/electricityCabinet/core_data_list/{electricityCabinetId}")
    public R queryEleCabinetCoreDataList(@PathVariable(value = "electricityCabinetId") Integer electricityCabinetId) {
        
        EleCabinetCoreData eleCabinetCoreData = eleCabinetCoreDataService.selectByEleCabinetId(electricityCabinetId);
        return R.ok(eleCabinetCoreData);
    }
    
    //首页概述详情统计
    @GetMapping(value = "/admin/electricityCabinet/homepageOverviewDetail")
    public R homepageOverviewDetail() {
        return electricityCabinetService.homepageOverviewDetail();
    }
    
    //首页用户分析
    @GetMapping(value = "/admin/electricityCabinet/homepageUserAnalysis")
    public R homepageUserAnalysis(@RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime) {
        return electricityCabinetService.homepageUserAnalysis(beginTime, endTime);
    }
    
    //首页柜机分析
    @GetMapping(value = "/admin/electricityCabinet/homepageElectricityCabinetAnalysis")
    public R homepageElectricityCabinetAnalysis() {
        return electricityCabinetService.homepageElectricityCabinetAnalysis();
    }
    
    //首页换电频次
    @GetMapping(value = "/admin/electricityCabinet/homepageExchangeOrderFrequency")
    public R homepageExchangeOrderFrequency(@RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId) {
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.fail("AUTH.0002", "没有权限操作！");
        }
        
        List<Integer> eleIdList = null;
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userDataType:{}", user.getDataType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            eleIdList = userTypeService.getEleIdListByDataType(user);
            if (org.springframework.util.CollectionUtils.isEmpty(eleIdList)) {
                return R.ok(new HomepageElectricityExchangeFrequencyVo());
            }
            
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.springframework.util.CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(new HomepageElectricityExchangeFrequencyVo());
            }
        }
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        HomepageElectricityExchangeFrequencyQuery homepageElectricityExchangeFrequencyQuery = HomepageElectricityExchangeFrequencyQuery.builder().beginTime(beginTime)
                .endTime(endTime).size(size).offset(offset).electricityCabinetId(electricityCabinetId).tenantId(tenantId).eleIdList(eleIdList).franchiseeIds(franchiseeIds).build();
        
        return electricityCabinetService.homepageExchangeOrderFrequency(homepageElectricityExchangeFrequencyQuery);
    }
    
    //首页电池分析
    @GetMapping(value = "/admin/electricityCabinet/homepageBatteryAnalysis")
    public R homepageBatteryAnalysis(@RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam(value = "batterySn", required = false) String batterySn) {
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.fail("AUTH.0002", "没有权限操作！");
        }
        
        List<Integer> eleIdList = null;
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userDataType:{}", user.getDataType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            eleIdList = userTypeService.getEleIdListByDataType(user);
            if (org.springframework.util.CollectionUtils.isEmpty(eleIdList)) {
                return R.ok(new HomepageBatteryVo());
            }
            
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.springframework.util.CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(new HomepageBatteryVo());
            }
        }
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        HomepageBatteryFrequencyQuery homepageBatteryFrequencyQuery = HomepageBatteryFrequencyQuery.builder().batterySn(batterySn).beginTime(beginTime).endTime(endTime)
                .offset(offset).size(size).franchiseeIds(franchiseeIds).tenantId(tenantId).build();
        
        return electricityCabinetService.homepageBatteryAnalysis(homepageBatteryFrequencyQuery);
    }
    
    /**
     * ota操作： 1--下载新  2-- 同步  3--升级
     */
    @PostMapping("/admin/electricityCabinet/ota/command")
    public R otaCommand(@RequestParam("eid") Integer eid, @RequestParam("operateType") Integer operateType, @RequestParam("versionType") Integer versionType,
            @RequestParam(value = "cellNos", required = false) List<Integer> cellNos) {
        return electricityCabinetService.otaCommand(eid, operateType, versionType, cellNos);
    }
    
    /**
     * ota操作检查
     */
    @GetMapping("/admin/electricityCabinet/ota/check")
    public R checkOtaSession(@RequestParam("sessionId") String sessionId) {
        if (StrUtil.isEmpty(sessionId)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return electricityCabinetService.checkOtaSession(sessionId);
    }
    
    @GetMapping("/admin/electricityCabinet/onlineLogList")
    public R getOnlineLogList(@RequestParam("size") Integer size, @RequestParam("offset") Integer offset, @RequestParam(value = "status", required = false) String status,
            @RequestParam("eleId") Integer eleId) {
        if (size < 0 || size > 50) {
            size = 50;
        }
        
        if (offset < 0) {
            offset = 0;
        }
        
        return eleOnlineLogService.queryOnlineLogList(size, offset, status, eleId);
    }
    
    
    @GetMapping("/admin/electricityCabinet/onlineLogCount")
    public R getOnlineLogCount(@RequestParam(value = "status", required = false) String status, @RequestParam("eleId") Integer eleId) {
        return eleOnlineLogService.queryOnlineLogCount(status, eleId);
    }
    
    /**
     * 列表页搜索接口
     *
     * @return
     */
    @GetMapping("/admin/electricityCabinet/search")
    public R search(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "storeId", required = false) Long storeId) {
        
        if (size < 0 || size > 50) {
            size = 20;
        }
        
        if (offset < 0) {
            offset = 0;
        }
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Integer> eleIdList = null;
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getDataType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            
            eleIdList = userTypeService.getEleIdListByDataType(user);
            if (CollectionUtils.isEmpty(eleIdList)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        ElectricityCabinetQuery cabinetQuery = ElectricityCabinetQuery.builder().size(size).offset(offset).name(name).tenantId(TenantContextHolder.getTenantId())
                .eleIdList(eleIdList).storeId(storeId).build();
        
        return R.ok(electricityCabinetService.eleCabinetSearch(cabinetQuery));
    }
    
    
    @GetMapping("/admin/electricityCabinet/queryName")
    public R queryName(@RequestParam(value = "eleId", required = false) Integer eleId, @RequestParam(value = "name", required = false) String name) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Integer> eleIdList = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE) || Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userDataType={}", user.getDataType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            
            eleIdList = userTypeService.getEleIdListByDataType(user);
            if (CollectionUtils.isEmpty(eleIdList)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        ElectricityCabinetQuery query = new ElectricityCabinetQuery();
        query.setId(eleId);
        query.setName(name);
        query.setEleIdList(eleIdList);
        query.setTenantId(TenantContextHolder.getTenantId());
        
        return R.ok(electricityCabinetService.selectByQuery(query));
    }
    
    @GetMapping("/admin/electricityCabinet/superAdminQueryName")
    public R superAdminQueryName(@RequestParam(value = "eleId", required = false) Integer eleId, @RequestParam(value = "name", required = false) String name) {
        
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)) {
            return R.fail("AUTH.0002", "没有权限操作！");
        }
        ElectricityCabinetQuery query = new ElectricityCabinetQuery();
        query.setId(eleId);
        query.setName(name);
        
        return R.ok(electricityCabinetService.superAdminSelectByQuery(query));
    }
    
    /**
     * 根据经纬度获取柜机列表
     * @param status 0-全部、1-少电、2-多电、3-锁仓、4-离线
     *
     * @return
     */
    @GetMapping("/admin/electricityCabinet/listByLongitudeAndLatitude")
    public R selectEleCabinetListByLongitudeAndLatitude(@RequestParam(value = "id", required = false) Integer id, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "status", required = false) Integer status) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Integer> eleIdList = null;
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getDataType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            
            eleIdList = userTypeService.getEleIdListByDataType(user);
            if (CollectionUtils.isEmpty(eleIdList)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        ElectricityCabinetQuery cabinetQuery = ElectricityCabinetQuery.builder().id(id).name(name).status(Objects.isNull(status) ? NumberConstant.ONE : status)
                .tenantId(TenantContextHolder.getTenantId()).eleIdList(eleIdList).build();
        
        return electricityCabinetService.selectEleCabinetListByLongitudeAndLatitude(cabinetQuery);
    }
    
    
    /**
     * 获取上传柜机照片所需的签名
     */
    @GetMapping(value = "/admin/acquire/upload/cabiet/file/sign")
    public R getUploadCabinetFileSign() {
        return electricityCabinetService.acquireIdcardFileSign();
    }
    
    @GetMapping(value = "/admin/electricityCabinet/batchOperate/list")
    public R batchOperateList(@RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "modelId", required = false) Integer modelId, @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus, @RequestParam(value = "version", required = false) String version) {
        if (Objects.isNull(size) || size < 0 || size > 50) {
            size = 10L;
        }
        
        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }
        
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Integer> eleIdList = null;
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getDataType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            
            eleIdList = userTypeService.getEleIdListByDataType(user);
            if (CollectionUtils.isEmpty(eleIdList)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
    
        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder().size(size).offset(offset).name(name).modelId(modelId).sn(sn).onlineStatus(onlineStatus)
                .version(version).eleIdList(eleIdList).tenantId(TenantContextHolder.getTenantId()).build();
        
        return electricityCabinetService.batchOperateList(electricityCabinetQuery);
    }
    
    /**
     * 查询租户下是否有该换电柜 按三元组
     */
    @GetMapping(value = "/admin/electricityCabinet/existsElectricityCabinet")
    public R existsElectricityCabinet(@RequestParam("productKey") String productKey, @RequestParam("deviceName") String deviceName) {
        return returnTripleResult(electricityCabinetService.existsElectricityCabinet(productKey, deviceName));
    }
    
    /**
     * 批量删除柜机
     */
    @PostMapping(value = "/admin/electricityCabinet/batchDelete")
    public R batchDelete(@RequestBody Set<Integer> ids) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return returnTripleResult(electricityCabinetService.batchDeleteCabinet(ids));
    }
    
    /**
     * 批量导入柜机
     */
    @PostMapping(value = "/admin/electricityCabinet/batchImport")
    public R batchImport(@RequestBody @Validated List<ElectricityCabinetImportQuery> list) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        if (list.size() > 200) {
            return R.fail("100563", "Excel模版中电柜数据不能超过200条，请检查修改后再操作");
        }
        
        return returnTripleResult(electricityCabinetService.batchImportCabinet(list));
    }
    
    /**
     * 批量修改柜机地址
     */
    @PostMapping(value = "/admin/electricityCabinet/batchUpdate")
    public R batchUpdateAddress(@RequestBody @Validated List<ElectricityCabinet> list) {
        return returnTripleResult(electricityCabinetService.batchUpdateAddress(list));
    }
    
    
    /**
     * 查询迁移柜机的型号：如果根据仓门数查询到多个型号，需要让用户选择迁移的型号
     * 将工厂账号下柜机迁移到扫码租户下，并物理删除工厂租户下的柜机信息
     */
    @PostMapping("/admin/electricityCabinet/transfer/queryModel")
    public R queryTransferCabinetModel(@RequestBody @Validated TransferCabinetModelRequest cabinetModelRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return returnTripleResult(electricityCabinetService.listTransferCabinetModel(cabinetModelRequest));
    }
    
    /**
     * 迁移柜机
     * 将工厂账号下柜机迁移到扫码租户下，并物理删除工厂租户下的柜机信息
     */
    @PostMapping("/admin/electricityCabinet/transfer")
    public R transferCabinet(@RequestBody @Validated ElectricityCabinetTransferQuery query) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return returnTripleResult(electricityCabinetService.transferCabinet(query));
    }
    
    /**
     * 柜机数据导出
     */
    @GetMapping(value = "/admin/electricityCabinet/exportExcel")
    public void exportExcel(@RequestParam(value = "name", required = false) String name, @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "usableStatus", required = false) Integer usableStatus, @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus,
            @RequestParam(value = "stockStatus", required = false) Integer stockStatus, @RequestParam(value = "warehouseId", required = false) Long warehouseId,
            @RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "id", required = false) Integer id, HttpServletResponse response) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new CustomBusinessException("用户不存在");
        }
        
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            throw new CustomBusinessException("用户权限不足");
        }
        
        ElectricityCabinetQuery query = ElectricityCabinetQuery.builder().size(Long.MAX_VALUE).offset(0L).name(name).address(address).usableStatus(usableStatus)
                .onlineStatus(onlineStatus).stockStatus(stockStatus).warehouseId(warehouseId).beginTime(beginTime).endTime(endTime).id(id)
                .tenantId(TenantContextHolder.getTenantId()).build();
        
        electricityCabinetService.exportExcel(query, response);
    }
    
    /**
     * 编辑换电柜租退标准回显
     */
    @GetMapping(value = "/admin/electricityCabinet/rentReturnEditEcho")
    public R rentReturnEditEcho(@RequestParam("id") Long id) {
        return R.ok(electricityCabinetService.rentReturnEditEcho(id));
    }
    
    /**
     * 批量编辑租退标准
     */
    @PostMapping(value = "/admin/electricityCabinet/batchEditRentReturn")
    public R batchEditRentReturn(@RequestBody @Validated ElectricityCabinetBatchEditRentReturnQuery rentReturnQuery) {
        return electricityCabinetService.batchEditRentReturn(rentReturnQuery);
    }
    
    /**
     * 运维端编辑租退标准回显
     */
    @GetMapping(value = "/admin/electricityCabinet/rentReturnEditEchoToOps")
    public R rentReturnEditEchoByDeviceName(@RequestParam("productKey") String productKey, @RequestParam("deviceName") String deviceName) {
        return electricityCabinetService.rentReturnEditEchoByDeviceName(productKey, deviceName);
    }
    
    /**
     * 修改柜机模式
     */
//    @PostMapping(value = "/admin/electricityCabinet/batchUpdateCabinetPattern")
//    public R batchUpdateCabinetPattern(@RequestBody @Validated ElectricityCabinetBatchEditRentReturnQuery rentReturnQuery) {
//
//    }
    
    /**
     * 修改柜机模式
     */
    @PostMapping(value = "/admin/electricityCabinet/updateCabinetPattern")
    public R updateCabinetPattern(@RequestBody @Validated EleCabinetPatternQuery query) {
        if (!SecurityUtils.isAdmin()) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return electricityCabinetService.updateCabinetPattern(query);
    }
    
    
}
