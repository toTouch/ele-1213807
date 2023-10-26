package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.core.wp.entity.AppTemplateQuery;
import com.xiliulou.core.wp.service.WeChatAppTemplateService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.config.WechatTemplateNotificationConfig;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.dto.BatteryExcelV3DTO;
import com.xiliulou.electricity.dto.bms.BatteryInfoDto;
import com.xiliulou.electricity.dto.bms.BatteryTrackDto;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityBatteryMapper;
import com.xiliulou.electricity.query.BatteryExcelV3Query;
import com.xiliulou.electricity.query.BindElectricityBatteryQuery;
import com.xiliulou.electricity.query.EleBatteryQuery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.query.HomepageBatteryFrequencyQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.excel.AutoHeadColumnWidthStyleStrategy;
import com.xiliulou.electricity.service.retrofit.BatteryPlatRetrofitService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.AESUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.*;
import com.xiliulou.electricity.web.query.battery.BatteryBatchOperateQuery;
import com.xiliulou.electricity.web.query.battery.BatteryInfoQuery;
import com.xiliulou.electricity.web.query.battery.BatteryLocationTrackQuery;
import com.xiliulou.electricity.web.query.battery.BatteryModifyQuery;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * 换电柜电池表(ElectricityBattery)表服务实现类
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
@Service
@Slf4j
public class ElectricityBatteryServiceImpl extends ServiceImpl<ElectricityBatteryMapper, ElectricityBattery>
        implements ElectricityBatteryService, CommonConstant{
    
    @Resource
    private ElectricityBatteryMapper electricitybatterymapper;
    
    @Autowired
    StoreService storeService;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    
    @Autowired
    WechatTemplateNotificationConfig wechatTemplateNotificationConfig;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    UserService userService;
    
    @Autowired
    WechatTemplateAdminNotificationService wechatTemplateAdminNotificationService;
    
    @Autowired
    WeChatAppTemplateService weChatAppTemplateService;
    
    @Autowired
    FranchiseeService franchiseeService;
    
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    
    @Autowired
    TemplateConfigService templateConfigService;
    
    @Autowired
    UserOauthBindService userOauthBindService;
    
    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;
    
    @Autowired
    RentBatteryOrderService rentBatteryOrderService;
    
    @Autowired
    ElectricityConfigService electricityConfigService;
    
    @Autowired
    BatteryGeoService geoService;
    
    @Autowired
    BatteryModelService batteryModelService;
    
    
    @Autowired
    BatteryPlatRetrofitService batteryPlatRetrofitService;
    
    @Autowired
    TenantService tenantService;
    
    protected ExecutorService bmsBatteryInsertThread = XllThreadPoolExecutors.newFixedThreadPool("BMS-BATTERY-INSERT-POOL", 1, "bms-battery-insert-pool-thread");
    
    /**
     * 根据电池SN码集查询
     *
     * @param tenantId 租户ID
     * @param snList   电池SN码
     * @return 电池信息集
     */
    @Slave
    @Override
    public List<ElectricityBattery> selectBySnList(Integer tenantId, List<String> snList) {
        if (ObjectUtils.isEmpty(tenantId) || CollectionUtils.isEmpty(snList)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ElectricityBattery> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ElectricityBattery::getTenantId, tenantId).in(ElectricityBattery::getSn, snList);
        return electricitybatterymapper.selectList(queryWrapper);
    }
    
    /**
     * 保存电池
     *
     * @param
     * @return
     */
    @Override
    @Transactional
    public R saveElectricityBattery(EleBatteryQuery query) {
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Integer count = electricitybatterymapper.selectCount(
                new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getSn, query.getSn())
                        .eq(ElectricityBattery::getDelFlag, ElectricityBattery.DEL_NORMAL));
        if (count > 0) {
            return R.fail("100224", "该电池SN已存在!无法重复创建");
        }
        
        
        Long franchiseeId = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            Store store = storeService.queryByUid(user.getUid());
            if (Objects.nonNull(store)) {
                franchiseeId = store.getFranchiseeId();
            }
        }
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
            if (Objects.nonNull(franchisee)) {
                franchiseeId = franchisee.getId();
            }
        }
        
        Pair<Boolean, String> result = callBatteryPlatSaveSn(Collections.singletonList(query.getSn()), query.getIsNeedSync());
        if (!result.getKey()) {
            return R.fail("200005", result.getRight());
        }
        
        ElectricityBattery saveBattery = new ElectricityBattery();
        BeanUtils.copyProperties(query, saveBattery);
        saveBattery.setFranchiseeId(franchiseeId);
        saveBattery.setPhysicsStatus(ElectricityBattery.PHYSICS_STATUS_NOT_WARE_HOUSE);
        saveBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_INPUT);
        saveBattery.setCreateTime(System.currentTimeMillis());
        saveBattery.setUpdateTime(System.currentTimeMillis());
        saveBattery.setTenantId(tenantId);
        electricitybatterymapper.insert(saveBattery);
        
        return R.ok();
    }
    
    @Override
    public R saveBatchFromExcel(BatteryExcelV3Query batteryExcelV3Query, Long uid) {
        Long franchiseeId = batteryExcelV3Query.getFranchiseeId();
        Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
        if (Objects.isNull(franchisee)) {
            log.error("Franchisee id is invalid! uid = {}", uid);
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }
    
        List<BatteryExcelV3DTO> batteryV3List = batteryExcelV3Query.getBatteryList();
        if(CollectionUtils.isEmpty(batteryV3List)){
            return R.fail("100601", "Excel模版中电池数据为空，请检查修改后再操作");
        }
        
        if (EXCEL_MAX_COUNT_TWO_THOUSAND < batteryV3List.size()) {
            return R.fail("100600", "Excel模版中数据不能超过2000条，请检查修改后再操作");
        }
        
        List<ElectricityBattery> saveList = new ArrayList<>();
        Set<String> snSet = new HashSet<>();
        
        for (BatteryExcelV3DTO batteryExcelV3DTO : batteryV3List) {
            if(ObjectUtils.isEmpty(batteryExcelV3DTO)){
                continue;
            }
            
            String sn = batteryExcelV3DTO.getSn();
            if (StringUtils.isEmpty(sn)) {
                continue;
            }
            
            // 判断数据库中是否已经存在该电池
            Integer exist = electricitybatterymapper.existBySn(sn);
            if (Objects.nonNull(exist)) {
                continue;
            }
            snSet.add(sn);
            
            // 构建ElectricityBattery持久化对象
            ElectricityBattery electricityBattery = new ElectricityBattery();
            
            electricityBattery.setSn(sn);
            electricityBattery.setModel(batteryModelService.analysisBatteryTypeByBatteryName(sn));
            electricityBattery.setVoltage(ObjectUtils.isEmpty(batteryExcelV3DTO.getV()) ? 0 : batteryExcelV3DTO.getV());
            electricityBattery.setCapacity(ObjectUtils.isEmpty(batteryExcelV3DTO.getC()) ? 0 : batteryExcelV3DTO.getC());
            electricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_INPUT);
            electricityBattery.setPhysicsStatus(ElectricityBattery.PHYSICS_STATUS_NOT_WARE_HOUSE);
            electricityBattery.setCreateTime(System.currentTimeMillis());
            electricityBattery.setUpdateTime(System.currentTimeMillis());
            electricityBattery.setPower(0.0);
            electricityBattery.setExchangeCount(0);
            electricityBattery.setChargeStatus(0);
            electricityBattery.setHealthStatus(0);
            electricityBattery.setDelFlag(ElectricityBattery.DEL_NORMAL);
            electricityBattery.setStatus(ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE);
            electricityBattery.setTenantId(TenantContextHolder.getTenantId());
            electricityBattery.setFranchiseeId(franchiseeId);
            
            saveList.add(electricityBattery);
        }
    
        if(CollectionUtils.isEmpty(snSet)){
            return R.fail("100603", "Excel模版中所有电池数据均已存在，请勿重复导入");
        }
        
        Map<String, String> headers = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        headers.put(CommonConstant.INNER_HEADER_APP, CommonConstant.APP_SAAS);
        headers.put(CommonConstant.INNER_HEADER_TIME, time);
        headers.put(CommonConstant.INNER_HEADER_INNER_TOKEN, AESUtils.encrypt(time, CommonConstant.APP_SAAS_AES_KEY));
        headers.put(CommonConstant.INNER_TENANT_ID, tenantService.queryByIdFromCache(TenantContextHolder.getTenantId()).getCode());
    
        BatteryBatchOperateQuery query = new BatteryBatchOperateQuery();
        query.setJsonBatterySnList(JsonUtil.toJson(snSet));
    
        // 线程池异步执行:保存到BMS系统中
        bmsBatteryInsertThread.execute(() -> {
            R r = batteryPlatRetrofitService.batchSave(headers, query);
            if (!r.isSuccess()) {
                log.error("CALL BATTERY ERROR! msg={},uid={}", r.getErrMsg(), uid);
            }
        });
    
        // 保存到本地数据库
        insertBatch(saveList);
        return R.ok();
    }
    
    private Pair<Boolean, String> callBatteryPlatSaveSn(List<String> list, Integer isNeedSync) {
        if (Objects.isNull(isNeedSync)) {
            return Pair.of(true, null);
        }
        
        Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(tenant)) {
            return Pair.of(false, "租户信息不能为空");
        }
        
        Map<String, String> headers = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        headers.put(CommonConstant.INNER_HEADER_APP, CommonConstant.APP_SAAS);
        headers.put(CommonConstant.INNER_HEADER_TIME, time);
        headers.put(CommonConstant.INNER_HEADER_INNER_TOKEN, AESUtils.encrypt(time, CommonConstant.APP_SAAS_AES_KEY));
        headers.put(CommonConstant.INNER_TENANT_ID, tenant.getCode());
        
        BatteryBatchOperateQuery batteryBatchOperateQuery = new BatteryBatchOperateQuery();
        batteryBatchOperateQuery.setJsonBatterySnList(JsonUtil.toJson(list));
        
        
        R r = batteryPlatRetrofitService.batchSave(headers, batteryBatchOperateQuery);
        if (!r.isSuccess()) {
            log.error("CALL BATTERY ERROR! msg={},uid={}", r.getErrMsg(), SecurityUtils.getUid());
            return Pair.of(false, r.getErrMsg());
        }
        return Pair.of(true, null);
    }
    
    private Pair<Boolean, String> callBatteryPlatDeleteSn(List<String> list, Integer isNeedSync) {
        if (Objects.isNull(isNeedSync)) {
            return Pair.of(true, null);
        }
        
        
        Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(tenant)) {
            return Pair.of(false, "租户信息不能为空");
        }
        
        Map<String, String> headers = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        headers.put(CommonConstant.INNER_HEADER_APP, CommonConstant.APP_SAAS);
        headers.put(CommonConstant.INNER_HEADER_TIME, time);
        headers.put(CommonConstant.INNER_HEADER_INNER_TOKEN, AESUtils.encrypt(time, CommonConstant.APP_SAAS_AES_KEY));
        headers.put(CommonConstant.INNER_TENANT_ID, tenant.getCode());
        
        BatteryBatchOperateQuery batteryBatchOperateQuery = new BatteryBatchOperateQuery();
        batteryBatchOperateQuery.setJsonBatterySnList(JsonUtil.toJson(list));
        R r = batteryPlatRetrofitService.batchDelete(headers, batteryBatchOperateQuery);
        if (!r.isSuccess()) {
            log.error("CALL BATTERY ERROR! msg={},uid={}", r.getErrMsg(), SecurityUtils.getUid());
            return Pair.of(false, r.getErrMsg());
        }
        return Pair.of(true, null);
    }
    
    private Pair<Boolean, String> callBatteryPlatModify(String newSn, String oldSn, Integer isNeedSync) {
        if (Objects.isNull(isNeedSync)) {
            return Pair.of(true, null);
        }
        
        Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(tenant)) {
            return Pair.of(false, "租户信息不能为空");
        }
        
        Map<String, String> headers = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        headers.put(CommonConstant.INNER_HEADER_APP, CommonConstant.APP_SAAS);
        headers.put(CommonConstant.INNER_HEADER_TIME, time);
        headers.put(CommonConstant.INNER_HEADER_INNER_TOKEN, AESUtils.encrypt(time, CommonConstant.APP_SAAS_AES_KEY));
        headers.put(CommonConstant.INNER_TENANT_ID, tenant.getCode());
        
        BatteryModifyQuery query = new BatteryModifyQuery();
        query.setNewSn(newSn);
        query.setOriginalSn(oldSn);
        R r = batteryPlatRetrofitService.modifyBatterySn(headers, query);
        if (!r.isSuccess()) {
            log.error("CALL BATTERY ERROR! msg={},uid={}", r.getErrMsg(), SecurityUtils.getUid());
            return Pair.of(false, r.getErrMsg());
        }
        return Pair.of(true, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> queryBatteryLocationTrack(Long uid, Long beginTime, Long endTime) {
        String sn = electricitybatterymapper.querySnByUid(uid);
        if (StrUtil.isEmpty(sn)) {
            return Triple.of(true, null, null);
        }
        
        BatteryLocationTrackQuery query = new BatteryLocationTrackQuery();
        query.setSn(sn);
        query.setBeginTime(beginTime);
        query.setEndTime(endTime);
        
        Triple<Boolean, String, List<BatteryTrackDto>> result = callBatteryServiceQueryBatteryTrack(query);
        if (!result.getLeft() || Objects.isNull(result.getRight())) {
            log.error("CALL BATTERY ERROR! uid={},msg={}", uid, result.getMiddle());
            return Triple.of(false, "200005", result.getMiddle());
        }
        
        
        return Triple.of(true, null, result.getRight());
    }
    
    @Override
    public void export(ElectricityBatteryQuery query, HttpServletResponse response) {
        List<ElectricityBattery> electricityBatteryList = electricitybatterymapper.queryList(query, NumberConstant.ZERO_L, Long.MAX_VALUE);
        if (CollectionUtils.isEmpty(electricityBatteryList)) {
            throw new CustomBusinessException("柜机列表为空！");
        }
        
        List<ElectricityBatteryExcelVO> excelVOS = new ArrayList<>(electricityBatteryList.size());
        int index = 0;
        
        for (ElectricityBattery battery : electricityBatteryList) {
            Franchisee franchisee = franchiseeService.queryByIdFromCache(battery.getFranchiseeId());
            UserInfo userInfo = userInfoService.queryByUidFromCache(battery.getUid());
            
            index++;
            
            ElectricityBatteryExcelVO excelVO = new ElectricityBatteryExcelVO();
            excelVO.setId(index);
            excelVO.setSn(battery.getSn());
            excelVO.setModel(battery.getModel());
            excelVO.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : "");
            excelVO.setPhysicsStatus(Objects.equals(battery.getPhysicsStatus(), ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE) ? "在仓" : "不在仓");
            excelVO.setBusinessStatus(acquireBatteryBusinessStatus(battery));
            excelVO.setUserName(Objects.isNull(userInfo) ? "" : userInfo.getName());
            excelVO.setIotCardNumber(battery.getIotCardNumber());
            excelVO.setCreateTime(Objects.isNull(battery.getCreateTime()) ? "" :DateUtil.format(DateUtil.date(battery.getCreateTime()), DatePattern.NORM_DATETIME_FORMATTER));
            
            if (Objects.nonNull(battery.getElectricityCabinetId())) {
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(battery.getElectricityCabinetId());
                excelVO.setCabinetName(Objects.nonNull(electricityCabinet) ? electricityCabinet.getName() : "");
            }
            
            excelVOS.add(excelVO);
        }
        
        String fileName = "电池列表.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, ElectricityBatteryExcelVO.class).sheet("sheet").registerWriteHandler(new AutoHeadColumnWidthStyleStrategy()).doWrite(excelVOS);
            return;
        } catch (IOException e) {
            log.error("导出报表失败！", e);
        }
    }
    
    private String acquireBatteryBusinessStatus(ElectricityBattery battery) {
        String result = "";
        if (Objects.isNull(battery) || Objects.isNull(battery.getBusinessStatus())) {
            return result;
        }
        
        switch (battery.getBusinessStatus()) {
            case 1:
                result = "已录入";
                break;
            case 2:
                result = "租借";
                break;
            case 3:
                result = "归还";
                break;
            case 4:
                result = "异常交换";
                break;
            default:
                result = "未知";
                break;
        }
        
        return result;
    }
    
    private Triple<Boolean, String, List<BatteryTrackDto>> callBatteryServiceQueryBatteryTrack(BatteryLocationTrackQuery batteryLocationTrackQuery) {
        Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(tenant)) {
            return Triple.of(false, "租户信息不能为空", null);
        }
        
        Map<String, String> headers = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        headers.put(CommonConstant.INNER_HEADER_APP, CommonConstant.APP_SAAS);
        headers.put(CommonConstant.INNER_HEADER_TIME, time);
        headers.put(CommonConstant.INNER_HEADER_INNER_TOKEN, AESUtils.encrypt(time, CommonConstant.APP_SAAS_AES_KEY));
        headers.put(CommonConstant.INNER_TENANT_ID, tenant.getCode());
        
        R<List<BatteryTrackDto>> r = batteryPlatRetrofitService.queryBatteryTrack(headers, batteryLocationTrackQuery);
        if (!r.isSuccess()) {
            log.error("CALL BATTERY ERROR! msg={},uid={}", r.getErrMsg(), SecurityUtils.getUid());
            return Triple.of(false, r.getErrMsg(), null);
        }
        return Triple.of(true, null, r.getData());
    }
    
    
    private Triple<Boolean, String, BatteryInfoDto> callBatteryServiceQueryBatteryInfo(BatteryInfoQuery batteryInfoQuery,Integer tenantId) {
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            return Triple.of(false, "租户信息不能为空", null);
        }
        
        Map<String, String> headers = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        headers.put(CommonConstant.INNER_HEADER_APP, CommonConstant.APP_SAAS);
        headers.put(CommonConstant.INNER_HEADER_TIME, time);
        headers.put(CommonConstant.INNER_HEADER_INNER_TOKEN, AESUtils.encrypt(time, CommonConstant.APP_SAAS_AES_KEY));
        headers.put(CommonConstant.INNER_TENANT_ID, tenant.getCode());
        
        R<BatteryInfoDto> r = batteryPlatRetrofitService.queryBatteryInfo(headers, batteryInfoQuery);
        if (!r.isSuccess()) {
            log.error("CALL BATTERY ERROR! msg={},uid={}", r.getErrMsg(), SecurityUtils.getUid());
            return Triple.of(false, r.getErrMsg(), null);
        }
        return Triple.of(true, null, r.getData());
        
        
    }
    
    
    /**
     * 修改电池
     *
     * @param electricityBattery
     * @return
     */
    @Override
    public Integer update(ElectricityBattery electricityBattery) {
        return electricitybatterymapper.update(electricityBattery);
    }
    
    @Override
    public R updateForAdmin(EleBatteryQuery eleQuery) {
        ElectricityBattery electricityBatteryDb = electricitybatterymapper.selectOne(
                new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getId, eleQuery.getId())
                        .eq(ElectricityBattery::getTenantId, TenantContextHolder.getTenantId())
                        .eq(ElectricityBattery::getDelFlag, ElectricityBattery.DEL_NORMAL));
        if (Objects.isNull(electricityBatteryDb)) {
            log.error("ELE ERROR, not found electricity battery id={}", eleQuery.getId());
            return R.fail("ELECTRICITY.0020", "电池不存在!");
        }
        
        Integer count = electricitybatterymapper.selectCount(
                new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getSn, eleQuery.getSn())
                        .eq(ElectricityBattery::getDelFlag, ElectricityBattery.DEL_NORMAL)
                        .ne(ElectricityBattery::getId, eleQuery.getId()));
        if (count > 0) {
            return R.fail("电池编号已绑定其他电池!");
        }
        
        if (!eleQuery.getSn().equalsIgnoreCase(electricityBatteryDb.getSn())) {
            Pair<Boolean, String> result = callBatteryPlatModify(eleQuery.getSn(), electricityBatteryDb.getSn(), eleQuery.getIsNeedSync());
            if (!result.getKey()) {
                return R.fail("200005", result.getRight());
            }
        }
        
        ElectricityBattery updateBattery = new ElectricityBattery();
        BeanUtil.copyProperties(eleQuery, updateBattery);
        updateBattery.setUpdateTime(System.currentTimeMillis());
        updateBattery.setTenantId(TenantContextHolder.getTenantId());
        Integer rows = electricitybatterymapper.update(updateBattery);
        if (rows > 0) {
            redisService.delete(CacheConstant.CACHE_BT_ATTR + electricityBatteryDb.getSn());
            return R.ok();
        } else {
            return R.fail("修改失败!");
        }
    }
    
    
    /**
     * 电池分页
     *
     * @param electricityBatteryQuery
     * @param
     * @return
     */
    @Override
    @Slave
    public R queryList(ElectricityBatteryQuery electricityBatteryQuery, Long offset, Long size) {
        Integer tenantId=TenantContextHolder.getTenantId();
        //如果按照电池型号搜索，需要进行转换,将短型号转换为数据库存放的原类型。
        if(StringUtils.isNotEmpty(electricityBatteryQuery.getModel())){
            String originalModel = batteryModelService.acquireOriginalModelByShortType(electricityBatteryQuery.getModel(), tenantId);
            if(StringUtils.isNotEmpty(originalModel)){
                electricityBatteryQuery.setModel(originalModel);
            }
        }
        
        List<ElectricityBattery> electricityBatteryList = electricitybatterymapper.queryList(electricityBatteryQuery, offset, size);
        if (CollectionUtils.isEmpty(electricityBatteryList)) {
            return R.ok(CollectionUtils.EMPTY_COLLECTION);
        }
        
        List<ElectricityBatteryVO> electricityBatteryVOList = electricityBatteryList.stream().map(item -> {
            ElectricityBatteryVO electricityBatteryVO = new ElectricityBatteryVO();
            BeanUtil.copyProperties(item, electricityBatteryVO);
            
            Franchisee franchisee = franchiseeService.queryByIdFromDB(item.getFranchiseeId());
            electricityBatteryVO.setFranchiseeName(Objects.isNull(franchisee) ? "" : franchisee.getName());
            electricityBatteryVO.setFranchiseeId(item.getFranchiseeId());
            
            if (Objects.equals(item.getBusinessStatus(), ElectricityBattery.BUSINESS_STATUS_LEASE) && Objects.nonNull(item.getUid())) {
                UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
                electricityBatteryVO.setUserName(Objects.nonNull(userInfo) ? userInfo.getName() : "");
            }
            
            if (Objects.equals(item.getPhysicsStatus(), ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE)) {
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(item.getElectricityCabinetId());
                electricityBatteryVO.setElectricityCabinetName(Objects.nonNull(electricityCabinet) ? electricityCabinet.getName() : "");
            } else {
                //不在仓电池电量从BMS平台获取
                BatteryInfoQuery batteryInfoQuery = new BatteryInfoQuery();
                batteryInfoQuery.setSn(item.getSn());
                Triple<Boolean, String, BatteryInfoDto> result = callBatteryServiceQueryBatteryInfo(batteryInfoQuery, tenantId);
                
                if (Boolean.TRUE.equals(result.getLeft()) && Objects.nonNull(result.getRight())) {
                    electricityBatteryVO.setPower(Objects.nonNull(result.getRight().getSoc()) ? result.getRight().getSoc() : 0.0);
                }
            }
            
            String batteryShortType = batteryModelService.acquireBatteryShortType(electricityBatteryVO.getModel(), tenantId);
            electricityBatteryVO.setOriginalModel(electricityBatteryVO.getModel());
            if(StringUtils.isNotEmpty(batteryShortType)){
                electricityBatteryVO.setModel(batteryShortType);
            }
            
            return electricityBatteryVO;
        }).collect(Collectors.toList());
        
        return R.ok(electricityBatteryVOList);
    }
    
    @Override
    @Slave
    public R queryBindListByPage(Long offset, Long size, Long franchiseeId) {
        
        List<ElectricityBatteryVO> batteryVOList = new ArrayList<>();
        
        //没有绑定加盟商的电池
        List<ElectricityBattery> notBindList = electricitybatterymapper.queryNotBindList(offset, size,
                TenantContextHolder.getTenantId());
        //当前加盟商绑定的电池
        List<ElectricityBattery> bindList = electricitybatterymapper.queryBindList(offset, size, franchiseeId,
                TenantContextHolder.getTenantId());
        
        if (CollectionUtils.isNotEmpty(notBindList)) {
            notBindList.forEach(item -> {
                ElectricityBatteryVO batteryVO = new ElectricityBatteryVO();
                BeanUtils.copyProperties(item, batteryVO);
                batteryVO.setIsBind(false);
                batteryVOList.add(batteryVO);
            });
        }
        
        if (CollectionUtils.isNotEmpty(bindList)) {
            bindList.forEach(item -> {
                ElectricityBatteryVO batteryVO = new ElectricityBatteryVO();
                BeanUtils.copyProperties(item, batteryVO);
                batteryVO.setIsBind(true);
                batteryVOList.add(batteryVO);
            });
        }
        
        return R.ok(batteryVOList);
    }
    
    @Override
    public void insert(ElectricityBattery electricityBattery) {
        electricitybatterymapper.insert(electricityBattery);
    }
    
    @Override
    public Triple<Boolean, String, Object> queryInfoByUid(Long uid, Integer isNeedLocation) {
/*        String sn = electricitybatterymapper.querySnByUid(uid);
        if (StrUtil.isEmpty(sn)) {
            return Triple.of(true, null, null);
        }*/
        
        ElectricityBattery electricityBattery = electricitybatterymapper.queryByUid(uid);
        if(Objects.isNull(electricityBattery)){
            return Triple.of(true, null, null);
        }
        
        BatteryInfoQuery batteryInfoQuery = new BatteryInfoQuery();
        batteryInfoQuery.setSn(electricityBattery.getSn());
        
        //为空也需要查询路径，兼容旧版本
        if (Objects.isNull(isNeedLocation) || Objects.equals(isNeedLocation, BatteryInfoQuery.NEED)) {
            batteryInfoQuery.setNeedLocation(BatteryInfoQuery.NEED);
        }
        
        Triple<Boolean, String, BatteryInfoDto> result = callBatteryServiceQueryBatteryInfo(batteryInfoQuery, TenantContextHolder.getTenantId());
        if (Boolean.FALSE.equals(result.getLeft())) {
            log.error("CALL BATTERY ERROR! uid={},msg={}", uid, result.getMiddle());
            return Triple.of(false, "200005", result.getMiddle());
        }
        
        if (Objects.isNull(result.getRight())) {
            log.error("BATTERY ERROR! not found bms'battery! uid={}", uid);
            return Triple.of(false, "200006", "该电池未录入电池服务平台");
        }
        
        ElectricityUserBatteryVo userBatteryVo = new ElectricityUserBatteryVo();
        userBatteryVo.setBatteryA(result.getRight().getBatteryA());
        userBatteryVo.setBatteryV(result.getRight().getBatteryV());
        userBatteryVo.setSn(electricityBattery.getSn());
        userBatteryVo.setLatitude(result.getRight().getLatitude());
        userBatteryVo.setLongitude(result.getRight().getLongitude());
        userBatteryVo.setPower(Double.valueOf(result.getRight().getSoc()));
        userBatteryVo.setUpdateTime(result.getRight().getUpdateTime());
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(electricityBattery.getFranchiseeId());
        if (Objects.nonNull(franchisee) && Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            userBatteryVo.setModel(batteryModelService.analysisBatteryTypeByBatteryName(electricityBattery.getSn()));
        }
        return Triple.of(true, null, userBatteryVo);
    }
    
    @Slave
    @Override
    public Integer querySumCount(ElectricityBatteryQuery electricityBatteryQuery) {
        return electricitybatterymapper.queryCount(electricityBatteryQuery);
    }
    
    @Override
    public BigEleBatteryVo queryMaxPowerByElectricityCabinetId(Integer electricityCabinetId) {
        return electricitybatterymapper.queryMaxPowerByElectricityCabinetId(electricityCabinetId);
    }
    
    @Slave
    @Override
    public R queryById(Long electricityBatteryId) {
        ElectricityBattery electricityBattery = electricitybatterymapper.selectById(electricityBatteryId,
                TenantContextHolder.getTenantId());
        if (Objects.isNull(electricityBattery)) {
            log.error("ELE ERROR, not found electricity battery id={}", electricityBatteryId);
            return R.fail("ELECTRICITY.0020", "电池不存在!");
        }
        
        ElectricityBatteryVO electricityBatteryVO = new ElectricityBatteryVO();
        BeanUtil.copyProperties(electricityBattery, electricityBatteryVO);
        
        if (Objects.equals(electricityBattery.getBusinessStatus(), ElectricityBattery.BUSINESS_STATUS_LEASE)
                && Objects.nonNull(electricityBattery.getUid())) {
            UserInfo userInfo = userInfoService.queryByUidFromCache(electricityBattery.getUid());
            if (Objects.nonNull(userInfo)) {
                electricityBatteryVO.setUserName(userInfo.getName());
            }
        }
        
        return R.ok(electricityBatteryVO);
    }
    
    /**
     * 删除电池
     *
     * @param id
     * @param isNeedSync
     * @return
     */
    @Override
    public R deleteElectricityBattery(Long id, Integer isNeedSync) {
        ElectricityBattery electricityBattery = electricitybatterymapper.selectOne(
                new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getId, id)
                        .eq(ElectricityBattery::getDelFlag, ElectricityBattery.DEL_NORMAL)
                        .eq(ElectricityBattery::getTenantId, TenantContextHolder.getTenantId()));
        if (Objects.isNull(electricityBattery)) {
            log.error("ELE ERROR ,not found electricitybattery,batteryId={}", id);
            return R.fail("100225", "未找到电池!");
        }
        
        if (ObjectUtil.equal(ElectricityBattery.BUSINESS_STATUS_LEASE, electricityBattery.getBusinessStatus())) {
            log.error("ELE ERROR ,electricity_battery is using,batteryId={}", id);
            return R.fail("100226", "电池正在租用中,无法删除!");
        }
        
        Pair<Boolean, String> result = callBatteryPlatDeleteSn(Collections.singletonList(electricityBattery.getSn()), isNeedSync);
        if (!result.getKey()) {
            return R.fail("200005", result.getRight());
        }
        
        int raws = electricitybatterymapper.deleteById(id, TenantContextHolder.getTenantId());
        geoService.deleteBySn(electricityBattery.getSn());
        if (raws > 0) {
            redisService.delete(CacheConstant.CACHE_BT_ATTR + electricityBattery.getSn());
            return R.ok();
        } else {
            return R.fail("100227", "删除失败!");
        }
    }
    
    @Override
    public ElectricityBattery queryByBindSn(String initElectricityBatterySn) {
        return electricitybatterymapper.selectOne(
                new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getSn, initElectricityBatterySn));
    }
    
    /**
     * 获取个人电池
     *
     * @param uid
     * @return
     */
    @Override
    public ElectricityBattery queryByUid(Long uid) {
        return baseMapper.queryByUid(uid);
    }
    
    @Override
    public ElectricityBattery queryBySnFromDb(String oldElectricityBatterySn) {
        return electricitybatterymapper.selectOne(
                new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getSn, oldElectricityBatterySn));
    }
    
    /**
     * 只会查处部分电池属性（ID，tenantId,sn）
     *
     * @param sn
     * @return
     */
    @Override
    public ElectricityBattery queryPartAttrBySnFromCache(String sn) {
        ElectricityBattery existsBt = redisService.getWithHash(CacheConstant.CACHE_BT_ATTR + sn,
                ElectricityBattery.class);
        if (Objects.nonNull(existsBt)) {
            return existsBt;
        }
        ElectricityBattery dbBattery = electricitybatterymapper.queryPartAttrBySn(sn);
        if (Objects.isNull(dbBattery)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_BT_ATTR + sn, dbBattery);
        return dbBattery;
    }
    
    @Override
    @Slave
    public ElectricityBattery queryBySnFromDb(String oldElectricityBatterySn, Integer tenantId) {
        return electricitybatterymapper.selectOne(
                new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getSn, oldElectricityBatterySn)
                        .eq(ElectricityBattery::getTenantId, tenantId));
    }
    
    @Override
    public ElectricityBatteryVO selectBatteryDetailInfoBySN(String sn) {
        return electricitybatterymapper.selectBatteryDetailInfoBySN(sn);
    }
    
    @Override
    public List<ElectricityBattery> queryWareHouseByElectricityCabinetId(Integer electricityCabinetId) {
        return electricitybatterymapper.selectList(
                new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getElectricityCabinetId,
                                electricityCabinetId)
                        .eq(ElectricityBattery::getPhysicsStatus, ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE)
                        .eq(ElectricityBattery::getDelFlag, ElectricityBattery.DEL_NORMAL));
    }
    
    /**
     * 更新电池绑定的用户
     *
     * @param electricityBattery
     * @return
     */
    @Override
    public Integer updateBatteryUser(ElectricityBattery electricityBattery) {
        return electricitybatterymapper.updateBatteryUser(electricityBattery);
    }
    
    /**
     * 更新电池状态
     *
     * @param electricityBattery
     * @return
     */
    @Override
    public Integer updateBatteryStatus(ElectricityBattery electricityBattery) {
        return electricitybatterymapper.updateBatteryStatus(electricityBattery);
    }
    
    @Slave
    @Override
    public R queryCount(ElectricityBatteryQuery electricityBatteryQuery) {
        return R.ok(electricitybatterymapper.queryCount(electricityBatteryQuery));
    }
    
    @Override
    public R batteryOutTimeInfo(Long tenantId) {
        String json = redisService.get(CacheConstant.CACHE_ADMIN_ALREADY_NOTIFICATION + tenantId);
        List<BorrowExpireBatteryVo> list = null;
        if (StrUtil.isNotBlank(json)) {
            list = JSON.parseArray(json, BorrowExpireBatteryVo.class);
        }
        return R.ok(list);
    }
    
    @Override
    public void handlerLowBatteryReminder() {
        Integer size = 300;
        Integer offset = 0;
        
        String batteryLevel = wechatTemplateNotificationConfig.getBatteryLevel();
        Long lowBatteryFrequency = Long.parseLong(wechatTemplateNotificationConfig.getLowBatteryFrequency()) * 60000;
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 hh:mm");
        
        while (true) {
            List<ElectricityBattery> borrowExpireBatteryList = electricitybatterymapper.queryLowBattery(offset, size,
                    batteryLevel);
            
            if (CollectionUtils.isEmpty(borrowExpireBatteryList)) {
                return;
            }
            
            borrowExpireBatteryList.parallelStream().forEach(electricityBattery -> {
                Long uid = electricityBattery.getUid();
                //                Long uid = null;
                //                FranchiseeUserInfo franchiseeUserInfo=franchiseeUserInfoService.selectByNowBattery(electricityBattery.getSn());
                //                if(Objects.nonNull(franchiseeUserInfo)){
                //                    UserInfo userInfo = userInfoService.queryByIdFromDB(franchiseeUserInfo.getUserInfoId());
                //                    if(Objects.nonNull(userInfo)){
                //                        uid = userInfo.getUid();
                //                    }
                //                }
                
                Integer tenantId = electricityBattery.getTenantId();
                boolean isOutTime = redisService.setNx(CacheConstant.CACHE_LOW_BATTERY_NOTIFICATION + uid, "ok",
                        lowBatteryFrequency, false);
                if (!isOutTime) {
                    return;
                }
                
                UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(uid, tenantId);
                if (Objects.isNull(userOauthBind)) {
                    log.error("USER_OAUTH_BIND IS NULL uid={},tenantId={}", uid, tenantId);
                    return;
                }
                String openId = userOauthBind.getThirdId();
                
                BaseMapper<ElectricityPayParams> mapper = electricityPayParamsService.getBaseMapper();
                QueryWrapper<ElectricityPayParams> wrapper = new QueryWrapper<>();
                wrapper.eq("tenant_id", tenantId);
                ElectricityPayParams ele = mapper.selectOne(wrapper);
                if (Objects.isNull(ele)) {
                    log.error("ELECTRICITY_PAY_PARAMS IS NULL ERROR! tenantId={}", tenantId);
                    return;
                }
                
                TemplateConfigEntity templateConfigEntity = templateConfigService.queryByTenantIdFromCache(tenantId);
                
                if (Objects.isNull(templateConfigEntity) || Objects.isNull(
                        templateConfigEntity.getBatteryOuttimeTemplate())) {
                    log.warn("TEMPLATE_CONFIG IS NULL WARN! tenantId={}", tenantId);
                    return;
                }
                
                AppTemplateQuery appTemplateQuery = new AppTemplateQuery();
                appTemplateQuery.setAppId(ele.getMerchantMinProAppId());
                appTemplateQuery.setSecret(ele.getMerchantMinProAppSecert());
                appTemplateQuery.setTouser(openId);
                appTemplateQuery.setFormId(RandomUtil.randomString(20));
                appTemplateQuery.setTemplateId(templateConfigEntity.getElectricQuantityRemindTemplate());
                appTemplateQuery.setPage("/pages/start/index");
                Map<String, Object> data = new HashMap<>(3);
                
                data.put("character_string1", electricityBattery.getPower() + "%");
                data.put("character_string2", electricityBattery.getSn());
                //data.put("keyword3", sdf.format(new Date(System.currentTimeMillis())));
                data.put("thing3", "当前电量较低，请及时换电。");
                
                appTemplateQuery.setData(data);
                log.info("LOW BATTERY POWER MESSAGE TO USER uid={}, sn={}", uid, electricityBattery.getSn());
                
                weChatAppTemplateService.sendWeChatAppTemplate(appTemplateQuery);
            });
            
            offset += size;
        }
    }
    
    @Slave
    @Override
    public List<HomepageBatteryFrequencyVo> homepageBatteryAnalysis(
            HomepageBatteryFrequencyQuery homepageBatteryFrequencyQuery) {
        return electricitybatterymapper.homepageBatteryAnalysis(homepageBatteryFrequencyQuery);
    }
    
    @Slave
    @Override
    public List<HomepageBatteryFrequencyVo> homepageBatteryAnalysisCount(
            HomepageBatteryFrequencyQuery homepageBatteryFrequencyQuery) {
        return electricitybatterymapper.homepageBatteryAnalysisCount(homepageBatteryFrequencyQuery);
    }
    
    @Slave
    @Override
    public R queryBatteryOverview(ElectricityBatteryQuery electricityBatteryQuery) {
        List<ElectricityBattery> electricityBatteryList = electricitybatterymapper.queryBatteryOverview(
                electricityBatteryQuery);
        
        if (ObjectUtil.isEmpty(electricityBatteryList)) {
            return R.ok(electricityBatteryList);
        }
        
        List<ElectricityBatteryVO> electricityBatteryVOList = new ArrayList<>();
        
        for (ElectricityBattery electricityBattery : electricityBatteryList) {
            
            ElectricityBatteryVO electricityBatteryVO = new ElectricityBatteryVO();
            BeanUtil.copyProperties(electricityBattery, electricityBatteryVO);
            
            if (Objects.equals(electricityBattery.getBusinessStatus(), ElectricityBattery.BUSINESS_STATUS_LEASE)
                    && Objects.nonNull(electricityBattery.getUid())) {
                UserInfo userInfo = userInfoService.queryByUidFromCache(electricityBattery.getUid());
                if (Objects.nonNull(userInfo)) {
                    electricityBatteryVO.setUserName(userInfo.getName());
                }
            }
            
            if (Objects.equals(electricityBattery.getPhysicsStatus(), ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE)
                    && Objects.nonNull(electricityBattery.getElectricityCabinetId())) {
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(
                        electricityBattery.getElectricityCabinetId());
                if (Objects.nonNull(electricityCabinet)) {
                    electricityBatteryVO.setElectricityCabinetName(electricityCabinet.getName());
                }
            }
            
            Franchisee franchisee = franchiseeService.queryByElectricityBatteryId(electricityBattery.getId());
            if (Objects.nonNull(franchisee)) {
                electricityBatteryVO.setFranchiseeName(franchisee.getName());
            }
            
            electricityBatteryVOList.add(electricityBatteryVO);
        }
        return R.ok(electricityBatteryVOList);
    }
    
    @Slave
    @Override
    public R batteryStatistical(ElectricityBatteryQuery electricityBatteryQuery) {
        return R.ok(electricitybatterymapper.batteryStatistical(electricityBatteryQuery));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R bindFranchiseeForBattery(BindElectricityBatteryQuery batteryQuery){
        //检查是否存在选中的电池信息
        if (CollectionUtils.isEmpty(batteryQuery.getElectricityBatteryIdList())) {
            return R.ok();
        }
        
        if(Objects.nonNull(batteryQuery.getFranchiseeId())){
            //进入电池绑定流程
            log.info("bind franchisee for battery. franchisee id: {}", batteryQuery.getFranchiseeId());
            Franchisee franchisee = franchiseeService.queryByIdFromCache(batteryQuery.getFranchiseeId().longValue());
            if(Objects.isNull(franchisee)){
                log.error("Franchisee id is invalid! franchisee id = {}", batteryQuery.getFranchiseeId());
                return R.fail("000038", "未找到加盟商!");
            }
        }else{
            //进入电池解绑流程
            log.info("unbind franchisee for battery. battery ids: {}", batteryQuery.getElectricityBatteryIdList());
            batteryQuery.setFranchiseeId(null);
        }
        
        return R.ok(electricitybatterymapper.bindFranchiseeId(batteryQuery));
    }
    
    @Override
    public List<ElectricityBattery> selectByBatteryIds(List<Long> batteryIds) {
        return electricitybatterymapper.selectByBatteryIds(batteryIds);
    }
    
    @Override
    public ElectricityBattery selectByBatteryIdAndFranchiseeId(Long batteryId, Long franchiseeId) {
        return electricitybatterymapper.selectOne(
                new LambdaQueryWrapper<ElectricityBattery>().eq(ElectricityBattery::getId, batteryId)
                        .eq(ElectricityBattery::getFranchiseeId, franchiseeId));
    }
    
    @Slave
    @Override
    public List<ElectricityBatteryVO> selectBatteryInfoByBatteryName(ElectricityBatteryQuery batteryQuery) {
        List<ElectricityBattery> batteryList = electricitybatterymapper.selectBatteryInfoByBatteryName(batteryQuery);
        if(CollectionUtils.isEmpty(batteryList)){
            return Collections.emptyList();
        }
        
        return batteryList.stream().map(item->{
            ElectricityBatteryVO electricityBatteryVO = new ElectricityBatteryVO();
            BeanUtils.copyProperties(item,electricityBatteryVO);
            electricityBatteryVO.setName(item.getSn());
            
            return electricityBatteryVO;
        }).collect(Collectors.toList());
    }
    
    /**
     * 检查是否有电池绑定加盟商
     *
     * @param id
     * @param tenantId
     * @return
     */
    @Override
    public Integer isFranchiseeBindBattery(Long id, Integer tenantId) {
        return electricitybatterymapper.isFranchiseeBindBattery(id, tenantId);
    }
    
    /**
     * 检查用户是否绑定有电池
     *
     * @return
     */
    @Override
    public Integer isUserBindBattery(Long uid, Integer tenantId) {
        return electricitybatterymapper.isUserBindBattery(uid, tenantId);
    }
    
    
    @Override
    public Integer insertBatch(List<ElectricityBattery> saveList) {
        return electricitybatterymapper.insertBatch(saveList);
    }
    
    @Override
    public ElectricityBattery queryUserAttrBySnFromDb(String sn) {
        return electricitybatterymapper.queryUserAttrBySn(sn);
    }
    
    
    /**
     * 迁移用户所属加盟商  获取用户电池型号
     *
     * @return
     */
    @Deprecated
    public Triple<Boolean, String, Object> selectUserLatestBatteryType() {
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(
                TenantContextHolder.getTenantId());
        if (Objects.isNull(electricityConfig)) {
            log.error("ELE ERROR!not found electricityConfig,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "000001", "系统异常");
        }
        
        if (!Objects.equals(electricityConfig.getIsMoveFranchisee(), ElectricityConfig.MOVE_FRANCHISEE_OPEN)) {
            log.error("ELE ERROR!not found open move franchisee,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "100353", "未启用加盟商迁移");
        }
        
        String batteryType = null;
        
        //1.查询当前用户最新换电订单
        ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderService.selectLatestByUid(
                SecurityUtils.getUid(), TenantContextHolder.getTenantId());
        if (Objects.nonNull(electricityCabinetOrder)) {
            String batterySn = electricityCabinetOrder.getNewElectricityBatterySn();
            batteryType = BatteryConstant.parseBatteryModelByBatteryName(batterySn);
        } else {
            //查询当前用户最新的租电订单
            RentBatteryOrder rentBatteryOrder = rentBatteryOrderService.selectLatestByUid(SecurityUtils.getUid(),
                    TenantContextHolder.getTenantId());
            String batterySn = rentBatteryOrder.getElectricityBatterySn();
            batteryType = BatteryConstant.parseBatteryModelByBatteryName(batterySn);
        }
        
        if (StringUtils.isBlank(batteryType)) {
            log.error("ELE ERROR!not found user batteryType,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "100352", "未找到用户电池型号");
        }
        
        Integer batteryModel = BatteryConstant.acquireBatteryModel(batteryType);
        if (Objects.isNull(batteryModel)) {
            log.error("ELE ERROR!not found user batteryModel,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "100352", "未找到用户电池型号");
        }
        
        return Triple.of(true, "", batteryModel);
    }
    
    @Slave
    @Override
    public Triple<Boolean, String, Object> queryBatteryInfoBySn(String sn) {
        ElectricityBattery electricityBattery = queryBySnFromDb(sn, TenantContextHolder.getTenantId());
        if (Objects.isNull(electricityBattery)) {
            return Triple.of(true, null, null);
        }
        
        ElectricityBatteryVO electricityBatteryVO = new ElectricityBatteryVO();
        BeanUtil.copyProperties(electricityBattery, electricityBatteryVO);
        
        if (Objects.equals(electricityBattery.getBusinessStatus(), ElectricityBattery.BUSINESS_STATUS_LEASE)
                && Objects.nonNull(electricityBattery.getUid())) {
            UserInfo userInfo = userInfoService.queryByUidFromCache(electricityBattery.getUid());
            if (Objects.nonNull(userInfo)) {
                electricityBatteryVO.setUserName(userInfo.getName());
            }
        }
        
        if (Objects.equals(electricityBattery.getPhysicsStatus(), ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE)
                && Objects.nonNull(electricityBattery.getElectricityCabinetId())) {
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(
                    electricityBattery.getElectricityCabinetId());
            if (Objects.nonNull(electricityCabinet)) {
                electricityBatteryVO.setElectricityCabinetName(electricityCabinet.getName());
            }
        }
        
        Franchisee franchisee = franchiseeService.queryByElectricityBatteryId(electricityBattery.getId());
        if (Objects.nonNull(franchisee)) {
            electricityBatteryVO.setFranchiseeName(franchisee.getName());
        }
        
        return Triple.of(true, null, electricityBatteryVO);
        
    }
    
    @Slave
    @Override
    public Triple<Boolean, String, Object> queryBatteryMapList(Integer offset, Integer size, List<Long> franchiseeIds) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        return Triple.of(true, null, electricitybatterymapper.queryPartAttrList(offset, size, franchiseeIds,
                TenantContextHolder.getTenantId()));
    }
    
    
}