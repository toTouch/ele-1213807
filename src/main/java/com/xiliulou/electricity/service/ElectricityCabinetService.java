package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;


/**
 * 换电柜表(TElectricityCabinet)表服务接口
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
public interface ElectricityCabinetService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinet queryByIdFromDB(Integer id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinet queryByIdFromCache(Integer id);


    /**
     * 新增数据
     *
     * @param electricityCabinet 实例对象
     * @return 实例对象
     */
    ElectricityCabinet insert(ElectricityCabinet electricityCabinet);

    /**
     * 修改数据
     *
     * @param electricityCabinet 实例对象
     * @return 实例对象
     */
    Integer update(ElectricityCabinet electricityCabinet);

    R save(ElectricityCabinet electricityCabinet);

    R edit(ElectricityCabinet electricityCabinet);

    R delete(Integer id);

    R queryList(ElectricityCabinetQuery electricityCabinetQuery);

    R showInfoByDistance(ElectricityCabinetQuery electricityCabinetQuery);

    Integer queryByModelId(Integer id);

    R disable(Integer id);

    R reboot(Integer id);
}