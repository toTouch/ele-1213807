package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.EleWarnMsg;
import java.util.List;

import com.xiliulou.electricity.query.EleWarnMsgQuery;
import com.xiliulou.electricity.query.ElectricityCabinetOrderQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 换电柜异常上报信息(TEleWarnMsg)表数据库访问层
 *
 * @author makejava
 * @since 2021-03-29 14:12:45
 */
public interface EleWarnMsgMapper extends BaseMapper<EleWarnMsg>{


    /**
     * 查询指定行数据
     *
     */
    List<EleWarnMsg> queryList(@Param("query") EleWarnMsgQuery eleWarnMsgQuery);


    Integer queryCount(@Param("query")EleWarnMsgQuery eleWarnMsgQuery);
}
