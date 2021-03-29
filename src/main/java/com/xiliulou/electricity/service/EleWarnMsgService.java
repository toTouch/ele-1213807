package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.EleWarnMsg;

/**
 * 换电柜异常上报信息(TEleWarnMsg)表服务接口
 *
 * @author makejava
 * @since 2021-03-29 14:12:45
 */
public interface EleWarnMsgService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    EleWarnMsg queryByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    EleWarnMsg queryByIdFromCache(Long id);

    /**
     * 新增数据
     *
     * @param eleWarnMsg 实例对象
     * @return 实例对象
     */
    EleWarnMsg insert(EleWarnMsg eleWarnMsg);

    /**
     * 修改数据
     *
     * @param eleWarnMsg 实例对象
     * @return 实例对象
     */
    Integer update(EleWarnMsg eleWarnMsg);


}