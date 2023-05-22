package com.xiliulou.electricity.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
@Slf4j
public class ElectricityBatteryDataServiceImpl extends ServiceImpl<ElectricityBatteryMapper, ElectricityBattery>
        implements ElectricityBatteryDataService {

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

    @Override
    @Slave
    public R selectAllBatteryPageData(long offset, long size, String sn, Long franchiseeId, Integer electricityCabinetId) {
        if (size < 0 || size > 50) {
            size = 10;
        }

        if (offset < 0) {
            offset = 0;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .sn(sn)
                .franchiseeId(franchiseeId)
                .electricityCabinetId(electricityCabinetId)
                .queryType(ElectricityBatteryDataQuery.QUERY_TYPE_ALL).build();

        List<ElectricityBatteryDataVO> electricityBatteries = electricitybatterymapper.queryBatteryList(electricityBatteryQuery, offset, size);
        if(CollectionUtils.isEmpty(electricityBatteries)){
            return R.ok(new ArrayList<EleBatteryDataVO>());
        }
        electricityBatteries.parallelStream().forEach(item->{
            Long uid = item.getUid();
            Long fid = item.getFranchiseeId();
            if (Objects.nonNull(uid)) {
                UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
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
        });
        return R.ok(queryDataFromBMS(electricityBatteries));
    }

    @Override
    public R selectAllBatteryDataCount( String sn, Long franchiseeId, Integer electricityCabinetId)  {
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .sn(sn)
                .franchiseeId(franchiseeId)
                .electricityCabinetId(electricityCabinetId)
                .queryType(ElectricityBatteryDataQuery.QUERY_TYPE_ALL).build();

        return R.ok(electricitybatterymapper.queryBatteryCount(electricityBatteryQuery));
    }

    @Override
	@Slave
    public R selectInCabinetBatteryPageData(long offset, long size, String sn, Long franchiseeId, Integer electricityCabinetId) {
        if (size < 0 || size > 50) {
            size = 10;
        }

        if (offset < 0) {
            offset = 0;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .sn(sn)
                .franchiseeId(franchiseeId)
                .electricityCabinetId(electricityCabinetId)
                .physicsStatus(ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE)
                .queryType(ElectricityBatteryDataQuery.QUERY_TYPE_INCABINET).build();
        List<ElectricityBatteryDataVO> electricityBatteries = electricitybatterymapper.queryBatteryList(electricityBatteryQuery, offset, size);
        if(CollectionUtils.isEmpty(electricityBatteries)){
            return R.ok(new ArrayList<EleBatteryDataVO>());
        }
        electricityBatteries.parallelStream().forEach(item->{
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
        });
        return R.ok(queryDataFromBMS(electricityBatteries));

    }

    @Override
	@Slave
    public R selectInCabinetBatteryDataCount(String sn, Long franchiseeId, Integer electricityCabinetId) {
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .sn(sn)
                .franchiseeId(franchiseeId)
                .electricityCabinetId(electricityCabinetId)
                .physicsStatus(ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE)
                .queryType(ElectricityBatteryDataQuery.QUERY_TYPE_INCABINET).build();
        return R.ok(electricitybatterymapper.queryBatteryCount(electricityBatteryQuery));
    }

    @Override
	@Slave
    public R selectPendingRentalBatteryPageData(long offset, long size, String sn, Long franchiseeId, Integer electricityCabinetId) {
        if (size < 0 || size > 50) {
            size = 10;
        }

        if (offset < 0) {
            offset = 0;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .sn(sn)
                .electricityCabinetId(electricityCabinetId)
                .businessStatus(ElectricityBattery.BUSINESS_STATUS_INPUT)
                .queryType(ElectricityBatteryDataQuery.QUERY_TYPE_PENDINGRENTAL).build();
        List<ElectricityBatteryDataVO> electricityBatteries = electricitybatterymapper.queryBatteryList(electricityBatteryQuery, offset, size);
        if(CollectionUtils.isEmpty(electricityBatteries)){
            return R.ok(new ArrayList<EleBatteryDataVO>());
        }
        electricityBatteries.parallelStream().forEach(item->{
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
        });
        return R.ok(queryDataFromBMS(electricityBatteries));

    }

    @Override
	@Slave
    public R selectPendingRentalBatteryDataCount(String sn, Long franchiseeId, Integer electricityCabinetId) {
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .sn(sn)
                .electricityCabinetId(electricityCabinetId)
                .businessStatus(ElectricityBattery.BUSINESS_STATUS_INPUT)
                .queryType(ElectricityBatteryDataQuery.QUERY_TYPE_PENDINGRENTAL).build();
        return R.ok(electricitybatterymapper.queryBatteryCount(electricityBatteryQuery));
    }

    @Override
	@Slave
    public R selectLeasedBatteryPageData(long offset, long size, String sn, Long franchiseeId, Integer electricityCabinetId) {
        if (size < 0 || size > 50) {
            size = 10;
        }

        if (offset < 0) {
            offset = 0;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .sn(sn)
                .electricityCabinetId(electricityCabinetId)
                .franchiseeId(franchiseeId)
                .queryType(ElectricityBatteryDataQuery.QUERY_TYPE_LEASED).build();
        List<ElectricityBatteryDataVO> electricityBatteries = electricitybatterymapper.queryBatteryList(electricityBatteryQuery, offset, size);
        if(CollectionUtils.isEmpty(electricityBatteries)){
            return R.ok(new ArrayList<EleBatteryDataVO>());
        }
        electricityBatteries.parallelStream().forEach(item->{
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
        });
        return R.ok(queryDataFromBMS(electricityBatteries));

    }

    @Override
	@Slave
    public R selectLeasedBatteryDataCount(String sn, Long franchiseeId, Integer electricityCabinetId) {
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .sn(sn)
                .electricityCabinetId(electricityCabinetId)
                .franchiseeId(franchiseeId)
                .queryType(ElectricityBatteryDataQuery.QUERY_TYPE_LEASED).build();
        return R.ok(electricitybatterymapper.queryBatteryCount(electricityBatteryQuery));
    }

    @Override
	@Slave
    public R selectStrayBatteryPageData(long offset, long size, String sn, Long franchiseeId, Integer electricityCabinetId) {
        if (size < 0 || size > 50) {
            size = 10;
        }

        if (offset < 0) {
            offset = 0;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .sn(sn)
                .electricityCabinetId(electricityCabinetId)
                .franchiseeId(franchiseeId)
                .businessStatus(ElectricityBattery.BUSINESS_STATUS_RETURN)
                .physicsStatus(ElectricityBattery.PHYSICS_STATUS_NOT_WARE_HOUSE)
                .queryType(ElectricityBatteryDataQuery.QUERY_TYPE_STRAY).build();
        List<ElectricityBatteryDataVO> electricityBatteries = electricitybatterymapper.queryBatteryList(electricityBatteryQuery, offset, size);
        if(CollectionUtils.isEmpty(electricityBatteries)){
            return R.ok(new ArrayList<EleBatteryDataVO>());
        }
        electricityBatteries.parallelStream().forEach(item->{
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
        });
        return R.ok(queryDataFromBMS(electricityBatteries));

    }

    @Override
	@Slave
    public R selectStrayBatteryDataCount(String sn, Long franchiseeId, Integer electricityCabinetId) {
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .sn(sn)
                .electricityCabinetId(electricityCabinetId)
                .franchiseeId(franchiseeId)
                .businessStatus(ElectricityBattery.BUSINESS_STATUS_RETURN)
                .physicsStatus(ElectricityBattery.PHYSICS_STATUS_NOT_WARE_HOUSE)
                .queryType(ElectricityBatteryDataQuery.QUERY_TYPE_STRAY).build();
        return R.ok(electricitybatterymapper.queryBatteryCount(electricityBatteryQuery));
    }

    @Override
	@Slave
    public R selectOverdueBatteryPageData(long offset, long size, String sn, Long franchiseeId, Integer electricityCabinetId) {
        if (size < 0 || size > 50) {
            size = 10;
        }

        if (offset < 0) {
            offset = 0;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .sn(sn)
                .electricityCabinetId(electricityCabinetId)
                .franchiseeId(franchiseeId)
                .businessStatus(ElectricityBattery.BUSINESS_STATUS_LEASE)
                .queryType(ElectricityBatteryDataQuery.QUERY_TYPE_OVERDUE)
                .currentTimeMillis(System.currentTimeMillis()).build();
        List<ElectricityBatteryDataVO> electricityBatteries = electricitybatterymapper.queryBatteryList(electricityBatteryQuery, offset, size);
        if(CollectionUtils.isEmpty(electricityBatteries)){
            return R.ok(new ArrayList<EleBatteryDataVO>());
        }
        electricityBatteries.parallelStream().forEach(item->{
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
        });
        return R.ok(queryDataFromBMS(electricityBatteries));

    }

    @Override
	@Slave
    public R selectOverdueBatteryDataCount(String sn, Long franchiseeId, Integer electricityCabinetId) {
        ElectricityBatteryDataQuery electricityBatteryQuery = ElectricityBatteryDataQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .sn(sn)
                .electricityCabinetId(electricityCabinetId)
                .franchiseeId(franchiseeId)
                .businessStatus(ElectricityBattery.BUSINESS_STATUS_LEASE)
                .queryType(ElectricityBatteryDataQuery.QUERY_TYPE_OVERDUE)
                .currentTimeMillis(System.currentTimeMillis()).build();
        return R.ok(electricitybatterymapper.queryBatteryCount(electricityBatteryQuery));
    }

    /**
     * 查询BMS
     * @param batteryInfoQuery
     * @return
     */
    @Override
    public BatteryInfoDto callBatteryServiceQueryBatteryInfo(BatteryInfoQuery batteryInfoQuery) {
        try{
            Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
            if (Objects.isNull(tenant)) {
                return null;
            }

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

        }catch (Exception e){
            log.error("BATTERYDATA QUERY BMS ERROR! ", e);
            return null;
        }

    }

    public List<EleBatteryDataVO> queryDataFromBMS(List<ElectricityBatteryDataVO> electricityBatteries){
        try {

            if (CollectionUtils.isEmpty(electricityBatteries)) {
                return new ArrayList<EleBatteryDataVO>();
            }

            List<EleBatteryDataVO> eleBatteryDataVOS=new ArrayList<>(electricityBatteries.size());

            for(int i=0;i<electricityBatteries.size();i++){
                EleBatteryDataVO vo=new EleBatteryDataVO();
                vo.setElectricityBatteryDataVO(electricityBatteries.get(i));
                eleBatteryDataVOS.add(vo);
            }

            eleBatteryDataVOS.parallelStream().forEach(item ->{
                ElectricityBatteryDataVO electricityBatteryDataVO = item.getElectricityBatteryDataVO();
                if (!Objects.isNull(electricityBatteryDataVO) && !Objects.isNull(electricityBatteryDataVO.getSn())) {
                    BatteryInfoQuery batteryInfoQuery = new BatteryInfoQuery();
                    batteryInfoQuery.setSn(electricityBatteryDataVO.getSn());
                    item.setBatteryInfoDto(callBatteryServiceQueryBatteryInfo(batteryInfoQuery));
                }
            });

            return eleBatteryDataVOS;

        }catch (Exception e){
            log.error("BATTERYDATA QUERY BMS ERROR! ", e);
            return new ArrayList<EleBatteryDataVO>();
        }
    }
}
