package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.RentCarOrder;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 租车记录(TRentCarOrder)表数据库访问层
 *
 * @author makejava
 * @since 2020-12-08 15:09:08
 */
public interface RentCarOrderMapper extends BaseMapper<RentCarOrder>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    RentCarOrder queryById(Long id);

    /**
     * 查询指定行数据
     *
     */
    List<RentCarOrder> queryList(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param rentCarOrder 实例对象
     * @return 对象列表
     */
    List<RentCarOrder> queryAll(RentCarOrder rentCarOrder);

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

}