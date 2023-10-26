package com.xiliulou.electricity.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.dto.bms.BatteryInfoDto;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityBatteryMapper;
import com.xiliulou.electricity.query.ElectricityBatteryDataQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.retrofit.BatteryPlatRetrofitService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.AESUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityBatteryDataVO;
import com.xiliulou.electricity.vo.api.EleBatteryDataVO;
import com.xiliulou.electricity.web.query.battery.BatteryInfoQuery;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
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
    
    @Override
    @Slave
    public R selectAllBatteryPageData(ElectricityBatteryDataQuery electricityBatteryQuery) {
        
        List<ElectricityBatteryDataVO> electricityBatteries = electricitybatterymapper.queryBatteryList(electricityBatteryQuery, electricityBatteryQuery.getOffset(),
                electricityBatteryQuery.getSize());
        
        //获取sn列表
        List<String> snList = electricityBatteries.parallelStream().map(ElectricityBatteryDataVO::getSn).collect(Collectors.toList());
        List<BatteryOtherProperties> otherPropertiesList = null;
        Map<String, Double> otherPropertiesMap = null;
        if (!CollectionUtils.isNotEmpty(snList)) {
            //根据获取的sn列表查询
            otherPropertiesList = batteryOtherPropertiesService.listBatteryOtherPropertiesBySn(snList);
            if (CollectionUtils.isNotEmpty(otherPropertiesList)) {
                otherPropertiesMap = otherPropertiesList.stream()
                        .collect(Collectors.toMap(BatteryOtherProperties::getBatteryName, BatteryOtherProperties::getBatteryV, (value1, value2) -> value1));
            }
        }
        
        if (CollectionUtils.isEmpty(electricityBatteries)) {
            return R.ok(new ArrayList<EleBatteryDataVO>());
        }
        
        Map<String, Double> finalOtherPropertiesMap = otherPropertiesMap;
        
        electricityBatteries.parallelStream().forEach(item -> {
            //设置电压
            if (Objects.nonNull(finalOtherPropertiesMap) && finalOtherPropertiesMap.containsKey(item.getSn())) {
                item.setBoxVoltage(finalOtherPropertiesMap.get(item.getSn()).intValue());
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
            
            String batteryShortType = batteryModelService.acquireBatteryShortType(item.getModel(), electricityBatteryQuery.getTenantId());
            if (StringUtils.isNotEmpty(batteryShortType)) {
                item.setModel(batteryShortType);
            }
            
        });
        return R.ok(queryDataFromBMS(electricityBatteries, electricityBatteryQuery.getTenant()));
    }
    
    @Override
    public R selectAllBatteryDataCount(ElectricityBatteryDataQuery electricityBatteryQuery) {
        
        return R.ok(electricitybatterymapper.queryBatteryCount(electricityBatteryQuery));
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
