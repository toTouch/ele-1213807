package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.UserDataScope;

import java.util.List;

/**
 * (UserDataScope)表服务接口
 *
 * @author zzlong
 * @since 2022-09-19 14:22:34
 */
public interface UserDataScopeService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    UserDataScope selectByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    UserDataScope selectByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<UserDataScope> selectByPage(int offset, int limit);

    /**
     * 新增数据
     *
     * @param userDataScope 实例对象
     * @return 实例对象
     */
    UserDataScope insert(UserDataScope userDataScope);

    /**
     * 修改数据
     *
     * @param userDataScope 实例对象
     * @return 实例对象
     */
    Integer update(UserDataScope userDataScope);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    Integer batchInsert(List<UserDataScope> userDataScopes);

    Integer deleteByUid(Long uid);

    List<UserDataScope> selectByUid(Long uid);

    List<Long> selectDataIdByUid(Long uid);
}
