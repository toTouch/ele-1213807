package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.query.BindElectricityBatteryQuery;
import com.xiliulou.electricity.query.ElectricityBatteryDataQuery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.query.HomepageBatteryFrequencyQuery;
import com.xiliulou.electricity.vo.*;
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
    
    Integer updateBatteryUser(ElectricityBattery electricityBattery);
    
    Integer updateBatteryStatus(ElectricityBattery electricityBattery);
    
    //    List<BorrowExpireBatteryVo> queryBorrowExpireBattery(@Param("curTime") long curTime, @Param("offset") Integer offset, @Param("size") Integer size);
    
    //@Select("select count(1) from t_electricity_battery where power < #{batteryLevel} and report_type = 1 and status = 2")
    //Long queryLowBatteryCount(@Param("batteryLevel")String batteryLevel);
    
    List<ElectricityBattery> queryLowBattery(@Param("offset") Integer offset, @Param("size") Integer size,
            @Param("batteryLevel") String batteryLevel);
    
    List<ElectricityBattery> queryNotBindList(@Param("offset") Long offset, @Param("size") Long size,
            @Param("tenantId") Integer tenantId);
    
    List<ElectricityBattery> queryBindList(@Param("offset") Long offset, @Param("size") Long size,
            @Param("franchiseeId") Long franchiseeId, @Param("tenantId") Integer tenantId);
    
    ElectricityBattery queryByUid(@Param("uid") Long uid);
    
    ElectricityBatteryVO selectBatteryDetailInfoBySN(@Param("sn") String sn);
    
    @Select("select power, last_deposit_cell_no from t_electricity_battery where electricity_cabinet_id = #{electricityCabinetId} and physics_status  = 0 and del_flag = 0 order by power desc limit 1")
    BigEleBatteryVo queryMaxPowerByElectricityCabinetId(@Param("electricityCabinetId") Integer electricityCabinetId);
    
    List<HomepageBatteryFrequencyVo> homepageBatteryAnalysis(
            @Param("query") HomepageBatteryFrequencyQuery homepageBatteryFrequencyQuery);
    
    List<HomepageBatteryFrequencyVo> homepageBatteryAnalysisCount(
            @Param("query") HomepageBatteryFrequencyQuery homepageBatteryFrequencyQuery);
    
    List<ElectricityBattery> queryBatteryOverview(@Param("query") ElectricityBatteryQuery electricityBatteryQuery);
    
    BatteryStatisticalVo batteryStatistical(ElectricityBatteryQuery electricityBatteryQuery);
    
    int unbindFranchiseeId(@Param("franchiseeId") Integer franchiseeId,
            @Param("updateBattery") ElectricityBattery updateBattery);
    
    int bindFranchiseeId(@Param("batteryQuery") BindElectricityBatteryQuery batteryQuery);
    
    List<ElectricityBattery> selectByBatteryIds(@Param("batteryIds") List<Long> batteryIds);
    
    List<ElectricityBattery> selectBatteryInfoByBatteryName(ElectricityBatteryQuery batteryQuery);
    
    Integer update(ElectricityBattery electricityBattery);
    
    Integer deleteById(@Param("id") Long id, @Param("tenantId") Integer tenantId);
    
    ElectricityBattery selectById(@Param("id") Long id, @Param("tenantId") Integer tenantId);
    
    Integer isFranchiseeBindBattery(@Param("franchiseeId") Long franchiseeId, @Param("tenantId") Integer tenantId);
    
    @Select("select id, sn,tenant_id,franchisee_id franchiseeId from t_electricity_battery where sn = #{sn}")
    ElectricityBattery queryPartAttrBySn(@Param("sn") String sn);

    List<ElectricityBatteryLocationVO> queryPartAttrList(@Param("offset") Integer offset, @Param("size") Integer size,
            @Param("franchiseeIds") List<Long> franchiseeIds,@Param("tenantId")Integer tenantId);

    Integer isUserBindBattery(@Param("uid") Long uid, @Param("tenantId") Integer tenantId);

    Integer insertBatch(@Param("list") List<ElectricityBattery> saveList);

    String querySnByUid(@Param("uid") Long uid);

    ElectricityBattery queryUserAttrBySn(@Param("sn") String sn);

    List<ElectricityBatteryDataVO> queryBatteryList(@Param("query") ElectricityBatteryDataQuery electricityBatteryQuery,
                                                    @Param("offset") Long offset, @Param("size") Long size);
    Integer queryBatteryCount(@Param("query") ElectricityBatteryDataQuery electricityBatteryQuery);

    List<ElectricityBatteryDataVO> queryOverdueBatteryList(@Param("query") ElectricityBatteryDataQuery electricityBatteryQuery,
                                              @Param("offset") Long offset, @Param("size") Long size);
    Integer queryOverdueBatteryCount(@Param("query") ElectricityBatteryDataQuery electricityBatteryQuery);
}
