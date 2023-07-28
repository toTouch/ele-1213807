package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.CarModelTag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 车辆型号标签表(CarModelTag)表数据库访问层
 *
 * @author zzlong
 * @since 2022-12-14 15:55:53
 */
public interface CarModelTagMapper extends BaseMapper<CarModelTag> {

    /**
     * 根据车辆型号ID集获取对应标签集
     *
     * @param carModelIdList 车辆型号ID集
     * @return 标签集
     */
    List<CarModelTag> selectByCarModelIds(@Param("carModelIdList") List<Integer> carModelIdList);

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    CarModelTag selectById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<CarModelTag> selectByPage(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param carModelTag 实例对象
     * @return 对象列表
     */
    List<CarModelTag> selectByQuery(CarModelTag carModelTag);

    /**
     * 新增数据
     *
     * @param carModelTag 实例对象
     * @return 影响行数
     */
    int insertOne(CarModelTag carModelTag);

    /**
     * 修改数据
     *
     * @param carModelTag 实例对象
     * @return 影响行数
     */
    int update(CarModelTag carModelTag);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    Integer batchInsert(List<CarModelTag> carModelTagList);

    Integer deleteByCarModelId(@Param("carModelId") long carModelId);
}
