package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.query.ElectricityCabinetModelQuery;

import java.util.List;

/**
 * 换电柜型号表(TElectricityCabinetModel)表服务接口
 *
 * @author makejava
 * @since 2020-11-25 11:01:04
 */
public interface ElectricityCabinetModelService {


      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetModel queryByIdFromCache(Integer id);

    R save(ElectricityCabinetModel electricityCabinetModel);

    R edit(ElectricityCabinetModel electricityCabinetModel);

    R delete(Integer id);

    R queryList(ElectricityCabinetModelQuery electricityCabinetModelQuery);

	R queryCount(ElectricityCabinetModelQuery electricityCabinetModelQuery);
}
