package com.xiliulou.electricity.service.impl;

import cn.hutool.core.lang.Pair;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.AssetConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.ElectricityBatteryMapper;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.asset.AssetInventoryService;
import com.xiliulou.electricity.service.retrofit.BatteryPlatRetrofitService;
import com.xiliulou.electricity.service.supper.AdminSupperService;
import com.xiliulou.electricity.tx.AdminSupperTxService;
import com.xiliulou.electricity.utils.AESUtils;
import com.xiliulou.electricity.web.query.battery.BatteryBatchOperateQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: Ant
 * @Date 2024/4/22
 * @Description:
 **/
@Slf4j
@Service
public class AdminSupperServiceImpl implements AdminSupperService {
    
    @Resource
    private BatteryPlatRetrofitService batteryPlatRetrofitService;
    
    @Resource
    private TenantService tenantService;
    
    @Resource
    private AssetInventoryService assetInventoryService;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private AdminSupperTxService adminSupperTxService;
    
    @Resource
    private ElectricityBatteryMapper electricityBatteryMapper;
    
    /**
     * 根据电池SN删除电池
     *
     * @param tenantId      租户ID
     * @param batterySnList 电池SN集
     * @return Pair<已删除的电池编码 、 未删除的电池编码>
     */
    @Transactional
    @Override
    public Pair<List<String>, List<String>> delBatteryBySnList(Integer tenantId, List<String> batterySnList) {
        if (ObjectUtils.isEmpty(tenantId) || CollectionUtils.isEmpty(batterySnList)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        // 查询租户信息
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (ObjectUtils.isEmpty(tenant)) {
            throw new BizException("ELECTRICITY.00101", "找不到租户");
        }
        
        // 优先去重
        List<String> batterySnDistinctList = batterySnList.stream().distinct().collect(Collectors.toList());
        
        // 根据参数获取电池数据
        List<ElectricityBattery> dbBatteryList = electricityBatteryMapper.selectListBySnList(tenantId, batterySnDistinctList);
        if (CollectionUtils.isEmpty(dbBatteryList)) {
            log.warn("delBatteryBySnList failed. The dbBatteryList is empty.");
            return Pair.of(null, batterySnDistinctList);
        }
        
        // 获取 DB 数据的 SN 集
        List<String> dbBatterySnList = dbBatteryList.stream().map(ElectricityBattery::getSn).collect(Collectors.toList());
        // 定义失败的 SN 集
        List<String> batterySnFailList = new ArrayList<>();
        // 定义等待删除的电池
        List<ElectricityBattery> batteryWaitList = new ArrayList<>();
        
        // 比对入参和查询结果是否一致
        if (batterySnDistinctList.size() != dbBatterySnList.size()) {
            // 若不一致，差集比对，记录下来，以便返回
            List<String> batterySnDiffList = batterySnDistinctList.stream().filter(batterySn -> !dbBatterySnList.contains(batterySn)).collect(Collectors.toList());
            batterySnFailList.addAll(batterySnDiffList);
        }
        
        // 根据加盟商 ID 对 DB 数据进行分组
        Map<Long, List<ElectricityBattery>> dbBatteryFranchiseeIdGroupMap = dbBatteryList.stream().collect(Collectors.groupingBy(ElectricityBattery::getFranchiseeId));
        dbBatteryFranchiseeIdGroupMap.forEach((k, v) -> {
            // 校验加盟商是否正在进行资产盘点
            Integer status = assetInventoryService.queryInventoryStatusByFranchiseeId(k, AssetTypeEnum.ASSET_TYPE_BATTERY.getCode());
            if (Objects.equals(status, AssetConstant.ASSET_INVENTORY_STATUS_TAKING)) {
                List<String> stocktakingSnList = v.stream().map(ElectricityBattery::getSn).collect(Collectors.toList());
                batterySnFailList.addAll(stocktakingSnList);
            } else {
                batteryWaitList.addAll(v);
            }
        });
        
        // 根据 UID 是否为空分组
        Map<Boolean, List<ElectricityBattery>> rentGroupBatteryList = batteryWaitList.stream()
                .collect(Collectors.groupingBy(batteryWait -> batteryWait.getUid() != null, Collectors.mapping(Function.identity(), Collectors.toList())));
        rentGroupBatteryList.forEach((k, v) -> {
            // 电池租用中的电池，不允许删除
            if (Boolean.TRUE.equals(k)) {
                List<String> rentSnList = v.stream().map(ElectricityBattery::getSn).collect(Collectors.toList());
                batterySnFailList.addAll(rentSnList);
                
                // 从待删除的里面删除这条数据
                batteryWaitList.removeIf(batteryWait -> rentSnList.contains(batteryWait.getSn()));
            } else {
                batteryWaitList.addAll(v);
            }
        });
        
        // 对等待删除的数据，进行删除
        List<String> batteryWaitSnList = batteryWaitList.stream().map(ElectricityBattery::getSn).distinct().collect(Collectors.toList());
        
        // 1、调用 BMS 删除
        Map<String, String> headers = new HashMap<>();
        String time = String.valueOf(System.currentTimeMillis());
        headers.put(CommonConstant.INNER_HEADER_APP, CommonConstant.APP_SAAS);
        headers.put(CommonConstant.INNER_HEADER_TIME, time);
        headers.put(CommonConstant.INNER_HEADER_INNER_TOKEN, AESUtils.encrypt(time, CommonConstant.APP_SAAS_AES_KEY));
        headers.put(CommonConstant.INNER_TENANT_ID, tenant.getCode());
        
        BatteryBatchOperateQuery batteryBatchOperateQuery = new BatteryBatchOperateQuery();
        batteryBatchOperateQuery.setJsonBatterySnList(JsonUtil.toJson(batteryWaitSnList));
        
        R result = batteryPlatRetrofitService.batchDelete(headers, batteryBatchOperateQuery);
        if (!result.isSuccess()) {
            log.error("delBatteryBySnList failed. batteryPlatRetrofitService.batchDelete failed. msg is {}", result.getErrMsg());
            return Pair.of(null, batterySnFailList);
        }
        
        // 2. 使用待删除的数据，删除电池以及电池配置
        adminSupperTxService.delBatteryBySnList(tenantId, batteryWaitSnList);
        
        // 3. 删除缓存
        batteryWaitSnList.forEach(dbBatterySn -> {
            redisService.delete(CacheConstant.CACHE_BT_ATTR + dbBatterySn);
        });
        
        return Pair.of(batteryWaitSnList, batterySnFailList);
    }
}
