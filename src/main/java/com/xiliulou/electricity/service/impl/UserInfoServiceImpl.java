package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.UserInfoMapper;
import com.xiliulou.electricity.query.UserInfoBatteryAddAndUpdate;
import com.xiliulou.electricity.query.UserInfoCarAddAndUpdate;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.utils.DbUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.Objects;

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
        UserInfo oldUserInfo=queryByIdFromDB(userInfoBatteryAddAndUpdate.getId());
        if(Objects.isNull(oldUserInfo)){
            return R.fail("ELECTRICITY.0019","未找到用户");
        }
        Store store=storeService.queryByIdFromCache(userInfoBatteryAddAndUpdate.getBatteryStoreId());
        if (Objects.isNull(store)) {
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }
        UserInfo userInfo=new UserInfo();
        BeanUtil.copyProperties(userInfoBatteryAddAndUpdate,userInfo);
        userInfo.setBatteryAreaId(store.getAreaId());
        userInfo.setNowElectricityBatterySn(userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
        userInfo.setUpdateTime(System.currentTimeMillis());
        userInfo.setServiceStatus(UserInfo.IS_SERVICE_STATUS);
        Integer update=userInfoMapper.update(userInfo);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //添加租电池记录

            // TODO 电池绑定用户 YG
            return null;
        });
        return R.ok();
    }


    @Override
    public R bindCar(UserInfoCarAddAndUpdate userInfoCarAddAndUpdate) {
        UserInfo oldUserInfo=queryByIdFromDB(userInfoCarAddAndUpdate.getId());
        if(Objects.isNull(oldUserInfo)){
            return R.fail("ELECTRICITY.0019","未找到用户");
        }
        Store store=storeService.queryByIdFromCache(userInfoCarAddAndUpdate.getCarStoreId());
        if (Objects.isNull(store)) {
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }
        UserInfo userInfo=new UserInfo();
        BeanUtil.copyProperties(userInfoCarAddAndUpdate,userInfo);
        userInfo.setUpdateTime(System.currentTimeMillis());
        userInfo.setServiceStatus(UserInfo.IS_SERVICE_STATUS);
        userInfoMapper.update(userInfo);
        return R.ok();
    }

    @Override
    public R queryList(UserInfoQuery userInfoQuery) {
        return null;
    }

    @Override
    public R disable(Long id) {
        UserInfo oldUserInfo=queryByIdFromDB(id);
        if(Objects.isNull(oldUserInfo)){
            return R.fail("ELECTRICITY.0019","未找到用户");
        }
        UserInfo userInfo=new UserInfo();
        userInfo.setUpdateTime(System.currentTimeMillis());
        userInfo.setUsableStatus(UserInfo.USER_UN_USABLE_STATUS);
        userInfoMapper.update(userInfo);
        return R.ok();
    }

    @Override
    public R reboot(Long id) {
        UserInfo oldUserInfo=queryByIdFromDB(id);
        if(Objects.isNull(oldUserInfo)){
            return R.fail("ELECTRICITY.0019","未找到用户");
        }
        UserInfo userInfo=new UserInfo();
        userInfo.setUpdateTime(System.currentTimeMillis());
        userInfo.setUsableStatus(UserInfo.USER_USABLE_STATUS);
        userInfoMapper.update(userInfo);
        return R.ok();
    }
}