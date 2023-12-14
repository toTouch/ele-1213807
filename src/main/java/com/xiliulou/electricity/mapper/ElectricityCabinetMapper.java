package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.bo.asset.ElectricityCabinetBO;
import com.xiliulou.electricity.dto.asset.CabinetBatchOutWarehouseDTO;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.queryModel.asset.AssetBatchExitWarehouseQueryModel;
import com.xiliulou.electricity.queryModel.asset.ElectricityCabinetEnableAllocateQueryModel;
import com.xiliulou.electricity.queryModel.asset.ElectricityCabinetUpdateFranchiseeAndStoreQueryModel;
import com.xiliulou.electricity.queryModel.asset.ElectricityCabinetListSnByFranchiseeQueryModel;
import com.xiliulou.electricity.vo.EleCabinetDataAnalyseVO;
import com.xiliulou.electricity.vo.ElectricityCabinetBatchOperateVo;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.electricity.vo.SearchVo;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    List<ElectricityCabinetVO> queryList(@Param("query") ElectricityCabinetQuery electricityCabinetQuery);


    List<ElectricityCabinetVO> showInfoByDistance(@Param("query") ElectricityCabinetQuery electricityCabinetQuery);


    List<Long> queryFullyElectricityBattery(@Param("id") Integer id, @Param("batteryType") String batteryType);


    List<Long> queryFullyElectricityBatteryForLowBatteryExchange(@Param("id") Integer id, @Param("batteryType") String batteryType, @Param("fullyCharged") Double fullyCharged);


    List<Map<String, Object>> queryNameList(@Param("size") Long size, @Param("offset") Long offset, @Param("eleIdList") List<Integer> eleIdList, @Param("tenantId") Integer tenantId);

    List<ElectricityCabinet> homeOne(@Param("eleIdList") List<Integer> eleIdList, @Param("tenantId") Integer tenantId);

    Integer queryCount(@Param("query") ElectricityCabinetQuery electricityCabinetQuery);

    List<HashMap<String, String>> homeThree(@Param("startTimeMilliDay") long startTimeMilliDay, @Param("endTimeMilliDay") Long endTimeMilliDay, @Param("eleIdList") List<Integer> eleIdList, @Param("tenantId") Integer tenantId);

    Integer queryCountByStoreIds(@Param("tenantId") Integer tenantId, @Param("storeIds") List<Long> storeIds);

	Integer queryCountByStoreIdsAndStatus(@Param("tenantId") Integer tenantId,@Param("storeIds") List<Long> storeIds,@Param("status") Integer status);
	
	Integer updateEleById(ElectricityCabinet electricityCabinet);

    List<ElectricityCabinet> selectEleCabinetListByLongitudeAndLatitude(@Param("query") ElectricityCabinetQuery cabinetQuery);
    List<ElectricityCabinetVO> queryName(@Param("tenantId") Integer tenantId, @Param("id") Integer id);
    
    List<ElectricityCabinet> eleCabinetSearch(ElectricityCabinetQuery query);

    List<ElectricityCabinet> selectByQuery(ElectricityCabinetQuery query);

    List<EleCabinetDataAnalyseVO> selecteleCabinetVOByQuery(ElectricityCabinetQuery query);

    Integer selectOfflinePageCount(ElectricityCabinetQuery cabinetQuery);

    List<ElectricityCabinet> superAdminSelectByQuery(ElectricityCabinetQuery query);

    List<Integer> selectEidByStoreId(@Param("tenantId") Integer tenantId, @Param("storeId") Long storeId);

    List<ElectricityCabinetVO> selectElectricityCabinetByAddress(ElectricityCabinetQuery electricityCabinetQuery);

    List<EleCabinetDataAnalyseVO> selectLockCellByQuery(ElectricityCabinetQuery cabinetQuery);

    Integer selectLockPageCount(ElectricityCabinetQuery cabinetQuery);

    List<EleCabinetDataAnalyseVO> selectPowerPage(ElectricityCabinetQuery cabinetQuery);

    Integer selectPowerPageCount(ElectricityCabinetQuery cabinetQuery);

    List<ElectricityCabinetBatchOperateVo> batchOperateList(ElectricityCabinetQuery query);

    List<SearchVo> cabinetSearch(@Param("size") Long size, @Param("offset") Long offset, @Param("name") String name,
                               @Param("tenantId") Integer tenantId);
    
    Integer existByProductKeyAndDeviceName(@Param("productKey") String productKey, @Param("deviceName") String deviceName);
    
    Integer batchOutWarehouse(CabinetBatchOutWarehouseDTO outWarehouseDTO);
    
    Integer existsByWarehouseId(Long wareHouseId);
    
    List<ElectricityCabinetBO> selectListByFranchiseeIdAndStockStatus(ElectricityCabinetListSnByFranchiseeQueryModel electricityCabinetListSnByFranchiseeQueryModel);
    
    Integer batchExitWarehouse(AssetBatchExitWarehouseQueryModel batchExitWarehouseQueryModel);
    
    Integer updateFranchiseeIdAndStoreId(ElectricityCabinetUpdateFranchiseeAndStoreQueryModel updateFranchiseeAndStoreQueryModel);
    
    List<ElectricityCabinetBO> selectListEnableAllocateCabinet(ElectricityCabinetEnableAllocateQueryModel enableAllocateQueryModel);
    
    List<ElectricityCabinetBO> selectListEnableExitWarehouseCabinet(@Param("idSet") Set<Long> idSet, @Param("tenantId") Integer tenantId, @Param("franchiseeId") Long franchiseeId, @Param("stockStatus") Integer stockStatus);
    
    List<ElectricityCabinetBO> selectListBySnList(@Param("snList") List<String> snList, @Param("tenantId")Integer tenantId, @Param("franchiseeId") Long franchiseeId);
    
    List<ElectricityCabinetVO> selectListForStatistics(@Param("query") ElectricityCabinetQuery electricityCabinetQuery);}
