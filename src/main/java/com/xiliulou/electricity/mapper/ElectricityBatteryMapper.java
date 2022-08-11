package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.vo.BigEleBatteryVo;
import com.xiliulou.electricity.vo.BorrowExpireBatteryVo;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 换电柜电池表(ElectricityBattery)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
public interface ElectricityBatteryMapper extends BaseMapper<ElectricityBattery> {


    List<ElectricityBattery> queryList(@Param("query") ElectricityBatteryQuery electricityBatteryQuery,
                                    @Param("offset") Long offset, @Param("size") Long size);

	Integer queryCount(@Param("query") ElectricityBatteryQuery electricityBatteryQuery);

	ElectricityBatteryVO selectBatteryInfo(@Param("uid") Long uid);

	Integer updateByOrder(ElectricityBattery electricityBattery);


    List<BorrowExpireBatteryVo>queryBorrowExpireBattery(@Param("curTime")long curTime, @Param("offset")Integer offset, @Param("size")Integer size);

    //@Select("select count(1) from t_electricity_battery where power < #{batteryLevel} and report_type = 1 and status = 2")
    //Long queryLowBatteryCount(@Param("batteryLevel")String batteryLevel);

    List<ElectricityBattery> queryLowBattery(@Param("offset")Integer offset, @Param("size")Integer size, @Param("batteryLevel")String batteryLevel);

    List<ElectricityBattery> queryNotBindList(@Param("offset")Long offset, @Param("size")Long size, @Param("franchiseeId")Integer franchiseeId, @Param("tenantId")Integer tenantId);

	ElectricityBattery queryByUid(@Param("uid") Long uid);

    ElectricityBatteryVO selectBatteryDetailInfoBySN(@Param("sn") String sn);

    @Select("select power, last_deposit_cell_no from t_electricity_battery where electricity_cabinet_id = #{electricityCabinetId} and del_flag = 0 order by power desc limit 1")
    BigEleBatteryVo queryMaxPowerByElectricityCabinetId(@Param("electricityCabinetId")Integer electricityCabinetId);
}
