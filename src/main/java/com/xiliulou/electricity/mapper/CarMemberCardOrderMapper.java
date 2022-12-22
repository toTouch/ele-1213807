package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.CarMemberCardOrder;

import java.util.List;

import com.xiliulou.electricity.query.RentCarMemberCardOrderQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 租车套餐订单表(CarMemberCardOrder)表数据库访问层
 *
 * @author zzlong
 * @since 2022-12-21 09:47:25
 */
public interface CarMemberCardOrderMapper extends BaseMapper<CarMemberCardOrder> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    CarMemberCardOrder selectById(Long id);

    /**
     * 查询指定行数据
     *
     * @return 对象列表
     */
    List<CarMemberCardOrder> selectByPage(RentCarMemberCardOrderQuery memberCardOrderQuery);

    Integer selectByPageCount(RentCarMemberCardOrderQuery memberCardOrderQuery);

    /**
     * 通过实体作为筛选条件查询
     *
     * @param carMemberCardOrder 实例对象
     * @return 对象列表
     */
    List<CarMemberCardOrder> selectByQuery(CarMemberCardOrder carMemberCardOrder);

    /**
     * 新增数据
     *
     * @param carMemberCardOrder 实例对象
     * @return 影响行数
     */
    int insertOne(CarMemberCardOrder carMemberCardOrder);

    /**
     * 修改数据
     *
     * @param carMemberCardOrder 实例对象
     * @return 影响行数
     */
    int update(CarMemberCardOrder carMemberCardOrder);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

}
