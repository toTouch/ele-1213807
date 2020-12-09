package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.UserInfoBatteryAddAndUpdate;
import com.xiliulou.electricity.query.UserInfoCarAddAndUpdate;
import com.xiliulou.electricity.query.UserInfoQuery;

/**
 * 用户列表(TUserInfo)表服务接口
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
public interface UserInfoService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    UserInfo queryByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    UserInfo queryByIdFromCache(Long id);

    /**
     * 新增数据
     *
     * @param userInfo 实例对象
     * @return 实例对象
     */
    UserInfo insert(UserInfo userInfo);

    /**
     * 修改数据
     *
     * @param userInfo 实例对象
     * @return 实例对象
     */
    Integer update(UserInfo userInfo);


    R bindBattery(UserInfoBatteryAddAndUpdate userInfoBatteryAddAndUpdate);

    R bindCar(UserInfoCarAddAndUpdate userInfoCarAddAndUpdate);

    R queryList(UserInfoQuery userInfoQuery);

    R disable(Long id);

    R reboot(Long id);

    R unBindBattery(Long id);

    R unBindCar(Long id);

    UserInfo queryByUid(Long uid);

    Integer homeOneTotal(Long first, Long now);

    Integer homeOneService(Long first, Long now);

    Integer homeOneMemberCar(Long first, Long now);
}