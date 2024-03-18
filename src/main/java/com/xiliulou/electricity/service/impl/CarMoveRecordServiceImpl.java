package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.CarMoveRecordMapper;
import com.xiliulou.electricity.query.CarMoveRecordQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.asset.AssertPermissionService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.CarMoveRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author: Kenneth
 * @Date: 2023/7/15 14:56
 * @Description:
 */

@Service
@Slf4j
public class CarMoveRecordServiceImpl implements CarMoveRecordService {
    
    @Autowired
    private CarMoveRecordMapper carMoveRecordMapper;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    StoreService storeService;
    
    @Autowired
    FranchiseeService franchiseeService;
    
    @Autowired
    ElectricityCarModelService electricityCarModelService;
    
    @Resource
    private AssertPermissionService assertPermissionService;
    
    /**
     * 获取车辆转移记录信息
     *
     * @param carMoveRecordQuery
     * @return
     */
    @Slave
    @Override
    public List<CarMoveRecordVO> queryCarMoveRecords(CarMoveRecordQuery carMoveRecordQuery) {
        Triple<List<Long>, List<Long>, Boolean> triple = assertPermissionService.assertPermissionByTriple(SecurityUtils.getUserInfo());
        if (!triple.getRight()) {
            return Collections.EMPTY_LIST;
        }
        carMoveRecordQuery.setFranchiseeIds(triple.getLeft());
        carMoveRecordQuery.setStoreIds(triple.getMiddle());
        
        
        List<CarMoveRecord> carMoveRecordList = carMoveRecordMapper.selectByPage(carMoveRecordQuery);
        
        List<CarMoveRecordVO> carMoveRecordVOList = new ArrayList<>();
        for (CarMoveRecord carMoveRecord : carMoveRecordList) {
            CarMoveRecordVO carMoveRecordVO = new CarMoveRecordVO();
            BeanUtil.copyProperties(carMoveRecord, carMoveRecordVO);
            
            Store oldStore = storeService.queryByIdFromCache(carMoveRecord.getOldStoreId());
            Store newStore = storeService.queryByIdFromCache(carMoveRecord.getNewStoreId());
            carMoveRecordVO.setOldStoreName(oldStore.getName());
            carMoveRecordVO.setNewStoreName(newStore.getName());
            
            Franchisee oldFranchisee = franchiseeService.queryByIdFromCache(carMoveRecord.getOldFranchiseeId());
            Franchisee newFranchisee = franchiseeService.queryByIdFromCache(carMoveRecord.getNewFranchiseeId());
            carMoveRecordVO.setOldFranchiseeName(oldFranchisee.getName());
            carMoveRecordVO.setNewFranchiseeName(newFranchisee.getName());
            
            User user = userService.queryByUidFromCache(carMoveRecord.getOperator());
            carMoveRecordVO.setOperatorName(user.getName());
            
            ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(carMoveRecord.getCarModelId().intValue());
            if (Objects.nonNull(electricityCarModel)) {
                carMoveRecordVO.setCarModelName(electricityCarModel.getName());
            }
            carMoveRecordVOList.add(carMoveRecordVO);
            
        }
        
        return carMoveRecordVOList;
    }
    
    @Slave
    @Override
    public Integer queryCarMoveRecordsCount(CarMoveRecordQuery carMoveRecordQuery) {
        Triple<List<Long>, List<Long>, Boolean> triple = assertPermissionService.assertPermissionByTriple(SecurityUtils.getUserInfo());
        if (!triple.getRight()) {
            return NumberConstant.ZERO;
        }
        carMoveRecordQuery.setFranchiseeIds(triple.getLeft());
        carMoveRecordQuery.setStoreIds(triple.getMiddle());
        
        return carMoveRecordMapper.selectCount(carMoveRecordQuery);
    }
    
    
}
