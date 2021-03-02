package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.EleRefundOrder;
import java.util.List;

import com.xiliulou.electricity.query.EleRefundQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 退款订单表(TEleRefundOrder)表数据库访问层
 *
 * @author makejava
 * @since 2021-02-22 10:17:06
 */
public interface EleRefundOrderMapper extends BaseMapper<EleRefundOrder>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    EleRefundOrder queryById(Long id);

    /**
     * 查询指定行数据
     *
     */
    IPage queryList(Page page, @Param("query") EleRefundQuery eleRefundQuery);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param eleRefundOrder 实例对象
     * @return 对象列表
     */
    List<EleRefundOrder> queryAll(EleRefundOrder eleRefundOrder);

    /**
     * 新增数据
     *
     * @param eleRefundOrder 实例对象
     * @return 影响行数
     */
    int insertOne(EleRefundOrder eleRefundOrder);

    /**
     * 修改数据
     *
     * @param eleRefundOrder 实例对象
     * @return 影响行数
     */
    int update(EleRefundOrder eleRefundOrder);

}