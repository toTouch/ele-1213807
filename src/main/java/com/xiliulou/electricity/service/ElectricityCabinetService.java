package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.merchant.AreaCabinetNumBO;
import com.xiliulou.electricity.entity.ElectricityAbnormalMessageNotify;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.Message;
import com.xiliulou.electricity.entity.MqNotifyCommon;
import com.xiliulou.electricity.query.BatteryReportQuery;
import com.xiliulou.electricity.query.EleOuterCommandQuery;
import com.xiliulou.electricity.query.ElectricityCabinetAddAndUpdate;
import com.xiliulou.electricity.query.ElectricityCabinetAddressQuery;
import com.xiliulou.electricity.query.ElectricityCabinetBatchEditRentReturnCountQuery;
import com.xiliulou.electricity.query.ElectricityCabinetImportQuery;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.query.ElectricityCabinetTransferQuery;
import com.xiliulou.electricity.query.HomepageBatteryFrequencyQuery;
import com.xiliulou.electricity.query.HomepageElectricityExchangeFrequencyQuery;
import com.xiliulou.electricity.query.api.ApiRequestQuery;
import com.xiliulou.electricity.request.asset.TransferCabinetModelRequest;
import com.xiliulou.electricity.vo.CabinetBatteryVO;
import com.xiliulou.electricity.vo.EleCabinetDataAnalyseVO;
import com.xiliulou.electricity.vo.ElectricityCabinetCountVO;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.electricity.vo.RentReturnEditEchoVO;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 换电柜表(TElectricityCabinet)表服务接口
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
public interface ElectricityCabinetService {

    /**
     * 根据主键ID集获取柜机基本信息
     * @param ids 主键ID集
     * @return
     */
    List<ElectricityCabinet> listByIds(Set<Integer> ids);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinet queryByIdFromCache(Integer id);

    /**
     * 修改数据
     *
     * @param electricityCabinet 实例对象
     * @return 实例对象
     */
    Integer update(ElectricityCabinet electricityCabinet);

    R save(ElectricityCabinetAddAndUpdate electricityCabinetAddAndUpdate);

    R edit(ElectricityCabinetAddAndUpdate electricityCabinetAddAndUpdate);

    R delete(Integer id);

    R queryList(ElectricityCabinetQuery electricityCabinetQuery);

    R showInfoByDistance(ElectricityCabinetQuery electricityCabinetQuery);

    R showInfoByDistanceV2(ElectricityCabinetQuery electricityCabinetQuery);

    Integer queryFullyElectricityBattery(Integer id, String batteryType);

    boolean deviceIsOnline(String productKey, String deviceName);

    Integer queryByModelId(Integer id);

    R updateStatus(Integer id, Integer usableStatus);

    R homeOne(Long beginTime, Long endTime);

    R homeTwo(Long beginTime, Long endTime);

    R homeThree(Long beginTime, Long endTime, Integer type);

    List<HashMap<String, String>> homeThreeInner(Long startTimeMilliDay, Long endTimeMilliDay, List<Integer> eleIdList, Integer tenantId);

    R home();

    ElectricityCabinet queryByProductAndDeviceName(String productKey, String deviceName);

    ElectricityCabinet queryFromCacheByProductAndDeviceName(String productKey, String deviceName);

    R checkOpenSessionId(String sessionId);

    R sendCommandToEleForOuter(EleOuterCommandQuery eleOuterCommandQuery);

    R queryByDeviceOuter(String productKey, String deviceName);

    R showInfoByStoreId(Long storeId);

    @Deprecated
    R queryByOrder(String productKey, String deviceName);

    R queryByRentBattery(String productKey, String deviceName);

    List<Map<String, Object>> queryNameList(Long size, Long offset, List<Integer> eleIdList, Integer tenantId);

    R batteryReport(BatteryReportQuery batteryReportQuery);

    List<ElectricityCabinet> queryByStoreId(Long storeId);

    R queryByDevice(String productKey, String deviceName);

    boolean isBusiness(ElectricityCabinet electricityCabinet);

    R queryCount(ElectricityCabinetQuery electricityCabinetQuery);

    Integer queryCountByStoreId(Long id);

    R checkBattery(String productKey, String deviceName, String batterySn, Boolean isParseBattery);

    R queryById(Integer id);

    R queryCabinetBelongFranchisee(Integer id);

    Triple<Boolean, String, Object> findUsableBatteryCellNoV2(Integer eid, String batteryType, Double fullyCharged, Long franchiseeId);

    Triple<Boolean, String, Object> findUsableBatteryCellNoV3(Integer eid, Franchisee franchisee, Double fullyCharged, ElectricityBattery electricityBattery, Long uid);

    Pair<Boolean, Integer> findUsableEmptyCellNo(Integer id);
    
    Pair<Boolean, Integer> findUsableEmptyCellNoV2(Integer eid, String version);

    R getFranchisee(String productKey, String deviceName);

    Integer querySumCount(ElectricityCabinetQuery electricityCabinetQuery);

    Integer queryCountByStoreIds(Integer tenantId, List<Long> storeIds);

    Integer queryCountByStoreIdsAndStatus(Integer tenantId, List<Long> storeIds,Integer status);

    R queryDeviceIsUnActiveFStatus(ApiRequestQuery apiRequestQuery);

    R queryAllElectricityCabinet(ElectricityCabinetQuery electricityCabinetQuery);

    int idempotentUpdateCupboard(ElectricityCabinet electricityCabinet, ElectricityCabinet updateElectricityCabinet);

    R queryElectricityCabinetBoxInfoById(Integer electricityCabinetId);

    R homepageTurnover();

    R homepageDeposit();

    R homepageOverviewDetail();

    R homepageBenefitAnalysis(Long beginTime, Long enTime);

    R homepageUserAnalysis(Long beginTime, Long enTime);

    R homepageElectricityCabinetAnalysis();

    R homepageExchangeOrderFrequency(HomepageElectricityExchangeFrequencyQuery homepageElectricityExchangeFrequencyQuery);

    R homepageBatteryAnalysis(HomepageBatteryFrequencyQuery homepageBatteryFrequencyQuery);

    R queryElectricityCabinetFileById(Integer electricityCabinetId);
    
    Triple<Boolean, String, Object> queryElectricityCabinetBoxInfoByCabinetId(Integer electricityCabinetId);

    List<ElectricityCabinet> selectBystoreIds(List<Long> storeIds);

    List<ElectricityCabinet> selectByFranchiseeIds(List<Long> franchiseeIds);

    R acquireIdcardFileSign();

    R queryName(Integer tenant,Integer id);

    List<ElectricityCabinet> eleCabinetSearch(ElectricityCabinetQuery query);

    List<ElectricityCabinet> selectByQuery(ElectricityCabinetQuery query);

    List<EleCabinetDataAnalyseVO> selecteleCabinetVOByQuery(ElectricityCabinetQuery cabinetQuery);

    R superAdminQueryName(Integer id);

    R selectEleCabinetListByLongitudeAndLatitude(ElectricityCabinetQuery cabinetQuery);
    
    
    R sendCommandToEleForOuterSuper(EleOuterCommandQuery eleOuterCommandQuery);
    
    R otaCommand(Integer eid, Integer operateType, Integer versionType, List<Integer> cellNos);
    
    R checkOtaSession(String sessionId);
    
    List<ElectricityCabinet> superAdminSelectByQuery(ElectricityCabinetQuery query);
    
    void sendFullBatteryMessage(List<Message>  messageList);

    List<MqNotifyCommon<ElectricityAbnormalMessageNotify>> buildAbnormalMessageNotify(ElectricityCabinet electricityCabinet);

    List<Integer> selectEidByStoreId(Long storeId);

    List<ElectricityCabinetVO> selectElectricityCabinetByAddress(ElectricityCabinetQuery electricityCabinetQuery);

    CabinetBatteryVO batteryStatistics(Long id);

    Triple<Boolean, String, Object> updateOnlineStatus(Long id);

    Triple<Boolean, String, Object> updateAddress(ElectricityCabinetAddressQuery eleCabinetAddressQuery);

    boolean isNoElectricityBattery(ElectricityCabinetBox electricityCabinetBox);

    boolean isBatteryInElectricity(ElectricityCabinetBox electricityCabinetBox);

    boolean isFullBattery(ElectricityCabinetBox electricityCabinetBox);

    boolean isExchangeable(ElectricityCabinetBox electricityCabinetBox, Double fullyCharged);

    Integer selectOfflinePageCount(ElectricityCabinetQuery cabinetQuery);

    List<EleCabinetDataAnalyseVO> selectLockCellByQuery(ElectricityCabinetQuery cabinetQuery);

    Integer selectLockPageCount(ElectricityCabinetQuery cabinetQuery);

    List<EleCabinetDataAnalyseVO> selectPowerPage(ElectricityCabinetQuery cabinetQuery);

    Integer selectPowerPageCount(ElectricityCabinetQuery cabinetQuery);

    R batchOperateList(ElectricityCabinetQuery query);

    R cabinetSearch(Long size, Long offset, String name , Integer tenantId);

    Triple<Boolean, String, Object> existsElectricityCabinet(String productKey, String deviceName);

    Triple<Boolean, String, Object> batchDeleteCabinet(Set<Integer> ids);

    Triple<Boolean, String, Object> batchImportCabinet(List<ElectricityCabinetImportQuery> list);

    Triple<Boolean, String, Object> transferCabinet(ElectricityCabinetTransferQuery query);
    
    Triple<Boolean, String, Object> listTransferCabinetModel(TransferCabinetModelRequest cabinetModelRequest);

    Triple<Boolean, String, Object> physicsDelete(ElectricityCabinet electricityCabinet);

    void exportExcel(ElectricityCabinetQuery query, HttpServletResponse response);

    Triple<Boolean, String, Object> batchUpdateAddress(List<ElectricityCabinet> list);

    void batchUpdate(List<ElectricityCabinet> list);
    
    R queryElectricityCabinetExtendData(Integer electricityCabinetId);
    
    R showBatteryVAndCapacity(Integer electricityCabinetId);
    
    R homeOneV2();
    
    List<ElectricityCabinetCountVO> queryCabinetCount(ElectricityCabinetQuery cabinetQuery);
    
    void addElectricityCabinetLocToGeo(ElectricityCabinet electricityCabinet);
    
    Integer existsByAreaId(Long areaId);
    
    List<AreaCabinetNumBO> countByAreaGroup(List<Long> areaIdList);
    
    /**
     * <p>
     *    Description: queryIdsBySnArray
     * </p>
     * @param snList snList
     * @param tenantId tenantId
     * @param sourceFranchiseeId sourceFranchiseeId
     * @return java.util.List<java.lang.Long>
     * <p>Project: ElectricityCabinetService</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/3/18
    */
    Map<String,Long> listIdsBySnArray(List<String> snList, Integer tenantId, Long sourceFranchiseeId);
    
    List<Integer> listIdsByName(String name);
    
    RentReturnEditEchoVO rentReturnEditEcho(Long id);
    
    void batchEditRentReturn(List<ElectricityCabinetBatchEditRentReturnCountQuery> countQueryList);
    
}
