package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.City;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.entity.RentCarOrder;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.UserInfoMapper;
import com.xiliulou.electricity.query.UserInfoBatteryAddAndUpdate;
import com.xiliulou.electricity.query.UserInfoCarAddAndUpdate;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.service.CityService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import com.xiliulou.electricity.service.RentCarOrderService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户列表(TUserInfo)表服务实现类
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {
    @Resource
    private UserInfoMapper userInfoMapper;
    @Autowired
    StoreService storeService;
    @Autowired
    RentBatteryOrderService rentBatteryOrderService;
    @Autowired
    RentCarOrderService rentCarOrderService;
    @Autowired
    CityService cityService;
    @Autowired
    ElectricityBatteryService electricityBatteryService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserInfo queryByIdFromDB(Long id) {
        return this.userInfoMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserInfo queryByIdFromCache(Long id) {
        return null;
    }

    /**
     * 新增数据
     *
     * @param userInfo 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfo insert(UserInfo userInfo) {
        this.userInfoMapper.insert(userInfo);
        return userInfo;
    }

    /**
     * 修改数据
     *
     * @param userInfo 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(UserInfo userInfo) {
        return this.userInfoMapper.update(userInfo);

    }

    @Override
    public R bindBattery(UserInfoBatteryAddAndUpdate userInfoBatteryAddAndUpdate) {
        UserInfo oldUserInfo = queryByIdFromDB(userInfoBatteryAddAndUpdate.getId());
        if (Objects.isNull(oldUserInfo)) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryBySn(userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
        if (Objects.isNull(oldElectricityBattery)) {
            return R.fail("ELECTRICITY.0020", "未找到电池");
        }
        UserInfo userInfo = new UserInfo();
        BeanUtil.copyProperties(userInfoBatteryAddAndUpdate, userInfo);
        RentBatteryOrder rentBatteryOrder = new RentBatteryOrder();
        if (Objects.nonNull(userInfoBatteryAddAndUpdate.getBatteryStoreId())) {
            Store store = storeService.queryByIdFromCache(userInfoBatteryAddAndUpdate.getBatteryStoreId());
            if (Objects.isNull(store)) {
                return R.fail("ELECTRICITY.0018", "未找到门店");
            }
            userInfo.setBatteryAreaId(store.getAreaId());
            rentBatteryOrder.setBatteryStoreName(store.getName());
        }
        userInfo.setNowElectricityBatterySn(userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
        userInfo.setUpdateTime(System.currentTimeMillis());
        userInfo.setServiceStatus(UserInfo.IS_SERVICE_STATUS);
        Integer update = userInfoMapper.update(userInfo);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //添加租电池记录=
            rentBatteryOrder.setUid(oldUserInfo.getUid());
            rentBatteryOrder.setName(userInfo.getName());
            rentBatteryOrder.setPhone(userInfo.getPhone());
            rentBatteryOrder.setIdNumber(userInfo.getIdNumber());
            rentBatteryOrder.setBatteryStoreId(userInfo.getBatteryStoreId());
            rentBatteryOrder.setElectricityBatterySn(userInfo.getInitElectricityBatterySn());
            rentBatteryOrder.setBatteryDeposit(userInfo.getBatteryDeposit());
            rentBatteryOrder.setCreateTime(System.currentTimeMillis());
            rentBatteryOrder.setStatus(RentBatteryOrder.IS_USE_STATUS);
            rentBatteryOrderService.insert(rentBatteryOrder);
            //电池绑定用户
            ElectricityBattery electricityBattery = new ElectricityBattery();
            electricityBattery.setId(oldElectricityBattery.getId());
            electricityBattery.setUid(userInfo.getUid());
            electricityBatteryService.update(electricityBattery);
            return null;
        });
        return R.ok();
    }


    @Override
    public R bindCar(UserInfoCarAddAndUpdate userInfoCarAddAndUpdate) {
        UserInfo oldUserInfo = queryByIdFromDB(userInfoCarAddAndUpdate.getId());
        if (Objects.isNull(oldUserInfo)) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        RentCarOrder rentCarOrder = new RentCarOrder();
        if(Objects.nonNull(userInfoCarAddAndUpdate.getCarStoreId())) {
            Store store = storeService.queryByIdFromCache(userInfoCarAddAndUpdate.getCarStoreId());
            if (Objects.isNull(store)) {
                return R.fail("ELECTRICITY.0018", "未找到门店");
            }
            rentCarOrder.setCarStoreName(store.getName());
        }
        UserInfo userInfo = new UserInfo();
        BeanUtil.copyProperties(userInfoCarAddAndUpdate, userInfo);
        userInfo.setUpdateTime(System.currentTimeMillis());
        Integer update = userInfoMapper.update(userInfo);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //添加租电池记录
            rentCarOrder.setUid(oldUserInfo.getUid());
            rentCarOrder.setName(userInfo.getName());
            rentCarOrder.setPhone(userInfo.getPhone());
            rentCarOrder.setIdNumber(userInfo.getIdNumber());
            rentCarOrder.setCarStoreId(userInfo.getCarStoreId());
            rentCarOrder.setCarSn(userInfo.getCarSn());
            rentCarOrder.setCarDeposit(userInfo.getCarDeposit());
            rentCarOrder.setNumberPlate(userInfo.getNumberPlate());
            rentCarOrder.setCreateTime(System.currentTimeMillis());
            rentCarOrder.setStatus(RentCarOrder.IS_USE_STATUS);
            rentCarOrderService.insert(rentCarOrder);
            return null;
        });
        return R.ok();
    }

    @Override
    public R queryList(UserInfoQuery userInfoQuery) {
        List<UserInfoVO> UserInfoVOList = userInfoMapper.queryList(userInfoQuery);
        if (ObjectUtil.isNotEmpty(UserInfoVOList)) {
            UserInfoVOList.parallelStream().forEach(e -> {
                //地区
                City city = cityService.queryByIdFromCache(e.getBatteryAreaId());
                if (Objects.nonNull(city)) {
                    e.setAreaName(city.getCity());
                    e.setPid(city.getPid());
                }
            });
        }
        return R.ok(UserInfoVOList.stream().sorted(Comparator.comparing(UserInfoVO::getUpdateTime).reversed()).collect(Collectors.toList()));
    }

    @Override
    public R disable(Long id) {
        UserInfo oldUserInfo = queryByIdFromDB(id);
        if (Objects.isNull(oldUserInfo)) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setUpdateTime(System.currentTimeMillis());
        userInfo.setUsableStatus(UserInfo.USER_UN_USABLE_STATUS);
        userInfoMapper.update(userInfo);
        return R.ok();
    }

    @Override
    public R reboot(Long id) {
        UserInfo oldUserInfo = queryByIdFromDB(id);
        if (Objects.isNull(oldUserInfo)) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setUpdateTime(System.currentTimeMillis());
        userInfo.setUsableStatus(UserInfo.USER_USABLE_STATUS);
        userInfoMapper.update(userInfo);
        return R.ok();
    }

    @Override
    public R unBindBattery(Long id) {
        UserInfo oldUserInfo = queryByIdFromDB(id);
        if (Objects.isNull(oldUserInfo)) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        Store store = storeService.queryByIdFromCache(oldUserInfo.getBatteryStoreId());
        if (Objects.isNull(store)) {
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setCarStoreId(oldUserInfo.getCarStoreId());
        userInfo.setCarSn(oldUserInfo.getCarSn());
        userInfo.setCarDeposit(oldUserInfo.getCarDeposit());
        userInfo.setNumberPlate(oldUserInfo.getNumberPlate());
        userInfo.setInitElectricityBatterySn(null);
        userInfo.setNowElectricityBatterySn(null);
        userInfo.setBatteryStoreId(null);
        userInfo.setBatteryAreaId(null);
        userInfo.setBatteryDeposit(null);
        userInfo.setServiceStatus(UserInfo.NO_SERVICE_STATUS);
        userInfo.setUpdateTime(System.currentTimeMillis());
        Integer update = userInfoMapper.unBind(userInfo);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //添加租电池记录
            RentCarOrder rentCarOrder = new RentCarOrder();
            rentCarOrder.setUid(oldUserInfo.getUid());
            rentCarOrder.setName(userInfo.getName());
            rentCarOrder.setPhone(userInfo.getPhone());
            rentCarOrder.setIdNumber(userInfo.getIdNumber());
            rentCarOrder.setCarStoreId(userInfo.getCarStoreId());
            rentCarOrder.setCarStoreName(store.getName());
            rentCarOrder.setCarSn(userInfo.getCarSn());
            rentCarOrder.setCarDeposit(userInfo.getCarDeposit());
            rentCarOrder.setNumberPlate(userInfo.getNumberPlate());
            rentCarOrder.setCreateTime(System.currentTimeMillis());
            rentCarOrder.setStatus(RentCarOrder.NO_USE_STATUS);
            rentCarOrderService.insert(rentCarOrder);
            return null;
        });
        return R.ok();
    }

    @Override
    public R unBindCar(Long id) {
        UserInfo oldUserInfo = queryByIdFromDB(id);
        if (Objects.isNull(oldUserInfo)) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        Store store = storeService.queryByIdFromCache(oldUserInfo.getCarStoreId());
        if (Objects.isNull(store)) {
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setInitElectricityBatterySn(oldUserInfo.getInitElectricityBatterySn());
        userInfo.setNowElectricityBatterySn(oldUserInfo.getNowElectricityBatterySn());
        userInfo.setBatteryStoreId(oldUserInfo.getBatteryStoreId());
        userInfo.setBatteryAreaId(oldUserInfo.getBatteryAreaId());
        userInfo.setBatteryDeposit(oldUserInfo.getBatteryDeposit());
        userInfo.setCarStoreId(null);
        userInfo.setCarSn(null);
        userInfo.setCarDeposit(null);
        userInfo.setNumberPlate(null);
        userInfo.setUpdateTime(System.currentTimeMillis());
        Integer update = userInfoMapper.unBind(userInfo);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //添加租电池记录
            RentCarOrder rentCarOrder = new RentCarOrder();
            rentCarOrder.setUid(oldUserInfo.getUid());
            rentCarOrder.setName(userInfo.getName());
            rentCarOrder.setPhone(userInfo.getPhone());
            rentCarOrder.setIdNumber(userInfo.getIdNumber());
            rentCarOrder.setCarStoreId(userInfo.getCarStoreId());
            rentCarOrder.setCarStoreName(store.getName());
            rentCarOrder.setCarSn(userInfo.getCarSn());
            rentCarOrder.setCarDeposit(userInfo.getCarDeposit());
            rentCarOrder.setNumberPlate(userInfo.getNumberPlate());
            rentCarOrder.setCreateTime(System.currentTimeMillis());
            rentCarOrder.setStatus(RentCarOrder.NO_USE_STATUS);
            rentCarOrderService.insert(rentCarOrder);
            return null;
        });
        return R.ok();
    }

    @Override
    public UserInfo queryByUid(Long uid) {
        return userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUid,uid)
                .eq(UserInfo::getServiceStatus,UserInfo.IS_SERVICE_STATUS).eq(UserInfo::getDelFlag,UserInfo.DEL_NORMAL));
    }
}