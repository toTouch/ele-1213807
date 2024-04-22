package com.xiliulou.electricity.service.impl;

import cn.hutool.core.lang.Pair;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.mapper.ElectricityBatteryMapper;
import com.xiliulou.electricity.service.supper.AdminSupperService;
import com.xiliulou.electricity.tx.AdminSupperTxService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
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
     * @return Pair<已删除的电池编码、未删除的电池编码>
     */
    @Transactional
    @Override
    public Pair<List<String>, List<String>> delBatteryBySnList(Integer tenantId, List<String> batterySnList) {
        // 优先去重
        List<String> batterySnDistinctList = batterySnList.stream().distinct().collect(Collectors.toList());
        // 根据参数获取电池数据
        List<ElectricityBattery> dbBatteryList = electricityBatteryMapper.selectListBySnArray(batterySnDistinctList, tenantId, null);
        if (CollectionUtils.isEmpty(dbBatteryList)) {
            log.warn("delBatteryBySnList failed. The dbBatteryList is empty.");
            return Pair.of(null, batterySnDistinctList);
        }
        
        List<String> dbBatterySnList = dbBatteryList.stream().map(ElectricityBattery::getSn).collect(Collectors.toList());
        List<String> batterySnFailList = new ArrayList<>();
        
        // 比对入参和查询结果是否一致
        if (batterySnDistinctList.size() != dbBatterySnList.size()) {
            // 若不一致，差集比对，记录下来，以便返回
            List<String> batterySnDiffList = batterySnDistinctList.stream().filter(batterySn -> !dbBatterySnList.contains(batterySn)).collect(Collectors.toList());
            batterySnFailList.addAll(batterySnDiffList);
        }
        
        // 使用查询出来的数据，删除电池以及电池配置
        adminSupperTxService.delBatteryBySnList(tenantId, dbBatterySnList);
        
        // 删除缓存
        dbBatterySnList.forEach(dbBatterySn -> {
            redisService.delete(CacheConstant.CACHE_BT_ATTR + dbBatterySn);
        });
        
        return Pair.of(dbBatterySnList, batterySnFailList);
    }
}
