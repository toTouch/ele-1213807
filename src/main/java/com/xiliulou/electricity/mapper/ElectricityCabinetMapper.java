package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 换电柜表(TElectricityCabinet)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
public interface ElectricityCabinetMapper extends BaseMapper<ElectricityCabinet> {


    /**
     * @param electricityCabinetQuery
     * @return 对象列表
     */
    List<ElectricityCabinetVO> queryList( @Param("query") ElectricityCabinetQuery electricityCabinetQuery);


    List<ElectricityCabinetVO> showInfoByDistance(@Param("query") ElectricityCabinetQuery electricityCabinetQuery);


    List<Long> queryFullyElectricityBattery(@Param("id") Integer id,@Param("batteryType") String batteryType);


    List<Map<String,Object>> queryNameList(@Param("size")Long size, @Param("offset")Long offset, @Param("eleIdList")List<Integer> eleIdList, @Param("tenantId")Integer tenantId);

    List<ElectricityCabinet> homeOne(@Param("eleIdList") List<Integer> eleIdList,@Param("tenantId") Integer tenantId);

	Integer queryCount(@Param("query") ElectricityCabinetQuery electricityCabinetQuery);

	List<HashMap<String, String>> homeThree(@Param("startTimeMilliDay") long startTimeMilliDay, @Param("endTimeMilliDay") Long endTimeMilliDay,@Param("eleIdList") List<Integer> eleIdList ,@Param("tenantId")Integer tenantId);
}
