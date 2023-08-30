package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.UserExtra;
import com.xiliulou.electricity.query.UpdateUserSourceQuery;
import com.xiliulou.electricity.query.UserSourceQuery;
import org.apache.commons.lang3.tuple.Triple;

/**
 * (UserExtra)表服务接口
 *
 * @author zzlong
 * @since 2023-07-03 15:08:23
 */
public interface UserExtraService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserExtra queryByIdFromDB(Long uid);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    UserExtra queryByIdFromCache(Long uid);

    /**
     * 新增数据
     *
     * @param userExtra 实例对象
     * @return 实例对象
     */
    UserExtra insert(UserExtra userExtra);

    /**
     * 修改数据
     *
     * @param userExtra 实例对象
     * @return 实例对象
     */
    Integer update(UserExtra userExtra);

    /**
     * 通过主键删除数据
     *
     * @param uid 主键
     * @return 是否成功
     */
    Integer deleteById(Long uid);

    void loginCallBack(UserSourceQuery query);

    Triple<Boolean, String, Object> updateUserSource(UpdateUserSourceQuery userSourceQuery);
}
