package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.query.api.ApiRequestQuery;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 换电柜表(TElectricityCabinet)表服务接口
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
public interface ElectricityCarService {

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCar queryByIdFromCache(Integer id);

    R save(ElectricityCarAddAndUpdate electricityCarAddAndUpdate);

    R edit(ElectricityCarAddAndUpdate electricityCarAddAndUpdate);

    R delete(Integer id);

    R queryList(ElectricityCarQuery electricityCarQuery);

    Integer queryByModelId(Integer id);

    R queryCount(ElectricityCarQuery electricityCarQuery);

    R bindUser(String phone);


}
