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
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    User queryByUidFromCache(Long uid);


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

    User queryByUserPhone(String phone, Integer type,Integer tenantId);

    Pair<Boolean, Object> queryListUser(Long uid, Long size, Long offset, String name, String phone, Integer type, Long startTime, Long endTime, Integer tenantId);

    Pair<Boolean, Object> updateAdminUser(AdminUserQuery adminUserQuery);

    Pair<Boolean, Object> deleteAdminUser(Long uid);

    Triple<Boolean, String, Object> updatePassword(PasswordQuery passwordQuery);

	Pair<Boolean, Object> addUserAddress(String cityCode);

    Pair<Boolean, Object> getUserDetail();

    R endLimitUser(Long uid);

    R addInnerUser(AdminUserQuery adminUserQuery);

    void deleteInnerUser(Long uid);

	Pair<Boolean, Object> queryCount(Long uid, String name, String phone, Integer type, Long startTime, Long endTime, Integer tenantId);

	String decryptPassword(String encryptPassword);

	List<User> queryByTenantIdAndType(Integer tenantId, Integer status);

    Triple<Boolean, String, Object> deleteNormalUser(Long uid);
}
