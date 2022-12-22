package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.CarModelTag;

import java.util.List;

/**
 * 车辆型号标签表(CarModelTag)表服务接口
 *
 * @author zzlong
 * @since 2022-12-14 15:55:53
 */
public interface CarModelTagService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    CarModelTag selectByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    CarModelTag selectByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<CarModelTag> selectByPage(int offset, int limit);

    /**
     * 新增数据
     *
     * @param carModelTag 实例对象
     * @return 实例对象
     */
    CarModelTag insert(CarModelTag carModelTag);

    /**
     * 修改数据
     *
     * @param carModelTag 实例对象
     * @return 实例对象
     */
    Integer update(CarModelTag carModelTag);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    Integer batchInsert(List<CarModelTag> buildCarModelTagList);

    Integer deleteByCarModelId(long carModelId);

    List<CarModelTag> selectByCarModelId(Integer id);
}
