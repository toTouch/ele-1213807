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
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    EleWarnMsg queryById(Long id);

    /**
     * 查询指定行数据
     *
     */
    IPage queryList(Page page, @Param("query") EleWarnMsgQuery eleWarnMsgQuery);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param eleWarnMsg 实例对象
     * @return 对象列表
     */
    List<EleWarnMsg> queryAll(EleWarnMsg eleWarnMsg);

    /**
     * 新增数据
     *
     * @param eleWarnMsg 实例对象
     * @return 影响行数
     */
    int insertOne(EleWarnMsg eleWarnMsg);

    /**
     * 修改数据
     *
     * @param eleWarnMsg 实例对象
     * @return 影响行数
     */
    int update(EleWarnMsg eleWarnMsg);

}