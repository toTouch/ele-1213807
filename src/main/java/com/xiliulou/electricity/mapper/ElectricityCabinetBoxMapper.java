package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.query.ElectricityCabinetBoxQuery;
import com.xiliulou.electricity.query.FreeCellNoQuery;
import com.xiliulou.electricity.vo.ElectricityCabinetBoxVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 换电柜仓门表(TElectricityCabinetBox)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-25 11:00:37
 */
public interface ElectricityCabinetBoxMapper extends BaseMapper<ElectricityCabinetBox> {


    /**
     * @return 对象列表
     */
    List<ElectricityCabinetBoxVO> queryList(@Param("query") ElectricityCabinetBoxQuery electricityCabinetBoxQuery);
    
    List<ElectricityCabinetBoxVO> selectBoxList(@Param("query") ElectricityCabinetBoxQuery electricityCabinetBoxQuery);


    void batchDeleteBoxByElectricityCabinetId(@Param("id") Integer id, @Param("updateTime") Long updateTime);


    int modifyByCellNo(ElectricityCabinetBox electricityCabinetNewBox);

    void modifyCellByCellNo(ElectricityCabinetBox electricityCabinetBox);
    
    List<ElectricityCabinetBox> queryElectricityBatteryBox(@Param("id") Integer id, @Param("cellNo") String cellNo,
            @Param("batteryType") String batteryType, @Param("fullCharged") Double fullCharged);
    
    List<ElectricityCabinetBox> queryUsableBatteryCellNo(@Param("eid") Integer id, @Param("type") String batteryType, @Param("fullV") Double fullyCharged);
    
    List<FreeCellNoQuery> queryUsableEmptyCellNo(@Param("eid") Integer eid);

    Integer modifyCellUsableStatus(@Param("cellNo") Integer cellNo, @Param("electricityCabinetId") Integer electricityCabinetId);

    Integer queryBoxCount(@Param("id") Integer id, @Param("tenantId") Integer tenantId);
    
    int selectUsableEmptyCellNumber(@Param("eid") Integer eid, @Param("tenantId") Integer tenantId);

    List<ElectricityCabinetBox> selectEleBoxAttrByEid(@Param("eid") Integer eid);
    
    Integer batchInsertEleBox(@Param("boxList") List<ElectricityCabinetBox> boxList);
    
    List<ElectricityCabinetBox> selectListByElectricityCabinetIdS(@Param("electricityCabinetIdS") List<Integer> electricityCabinetIdS, @Param("tenantId") Integer tenantId);
    
    List<ElectricityCabinetBox> selectUsableEmptyCell(Integer eid);
    
    ElectricityCabinetBox selectEleBoxByBatteryId(@Param("batteryId") Long batteryId);
    
    List<ElectricityCabinetBox> selectListByEids(@Param("eIdList") List<Integer> electricityCabinetIdList);
    
    
    List<ElectricityCabinetBox> selectHaveBatteryCellId(@Param("eid") Integer eid);
    
    List<ElectricityCabinetBox> selectListBySnList(@Param("snList") List<String> snList);
    
    List<ElectricityCabinetBox> selectListNotUsableBySn(@Param("sn") String sn,@Param("cabinetId") Integer cabinetId,@Param("cellNo") String cellNo);
    
    int updateLockSnByEidAndCellNo(@Param("eid") Integer eId, @Param("cellNo") Integer cellNo, @Param("lockSn") String lockSn);
    
    List<ElectricityCabinetBox> selectListBySnAndEid(@Param("sn") String sn, @Param("eid") Integer eid);
    
    List<ElectricityCabinetBox> selectListByLockSn(@Param("lockSn") String lockSn);
}
