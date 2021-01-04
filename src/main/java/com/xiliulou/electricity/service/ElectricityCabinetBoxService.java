package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.query.ElectricityCabinetBoxQuery;

import java.util.List;


/**
 * 换电柜仓门表(TElectricityCabinetBox)表服务接口
 *
 * @author makejava
 * @since 2020-11-25 11:00:37
 */
public interface ElectricityCabinetBoxService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetBox queryByIdFromDB(Long id);



    /**
     * 新增数据
     *
     * @param electricityCabinetBox 实例对象
     * @return 实例对象
     */
    ElectricityCabinetBox insert(ElectricityCabinetBox electricityCabinetBox);

    /**
     * 修改数据
     *
     * @param electricityCabinetBox 实例对象
     * @return 实例对象
     */
    Integer update(ElectricityCabinetBox electricityCabinetBox);


    void batchInsertBoxByModelId(ElectricityCabinetModel electricityCabinetModel, Integer id);

    void batchDeleteBoxByElectricityCabinetId(Integer id);

    R queryList(ElectricityCabinetBoxQuery electricityCabinetBoxQuery);

    R modify(ElectricityCabinetBox electricityCabinetBox);

    List<ElectricityCabinetBox> queryBoxByElectricityCabinetId(Integer id);

    List<ElectricityCabinetBox> queryNoElectricityBatteryBox(Integer id);

    List<ElectricityCabinetBox> queryElectricityBatteryBox(Integer id,String cellNo);

    void modifyByCellNo(ElectricityCabinetBox electricityCabinetBox);

    ElectricityCabinetBox queryByCellNo(Integer electricityCabinetId,String cellNo);


}