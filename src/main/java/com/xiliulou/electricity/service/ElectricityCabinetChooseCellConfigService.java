package com.xiliulou.electricity.service;


import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetChooseCellConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * @author renhang
 * @date 2023/3/14 舒适换电格挡配置
 */
public interface ElectricityCabinetChooseCellConfigService {
    
    ElectricityCabinetChooseCellConfig queryConfigByNumFromDB(Integer num);
    
    /**
     * 根据编号查询缓存中的机柜选择单元配置
     *
     * @param num 编号
     * @return 返回查询到的配置
     */
    ElectricityCabinetChooseCellConfig queryConfigByNumFromCache(Integer num);
    
    /**
     * 舒适换电获取满电仓
     *
     * @param uid         用户ID等
     * @param usableBoxes 可用的换电箱列表
     * @return 返回一个Pair对象，其中包含一个布尔值表示是否满足条件，以及一个字符串表示推荐的换电箱编号（如果满足条件）
     */
    Pair<Boolean, ElectricityCabinetBox> comfortExchangeGetFullCell(Long uid, List<ElectricityCabinetBox> usableBoxes, Double fullyCharged);
    
    
    /**
     * 舒适换电获取空仓
     *
     * @param uid         用户ID等
     * @param usableBoxes 可用的换电箱列表
     * @return 返回一个Pair对象，其中包含一个布尔值表示是否满足条件，以及一个字符串表示推荐的换电箱编号（如果满足条件）
     */
    Pair<Boolean, Integer> comfortExchangeGetEmptyCell(Long uid, List<ElectricityCabinetBox> usableBoxes);
}
