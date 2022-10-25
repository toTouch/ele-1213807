package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityCarMapper;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityCarVO;
import com.xiliulou.electricity.web.query.OauthBindQuery;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 换电柜表(TElectricityCar)表服务实现类
 *
 * @author makejava
 * @since 2022-06-06 16:00:14
 */
@Service("electricityCarService")
@Slf4j
public class ElectricityCarServiceImpl implements ElectricityCarService {
    @Resource
    private ElectricityCarMapper electricityCarMapper;
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityCarModelService electricityCarModelService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    FranchiseeUserInfoService franchiseeUserInfoService;
    @Autowired
    EleBindCarRecordService eleBindCarRecordService;
    @Autowired
    ElectricityBatteryService electricityBatteryService;


    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCar queryByIdFromCache(Integer id) {
        //先查缓存
        ElectricityCar cacheElectricityCar = redisService.getWithHash(CacheConstant.CACHE_ELECTRICITY_CAR + id, ElectricityCar.class);
        if (Objects.nonNull(cacheElectricityCar)) {
            return cacheElectricityCar;
        }
        //缓存没有再查数据库
        ElectricityCar electricityCar = electricityCarMapper.selectById(id);
        if (Objects.isNull(electricityCar)) {
            return null;
        }
        //放入缓存
        redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CAR + id, electricityCar);
        return electricityCar;
    }

    @Override
    @Transactional
    public R save(ElectricityCarAddAndUpdate electricityCarAddAndUpdate) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY CAR  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //操作频繁
        boolean result = redisService.setNx(CacheConstant.CAR_SAVE_UID + user.getUid(), "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //换电柜车辆
        ElectricityCar electricityCar = new ElectricityCar();
        BeanUtil.copyProperties(electricityCarAddAndUpdate, electricityCar);
        electricityCar.setTenantId(tenantId);
        electricityCar.setCreateTime(System.currentTimeMillis());
        electricityCar.setUpdateTime(System.currentTimeMillis());
        electricityCar.setDelFlag(ElectricityCabinet.DEL_NORMAL);

        //查找车辆型号
        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(electricityCar.getModelId());
        if (Objects.isNull(electricityCarModel)) {
            return R.fail("100005", "未找到车辆型号");
        }
        ElectricityCar existElectricityCar = electricityCarMapper.selectOne(new LambdaQueryWrapper<ElectricityCar>().eq(ElectricityCar::getSn, electricityCarAddAndUpdate.getSn()).eq(ElectricityCar::getTenantId, tenantId));
        if (Objects.nonNull(existElectricityCar)) {
            return R.fail("100017", "已存在该编号车辆");
        }

        electricityCar.setModel(electricityCarModel.getName());
        electricityCar.setModelId(electricityCarModel.getId());

        int insert = electricityCarMapper.insert(electricityCar);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            //新增缓存
            redisService.saveWithHash(CacheConstant.CACHE_ELECTRICITY_CAR + electricityCar.getId(), electricityCar);
            return electricityCar;
        });
        return R.ok(electricityCar.getId());
    }

    @Override
    @Transactional
    public R edit(ElectricityCarAddAndUpdate electricityCarAddAndUpdate) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY CAR  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //操作频繁
        boolean result = redisService.setNx(CacheConstant.CAR_EDIT_UID + user.getUid(), "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }

        //换电柜车辆
        ElectricityCar electricityCar = new ElectricityCar();
        BeanUtil.copyProperties(electricityCarAddAndUpdate, electricityCar);
        ElectricityCar oldElectricityCar = queryByIdFromCache(electricityCar.getId());
        if (Objects.isNull(oldElectricityCar)) {
            return R.fail("100007", "未找到车辆");
        }

        if (Objects.equals(tenantId, oldElectricityCar.getTenantId())) {
            return R.ok();
        }

        //车辆老型号
        Integer oldModelId = oldElectricityCar.getModelId();
        //查找快递柜型号
        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(electricityCar.getModelId());
        if (Objects.isNull(electricityCarModel)) {
            return R.fail("100005", "未找到车辆型号");
        }

        if (Objects.equals(tenantId, electricityCarModel.getTenantId())) {
            return R.ok();
        }

        if (!oldModelId.equals(electricityCar.getModelId())) {
            return R.fail("ELECTRICITY.0010", "不能修改型号");
        }
        ElectricityCar existElectricityCar = electricityCarMapper.selectOne(new LambdaQueryWrapper<ElectricityCar>().eq(ElectricityCar::getSn, electricityCarAddAndUpdate.getSn()).eq(ElectricityCar::getTenantId, tenantId));
        if (Objects.nonNull(existElectricityCar)) {
            return R.fail("100017", "已存在该编号车辆");
        }

        electricityCar.setUpdateTime(System.currentTimeMillis());

        int update = electricityCarMapper.updateById(electricityCar);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CAR + electricityCar.getId());
            return null;
        });
        return R.ok();
    }

    @Override
    @Transactional
    public R delete(Integer id) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        ElectricityCar electricityCar = queryByIdFromCache(id);
        if (Objects.isNull(electricityCar)) {
            return R.fail("100007", "未找到车辆");
        }

        if (Objects.equals(tenantId, electricityCar.getTenantId())) {
            return R.ok();
        }

        //删除数据库
        electricityCar.setId(id);
        electricityCar.setUpdateTime(System.currentTimeMillis());
        electricityCar.setDelFlag(ElectricityCar.DEL_DEL);
        int update = electricityCarMapper.updateById(electricityCar);
        DbUtils.dbOperateSuccessThen(update, () -> {

            //删除缓存
            redisService.delete(CacheConstant.CACHE_ELECTRICITY_CAR + id);
            return null;
        });
        return R.ok();
    }

    @Override
    @DS("slave_1")
    public R queryList(ElectricityCarQuery electricityCarQuery) {
        List<ElectricityCarVO> electricityCarVOS = electricityCarMapper.queryList(electricityCarQuery);
        if (CollectionUtils.isEmpty(electricityCarVOS)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        List<ElectricityCarVO> carVOList = electricityCarVOS.parallelStream().peek(item -> {
            ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(item.getUid());
            if (Objects.nonNull(electricityBattery)) {
                item.setBatterySn(electricityBattery.getSn());
            }
        }).collect(Collectors.toList());

        return R.ok(carVOList);
    }

    @Override
    public Integer queryByModelId(Integer id) {
        return electricityCarMapper.selectCount(Wrappers.<ElectricityCar>lambdaQuery().eq(ElectricityCar::getModelId, id).eq(ElectricityCar::getDelFlag, ElectricityCar.DEL_NORMAL).eq(ElectricityCar::getTenantId, TenantContextHolder.getTenantId()));
    }

    @Override
    public R queryCount(ElectricityCarQuery electricityCarQuery) {
        return R.ok(electricityCarMapper.queryCount(electricityCarQuery));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R bindUser(ElectricityCarBindUser electricityCarBindUser) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY CAR  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到操作用户");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityCarBindUser.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY CAR ERROR! not found user userId:{}", electricityCarBindUser.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(userInfo.getTenantId(),tenantId)){
            return R.ok();
        }

        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("ELECTRICITY CAR ERROR! not found user! userId:{}", userInfo.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.nonNull(franchiseeUserInfo.getBindCarId())) {
            log.error("ELECTRICITY CAR ERROR! not found user! userId:{}", userInfo.getUid());
            return R.fail("100012", "已绑定车辆");
        }

        if (Objects.isNull(franchiseeUserInfo.getRentCarDeposit())) {
            log.error("ELECTRICITY CAR ERROR! not found user! userId:{}", userInfo.getUid());
            return R.fail("100013", "未缴纳租车押金");
        }

        if (Objects.isNull(franchiseeUserInfo.getRentCarCardId())) {
            log.error("ELECTRICITY CAR ERROR! not found user! userId:{}", userInfo.getUid());
            return R.fail("100014", "未购买租车套餐");
        }
        Long now = System.currentTimeMillis();
        if (Objects.equals(franchiseeUserInfo.getRentCarStatus(), FranchiseeUserInfo.RENT_CAR_STATUS_IS_DEPOSIT) && Objects.nonNull(franchiseeUserInfo.getRentCarMemberCardExpireTime()) && franchiseeUserInfo.getRentCarMemberCardExpireTime() < now) {
            log.error("ELECTRICITY CAR ERROR! rent car memberCard  is Expire ! uid:{}", user.getUid());
            return R.fail("100013", "租车套餐已过期");
        }

        ElectricityCar electricityCar = queryByIdFromCache(electricityCarBindUser.getCarId());
        if (Objects.isNull(electricityCar)) {
            return R.fail("100007", "未找到车辆");
        }

        if (Objects.equals(electricityCar.getTenantId(),tenantId)){
            return R.ok();
        }

        if (!Objects.equals(electricityCar.getModelId(), franchiseeUserInfo.getBindCarModelId())) {
            log.error("ELECTRICITY CAR ERROR! user bind carModel not equals will bond carModel! userId:{}", userInfo.getUid());
            return R.fail("100016", "用户缴纳的车辆型号押金与绑定的不符");
        }

        FranchiseeUserInfo updateFranchiseeUserInfo = new FranchiseeUserInfo();
        updateFranchiseeUserInfo.setId(franchiseeUserInfo.getId());
        updateFranchiseeUserInfo.setBindCarId(electricityCarBindUser.getCarId());
        updateFranchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
        updateFranchiseeUserInfo.setRentCarStatus(FranchiseeUserInfo.RENT_CAR_STATUS_IS_RENT_CAR);
        franchiseeUserInfoService.update(updateFranchiseeUserInfo);

        //新增操作记录
        EleBindCarRecord eleBindCarRecord = EleBindCarRecord.builder()
                .carId(electricityCar.getId())
                .sn(electricityCar.getSn())
                .operateUser(user.getUsername())
                .model(electricityCar.getModel())
                .phone(userInfo.getPhone())
                .status(EleBindCarRecord.BIND_CAR)
                .userName(userInfo.getName())
                .tenantId(electricityCar.getTenantId())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleBindCarRecordService.insert(eleBindCarRecord);

        electricityCar.setStatus(ElectricityCar.CAR_IS_RENT);
        electricityCar.setUid(electricityCarBindUser.getUid());
        electricityCar.setPhone(userInfo.getPhone());
        electricityCar.setUserInfoId(userInfo.getId());
        electricityCar.setUserName(userInfo.getName());
        electricityCar.setUpdateTime(System.currentTimeMillis());
        return R.ok(electricityCarMapper.updateById(electricityCar));
    }

    @Override
    public R unBindUser(ElectricityCarBindUser electricityCarBindUser) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY CAR  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到操作用户");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityCarBindUser.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY CAR ERROR! not found user userId:{}", electricityCarBindUser.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(userInfo.getTenantId(),tenantId)){
            return R.ok();
        }

        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("ELECTRICITY CAR ERROR! not found user! userId:{}", userInfo.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.isNull(franchiseeUserInfo.getBindCarId())) {
            log.error("ELECTRICITY CAR ERROR! not found user! userId:{}", userInfo.getUid());
            return R.fail("100015", "用户未绑定车辆");
        }

        ElectricityCar electricityCar = queryByIdFromCache(electricityCarBindUser.getCarId());
        if (Objects.isNull(electricityCar)) {
            return R.fail("100007", "未找到车辆");
        }

        if (Objects.equals(electricityCar.getTenantId(),tenantId)){
            return R.ok();
        }

        FranchiseeUserInfo updateFranchiseeUserInfo = new FranchiseeUserInfo();
        updateFranchiseeUserInfo.setId(franchiseeUserInfo.getId());
        updateFranchiseeUserInfo.setBindCarId(null);
        updateFranchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
        updateFranchiseeUserInfo.setRentCarStatus(FranchiseeUserInfo.RENT_CAR_STATUS_IS_DEPOSIT);
        franchiseeUserInfoService.updateRentCar(updateFranchiseeUserInfo);

        //新增操作记录
        EleBindCarRecord eleBindCarRecord = EleBindCarRecord.builder()
                .carId(electricityCar.getId())
                .sn(electricityCar.getSn())
                .operateUser(user.getUsername())
                .model(electricityCar.getModel())
                .phone(userInfo.getPhone())
                .status(EleBindCarRecord.NOT_BIND_CAR)
                .userName(userInfo.getName())
                .tenantId(electricityCar.getTenantId())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleBindCarRecordService.insert(eleBindCarRecord);

        electricityCar.setStatus(ElectricityCar.CAR_NOT_RENT);
        electricityCar.setUid(null);
        electricityCar.setPhone(null);
        electricityCar.setUserInfoId(null);
        electricityCar.setUserName(null);
        electricityCar.setUpdateTime(System.currentTimeMillis());
        return R.ok(electricityCarMapper.updateBindUser(electricityCar));
    }

    @Override
    public ElectricityCar queryInfoByUid(Long uid) {
        return electricityCarMapper.selectOne(new LambdaQueryWrapper<ElectricityCar>().eq(ElectricityCar::getUid, uid));
    }

    @Override
    public Integer queryCountByStoreIds(Integer tenantId, List<Long> storeIds) {
        return electricityCarMapper.queryCountByStoreIds(tenantId, storeIds);
    }
}
