package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.entity.ElectricityCarModel;
import com.xiliulou.electricity.query.ElectricityCabinetModelQuery;
import com.xiliulou.electricity.query.ElectricityCarModelQuery;

/**
 * 换电柜型号表(TElectricityCarModel)表服务接口
 *
 * @author makejava
 * @since 2020-11-25 11:01:04
 */
public interface ElectricityCarModelService {


      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
      ElectricityCarModel queryByIdFromCache(Integer id);

    R save(ElectricityCarModel electricityCarModel);

    R edit(ElectricityCarModel electricityCarModel);

    R delete(Integer id);

    R queryList(ElectricityCarModelQuery electricityCarModelQuery);

	R queryCount(ElectricityCarModelQuery electricityCarModelQuery);
}
