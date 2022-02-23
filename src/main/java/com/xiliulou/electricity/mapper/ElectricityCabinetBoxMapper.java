package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.query.ElectricityCabinetBoxQuery;
import com.xiliulou.electricity.vo.ElectricityCabinetBoxVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

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


    void batchDeleteBoxByElectricityCabinetId(@Param("id") Integer id, @Param("updateTime") Long updateTime);


    int modifyByCellNo(ElectricityCabinetBox electricityCabinetNewBox);

    void modifyCellByCellNo(ElectricityCabinetBox electricityCabinetBox);

    List<ElectricityCabinetBox> queryElectricityBatteryBox(@Param("id") Integer id, @Param("cellNo") String cellNo, @Param("batteryType") String batteryType);

    List<ElectricityCabinetBox> queryUsableBatteryCellNo(@Param("eid") Integer id, @Param("type") String batteryType, @Param("fullV") Double fullyCharged);

    List<ElectricityCabinetBox> queryUsableEmptyCellNo(@Param("eid") Integer eid);

    Integer modifyCellUsableStatus(@Param("cellNo") Integer cellNo,@Param("electricityCabinetId") Integer electricityCabinetId);


}
