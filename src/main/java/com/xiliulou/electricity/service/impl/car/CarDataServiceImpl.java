package com.xiliulou.electricity.service.impl.car;

import com.google.common.collect.Lists;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.CarDataEntity;
import com.xiliulou.electricity.entity.car.CarDataResultVO;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.car.CarDataQueryEnum;
import com.xiliulou.electricity.mapper.ElectricityBatteryMapper;
import com.xiliulou.electricity.mapper.ElectricityCarMapper;
import com.xiliulou.electricity.query.car.CarDataConditionReq;
import com.xiliulou.electricity.query.car.CarDataQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarDataService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.Jt808DeviceInfoVo;
import com.xiliulou.electricity.vo.car.CarDataVO;
import com.xiliulou.electricity.vo.car.PageDataResult;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class CarDataServiceImpl implements CarDataService {

    @Autowired
    private UserDataScopeService userDataScopeService;
    @Autowired
    private TenantService tenantService;
    @Autowired
    private Jt808CarService jt808CarService;
    @Resource
    private ElectricityCarMapper electricityCarMapper;
    @Resource
    private ElectricityBatteryMapper electricityBatteryMapper;
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    StoreService storeService;

    @Autowired
    private CarRentalPackageService carRentalPackageService;

    @Override
    public PageDataResult queryAllCarDataPage(CarDataConditionReq carDataConditionReq) {
        if (checkPageAndDataType(carDataConditionReq)) {
            return new PageDataResult();
        }
        // 统一获取租户ID
        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("TENANT ERROR! tenantEntity not exists! id={}", tenantId);
            return new PageDataResult();
        }
        carDataConditionReq.setTenantId(tenantId);
        // 方法路由，根据queryType获取不同结果
        if (Objects.equals(carDataConditionReq.getQueryType(), CarDataQueryEnum.ALL.getCode())) {
            return queryAllCarData(carDataConditionReq);
        } else if (Objects.equals(carDataConditionReq.getQueryType(), CarDataQueryEnum.RENT.getCode())) {
            return queryRentCarData(carDataConditionReq);
        } else if (Objects.equals(carDataConditionReq.getQueryType(), CarDataQueryEnum.NOT_RENT.getCode())) {
            return queryNotRentCarData(carDataConditionReq);
        } else if (Objects.equals(carDataConditionReq.getQueryType(), CarDataQueryEnum.OVERDUE.getCode())) {
            return queryOverdueCarData(carDataConditionReq);
        } else if (Objects.equals(carDataConditionReq.getQueryType(), CarDataQueryEnum.OFFLINE.getCode())) {
            return queryOfflineCarData(carDataConditionReq);
        }
        return new PageDataResult();
    }

    @Override
    public R queryAllCarDataPage(Long offset, Long size, Long franchiseeId, Long storeId, Integer modelId, String sn, String userName, String phone,Long uid) {
        if (size < 0 || size > 50) {
            size = Long.valueOf(10);
        }

        if (offset < 0) {
            offset = Long.valueOf(0);
        }
        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("TENANT ERROR! tenantEntity not exists! id={}", tenantId);
            return R.ok(Collections.EMPTY_LIST);
        }
        CarDataQuery build = CarDataQuery.builder().tenantId(tenantId).sn(sn).franchiseeId(franchiseeId).storeId(storeId).modelId(modelId).userName(userName).phone(phone).uid(uid).build();
        List<com.xiliulou.electricity.entity.car.CarDataVO> carDataVOS = electricityCarMapper.queryCarPageByCondition(build, offset, size);
        if(CollectionUtils.isEmpty(carDataVOS)){
            return R.ok(new ArrayList<CarDataResultVO>());
        }
        ArrayList  resultList=new ArrayList<CarDataResultVO>();
        carDataVOS.stream().forEachOrdered(item->{
            if (Objects.nonNull(item)){
                CarDataResultVO vo= new CarDataResultVO();
                Jt808DeviceInfoVo jt808DeviceInfoVo=new Jt808DeviceInfoVo();
                if (Objects.nonNull(item.getFranchiseeId())){
                    Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
                    if (Objects.nonNull(franchisee)) {
                        item.setFranchiseeName(franchisee.getName());
                    }
                }
                if (Objects.nonNull(item.getStoreId())){
                    Store store = storeService.queryByIdFromCache(item.getStoreId());
                    if(Objects.nonNull(store)) {
                        item.setStoreName(store.getName());
                    }
                }
                if (Objects.nonNull(item.getRentalPackageId())){
                    CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(item.getRentalPackageId());
                    if(Objects.nonNull(carRentalPackagePO)){
                        item.setRentalPackageName(carRentalPackagePO.getName());
                    }
                }
                if  (Objects.nonNull(item.getSn())){
                    // 获取设备信息
                    Pair<Boolean, Object> carDevice = jt808CarService.queryDeviceInfo(item.getSn());
                    if (carDevice.getLeft()) {
                        if(Objects.nonNull(carDevice.getRight())){
                            jt808DeviceInfoVo = (Jt808DeviceInfoVo) carDevice.getRight();
                        }
                    }
                }
                if (Objects.isNull(jt808DeviceInfoVo)){
                    jt808DeviceInfoVo=new Jt808DeviceInfoVo();
                }
                vo.setJt808DeviceInfoVo(jt808DeviceInfoVo);
                vo.setCarDataVO(item);
                resultList.add(vo);
            }
        });
        return R.ok(resultList);
    }

    @Override
    public R queryAllCarDataCount(Long franchiseeId, Long storeId, Integer modelId, String sn, String userName, String phone,Long uid) {
        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("TENANT ERROR! tenantEntity not exists! id={}", tenantId);
            return R.ok(Integer.valueOf(0));
        }
        CarDataQuery build = CarDataQuery.builder().tenantId(tenantId).sn(sn).franchiseeId(franchiseeId).storeId(storeId).modelId(modelId).userName(userName).phone(phone).uid(uid).build();
        return R.ok(electricityCarMapper.queryCarDataCountByCondition(build));

    }

    @Override
    public R queryPendingRentalCarDataPage(Long offset, Long size, Long franchiseeId, Long storeId, Integer modelId, String sn, String userName, String phone,Long uid) {
        if (size < 0 || size > 50) {
            size = Long.valueOf(10);
        }

        if (offset < 0) {
            offset = Long.valueOf(0);
        }
        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("TENANT ERROR! tenantEntity not exists! id={}", tenantId);
            return R.ok(Collections.EMPTY_LIST);
        }
        CarDataQuery build = CarDataQuery.builder().tenantId(tenantId).sn(sn).franchiseeId(franchiseeId).storeId(storeId).modelId(modelId).userName(userName).phone(phone).uid(uid).status(ElectricityCar.STATUS_NOT_RENT).build();
        List<com.xiliulou.electricity.entity.car.CarDataVO> carDataVOS = electricityCarMapper.queryCarPageByCondition(build, offset, size);
        if(CollectionUtils.isEmpty(carDataVOS)){
            return R.ok(new ArrayList<CarDataResultVO>());
        }
        ArrayList  resultList=new ArrayList<CarDataResultVO>();
        carDataVOS.parallelStream().forEachOrdered(item->{
            if (Objects.nonNull(item)){
                CarDataResultVO vo= new CarDataResultVO();
                Jt808DeviceInfoVo jt808DeviceInfoVo=new Jt808DeviceInfoVo();
                if (Objects.nonNull(item.getFranchiseeId())){
                    Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
                    if (Objects.nonNull(franchisee)) {
                        item.setFranchiseeName(franchisee.getName());
                    }
                }
                if (Objects.nonNull(item.getStoreId())){
                    Store store = storeService.queryByIdFromCache(item.getStoreId());
                    if(Objects.nonNull(store)) {
                        item.setStoreName(store.getName());
                    }
                }
                if (Objects.nonNull(item.getRentalPackageId())){
                    CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(item.getRentalPackageId());
                    if(Objects.nonNull(carRentalPackagePO)){
                        item.setRentalPackageName(carRentalPackagePO.getName());
                    }
                }
                if  (Objects.nonNull(item.getSn())){
                    // 获取设备信息
                    Pair<Boolean, Object> carDevice = jt808CarService.queryDeviceInfo(item.getSn());
                    if (carDevice.getLeft()) {
                        if(Objects.nonNull(carDevice.getRight())){
                            jt808DeviceInfoVo = (Jt808DeviceInfoVo) carDevice.getRight();
                        }
                    }
                }
                if (Objects.isNull(jt808DeviceInfoVo)){
                    jt808DeviceInfoVo=new Jt808DeviceInfoVo();
                }
                vo.setJt808DeviceInfoVo(jt808DeviceInfoVo);
                vo.setCarDataVO(item);
                resultList.add(vo);
            }
        });
        return R.ok(resultList);
    }

    @Override
    public R queryPendingRentalCarDataCount(Long franchiseeId, Long storeId, Integer modelId, String sn, String userName, String phone,Long uid) {
        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("TENANT ERROR! tenantEntity not exists! id={}", tenantId);
            return R.ok(Integer.valueOf(0));
        }
        CarDataQuery build = CarDataQuery.builder().tenantId(tenantId).sn(sn).franchiseeId(franchiseeId).storeId(storeId).modelId(modelId).userName(userName).phone(phone).uid(uid).status(ElectricityCar.STATUS_NOT_RENT).build();
        return R.ok(electricityCarMapper.queryCarDataCountByCondition(build));
    }

    @Override
    public R queryLeasedCarDataPage(Long offset, Long size, Long franchiseeId, Long storeId, Integer modelId, String sn, String userName, String phone,Long uid) {
        if (size < 0 || size > 50) {
            size = Long.valueOf(10);
        }

        if (offset < 0) {
            offset = Long.valueOf(0);
        }
        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("TENANT ERROR! tenantEntity not exists! id={}", tenantId);
            return R.ok(Collections.EMPTY_LIST);
        }
        CarDataQuery build = CarDataQuery.builder().tenantId(tenantId).sn(sn).franchiseeId(franchiseeId).storeId(storeId).modelId(modelId).userName(userName).phone(phone).uid(uid).status(ElectricityCar.STATUS_IS_RENT).build();
        List<com.xiliulou.electricity.entity.car.CarDataVO> carDataVOS = electricityCarMapper.queryCarPageByCondition(build, offset, size);
        if(CollectionUtils.isEmpty(carDataVOS)){
            return R.ok(new ArrayList<CarDataResultVO>());
        }
        ArrayList  resultList=new ArrayList<CarDataResultVO>();
        carDataVOS.parallelStream().forEachOrdered(item->{
            if (Objects.nonNull(item)){
                CarDataResultVO vo= new CarDataResultVO();
                Jt808DeviceInfoVo jt808DeviceInfoVo=new Jt808DeviceInfoVo();
                if (Objects.nonNull(item.getFranchiseeId())){
                    Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
                    if (Objects.nonNull(franchisee)) {
                        item.setFranchiseeName(franchisee.getName());
                    }
                }
                if (Objects.nonNull(item.getStoreId())){
                    Store store = storeService.queryByIdFromCache(item.getStoreId());
                    if(Objects.nonNull(store)) {
                        item.setStoreName(store.getName());
                    }
                }
                if (Objects.nonNull(item.getRentalPackageId())){
                    CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(item.getRentalPackageId());
                    if(Objects.nonNull(carRentalPackagePO)){
                        item.setRentalPackageName(carRentalPackagePO.getName());
                    }
                }
                if  (Objects.nonNull(item.getSn())){
                    // 获取设备信息
                    Pair<Boolean, Object> carDevice = jt808CarService.queryDeviceInfo(item.getSn());
                    if (carDevice.getLeft()) {
                        if(Objects.nonNull(carDevice.getRight())){
                            jt808DeviceInfoVo = (Jt808DeviceInfoVo) carDevice.getRight();
                        }

                    }
                }
                if (Objects.isNull(jt808DeviceInfoVo)){
                    jt808DeviceInfoVo=new Jt808DeviceInfoVo();
                }
                vo.setJt808DeviceInfoVo(jt808DeviceInfoVo);
                vo.setCarDataVO(item);
                resultList.add(vo);
            }
        });
        return R.ok(resultList);
    }

    @Override
    public R queryLeasedCarDataCount(Long franchiseeId, Long storeId, Integer modelId, String sn, String userName, String phone,Long uid) {
        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("TENANT ERROR! tenantEntity not exists! id={}", tenantId);
            return R.ok(Integer.valueOf(0));
        }
        CarDataQuery build = CarDataQuery.builder().tenantId(tenantId).sn(sn).franchiseeId(franchiseeId).storeId(storeId).modelId(modelId).userName(userName).phone(phone).uid(uid).status(ElectricityCar.STATUS_IS_RENT).build();
        return R.ok(electricityCarMapper.queryCarDataCountByCondition(build));

    }

    @Override
    public R queryOverdueCarDataPage(Long offset, Long size, Long franchiseeId, Long storeId, Integer modelId, String sn, String userName, String phone,Long uid) {
        if (size < 0 || size > 50) {
            size = Long.valueOf(10);
        }

        if (offset < 0) {
            offset = Long.valueOf(0);
        }
        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("TENANT ERROR! tenantEntity not exists! id={}", tenantId);
            return R.ok(Collections.EMPTY_LIST);
        }
        CarDataQuery build = CarDataQuery.builder().tenantId(tenantId).sn(sn).franchiseeId(franchiseeId).storeId(storeId).modelId(modelId).userName(userName).phone(phone).uid(uid).status(ElectricityCar.STATUS_IS_RENT).overdueTime(System.currentTimeMillis()).build();
        List<com.xiliulou.electricity.entity.car.CarDataVO> carDataVOS = electricityCarMapper.queryCarPageByCondition(build, offset, size);

        if(CollectionUtils.isEmpty(carDataVOS)){
            return R.ok(new ArrayList<CarDataResultVO>());
        }
        ArrayList  resultList=new ArrayList<CarDataResultVO>();
        carDataVOS.parallelStream().forEachOrdered(item->{
            if (Objects.nonNull(item)){
                CarDataResultVO vo= new CarDataResultVO();
                Jt808DeviceInfoVo jt808DeviceInfoVo=new Jt808DeviceInfoVo();
                if (Objects.nonNull(item.getFranchiseeId())){
                    Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
                    if (Objects.nonNull(franchisee)) {
                        item.setFranchiseeName(franchisee.getName());
                    }
                }
                if (Objects.nonNull(item.getStoreId())){
                    Store store = storeService.queryByIdFromCache(item.getStoreId());
                    if(Objects.nonNull(store)) {
                        item.setStoreName(store.getName());
                    }
                }
                if (Objects.nonNull(item.getRentalPackageId())){
                    CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(item.getRentalPackageId());
                    if(Objects.nonNull(carRentalPackagePO)){
                        item.setRentalPackageName(carRentalPackagePO.getName());
                    }
                }
                if  (Objects.nonNull(item.getSn())){
                    // 获取设备信息
                    Pair<Boolean, Object> carDevice = jt808CarService.queryDeviceInfo(item.getSn());
                    if (carDevice.getLeft()) {
                        if(Objects.nonNull(carDevice.getRight())){
                            jt808DeviceInfoVo = (Jt808DeviceInfoVo) carDevice.getRight();
                        }
                    }
                }
                if (Objects.isNull(jt808DeviceInfoVo)){
                    jt808DeviceInfoVo=new Jt808DeviceInfoVo();
                }
                vo.setJt808DeviceInfoVo(jt808DeviceInfoVo);
                vo.setCarDataVO(item);
                resultList.add(vo);
            }
        });
        return R.ok(resultList);
    }

    @Override
    public R queryOverdueCarDataCount(Long franchiseeId, Long storeId, Integer modelId, String sn, String userName, String phone,Long uid) {

        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("TENANT ERROR! tenantEntity not exists! id={}", tenantId);
            return R.ok(Integer.valueOf(0));
        }
        CarDataQuery build = CarDataQuery.builder().tenantId(tenantId).sn(sn).franchiseeId(franchiseeId).storeId(storeId).modelId(modelId).userName(userName).phone(phone).uid(uid).status(ElectricityCar.STATUS_IS_RENT).overdueTime(System.currentTimeMillis()).build();
        return R.ok(electricityCarMapper.queryCarDataCountByCondition(build));

    }

    /**
     * 查询所有的车辆数据
     */
    @Slave
    private PageDataResult queryAllCarData(CarDataConditionReq carDataConditionReq) {
        // 查库的信息
        List<CarDataEntity> carDataEntityList = electricityCarMapper.queryAllCarData(carDataConditionReq);
        Integer count = electricityCarMapper.queryAllCarDataCount(carDataConditionReq);
        List<CarDataVO> carDataVOList = getCarInfoByJt808(carDataEntityList);
        carDataVOList = queryBatteryByUserId(carDataVOList);
        return PageDataResult.result(count, carDataConditionReq.getSize(), carDataConditionReq.getOffset(), carDataVOList);
    }

    /**
     * 查询已租的车辆数据
     *
     * @param carDataConditionReq
     * @return
     */
    @Slave
    private PageDataResult queryRentCarData(CarDataConditionReq carDataConditionReq) {
        // 查库的信息
        List<CarDataEntity> carDataEntityList = electricityCarMapper.queryRentCarData(carDataConditionReq);
        Integer count = electricityCarMapper.queryRentCarDataCount(carDataConditionReq);
        List<CarDataVO> carDataVOList = getCarInfoByJt808(carDataEntityList);
        carDataVOList = queryBatteryByUserId(carDataVOList);
        return PageDataResult.result(count, carDataConditionReq.getSize(), carDataConditionReq.getOffset(), carDataVOList);
    }

    /**
     * 查询未租的车辆数据
     *
     * @param carDataConditionReq
     * @return
     */
    @Slave
    private PageDataResult queryNotRentCarData(CarDataConditionReq carDataConditionReq) {
        // 查库的信息
        List<CarDataEntity> carDataEntityList = electricityCarMapper.queryNotRentCarData(carDataConditionReq);
        Integer count = electricityCarMapper.queryNotRentCarDataCount(carDataConditionReq);
        List<CarDataVO> carDataVOList = getCarInfoByJt808(carDataEntityList);
        carDataVOList = queryBatteryByUserId(carDataVOList);
        return PageDataResult.result(count, carDataConditionReq.getSize(), carDataConditionReq.getOffset(), carDataVOList);
    }

    /**
     * 查询逾期的车辆数据
     *
     * @param carDataConditionReq
     * @return
     */
    @Slave
    private PageDataResult queryOverdueCarData(CarDataConditionReq carDataConditionReq) {
        // 查库的信息
        List<CarDataEntity> carDataEntityList = electricityCarMapper.queryOverdueCarData(carDataConditionReq);
        Integer count = electricityCarMapper.queryOverdueCarDataCount(carDataConditionReq);
        List<CarDataVO> carDataVOList = getCarInfoByJt808(carDataEntityList);
        carDataVOList = queryBatteryByUserId(carDataVOList);
        return PageDataResult.result(count, carDataConditionReq.getSize(), carDataConditionReq.getOffset(), carDataVOList);
    }

    /**
     * 查询下线的车辆数据
     *
     * @param carDataConditionReq
     * @return
     */
    @Slave
    private PageDataResult queryOfflineCarData(CarDataConditionReq carDataConditionReq) {
        // 查库的信息
        List<CarDataEntity> carDataEntityList = electricityCarMapper.queryOfflineCarData(carDataConditionReq);
        Integer count = electricityCarMapper.queryOfflineCarDataCount(carDataConditionReq);
        List<CarDataVO> carDataVOList = CarDataVO.carDataEntityListToCarDataVOList(carDataEntityList);
        carDataVOList = queryBatteryByUserId(carDataVOList);
        return PageDataResult.result(count, carDataConditionReq.getSize(), carDataConditionReq.getOffset(), carDataVOList);
    }

    /**
     * 通过jt808查询位置信息
     *
     * @param carDataEntityList
     * @return
     */
    private List<CarDataVO> getCarInfoByJt808(List<CarDataEntity> carDataEntityList) {
        if (CollectionUtils.isEmpty(carDataEntityList)) {
            return Lists.newArrayList();
        }
        List<CarDataVO> carDataVOList = CarDataVO.carDataEntityListToCarDataVOList(carDataEntityList);
        for (CarDataVO carDataVO : carDataVOList) {
            // 获取设备信息
            Pair<Boolean, Object> carDevice = jt808CarService.queryDeviceInfo(carDataVO.getCarSn());
            if (carDevice.getLeft()) {
                Jt808DeviceInfoVo jt808DeviceInfoVo = (Jt808DeviceInfoVo) carDevice.getRight();
                carDataVO.setLatitude(jt808DeviceInfoVo.getLatitude());
                carDataVO.setLongitude(jt808DeviceInfoVo.getLongitude());
            }
        }
        return carDataVOList;
    }

    private List<CarDataVO> queryBatteryByUserId(List<CarDataVO> carDataVOList) {
        if (CollectionUtils.isEmpty(carDataVOList)) {
            return Lists.newArrayList();
        }
        for (CarDataVO carDataVO : carDataVOList) {
            ElectricityBattery electricityBattery = electricityBatteryMapper.queryByUid(carDataVO.getUid());
            carDataVO.setBatterySn(electricityBattery.getSn());
            carDataVO.setPower(electricityBattery.getPower());
            carDataVO.setVoltage(electricityBattery.getVoltage());
        }
        return carDataVOList;
    }

    //校验页码和数据类型
    private Boolean checkPageAndDataType(CarDataConditionReq carDataConditionReq) {
        if (carDataConditionReq.getOffset() < 0) {
            carDataConditionReq.setOffset(1);
        }
        if (carDataConditionReq.getSize() > 50 || carDataConditionReq.getSize() < 0) {
            carDataConditionReq.setSize(10);
        }
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
        }
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return true;
            }
        }
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return true;
        }
        return false;
    }
}
