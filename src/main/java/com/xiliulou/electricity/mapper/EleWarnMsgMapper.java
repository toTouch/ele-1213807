package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.EleWarnMsg;

import java.util.List;
import java.util.Map;

import com.xiliulou.electricity.query.EleWarnMsgQuery;
import com.xiliulou.electricity.query.ElectricityCabinetOrderQuery;
import com.xiliulou.electricity.vo.EleWarnMsgVo;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 换电柜异常上报信息(TEleWarnMsg)表数据库访问层
 *
 * @author makejava
 * @since 2021-03-29 14:12:45
 */
public interface EleWarnMsgMapper extends BaseMapper<EleWarnMsg> {


    /**
     * 查询指定行数据
     */
    List<EleWarnMsg> queryList(@Param("query") EleWarnMsgQuery eleWarnMsgQuery);

    List<EleWarnMsgVo> queryAllTenantList(@Param("query") EleWarnMsgQuery eleWarnMsgQuery);

    Integer queryAllTenantCount();

    Integer queryCount(@Param("query") EleWarnMsgQuery eleWarnMsgQuery);

    List<Map<String, Object>> queryStatisticsEleWarmMsg(@Param("query") EleWarnMsgQuery eleWarnMsgQuery);

    List<Map<String,Object>> queryStatisticEleWarnMsgByElectricityCabinet(@Param("query") EleWarnMsgQuery eleWarnMsgQuery);

    List<EleWarnMsgVo> queryStatisticEleWarnMsgRanking(@Param("query") EleWarnMsgQuery eleWarnMsgQuery);

    List<EleWarnMsgVo> queryStatisticEleWarnMsgRankingByElectricityCabinetId(@Param("query") EleWarnMsgQuery eleWarnMsgQuery);

    EleWarnMsgVo queryStatisticEleWarnMsgForTenant(@Param("electricityCabinetId") Integer electricityCabinetId);

    Integer queryStatisticEleWarnMsgRankingCount();

    List<Integer> selectEidByCellFailureList(List<Integer> failureList);

    List<Integer> selectEidByCabinetFailureList(List<Integer> failureList);
}
