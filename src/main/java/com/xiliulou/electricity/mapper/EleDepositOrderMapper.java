package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.EleDepositOrder;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 缴纳押金订单表(TEleDepositOrder)表数据库访问层
 *
 * @author makejava
 * @since 2021-02-22 10:16:44
 */
public interface EleDepositOrderMapper extends BaseMapper<EleDepositOrder>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    EleDepositOrder queryById(Long id);

    /**
     * 查询指定行数据
     *
     */
    List<EleDepositOrder> queryList(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param eleDepositOrder 实例对象
     * @return 对象列表
     */
    List<EleDepositOrder> queryAll(EleDepositOrder eleDepositOrder);

    /**
     * 新增数据
     *
     * @param eleDepositOrder 实例对象
     * @return 影响行数
     */
    int insertOne(EleDepositOrder eleDepositOrder);

    /**
     * 修改数据
     *
     * @param eleDepositOrder 实例对象
     * @return 影响行数
     */
    int update(EleDepositOrder eleDepositOrder);

}