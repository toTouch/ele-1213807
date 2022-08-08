package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.query.EleBindCarRecordQuery;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.query.ElectricityCarQuery;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.electricity.vo.ElectricityCarVO;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 换电柜表(TElectricityCar)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
public interface ElectricityCarMapper extends BaseMapper<ElectricityCar> {


    List<ElectricityCarVO> queryList(@Param("query") ElectricityCarQuery electricityCarQuery);

    Integer queryCount(@Param("query") ElectricityCarQuery electricityCarQuery);

    Integer updateBindUser(ElectricityCar electricityCar);

    Integer queryCountByStoreIds(@Param("tenantId") Integer tenantId,@Param("storeIds") List<Long> storeIds);

}
