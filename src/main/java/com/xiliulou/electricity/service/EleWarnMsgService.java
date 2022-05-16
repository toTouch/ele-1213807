package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleWarnMsg;
import com.xiliulou.electricity.query.EleWarnMsgQuery;

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


    R queryList(EleWarnMsgQuery eleWarnMsgQuery);

    R queryAllTenant(EleWarnMsgQuery EleWarnMsgQuery);

    R queryAllTenantCount();

    R queryCount(EleWarnMsgQuery eleWarnMsgQuery);

	void delete(Long id);

	R queryStatisticsEleWarmMsg(EleWarnMsgQuery eleWarnMsgQuery);

	R queryStatisticEleWarnMsgByElectricityCabinet(EleWarnMsgQuery eleWarnMsgQuery);

	R queryStatisticEleWarnMsgRanking(EleWarnMsgQuery eleWarnMsgQuery);
}
