package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.RentBatteryOrder;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 租电池记录(TRentBatteryOrder)表数据库访问层
 *
 * @author makejava
 * @since 2020-12-08 15:08:47
 */
public interface RentBatteryOrderMapper extends BaseMapper<RentBatteryOrder>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    RentBatteryOrder queryById(Long id);

    /**
     * 查询指定行数据
     *
     */
    List<RentBatteryOrder> queryList(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param rentBatteryOrder 实例对象
     * @return 对象列表
     */
    List<RentBatteryOrder> queryAll(RentBatteryOrder rentBatteryOrder);

    /**
     * 新增数据
     *
     * @param rentBatteryOrder 实例对象
     * @return 影响行数
     */
    int insertOne(RentBatteryOrder rentBatteryOrder);

    /**
     * 修改数据
     *
     * @param rentBatteryOrder 实例对象
     * @return 影响行数
     */
    int update(RentBatteryOrder rentBatteryOrder);

}