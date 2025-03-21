package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.bo.ElectricityCabinetCardInfoBO;
import com.xiliulou.electricity.bo.asset.ElectricityCabinetBO;
import com.xiliulou.electricity.bo.cabinet.ElectricityCabinetMapBO;
import com.xiliulou.electricity.bo.merchant.AreaCabinetNumBO;
import com.xiliulou.electricity.dto.asset.CabinetBatchOutWarehouseDTO;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.query.ElectricityCabinetIdByFilterQuery;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.query.asset.AssetBatchExitWarehouseQueryModel;
import com.xiliulou.electricity.query.asset.AssetEnableExitWarehouseQueryModel;
import com.xiliulou.electricity.query.asset.ElectricityCabinetEnableAllocateQueryModel;
import com.xiliulou.electricity.query.asset.ElectricityCabinetListSnByFranchiseeQueryModel;
import com.xiliulou.electricity.query.asset.ElectricityCabinetUpdateFranchiseeAndStoreQueryModel;
import com.xiliulou.electricity.vo.*;
import org.apache.ibatis.annotations.Param;

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
    List<ElectricityCabinetVO> queryList(@Param("query") ElectricityCabinetQuery electricityCabinetQuery);


    List<ElectricityCabinetVO> showInfoByDistance(@Param("query") ElectricityCabinetQuery electricityCabinetQuery);


    List<Long> queryFullyElectricityBattery(@Param("id") Integer id, @Param("batteryType") String batteryType);


    List<Long> queryFullyElectricityBatteryForLowBatteryExchange(@Param("id") Integer id, @Param("batteryType") String batteryType, @Param("fullyCharged") Double fullyCharged);


    List<Map<String, Object>> queryNameList(@Param("size") Long size, @Param("offset") Long offset, @Param("eleIdList") List<Integer> eleIdList, @Param("tenantId") Integer tenantId);

    List<ElectricityCabinet> homeOne(@Param("eleIdList") List<Integer> eleIdList, @Param("tenantId") Integer tenantId);

    Integer queryCount(@Param("query") ElectricityCabinetQuery electricityCabinetQuery);

    Integer queryCountByStoreIds(@Param("tenantId") Integer tenantId, @Param("storeIds") List<Long> storeIds);

	Integer queryCountByStoreIdsAndStatus(@Param("tenantId") Integer tenantId,@Param("storeIds") List<Long> storeIds,@Param("status") Integer status);
	
	Integer updateEleById(ElectricityCabinet electricityCabinet);

    List<ElectricityCabinetMapBO> selectEleCabinetListByLongitudeAndLatitude(@Param("query") ElectricityCabinetQuery cabinetQuery);
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
    
    List<ElectricityCabinetBO> selectListEnableExitWarehouseCabinet(AssetEnableExitWarehouseQueryModel queryModel);
    
    List<ElectricityCabinetBO> selectListBySnList(@Param("snList") List<String> snList, @Param("tenantId")Integer tenantId, @Param("franchiseeId") Long franchiseeId);
    
    List<ElectricityCabinetVO> selectListByPage(@Param("size") Long size, @Param("offset") Long offset);
    
    List<ElectricityCabinetVO> selectListForStatistics(@Param("query") ElectricityCabinetQuery electricityCabinetQuery);
    
    List<ElectricityCabinetBO> selectListByIdList(@Param("idList") List<Integer> idList);
    
    Integer existsByAreaId(Long areaId);
    
    List<AreaCabinetNumBO> countByAreaGroup(@Param("areaIdList") List<Long> areaIdList);
    
    List<ElectricityCabinetCountVO> selectCabinetCount(ElectricityCabinetQuery cabinetQuery);
    
    Integer updateCabinetById(ElectricityCabinet electricityCabinet);
    
    List<Integer> listIdsByName(@Param("name") String name);
    
    /**
     * <p>
     *    Description: queryIdsBySnArray
     * </p>
     * @param snList snList
     * @param tenantId tenantId
     * @param sourceFranchiseeId sourceFranchiseeId
     * @return java.util.List<java.lang.Long>
     * <p>Project: ElectricityCabinetMapper</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <a herf="https://benyun.feishu.cn/wiki/GrNjwBNZkipB5wkiws2cmsEDnVU#S5pYdtn2ooNnzqxWFbxcqGownbe">12.8 资产调拨（2条优化点)</a>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/3/18
    */
    List<ElectricityCabinetBO> selectListBySnArray(@Param("list") List<String> snList, @Param("tenantId") Integer tenantId, @Param("franchiseeId") Long sourceFranchiseeId);
    
    List<ElectricityCabinetVO> selectListSuperAdminPage(@Param("query") ElectricityCabinetQuery electricityCabinetQuery);

    List<ElectricityCabinetVO> selectListLowPowerPage(ElectricityCabinetQuery electricityCabinetQuery);

    Integer countLowPowerTotal(ElectricityCabinetQuery electricityCabinetQuery);

    List<Integer> selectCabinetIdByFilter(ElectricityCabinetIdByFilterQuery query);

    List<CabinetLocationVO> selectCabinetLocationByPage(@Param("size") Long size, @Param("offset") Long offset);

    List<ElectricityCabinetCardInfoBO> selectEleCardInfoByTenant(@Param("list") List<Integer> list);
}
