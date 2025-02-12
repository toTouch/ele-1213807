package com.xiliulou.electricity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.ExportMutualBatteryBO;
import com.xiliulou.electricity.entity.BatteryChangeInfo;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.query.BatteryExcelV3Query;
import com.xiliulou.electricity.query.BindElectricityBatteryQuery;
import com.xiliulou.electricity.query.EleBatteryQuery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.query.HomepageBatteryFrequencyQuery;
import com.xiliulou.electricity.query.asset.AssetEnableExitWarehouseQueryModel;
import com.xiliulou.electricity.query.supper.DelBatteryReq;
import com.xiliulou.electricity.request.asset.AssetBatchExitWarehouseRequest;
import com.xiliulou.electricity.request.asset.BatteryAddRequest;
import com.xiliulou.electricity.request.asset.ElectricityBatteryBatchUpdateFranchiseeRequest;
import com.xiliulou.electricity.request.asset.ElectricityBatteryEnableAllocateRequest;
import com.xiliulou.electricity.request.asset.ElectricityBatterySnSearchRequest;
import com.xiliulou.electricity.vo.BatteryChangeInfoVO;
import com.xiliulou.electricity.vo.BigEleBatteryVo;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;
import com.xiliulou.electricity.vo.HomepageBatteryFrequencyVo;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 换电柜电池表(ElectricityBattery)表服务接口
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
public interface ElectricityBatteryService extends IService<ElectricityBattery> {

    /**
     * 根据电池SN码集查询
     * @param tenantId 租户ID
     * @param snList 电池SN码
     * @return 电池信息集
     */
    List<ElectricityBattery> selectBySnList(Integer tenantId, List<String> snList);
    
    R saveElectricityBatteryV2(BatteryAddRequest batteryAddRequest);
    
    Integer update(ElectricityBattery electricityBattery);
    
    R updateForAdmin(EleBatteryQuery electricityBattery);

    R queryList(ElectricityBatteryQuery electricityBatteryQuery, Long offset, Long size);

    R queryById(Long electricityBatteryId);

    R deleteElectricityBattery(Long id, Integer isNeedSync);

    ElectricityBattery queryByBindSn(String initElectricityBatterySn);

    ElectricityBattery queryByUid(Long uid);

    ElectricityBattery queryBySnFromDb(String oldElectricityBatterySn);
    
    ElectricityBattery queryPartAttrBySnFromCache(String sn);

    ElectricityBattery queryBySnFromDb(String oldElectricityBatterySn, Integer tenantId);

    Integer updateBatteryUser(ElectricityBattery electricityBattery);
    
    List<ElectricityBattery> listBatteryByGuessUid(Long guessUid);
    
    List<ElectricityBattery> listBatteryBySnList(List<String> snList);
    
    Integer batchUpdateBatteryGuessUid(List<Long> batteryIdList,Long guessUid);

    Integer updateBatteryStatus(ElectricityBattery electricityBattery);

    R queryCount(ElectricityBatteryQuery electricityBatteryQuery);

    R batteryOutTimeInfo(Long uid);

    void handlerLowBatteryReminder();

    R queryBindListByPage(Long offset, Long size, Long franchiseeId);

    void insert(ElectricityBattery electricityBattery);

    Triple<Boolean, String, Object> queryInfoByUid(Long uid, Integer isNeedLocation);

    Integer querySumCount(ElectricityBatteryQuery electricityBatteryQuery);
    
    BigEleBatteryVo queryMaxPowerByElectricityCabinetId(Integer electricityCabinetId);

    ElectricityBatteryVO selectBatteryDetailInfoBySN(String sn);

    List<ElectricityBattery> queryWareHouseByElectricityCabinetId(Integer electricityCabinetId);

    List<HomepageBatteryFrequencyVo> homepageBatteryAnalysis(HomepageBatteryFrequencyQuery homepageBatteryFrequencyQuery);

    List<HomepageBatteryFrequencyVo> homepageBatteryAnalysisCount(HomepageBatteryFrequencyQuery homepageBatteryFrequencyQuery);

    R queryBatteryOverview(ElectricityBatteryQuery electricityBatteryQuery);

    R batteryStatistical(ElectricityBatteryQuery electricityBatteryQuery);

    R bindFranchiseeForBattery(BindElectricityBatteryQuery bindElectricityBatteryQuery);
    
    R<Object> bindFranchiseeForBatteryV2(BindElectricityBatteryQuery bindElectricityBatteryQuery);

    List<ElectricityBattery> selectByBatteryIds(List<Long> batteryIds);

    ElectricityBattery selectByBatteryIdAndFranchiseeId(Long batteryId,Long franchiseeId);
    
    List<ElectricityBatteryVO> selectBatteryInfoByBatteryName(ElectricityBatteryQuery batteryQuery);

    Integer isFranchiseeBindBattery(Long id,Integer tenantId);

    Triple<Boolean, String, Object> queryBatteryInfoBySn(String sn);

    Triple<Boolean, String, Object> queryBatteryMapList(Integer offset, Integer size, List<Long> franchiseeIds);

    Integer isUserBindBattery(Long uid, Integer tenantId);

    Integer insertBatch(List<ElectricityBattery> saveList);

    ElectricityBattery queryUserAttrBySnFromDb(String sn);

    Triple<Boolean, String, Object> queryBatteryLocationTrack(Long uid, Long beginTime, Long endTime);

    void export(ElectricityBatteryQuery query, HttpServletResponse response);
    
    R saveBatchFromExcel(BatteryExcelV3Query batteryExcelV3Query, Long uid);
    
    List<ElectricityBatteryVO> listSnByFranchiseeId(ElectricityBatterySnSearchRequest electricityBatterySnSearchRequest);
    
    Integer existsByWarehouseId(Long wareHouseId);
    
    Integer batchExitWarehouse(AssetBatchExitWarehouseRequest assetBatchExitWarehouseRequest);
    
    List<ElectricityBatteryVO> listEnableAllocateBattery(ElectricityBatteryEnableAllocateRequest electricityBatteryEnableAllocateRequest);
    
    Integer batchUpdateFranchiseeId(List<ElectricityBatteryBatchUpdateFranchiseeRequest> batchUpdateFranchiseeRequestList);
    
    List<ElectricityBatteryVO> listEnableExitWarehouseBattery(AssetEnableExitWarehouseQueryModel queryModel);
    
    List<ElectricityBattery> queryListByIdList(List<Long> batteryIdSet);
    
    List<ElectricityBatteryVO> listBatteriesBySn(Integer offset, Integer size, Integer tenantId, Long franchiseeId, String sn);
    
    List<ElectricityBatteryVO> listBatteriesBySnV2(Integer offset, Integer size, Long uid, String sn);
    
    List<ElectricityBatteryVO> getListBatteriesByFranchisee(Integer offset, Integer size, Integer tenantId, List<Long> franchiseeIdList, String sn);
    
    /**
     * <p>
     *    Description: queryIdsBySnArray
     * </p>
     * @param snList snList
     * @param tenantId tenantId
     * @param sourceFranchiseeId sourceFranchiseeId
     * @return java.util.List<java.lang.Long>
     * <p>Project: ElectricityBatteryService</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/3/18
    */
    Map<String,Long> listIdsBySnArray(List<String> snList, Integer tenantId, Long sourceFranchiseeId);
    
    List<ElectricityBattery> listBatteryByEid(List<Integer> electricityCabinetIdList);
    
    List<ElectricityBattery> listBySnList(List<String> item, Integer tenantId, List<Long> bindFranchiseeIdList);
    
    R deleteBatteryByExcel(DelBatteryReq delBatteryReq);


    List<ExportMutualBatteryBO> queryMutualBattery(Integer tenantId, List<Long> franchiseeIds);
    
    List<BatteryChangeInfoVO> getBatteryChangeOtherInfo(List<BatteryChangeInfo> list);
    
    Integer existsByBatteryType(String batteryType, Integer tenantId);
    
    Map<Long, ElectricityBattery> listUserBatteryByUidList(List<Long> uidList, Integer tenantId);
}
