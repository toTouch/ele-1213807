package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.EleUserAuth;

/**
 * 实名认证信息(TEleUserAuth)表服务接口
 *
 * @author makejava
 * @since 2021-02-20 13:37:38
 */
public interface EleUserAuthService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    EleUserAuth queryByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    EleUserAuth queryByIdFromCache(Long id);

    /**
     * 新增数据
     *
     * @param eleUserAuth 实例对象
     * @return 实例对象
     */
    EleUserAuth insert(EleUserAuth eleUserAuth);

    /**
     * 修改数据
     *
     * @param eleUserAuth 实例对象
     * @return 实例对象
     */
    Integer update(EleUserAuth eleUserAuth);


}