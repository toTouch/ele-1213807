package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.RentCarOrder;

import java.util.List;

import com.xiliulou.electricity.query.RentCarOrderQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 租车订单表(RentCarOrder)表数据库访问层
 *
 * @author zzlong
 * @since 2022-12-21 09:47:57
 */
public interface RentCarOrderMapper extends BaseMapper<RentCarOrder> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    RentCarOrder selectById(Long id);

    /**
     * 查询指定行数据
     * @return 对象列表
     */
    List<RentCarOrder> selectByPage(RentCarOrderQuery rentCarOrderQuery);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param rentCarOrder 实例对象
     * @return 对象列表
     */
    List<RentCarOrder> selectByQuery(RentCarOrder rentCarOrder);

    /**
     * 新增数据
     *
     * @param rentCarOrder 实例对象
     * @return 影响行数
     */
    int insertOne(RentCarOrder rentCarOrder);

    /**
     * 修改数据
     *
     * @param rentCarOrder 实例对象
     * @return 影响行数
     */
    int update(RentCarOrder rentCarOrder);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    Integer selectPageCount(RentCarOrderQuery rentCarOrderQuery);
}
