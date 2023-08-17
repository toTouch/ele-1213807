package com.xiliulou.electricity.service.impl.car;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.car.CarDataEntity;
import com.xiliulou.electricity.enums.car.CarDataQueryEnum;
import com.xiliulou.electricity.mapper.ElectricityCarMapper;
import com.xiliulou.electricity.query.car.CarDataConditionReq;
import com.xiliulou.electricity.service.Jt808CarService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.car.CarDataService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.Jt808DeviceInfoVo;
import com.xiliulou.electricity.vo.car.CarDataResult;
import com.xiliulou.electricity.vo.car.CarDataVO;
import com.xiliulou.electricity.vo.car.CarLocation;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    @Override
    public CarDataResult queryAllCarDataPage(CarDataConditionReq carDataConditionReq) {
        if(checkPageAndDataType(carDataConditionReq)){
            return new CarDataResult();
        }
        // 统一获取租户ID
        Integer tenantId = TenantContextHolder.getTenantId();
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        if (Objects.isNull(tenant)) {
            log.error("TENANT ERROR! tenantEntity not exists! id={}", tenantId);
            return new CarDataResult();
        }
        carDataConditionReq.setTenantId(tenantId);
        // 方法路由，根据queryType获取不同结果
        if(Objects.equals(carDataConditionReq.getQueryType(),CarDataQueryEnum.ALL.getCode())){
            return queryAllCarData(carDataConditionReq);
        }else if(Objects.equals(carDataConditionReq.getQueryType(),CarDataQueryEnum.RENT.getCode())){
            return queryRentCarData(carDataConditionReq);
        }else if(Objects.equals(carDataConditionReq.getQueryType(),CarDataQueryEnum.NOT_RENT.getCode())){
            return queryNotRentCarData(carDataConditionReq);
        }else if(Objects.equals(carDataConditionReq.getQueryType(),CarDataQueryEnum.OVERDUE.getCode())){
            return queryOverdueCarData(carDataConditionReq);
        }
        return new CarDataResult();
    }
    @Override
    @Slave
    public Integer queryAllCarDataCount(CarDataConditionReq carDataConditionReq) {
        return null;
    }
    /**
     * 查询所有的车辆数据
     */
    @Slave
    private CarDataResult queryAllCarData(CarDataConditionReq carDataConditionReq){
        // 查库的信息
        List<CarDataEntity> carDataEntityList = electricityCarMapper.queryAllCarData(carDataConditionReq);
        Integer count = electricityCarMapper.queryAllCarDataCount(carDataConditionReq);
        List<CarDataVO> carDataVOList = getCarInfoByJt808(carDataEntityList);
        return CarDataResult.result(count,carDataConditionReq.getSize(),carDataConditionReq.getOffset(),carDataVOList);
    }

    /**
     * 查询已租的车辆数据
     * @param carDataConditionReq
     * @return
     */
    @Slave
    private CarDataResult queryRentCarData(CarDataConditionReq carDataConditionReq){
        // 查库的信息
        List<CarDataEntity> carDataEntityList = electricityCarMapper.queryRentCarData(carDataConditionReq);
        Integer count = electricityCarMapper.queryRentCarDataCount(carDataConditionReq);
        List<CarDataVO> carDataVOList = getCarInfoByJt808(carDataEntityList);
        return CarDataResult.result(count,carDataConditionReq.getSize(),carDataConditionReq.getOffset(),carDataVOList);
    }

    /**
     * 查询未租的车辆数据
     * @param carDataConditionReq
     * @return
     */
    @Slave
    private CarDataResult queryNotRentCarData(CarDataConditionReq carDataConditionReq){
        // 查库的信息
        List<CarDataEntity> carDataEntityList = electricityCarMapper.queryNotRentCarData(carDataConditionReq);
        Integer count = electricityCarMapper.queryNotRentCarDataCount(carDataConditionReq);
        List<CarDataVO> carDataVOList = getCarInfoByJt808(carDataEntityList);
        return CarDataResult.result(count,carDataConditionReq.getSize(),carDataConditionReq.getOffset(),carDataVOList);
    }

    /**
     * 查询逾期的车辆数据
     * @param carDataConditionReq
     * @return
     */
    @Slave
    private CarDataResult queryOverdueCarData(CarDataConditionReq carDataConditionReq){
        // 查库的信息
        List<CarDataEntity> carDataEntityList = electricityCarMapper.queryOverdueCarData(carDataConditionReq);
        Integer count = electricityCarMapper.queryOverdueCarDataCount(carDataConditionReq);
        List<CarDataVO> carDataVOList = getCarInfoByJt808(carDataEntityList);
        return CarDataResult.result(count,carDataConditionReq.getSize(),carDataConditionReq.getOffset(),carDataVOList);
    }


    /**
     * 通过jt808查询位置信息
     * @param carDataEntityList
     * @return
     */
    private List<CarDataVO> getCarInfoByJt808(List<CarDataEntity> carDataEntityList){
        List<CarDataVO> carDataVOList = CarDataVO.carDataEntityListToCarDataVOList(carDataEntityList);
        for(CarDataVO carDataVO : carDataVOList){
            // 获取设备信息
            Pair<Boolean, Object> carDevice  = jt808CarService.queryDeviceInfo(carDataVO.getCarSn());
            if(carDevice.getLeft()){
                Jt808DeviceInfoVo jt808DeviceInfoVo = (Jt808DeviceInfoVo) carDevice.getRight();
                carDataVO.setLatitude(jt808DeviceInfoVo.getLatitude());
                carDataVO.setLongitude(jt808DeviceInfoVo.getLongitude());
            }
        }
        return carDataVOList;
    }

    //校验页码和数据类型
    private Boolean checkPageAndDataType(CarDataConditionReq carDataConditionReq){
        if(carDataConditionReq.getOffset() < 0){
            carDataConditionReq.setOffset(1);
        }
        if(carDataConditionReq.getSize() > 50 || carDataConditionReq.getSize() < 0){
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
