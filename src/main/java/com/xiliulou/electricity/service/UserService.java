package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.BindElectricityBatteryQuery;
import com.xiliulou.electricity.query.BindElectricityCabinetQuery;
import com.xiliulou.electricity.query.BindStoreQuery;
import com.xiliulou.electricity.web.query.AdminUserQuery;
import com.xiliulou.electricity.web.query.PasswordQuery;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * (User)表服务接口
 *
 * @author makejava
 * @since 2020-11-27 11:19:51
 */
public interface UserService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param uid 主键
     * @return 实例对象
     */
    User queryByIdFromDB(Long uid);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    User queryByUidFromCache(Long uid);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<User> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param user 实例对象
     * @return 实例对象
     */
    User insert(User user);

    /**
     * 修改数据
     * oldUser必须包括手机号和uid
     *
     * @return 实例对象
     */
    Integer updateUser(User updateUser, User oldUser);

    /**
     * 通过主键删除数据
     *
     * @param uid 主键
     * @return 是否成功
     */
    Boolean deleteById(Long uid);

    User queryByUserName(String username);

    Triple<Boolean, String, Object> addAdminUser(AdminUserQuery adminUserQuery);

    User queryByUserPhone(String phone, Integer type);

    Pair<Boolean, Object> queryListUser(Long uid, Long size, Long offset, String name, String phone, Integer type, Long startTime, Long endTime);

    Pair<Boolean, Object> updateAdminUser(AdminUserQuery adminUserQuery);

    Pair<Boolean, Object> deleteAdminUser(Long uid);

    Triple<Boolean, String, Object> updatePassword(PasswordQuery passwordQuery);

	Pair<Boolean, Object> addUserAddress(String cityCode);

    Pair<Boolean, Object> getUserDetail();


    R bindElectricityCabinet(BindElectricityCabinetQuery bindElectricityCabinetQuery);

    R queryElectricityCabinetList(Long uid);
}