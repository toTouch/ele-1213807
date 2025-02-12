package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.domain.car.CarInfoDO;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.clickhouse.CarAttr;
import com.xiliulou.electricity.query.ElectricityCarAddAndUpdate;
import com.xiliulou.electricity.query.ElectricityCarBindUser;
import com.xiliulou.electricity.query.ElectricityCarMoveQuery;
import com.xiliulou.electricity.query.ElectricityCarQuery;
import com.xiliulou.electricity.query.UserCarLikeSnQuery;
import com.xiliulou.electricity.query.asset.AssetEnableExitWarehouseQueryModel;
import com.xiliulou.electricity.query.jt808.CarPositionReportQuery;
import com.xiliulou.electricity.request.asset.AssetBatchExitWarehouseRequest;
import com.xiliulou.electricity.request.asset.CarAddRequest;
import com.xiliulou.electricity.request.asset.CarBatchSaveRequest;
import com.xiliulou.electricity.request.asset.CarOutWarehouseRequest;
import com.xiliulou.electricity.request.asset.CarUpdateRequest;
import com.xiliulou.electricity.request.asset.ElectricityCarBatchUpdateFranchiseeAndStoreRequest;
import com.xiliulou.electricity.request.asset.ElectricityCarSnSearchRequest;
import com.xiliulou.electricity.service.impl.car.biz.CarRentalOrderBizServiceImpl;
import com.xiliulou.electricity.vo.ElectricityCarVO;
import com.xiliulou.electricity.vo.UserCarLikeVO;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 换电柜表(TElectricityCabinet)表服务接口
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
public interface ElectricityCarService {
    
    /**
     * 根据ID更新车辆绑定用户，包含绑定、解绑
     *
     * @param electricityCarUpdate
     * @return true(成功)、false(失败)
     */
    boolean updateCarBindStatusById(ElectricityCar electricityCarUpdate);
    
    
    /**
     * 根据用户ID查询车辆信息
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return 车辆信息
     */
    ElectricityCar selectByUid(Integer tenantId, Long uid);
    
    /**
     * 根据车辆型号ID判定，是否存在未租车辆
     *
     * @param carModelId 车辆型号ID
     * @return true(存在)、false(不存在)
     */
    boolean checkUnleasedByCarModelId(Integer carModelId);
    
    /**
     * 根据车辆型号ID，判定是否进行绑定
     *
     * @param carModelId 车辆型号ID
     * @return true(绑定)、false(未绑定)
     */
    boolean checkBindingByCarModelId(Integer carModelId);
    
    /**
     * 根据 uid 查询车辆信息<br /> 复合查询，车辆、门店、车辆经纬度
     *
     * @param tenantId 租户ID
     * @param carId    车辆ID
     * @return
     */
    CarInfoDO queryByCarId(Integer tenantId, Long carId);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCar queryByIdFromCache(Integer id);
    
    
    R saveV2(CarAddRequest carAddRequest);
    
    R delete(Integer id);
    
    R queryList(ElectricityCarQuery electricityCarQuery);
    
    Integer queryByModelId(Integer id);
    
    R queryCount(ElectricityCarQuery electricityCarQuery);
    
    ElectricityCar queryInfoByUid(Long uid);
    
    Integer queryCountByStoreIds(Integer tenantId, List<Long> storeIds);
    
    
    ElectricityCar selectBySn(String sn, Integer tenantId);
    
    Integer update(ElectricityCar updateElectricityCar);
    
    Integer carUnBindUser(ElectricityCar updateElectricityCar);
    
    Integer updateLockTypeByIds(List<Long> tempIds, Integer typeLock);
    
    Boolean carLockCtrl(String str, Integer lockType);
    
    R positionReport(CarPositionReportQuery carPositionReportQuery);
    
    List<ElectricityCar> queryByStoreIds(List<Long> storeIds);
    
    R queryElectricityCarOverview(String sn, List<Integer> franchiseeIds);
    
    R batteryStatistical(List<Integer> carIdList, Integer tenantId);
    
    R attrList(Long beginTime, Long endTime);
    
    CarAttr queryLastReportPointBySn(String sn);
    
    Integer isUserBindCar(Long uid, Integer tenantId);
    
    R queryElectricityCarMove(Long storeId, String sn, Long size, Long offset);
    
    R electricityCarMove(ElectricityCarMoveQuery electricityCarMoveQuery);
    
    Boolean retryCarLockCtrl(String str, Integer lockType, Integer retryCount);
    
    List<ElectricityCarVO> listBySnList(List<String> snList, Integer tenantId, Long franchiseeId);
    
    List<ElectricityCarVO> listByFranchiseeIdAndStockStatus(ElectricityCarSnSearchRequest electricityCarSnSearchRequest);
    
    Integer batchExitWarehouse(AssetBatchExitWarehouseRequest assetBatchExitWarehouseRequest);
    
    R batchUpdateFranchiseeIdAndStoreId(CarOutWarehouseRequest carOutWarehouseRequest);
    
    R bathSaveCar(CarBatchSaveRequest carBatchSaveRequest);
    
    List<ElectricityCar> queryModelIdBySidAndIds(List<Long> carIds, Long sourceSid, Integer status, Integer tenantId);
    
    Integer existsByWarehouseId(Long wareHouseId);
    
    List<ElectricityCarVO> listByIds(Set<Integer> idSet);
    
    List<ElectricityCarVO> listEnableExitWarehouseCar(AssetEnableExitWarehouseQueryModel queryModel);
    
    Integer batchUpdateRemove(List<ElectricityCarBatchUpdateFranchiseeAndStoreRequest> carBatchUpdateFranchiseeAndStoreRequestList);
    
    R queryCountByWarehouse(ElectricityCarQuery electricityCarQuery);
    
    /**
     * 根据更换手机号
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    Integer updatePhoneByUid(Integer tenantId, Long uid, String newPhone);
    
    /**
     * <p>
     * Description: 用户根据车辆Sn码模糊查询 P0需求 15.1 实名用户列表（16条优化项）iv.4 模糊搜索车辆SN码
     * </p>
     *
     * @param likeSnQuery {@see UserCarLikeSnQuery} 查询参数
     * @return com.xiliulou.core.web.R<com.xiliulou.electricity.vo.UserCarLikeVo>
     * <p>Project: ElectricityCarService</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/3/13
     */
    R<List<UserCarLikeVO>> listSnByLike(UserCarLikeSnQuery likeSnQuery);
    
    /**
     * <p>
     * Description: queryIdsBySnArray
     * </p>
     *
     * @param snList             snList
     * @param tenantId           tenantId
     * @param sourceFranchiseeId sourceFranchiseeId
     * @return java.util.List<java.lang.Long>
     * <p>Project: ElectricityCarService</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/3/18
     */
    Map<String, Long> listIdsBySnArray(List<String> snList, Integer tenantId, Long sourceFranchiseeId);
    
    
    R editV2(CarUpdateRequest carUpdateRequest, Long uid);
    
    /**
     * 根据租户id+用户id批量查询
     *
     * @param tenantId
     * @param uidList
     * @author caobotao.cbt
     * @date 2024/11/26 11:47
     */
    List<ElectricityCar> queryListByTenantIdAndUidList(Integer tenantId, List<Long> uidList);
    
    List<ElectricityCar> listNoDelByUidList(Integer tenantId, List<Long> uidList);
}
