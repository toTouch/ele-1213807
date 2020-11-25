package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ElectricityCabinet;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 换电柜表(TElectricityCabinet)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
public interface ElectricityCabinetMapper extends BaseMapper<ElectricityCabinet>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinet queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ElectricityCabinet> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param electricityCabinet 实例对象
     * @return 对象列表
     */
    List<ElectricityCabinet> queryAll(ElectricityCabinet electricityCabinet);

    /**
     * 新增数据
     *
     * @param electricityCabinet 实例对象
     * @return 影响行数
     */
    int insertOne(ElectricityCabinet electricityCabinet);

    /**
     * 修改数据
     *
     * @param electricityCabinet 实例对象
     * @return 影响行数
     */
    int update(ElectricityCabinet electricityCabinet);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

}