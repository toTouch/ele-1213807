package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityCarMapper;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

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


    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCar queryByIdFromCache(Integer id) {
        //先查缓存
        ElectricityCar cacheElectricityCar = redisService.getWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CAR + id, ElectricityCar.class);
        if (Objects.nonNull(cacheElectricityCar)) {
            return cacheElectricityCar;
        }
        //缓存没有再查数据库
        ElectricityCar electricityCar = electricityCarMapper.selectById(id);
        if (Objects.isNull(electricityCar)) {
            return null;
        }
        //放入缓存
        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CAR + id, electricityCar);
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
        boolean result = redisService.setNx(ElectricityCabinetConstant.CAR_SAVE_UID + user.getUid(), "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //换电柜
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
        electricityCar.setModel(electricityCarModel.getName());
        electricityCar.setModelId(electricityCarModel.getId());

        int insert = electricityCarMapper.insert(electricityCar);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            //新增缓存
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELECTRICITY_CAR + electricityCar.getId(), electricityCar);
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

        //操作频繁
        boolean result = redisService.setNx(ElectricityCabinetConstant.CAR_EDIT_UID + user.getUid(), "1", 3 * 1000L, false);
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

        //车辆老型号
        Integer oldModelId = oldElectricityCar.getModelId();
        //查找快递柜型号
        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(electricityCar.getModelId());
        if (Objects.isNull(electricityCarModel)) {
            return R.fail("100005", "未找到车辆型号");
        }
        if (!oldModelId.equals(electricityCar.getModelId())) {
            return R.fail("ELECTRICITY.0010", "不能修改型号");
        }
        electricityCar.setUpdateTime(System.currentTimeMillis());

        int update = electricityCarMapper.updateById(electricityCar);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.delete(ElectricityCabinetConstant.CACHE_ELECTRICITY_CAR + electricityCar.getId());
            return null;
        });
        return R.ok();
    }

    @Override
    @Transactional
    public R delete(Integer id) {

        ElectricityCar electricityCar = queryByIdFromCache(id);
        if (Objects.isNull(electricityCar)) {
            return R.fail("100007", "未找到车辆");
        }

        //删除数据库
        electricityCar.setId(id);
        electricityCar.setUpdateTime(System.currentTimeMillis());
        electricityCar.setDelFlag(ElectricityCar.DEL_DEL);
        int update = electricityCarMapper.updateById(electricityCar);
        DbUtils.dbOperateSuccessThen(update, () -> {

            //删除缓存
            redisService.delete(ElectricityCabinetConstant.CACHE_ELECTRICITY_CAR + id);
            return null;
        });
        return R.ok();
    }

    @Override
    @DS("slave_1")
    public R queryList(ElectricityCarQuery electricityCarQuery) {
        return R.ok(electricityCarMapper.queryList(electricityCarQuery));
    }

    @Override
    public Integer queryByModelId(Integer id) {
        return electricityCarMapper.selectCount(Wrappers.<ElectricityCar>lambdaQuery().eq(ElectricityCar::getModelId, id).eq(ElectricityCar::getDelFlag, ElectricityCar.DEL_NORMAL));
    }

    @Override
    public R queryCount(ElectricityCarQuery electricityCarQuery) {
        return R.ok(electricityCarMapper.queryCount(electricityCarQuery));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R bindUser(ElectricityCarBindUser electricityCarBindUser) {
        UserInfo userInfo = userInfoService.queryByUid(electricityCarBindUser.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY CAR ERROR! not found user userId:{}", electricityCarBindUser.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("ELECTRICITY CAR ERROR! not found user! userId:{}", userInfo.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        ElectricityCar electricityCar = queryByIdFromCache(electricityCarBindUser.getCarId());
        if (Objects.isNull(electricityCar)) {
            return R.fail("100007", "未找到车辆");
        }

        FranchiseeUserInfo updateFranchiseeUserInfo = new FranchiseeUserInfo();
        updateFranchiseeUserInfo.setId(franchiseeUserInfo.getId());
        updateFranchiseeUserInfo.setBindCarId(electricityCarBindUser.getCarId());
        updateFranchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
        franchiseeUserInfoService.update(updateFranchiseeUserInfo);

        electricityCar.setStatus(ElectricityCar.CAR_IS_RENT);
        electricityCar.setUid(electricityCarBindUser.getUid());
        electricityCar.setPhone(userInfo.getPhone());
        electricityCar.setUserInfoId(userInfo.getId());
        electricityCar.setUserName(userInfo.getName());
        electricityCar.setUpdateTime(System.currentTimeMillis());
        return R.ok(electricityCarMapper.updateById(electricityCar));
    }


}
