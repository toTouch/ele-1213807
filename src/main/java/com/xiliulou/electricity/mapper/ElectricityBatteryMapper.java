package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.bo.ExportMutualBatteryBO;
import com.xiliulou.electricity.bo.asset.ElectricityBatteryBO;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.query.BindElectricityBatteryQuery;
import com.xiliulou.electricity.query.ElectricityBatteryDataQuery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.query.HomepageBatteryFrequencyQuery;
import com.xiliulou.electricity.query.asset.AssetBatchExitWarehouseQueryModel;
import com.xiliulou.electricity.query.asset.AssetEnableExitWarehouseQueryModel;
import com.xiliulou.electricity.query.asset.ElectricityBatteryBatchUpdateFranchiseeQueryModel;
import com.xiliulou.electricity.query.asset.ElectricityBatteryEnableAllocateQueryModel;
import com.xiliulou.electricity.query.asset.ElectricityBatteryListSnByFranchiseeQueryModel;
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
    
    /**
     * 根据电池编码批量删除电池
     * @param tenantId
     * @param batterySnList
     * @return
     */
    Integer batchDeleteBySnList(@Param("tenantId") Integer tenantId, @Param("batterySnList") List<String> batterySnList);
    
    /**
     * 根据电池SN集查询电池
     * <p>用于内部接口，删除电池使用</p>
     *
     * @param franchiseeIdList
     * @param tenantId             租户ID
     * @param batterySns           电池SN编码集
     * @param bindFranchiseeIdList
     * @return List<ElectricityBattery>
     */
    List<ElectricityBattery> selectListBySnList(@Param("tenantId") Integer tenantId, @Param("batterySns") List<String> batterySns,@Param("franchiseeIdList") List<Long> bindFranchiseeIdList);
    
    List<ElectricityBattery> queryList(@Param("query") ElectricityBatteryQuery electricityBatteryQuery,
            @Param("offset") Long offset, @Param("size") Long size);
    
    Integer queryCount(@Param("query") ElectricityBatteryQuery electricityBatteryQuery);
    
    ElectricityBatteryVO selectBatteryInfo(@Param("uid") Long uid);
    
    Integer updateBatteryUser(ElectricityBattery electricityBattery);
    
    List<ElectricityBattery> selectListBatteryByGuessUid(@Param("guessUid") Long guessUid);
    
    List<ElectricityBattery> selectListBatteryBySnList(@Param("snList") List<String> snList);
    
    Integer batchUpdateBatteryGuessUid(@Param("batteryIdList") List<Long> IdList, @Param("guessUid") Long guessUid);
    
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
    
    int bindFranchiseeId(@Param("batteryQuery") BindElectricityBatteryQuery batteryQuery, @Param("stockStatus") Integer stockStatus);
    
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
    
    Integer updateGuessUidById(@Param("id") Long id);
    
    List<ElectricityBatteryDataVO> queryStrayBatteryList(@Param("query") ElectricityBatteryDataQuery electricityBatteryQuery,
                                                    @Param("offset") Long offset, @Param("size") Long size);
    Integer queryStrayBatteryCount(@Param("query") ElectricityBatteryDataQuery electricityBatteryQuery);

    List<ElectricityBatteryDataVO> queryOverdueBatteryList(@Param("query") ElectricityBatteryDataQuery electricityBatteryQuery,
                                              @Param("offset") Long offset, @Param("size") Long size);
    // TODO(heyafeng) 2024/6/7 16:30 换电套餐到期时间<当期时间为过期 车辆套餐<=当前时间为过期
    Integer queryOverdueBatteryCount(@Param("query") ElectricityBatteryDataQuery electricityBatteryQuery);

    List<ElectricityBatteryDataVO> queryOverdueCarBatteryList(@Param("query") ElectricityBatteryDataQuery electricityBatteryQuery,
                                                           @Param("offset") Long offset, @Param("size") Long size);
    Integer queryOverdueCarBatteryCount(@Param("query") ElectricityBatteryDataQuery electricityBatteryQuery);

    /**
     * 物理状态和业务状态查询
     * @param electricityBatteryQuery
     * @param offset
     * @param size
     * @return
     */
    List<ElectricityBatteryDataVO> queryStockBatteryList(@Param("query") ElectricityBatteryDataQuery electricityBatteryQuery,
                                                           @Param("offset") Long offset, @Param("size") Long size);

    /**
     * 物理状态和业务状态查询结果统计个数
     * @param electricityBatteryQuery
     * @return
     */
    Integer queryStockBatteryCount(@Param("query") ElectricityBatteryDataQuery electricityBatteryQuery);
    
    
    Integer existBySn(String sn);
    
    List<ElectricityBatteryBO> selectListSnByFranchiseeId(ElectricityBatteryListSnByFranchiseeQueryModel queryModel);
    
    Integer existsByWarehouseId(Long wareHouseId);
    
    Integer batchExitWarehouse(AssetBatchExitWarehouseQueryModel assetBatchExitWarehouseQueryModel);
    
    List<ElectricityBatteryBO> selectListEnableAllocateBattery(ElectricityBatteryEnableAllocateQueryModel queryModel);
    
    Integer updateFranchiseeId(ElectricityBatteryBatchUpdateFranchiseeQueryModel updateFranchiseeQueryModel);
    
    List<ElectricityBatteryBO> selectListEnableExitWarehouseBattery(AssetEnableExitWarehouseQueryModel queryModel);
    
    List<ElectricityBattery> selectListByIdList(@Param("idList") List<Long> idList);
    
    /**
     * <p>
     *    Description: queryIdsBySnArray
     * </p>
     * @param snList snList
     * @param tenantId tenantId
     * @param sourceFranchiseeId sourceFranchiseeId
     * @return java.util.List<java.lang.Long>
     * <p>Project: ElectricityBatteryMapper</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <a herf="https://benyun.feishu.cn/wiki/GrNjwBNZkipB5wkiws2cmsEDnVU#S5pYdtn2ooNnzqxWFbxcqGownbe">12.8 资产调拨（2条优化点)</a>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/3/18
    */
    List<ElectricityBattery> selectListBySnArray(@Param("list") List<String> snList, @Param("tenantId") Integer tenantId, @Param("franchiseeId") Long sourceFranchiseeId);
    
    List<ElectricityBatteryVO> selectListBatteriesBySn(@Param("offset") Integer offset, @Param("size") Integer size, @Param("tenantId") Integer tenantId, @Param("franchiseeId") Long franchiseeId, @Param("sn") String sn);
    
    List<ElectricityBatteryVO> selectListBatteriesByFranchisee(@Param("offset") Integer offset, @Param("size") Integer size, @Param("tenantId") Integer tenantId,
            @Param("franchiseeIds") List<Long> franchiseeIds, @Param("sn") String sn);
    
    List<ElectricityBattery> selectListByEid(@Param("eIdList") List<Integer> electricityCabinetIdList);
    
    List<ExportMutualBatteryBO> selectMutualBattery(@Param("tenantId") Integer tenantId);
}
