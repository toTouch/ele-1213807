package com.xiliulou.electricity.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.api.client.util.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.dto.OrderForBatteryDTO;
import com.xiliulou.electricity.dto.bms.BatteryInfoDto;
import com.xiliulou.electricity.entity.BatteryModel;
import com.xiliulou.electricity.entity.BatteryOtherProperties;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.ElectricityBatteryMapper;
import com.xiliulou.electricity.query.ElectricityBatteryDataQuery;
import com.xiliulou.electricity.service.BatteryModelService;
import com.xiliulou.electricity.service.BatteryOtherPropertiesService;
import com.xiliulou.electricity.service.ElectricityBatteryDataService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.retrofit.BatteryPlatRetrofitService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.AESUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityBatteryDataVO;
import com.xiliulou.electricity.vo.api.EleBatteryDataVO;
import com.xiliulou.electricity.web.query.battery.BatteryInfoQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ElectricityBatteryDataServiceImpl extends ServiceImpl<ElectricityBatteryMapper, ElectricityBattery> implements ElectricityBatteryDataService {
    
    @Resource
    private ElectricityBatteryMapper electricitybatterymapper;
    
    @Autowired
    UserDataScopeService userDataScopeService;
    
    @Autowired
    TenantService tenantService;
    
    @Autowired
    BatteryPlatRetrofitService batteryPlatRetrofitService;
    
    @Autowired
    FranchiseeService franchiseeService;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    BatteryModelService batteryModelService;
    
    @Autowired
    BatteryOtherPropertiesService batteryOtherPropertiesService;
    
    @Qualifier("redisService")
    @Autowired
    private RedisService redisService;
    
    @Override
    @Slave
    public R selectAllBatteryPageData(ElectricityBatteryDataQuery electricityBatteryQuery) {
        
        List<ElectricityBatteryDataVO> electricityBatteries = electricitybatterymapper.queryBatteryList(electricityBatteryQuery, electricityBatteryQuery.getOffset(),
                electricityBatteryQuery.getSize());
        
        if (CollectionUtils.isEmpty(electricityBatteries)) {
            return R.ok(new ArrayList<EleBatteryDataVO>());
        }
        
        //获取sn列表
        List<String> snList = electricityBatteries.parallelStream().map(ElectricityBatteryDataVO::getSn).collect(Collectors.toList());
        List<BatteryOtherProperties> otherPropertiesList = null;
        Map<String, Double> otherPropertiesMap = null;
        if (CollectionUtils.isNotEmpty(snList)) {
            //根据获取的sn列表查询
            otherPropertiesList = batteryOtherPropertiesService.listBatteryOtherPropertiesBySn(snList);
            if (CollectionUtils.isNotEmpty(otherPropertiesList)) {
                otherPropertiesMap = otherPropertiesList.stream()
                        .collect(Collectors.toMap(BatteryOtherProperties::getBatteryName, BatteryOtherProperties::getBatteryV, (value1, value2) -> value1));
            }
        }
        
        Map<String, Double> finalOtherPropertiesMap = otherPropertiesMap;
        
        //获取电池型号
        Map<String, BatteryModel> batteryModelMap = null;
        List<BatteryModel> batteryModels = batteryModelService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (CollectionUtils.isNotEmpty(batteryModels)) {
            batteryModelMap = batteryModels.stream().collect(Collectors.toMap(BatteryModel::getBatteryType, Function.identity(), (item1, item2) -> item2));
        }
        Map<String, BatteryModel> finalBatteryModelMap = batteryModelMap;
        
        electricityBatteries.parallelStream().forEach(item -> {
            //设置电压
            if (Objects.nonNull(finalOtherPropertiesMap) && finalOtherPropertiesMap.containsKey(item.getSn())) {
                Double boxVoltage = finalOtherPropertiesMap.get(item.getSn());
                if (Objects.nonNull(boxVoltage)) {
                    item.setBoxVoltage(boxVoltage.intValue());
                }
            }
            
            //设置异常交换用户
            Long guessUid = item.getGuessUid();
            if (Objects.nonNull(guessUid)) {
                UserInfo guessUserInfo = userInfoService.queryByUidFromCache(guessUid);
                if (Objects.nonNull(guessUserInfo)) {
                    item.setGuessUserName(guessUserInfo.getName());
                    item.setGuessUserPhone(guessUserInfo.getPhone());
                }
            }
            
            Long userId = item.getUid();
            Long fid = item.getFranchiseeId();
            if (Objects.nonNull(userId)) {
                UserInfo userInfo = userInfoService.queryByUidFromCache(userId);
                if (Objects.nonNull(userInfo)) {
                    item.setName(userInfo.getName());
                    item.setUserName(userInfo.getUserName());
                    item.setPhone(userInfo.getPhone());
                }
            }
            if (Objects.nonNull(fid)) {
                Franchisee franchisee = franchiseeService.queryByIdFromCache(fid);
                if (Objects.nonNull(franchisee)) {
                    item.setFranchiseeName(franchisee.getName());
                }
            }
            
//            String batteryShortType = batteryModelService.acquireBatteryShortType(item.getModel(), electricityBatteryQuery.getTenantId());
//            if (StringUtils.isNotEmpty(batteryShortType)) {
//                item.setModel(batteryShortType);
//            }
    
            if (CollectionUtils.isNotEmpty(Collections.singleton(finalBatteryModelMap)) && finalBatteryModelMap.containsKey(item.getModel())) {
                BatteryModel batteryModel = finalBatteryModelMap.get(item.getModel());
                if (Objects.nonNull(batteryModel)) {
                    item.setModel(batteryModel.getBatteryVShort());
                }
        
                if (Objects.nonNull(batteryModel) && Objects.nonNull(batteryModel.getCapacity())) {
                    item.setDbCapacity(batteryModel.getCapacity());
                }
            }
        });
        return R.ok(queryDataFromBMS(electricityBatteries, electricityBatteryQuery.getTenant()));
    }
    
    @Override
    public R selectAllBatteryDataCount(ElectricityBatteryDataQuery electricityBatteryQuery) {
        
        return R.ok(electricitybatterymapper.queryBatteryCount(electricityBatteryQuery));
    }
    
    @Override
    public R updateGuessUserInfo(Long id) {
        return R.ok(electricitybatterymapper.updateGuessUidById(id));
    }
    
    @Override
    @Slave
    public R selectInCabinetBatteryPageData(ElectricityBatteryDataQuery electricityBatteryQuery) {
        
        List<ElectricityBatteryDataVO> electricityBatteries = electricitybatterymapper.queryBatteryList(electricityBatteryQuery, electricityBatteryQuery.getOffset(),
                electricityBatteryQuery.getSize());
        if (CollectionUtils.isEmpty(electricityBatteries)) {
            return R.ok(new ArrayList<EleBatteryDataVO>());
        }
        electricityBatteries.parallelStream().forEach(item -> {
            Long userId = item.getUid();
            Long fId = item.getFranchiseeId();
            if (Objects.nonNull(userId)) {
                UserInfo userInfo = userInfoService.queryByUidFromCache(userId);
                if (Objects.nonNull(userInfo)) {
                    item.setName(userInfo.getName());
                    item.setUserName(userInfo.getUserName());
                    item.setPhone(userInfo.getPhone());
                }
            }
            if (Objects.nonNull(fId)) {
                Franchisee franchisee = franchiseeService.queryByIdFromCache(fId);
                if (Objects.nonNull(franchisee)) {
                    item.setFranchiseeName(franchisee.getName());
                }
            }
            
            String batteryShortType = batteryModelService.acquireBatteryShortType(item.getModel(), electricityBatteryQuery.getTenantId());
            if (StringUtils.isNotEmpty(batteryShortType)) {
                item.setModel(batteryShortType);
            }
        });
        return R.ok(queryDataFromBMS(electricityBatteries, electricityBatteryQuery.getTenant()));
        
    }
    
    @Override
    @Slave
    public R selectInCabinetBatteryDataCount(ElectricityBatteryDataQuery electricityBatteryQuery) {
        
        return R.ok(electricitybatterymapper.queryBatteryCount(electricityBatteryQuery));
    }
    
    @Override
    @Slave
    public R selectPendingRentalBatteryPageData(ElectricityBatteryDataQuery electricityBatteryQuery) {
        
        List<ElectricityBatteryDataVO> electricityBatteries = electricitybatterymapper.queryBatteryList(electricityBatteryQuery, electricityBatteryQuery.getOffset(),
                electricityBatteryQuery.getSize());
        if (CollectionUtils.isEmpty(electricityBatteries)) {
            return R.ok(new ArrayList<EleBatteryDataVO>());
        }
        electricityBatteries.parallelStream().forEach(item -> {
            Long userId = item.getUid();
            Long fId = item.getFranchiseeId();
            if (Objects.nonNull(userId)) {
                UserInfo userInfo = userInfoService.queryByUidFromCache(userId);
                if (Objects.nonNull(userInfo)) {
                    item.setName(userInfo.getName());
                    item.setUserName(userInfo.getUserName());
                    item.setPhone(userInfo.getPhone());
                }
            }
            if (Objects.nonNull(fId)) {
                Franchisee franchisee = franchiseeService.queryByIdFromCache(fId);
                if (Objects.nonNull(franchisee)) {
                    item.setFranchiseeName(franchisee.getName());
                }
            }
            
            String batteryShortType = batteryModelService.acquireBatteryShortType(item.getModel(), electricityBatteryQuery.getTenantId());
            if (StringUtils.isNotEmpty(batteryShortType)) {
                item.setModel(batteryShortType);
            }
        });
        return R.ok(queryDataFromBMS(electricityBatteries, electricityBatteryQuery.getTenant()));
        
    }
    
    @Override
    @Slave
    public R selectPendingRentalBatteryDataCount(ElectricityBatteryDataQuery electricityBatteryQuery) {
        
        return R.ok(electricitybatterymapper.queryBatteryCount(electricityBatteryQuery));
    }
    
    @Override
    @Slave
    public R selectLeasedBatteryPageData(ElectricityBatteryDataQuery electricityBatteryQuery) {
        
        List<ElectricityBatteryDataVO> electricityBatteries = electricitybatterymapper.queryBatteryList(electricityBatteryQuery, electricityBatteryQuery.getOffset(),
                electricityBatteryQuery.getSize());
        if (CollectionUtils.isEmpty(electricityBatteries)) {
            return R.ok(new ArrayList<EleBatteryDataVO>());
        }
        electricityBatteries.parallelStream().forEach(item -> {
            Long userId = item.getUid();
            Long fId = item.getFranchiseeId();
            if (Objects.nonNull(userId)) {
                UserInfo userInfo = userInfoService.queryByUidFromCache(userId);
                if (Objects.nonNull(userInfo)) {
                    item.setName(userInfo.getName());
                    item.setUserName(userInfo.getUserName());
                    item.setPhone(userInfo.getPhone());
                }
            }
            if (Objects.nonNull(fId)) {
                Franchisee franchisee = franchiseeService.queryByIdFromCache(fId);
                if (Objects.nonNull(franchisee)) {
                    item.setFranchiseeName(franchisee.getName());
                }
            }
            
            String batteryShortType = batteryModelService.acquireBatteryShortType(item.getModel(), electricityBatteryQuery.getTenantId());
            if (StringUtils.isNotEmpty(batteryShortType)) {
                item.setModel(batteryShortType);
            }
        });
        return R.ok(queryDataFromBMS(electricityBatteries, electricityBatteryQuery.getTenant()));
        
    }
    
    @Override
    @Slave
    public R selectLeasedBatteryDataCount(ElectricityBatteryDataQuery electricityBatteryQuery) {
        
        return R.ok(electricitybatterymapper.queryBatteryCount(electricityBatteryQuery));
    }
    
    @Override
    @Slave
    public R selectStrayBatteryPageData(ElectricityBatteryDataQuery electricityBatteryQuery) {
        
        List<ElectricityBatteryDataVO> electricityBatteries = electricitybatterymapper.queryStrayBatteryList(electricityBatteryQuery, electricityBatteryQuery.getOffset(),
                electricityBatteryQuery.getSize());
        if (CollectionUtils.isEmpty(electricityBatteries)) {
            return R.ok(new ArrayList<EleBatteryDataVO>());
        }
        electricityBatteries.parallelStream().forEach(item -> {
            Long userId = item.getUid();
            Long fId = item.getFranchiseeId();
            if (Objects.nonNull(userId)) {
                UserInfo userInfo = userInfoService.queryByUidFromCache(userId);
                if (Objects.nonNull(userInfo)) {
                    item.setName(userInfo.getName());
                    item.setUserName(userInfo.getUserName());
                    item.setPhone(userInfo.getPhone());
                }
            }
            if (Objects.nonNull(fId)) {
                Franchisee franchisee = franchiseeService.queryByIdFromCache(fId);
                if (Objects.nonNull(franchisee)) {
                    item.setFranchiseeName(franchisee.getName());
                }
            }
            
            String batteryShortType = batteryModelService.acquireBatteryShortType(item.getModel(), electricityBatteryQuery.getTenantId());
            if (StringUtils.isNotEmpty(batteryShortType)) {
                item.setModel(batteryShortType);
            }
        });
        return R.ok(queryDataFromBMS(electricityBatteries, electricityBatteryQuery.getTenant()));
        
    }
    
    @Override
    @Slave
    public R selectStrayBatteryDataCount(ElectricityBatteryDataQuery electricityBatteryQuery) {
        
        return R.ok(electricitybatterymapper.queryStrayBatteryCount(electricityBatteryQuery));
    }
    
    @Override
    @Slave
    public R selectOverdueBatteryPageData(ElectricityBatteryDataQuery electricityBatteryQuery) {
        
        List<ElectricityBatteryDataVO> electricityBatteries = electricitybatterymapper.queryOverdueBatteryList(electricityBatteryQuery, electricityBatteryQuery.getOffset(),
                electricityBatteryQuery.getSize());
        if (CollectionUtils.isEmpty(electricityBatteries)) {
            return R.ok(new ArrayList<EleBatteryDataVO>());
        }
        electricityBatteries.parallelStream().forEach(item -> {
            Long userId = item.getUid();
            Long fId = item.getFranchiseeId();
            if (Objects.nonNull(userId)) {
                UserInfo userInfo = userInfoService.queryByUidFromCache(userId);
                if (Objects.nonNull(userInfo)) {
                    item.setName(userInfo.getName());
                    item.setUserName(userInfo.getUserName());
                    item.setPhone(userInfo.getPhone());
                }
            }
            if (Objects.nonNull(fId)) {
                Franchisee franchisee = franchiseeService.queryByIdFromCache(fId);
                if (Objects.nonNull(franchisee)) {
                    item.setFranchiseeName(franchisee.getName());
                }
            }
            
            String batteryShortType = batteryModelService.acquireBatteryShortType(item.getModel(), electricityBatteryQuery.getTenantId());
            if (StringUtils.isNotEmpty(batteryShortType)) {
                item.setModel(batteryShortType);
            }
        });
        return R.ok(queryDataFromBMS(electricityBatteries, electricityBatteryQuery.getTenant()));
        
    }
    
    @Override
    @Slave
    public R selectOverdueBatteryDataCount(ElectricityBatteryDataQuery electricityBatteryQuery) {
        
        return R.ok(electricitybatterymapper.queryOverdueBatteryCount(electricityBatteryQuery));
    }
    
    @Override
    @Slave
    public R selectOverdueCarBatteryPageData(ElectricityBatteryDataQuery electricityBatteryQuery) {
        
        List<ElectricityBatteryDataVO> electricityBatteries = electricitybatterymapper.queryOverdueCarBatteryList(electricityBatteryQuery, electricityBatteryQuery.getOffset(),
                electricityBatteryQuery.getSize());
        if (CollectionUtils.isEmpty(electricityBatteries)) {
            return R.ok(new ArrayList<EleBatteryDataVO>());
        }
        electricityBatteries.parallelStream().forEach(item -> {
            Long userId = item.getUid();
            Long fId = item.getFranchiseeId();
            if (Objects.nonNull(userId)) {
                UserInfo userInfo = userInfoService.queryByUidFromCache(userId);
                if (Objects.nonNull(userInfo)) {
                    item.setName(userInfo.getName());
                    item.setUserName(userInfo.getUserName());
                    item.setPhone(userInfo.getPhone());
                }
            }
            if (Objects.nonNull(fId)) {
                Franchisee franchisee = franchiseeService.queryByIdFromCache(fId);
                if (Objects.nonNull(franchisee)) {
                    item.setFranchiseeName(franchisee.getName());
                }
            }
        });
        return R.ok(queryDataFromBMS(electricityBatteries, electricityBatteryQuery.getTenant()));
        
    }
    
    @Override
    @Slave
    public R selectOverdueCarBatteryDataCount(ElectricityBatteryDataQuery electricityBatteryQuery) {
        
        return R.ok(electricitybatterymapper.queryOverdueCarBatteryCount(electricityBatteryQuery));
    }
    
    /**
     * 查询BMS
     *
     * @param batteryInfoQuery
     * @return
     */
    @Override
    public BatteryInfoDto callBatteryServiceQueryBatteryInfo(BatteryInfoQuery batteryInfoQuery, Tenant tenant) {
        try {
            
            Map<String, String> headers = new HashMap<>();
            String time = String.valueOf(System.currentTimeMillis());
            headers.put(CommonConstant.INNER_HEADER_APP, CommonConstant.APP_SAAS);
            headers.put(CommonConstant.INNER_HEADER_TIME, time);
            headers.put(CommonConstant.INNER_HEADER_INNER_TOKEN, AESUtils.encrypt(time, CommonConstant.APP_SAAS_AES_KEY));
            headers.put(CommonConstant.INNER_TENANT_ID, tenant.getCode());
            
            R<BatteryInfoDto> r = batteryPlatRetrofitService.queryBatteryInfo(headers, batteryInfoQuery);
            if (!r.isSuccess()) {
                log.error("CALL BATTERY ERROR! msg={},uid={}", r.getErrMsg(), SecurityUtils.getUid());
                return null;
            }
            
            return r.getData();
            
        } catch (Exception e) {
            log.error("BATTERYDATA QUERY BMS ERROR! ", e);
            return null;
        }
        
    }
    
    public List<EleBatteryDataVO> queryDataFromBMS(List<ElectricityBatteryDataVO> electricityBatteries, Tenant tenant) {
        try {
            
            if (CollectionUtils.isEmpty(electricityBatteries)) {
                return new ArrayList<EleBatteryDataVO>();
            }
            
            List<EleBatteryDataVO> eleBatteryDataVOS = new ArrayList<>(electricityBatteries.size());
            
            for (int i = 0; i < electricityBatteries.size(); i++) {
                EleBatteryDataVO vo = new EleBatteryDataVO();
                vo.setElectricityBatteryDataVO(electricityBatteries.get(i));
                eleBatteryDataVOS.add(vo);
            }
            
            eleBatteryDataVOS.parallelStream().forEach(item -> {
                ElectricityBatteryDataVO electricityBatteryDataVO = item.getElectricityBatteryDataVO();
                if (!Objects.isNull(electricityBatteryDataVO) && !Objects.isNull(electricityBatteryDataVO.getSn())) {
                    BatteryInfoQuery batteryInfoQuery = new BatteryInfoQuery();
                    batteryInfoQuery.setSn(electricityBatteryDataVO.getSn());
                    item.setBatteryInfoDto(callBatteryServiceQueryBatteryInfo(batteryInfoQuery, tenant));
                }
                
                // 租借状态电池，查询其修改为租借状态时，对应的订单
                if (Objects.equals(electricityBatteryDataVO.getBusinessStatus(), ElectricityBattery.BUSINESS_STATUS_LEASE)) {
                    OrderForBatteryDTO orderForBatteryDTO = redisService.getWithHash(
                            String.format(CacheConstant.ORDER_FOR_BATTERY_WITH_BUSINESS_STATUS_LEASE, electricityBatteryDataVO.getSn()), OrderForBatteryDTO.class);
                    item.setOrderForBatteryDTO(orderForBatteryDTO);
                }
            });
            
            return eleBatteryDataVOS;
            
        } catch (Exception e) {
            log.error("BATTERYDATA QUERY BMS ERROR! ", e);
            return new ArrayList<EleBatteryDataVO>();
        }
    }
    
    
    @Override
    @Slave
    public R queryStockBatteryPageData(ElectricityBatteryDataQuery electricityBatteryQuery) {
        
        List<ElectricityBatteryDataVO> electricityBatteries = electricitybatterymapper.queryStockBatteryList(electricityBatteryQuery, electricityBatteryQuery.getOffset(),
                electricityBatteryQuery.getSize());
        
        return R.ok(buildEleBatteryDataVOList(electricityBatteries, electricityBatteryQuery.getTenant()));
    }
    
    
    @Override
    @Slave
    public R queryStockBatteryPageDataCount(ElectricityBatteryDataQuery electricityBatteryQuery) {
        
        return R.ok(electricitybatterymapper.queryStockBatteryCount(electricityBatteryQuery));
    }
    
    
    // 组装EleBatteryDataVO
    private List<EleBatteryDataVO> buildEleBatteryDataVOList(List<ElectricityBatteryDataVO> electricityBatteries, Tenant tenant) {
        if (CollectionUtils.isEmpty(electricityBatteries)) {
            return Lists.newArrayList();
        }
        electricityBatteries.parallelStream().forEach(item -> {
            if (Objects.nonNull(item)) {
                Long uid = item.getUid();
                Long fId = item.getFranchiseeId();
                if (Objects.nonNull(uid)) {
                    UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
                    if (Objects.nonNull(userInfo)) {
                        item.setName(userInfo.getName());
                        item.setUserName(userInfo.getUserName());
                        item.setPhone(userInfo.getPhone());
                    }
                }
                if (Objects.nonNull(fId)) {
                    Franchisee franchisee = franchiseeService.queryByIdFromCache(fId);
                    if (Objects.nonNull(franchisee)) {
                        item.setFranchiseeName(franchisee.getName());
                    }
                }
            }
        });
        return queryDataFromBMS(electricityBatteries, tenant);
    }
    
    
}
