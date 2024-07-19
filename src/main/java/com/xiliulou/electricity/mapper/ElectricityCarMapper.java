package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.bo.asset.ElectricityCarBO;
import com.xiliulou.electricity.domain.car.CarInfoDO;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.car.CarDataEntity;
import com.xiliulou.electricity.entity.car.CarDataVO;
import com.xiliulou.electricity.query.ElectricityCarQuery;
import com.xiliulou.electricity.query.UserCarLikeSnQuery;
import com.xiliulou.electricity.query.asset.AssetBatchExitWarehouseQueryModel;
import com.xiliulou.electricity.query.asset.AssetEnableExitWarehouseQueryModel;
import com.xiliulou.electricity.query.asset.ElectricityCarListSnByFranchiseeQueryModel;
import com.xiliulou.electricity.query.asset.ElectricityCarUpdateFranchiseeAndStoreQueryModel;
import com.xiliulou.electricity.query.car.CarDataConditionReq;
import com.xiliulou.electricity.query.car.CarDataQuery;
import com.xiliulou.electricity.vo.ElectricityCarMoveVo;
import com.xiliulou.electricity.vo.ElectricityCarOverviewVo;
import com.xiliulou.electricity.vo.ElectricityCarVO;
import com.xiliulou.electricity.vo.UserCarLikeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 换电柜表(TElectricityCar)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
public interface ElectricityCarMapper extends BaseMapper<ElectricityCar> {
    
    /**
     * 根据ID更新车辆绑定用户，包含绑定、解绑
     *
     * @param electricityCarUpdate
     * @return 操作条数
     */
    int updateCarBindStatusById(ElectricityCar electricityCarUpdate);
    
    
    /**
     * 根据 uid 查询车辆信息<br /> 复合查询，车辆、门店、车辆经纬度
     *
     * @param tenantId 租户ID
     * @param carId    车辆ID
     * @return
     */
    CarInfoDO queryByCarId(@Param("tenantId") Integer tenantId, @Param("carId") Long carId);
    
    List<ElectricityCarVO> queryList(@Param("query") ElectricityCarQuery electricityCarQuery);
    
    Integer queryCount(@Param("query") ElectricityCarQuery electricityCarQuery);
    
    Integer updateBindUser(ElectricityCar electricityCar);
    
    Integer queryCountByStoreIds(@Param("tenantId") Integer tenantId, @Param("storeIds") List<Long> storeIds);
    
    ElectricityCar selectBySn(@Param("sn") String sn, @Param("tenantId") Integer tenantId);
    
    Integer updateLockTypeById(@Param("ids") List<Long> tempIds, @Param("typeLock") Integer typeLock);
    
    List<ElectricityCar> queryByStoreIds(@Param("storeIds") List<Long> storeIds, @Param("tenantId") Integer tenantId);
    
    List<ElectricityCarOverviewVo> queryElectricityCarOverview(@Param("carIds") List<Integer> sidList, @Param("sn") String sn, @Param("tenantId") Integer tenantId);
    
    Long batteryStatistical(@Param("carIds") List<Integer> carIdList, @Param("tenantId") Integer tenantId);
    
    List<ElectricityCarMoveVo> queryEnableMoveCarByStoreId(@Param("storeId") Long storeId, @Param("sn") String sn, @Param("size") Long size, @Param("offset") Long offset,
            @Param("tenantId") Integer tenantId);
    
    List<ElectricityCar> queryModelIdBySidAndIds(@Param("carIds") List<Long> carIds, @Param("sid") Long sid, @Param("status") Integer status, @Param("tenantId") Integer tenantId);
    
    Integer updateStoreIdByIds(@Param("sid") Long sid, @Param("carIds") List<Long> carIds, @Param("updateTime") Long updateTime, @Param("tenantId") Integer tenantId);
    
    Integer isUserBindCar(@Param("uid") Long uid, @Param("tenantId") Integer tenantId);
    
    /**
     * 查询车辆所有的信息，运维数据，不可调用
     *
     * @return
     */
    List<CarDataEntity> queryAllCarData(@Param("query") CarDataConditionReq carDataConditionReq);
    
    /**
     * 查询所有车辆信息总数
     *
     * @return
     */
    Integer queryAllCarDataCount(@Param("query") CarDataConditionReq carDataConditionReq);
    
    /**
     * 查询已租车辆
     *
     * @return
     */
    List<CarDataEntity> queryRentCarData(@Param("query") CarDataConditionReq carDataConditionReq);
    
    /**
     * 查询已租车辆总数
     *
     * @return
     */
    Integer queryRentCarDataCount(@Param("query") CarDataConditionReq carDataConditionReq);
    
    
    /**
     * 查询未租车辆
     *
     * @return
     */
    List<CarDataEntity> queryNotRentCarData(@Param("query") CarDataConditionReq carDataConditionReq);
    
    /**
     * 查询未租车辆总数
     *
     * @return
     */
    Integer queryNotRentCarDataCount(@Param("query") CarDataConditionReq carDataConditionReq);
    
    /**
     * 查询套餐已经到期的车辆数据
     *
     * @param carDataConditionReq
     * @return
     */
    List<CarDataEntity> queryOverdueCarData(@Param("query") CarDataConditionReq carDataConditionReq);
    
    /**
     * 查询套餐已经到期的车辆数据总数
     *
     * @param carDataConditionReq
     * @return
     */
    Integer queryOverdueCarDataCount(@Param("query") CarDataConditionReq carDataConditionReq);
    
    
    /**
     * 查询套餐已经到期的车辆数据
     *
     * @param carDataConditionReq
     * @return
     */
    List<CarDataEntity> queryOfflineCarData(@Param("query") CarDataConditionReq carDataConditionReq);
    
    /**
     * 查询套餐已经到期的车辆数据总数
     *
     * @param carDataConditionReq
     * @return
     */
    Integer queryOfflineCarDataCount(@Param("query") CarDataConditionReq carDataConditionReq);
    
    // TODO(heyafeng) 2024/6/7 16:30 换电套餐到期时间<当期时间为过期 车辆套餐<=当前时间为过期
    List<CarDataVO> queryCarPageByCondition(@Param("query") CarDataQuery carDataQuery, @Param("offset") Long offset, @Param("size") Long size);
    
    Integer queryCarDataCountByCondition(@Param("query") CarDataQuery carDataQuery);
    
    List<ElectricityCarVO> selectListBySnList(@Param("snList") List<String> snList, @Param("tenantId") Integer tenantId, @Param("franchiseeId") Long franchiseeId);
    
    List<ElectricityCarBO> selectListByFranchiseeIdAndStockStatus(ElectricityCarListSnByFranchiseeQueryModel queryModel);
    
    Integer batchExitWarehouse(AssetBatchExitWarehouseQueryModel assetBatchExitWarehouseQueryModel);
    
    Integer existOutWarehouse(@Param("idList") List<Integer> idList, @Param("tenantId") Integer tenantId);
    
    Integer batchUpdateFranchiseeIdAndStoreByIdList(@Param("idList") List<Integer> idList, @Param("franchiseeId") Long franchiseeId, @Param("storeId") Integer storeId,
            @Param("tenantId") Integer tenantId, @Param("updateTime") Long updateTime, @Param("stockStatus") Integer stockStatus);
    
    Integer batchInsertCar(@Param("list") List<ElectricityCar> saveList);
    
    Integer existsByWarehouseId(Long wareHouseId);
    
    List<ElectricityCarBO> selectListByIds(@Param("idSet") Set<Integer> idSet);
    
    List<ElectricityCarBO> selectListEnableExitWarehouseCar(AssetEnableExitWarehouseQueryModel queryModel);
    
    Integer updateFranchiseeIdAndStoreId(ElectricityCarUpdateFranchiseeAndStoreQueryModel updateFranchiseeAndStoreQueryModel);
    
    Integer queryCountByWarehouse(@Param("query") ElectricityCarQuery electricityCarQuery);
    
    Integer updatePhoneByUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("newPhone") String newPhone);
    
    /**
     * <p>
     * Description: selectCarSnByLike P0需求 15.1 实名用户列表（16条优化项）iv.4 模糊搜索车辆SN码
     * </p>
     *
     * @param query query
     * @return java.util.List<com.xiliulou.electricity.vo.UserCarLikeVo>
     * <p>Project: ElectricityCarMapper</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/3/13
     */
    List<UserCarLikeVO> selectListCarSnByLike(@Param("query") UserCarLikeSnQuery query);
    
    /**
     * <p>
     *    Description: queryIdsBySnArray
     * </p>
     * @param snList snList
     * @param tenantId tenantId
     * @param sourceFranchiseeId sourceFranchiseeId
     * @return java.util.List<java.lang.Long>
     * <p>Project: ElectricityCarMapper</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <a herf="https://benyun.feishu.cn/wiki/GrNjwBNZkipB5wkiws2cmsEDnVU#S5pYdtn2ooNnzqxWFbxcqGownbe">12.8 资产调拨（2条优化点)</a>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/3/18
    */
    List<ElectricityCarBO> selectListBySnArray(@Param("list") List<String> snList, @Param("tenantId") Integer tenantId, @Param("franchiseeId") Long sourceFranchiseeId);
}
