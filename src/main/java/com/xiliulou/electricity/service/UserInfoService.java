package com.xiliulou.electricity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.UserInfoBatteryAddAndUpdate;
import com.xiliulou.electricity.query.UserInfoQuery;

import java.util.HashMap;
import java.util.List;

/**
 * 用户列表(TUserInfo)表服务接口
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
public interface UserInfoService extends IService<UserInfo> {

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
    UserInfo selectUserByUid(Long id);

    /**
     * 新增数据
     *
     * @param userInfo 实例对象
     * @return 实例对象
     */
    Integer insert(UserInfo userInfo);

    /**
     * 修改数据
     *
     * @param userInfo 实例对象
     * @return 实例对象
     */
    Integer update(UserInfo userInfo);

    R webBindBattery(UserInfoBatteryAddAndUpdate userInfoBatteryAddAndUpdate);

    R queryList(UserInfoQuery userInfoQuery);

    R updateStatus(Long id,Integer usableStatus);

    R webUnBindBattery(Long id);

    UserInfo queryByUid(Long uid);

    Integer homeOne(Long first, Long now,Integer tenantId);

    List<HashMap<String, String>> homeThree(long startTimeMilliDay, Long endTimeMilliDay,Integer tenantId);

    R getMemberCardInfo(Long uid);

    R queryUserInfo();

    R verifyAuth(Long id,Integer authStatus);

    R updateAuth(UserInfo userInfo);

    R queryUserAuthInfo(UserInfoQuery userInfoQuery);

	R queryCount(UserInfoQuery userInfoQuery);
}
