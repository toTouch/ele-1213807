package com.xiliulou.electricity.service.supper;

import cn.hutool.core.lang.Pair;

import java.util.List;

/**
 * @author: Ant
 * @Date 2024/4/22
 * @Description:
 **/
public interface AdminSupperService {
    
    /**
     * 根据电池SN删除电池
     * @param tenantId 租户ID
     * @param batterySnList 电池SN集
     * @return Pair<已删除的电池编码、未删除的电池编码>
     */
    Pair<List<String>, List<String>> delBatteryBySnList(Integer tenantId, List<String> batterySnList);
}
