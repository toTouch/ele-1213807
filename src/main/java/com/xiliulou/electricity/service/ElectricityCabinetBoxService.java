package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.query.ElectricityCabinetBoxQuery;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

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
    ElectricityCabinetBox queryByIdFromDB(Long id,Integer tenantId);

    void batchInsertBoxByModelId(ElectricityCabinetModel electricityCabinetModel, Integer id);

    void batchDeleteBoxByElectricityCabinetId(Integer id);

    R queryList(ElectricityCabinetBoxQuery electricityCabinetBoxQuery);
    
    R selectBoxList(ElectricityCabinetBoxQuery electricityCabinetBoxQuery);

    R modify(ElectricityCabinetBox electricityCabinetBox);

    List<ElectricityCabinetBox> queryBoxByElectricityCabinetId(Integer id);

    List<ElectricityCabinetBox> queryNoElectricityBatteryBox(Integer id);
    
    List<ElectricityCabinetBox> queryElectricityBatteryBox(ElectricityCabinet electricityCabinet, String cellNo,
            String batteryType, Double fullCharged);
    ElectricityCabinetBox queryByCellNo(Integer electricityCabinetId,String cellNo);

	void modifyByCellNo(ElectricityCabinetBox electricityCabinetNewBox);

    void modifyCellByCellNo(ElectricityCabinetBox electricityCabinetBox);

    List<ElectricityCabinetBox> queryUsableBatteryCellNo(Integer id, String batteryType, Double fullyCharged);

    List<ElectricityCabinetBox> findUsableEmptyCellNo(Integer eid);
    
    int selectUsableEmptyCellNumber(Integer eid, Integer tenantId);

    /**
     * 禁用异常仓门
     * @param cellNo
     * @param electricityCabinetId
     * @return
     */
    Integer disableCell(Integer cellNo,Integer electricityCabinetId);

    List<ElectricityCabinetBox> queryAllBoxByElectricityCabinetId(Integer electricityCabinetId);

    ElectricityCabinetBox queryBySn(String sn,Integer electricityCabinetId);

    R queryBoxCount(Integer electricityCabinet,Integer tenantId);

    ElectricityCabinetBox selectByBatteryId(Long id);
    
    Triple<Boolean, String, Object> selectAvailableBoxNumber(Integer electricityCabinetId, Integer tenantId);
}
