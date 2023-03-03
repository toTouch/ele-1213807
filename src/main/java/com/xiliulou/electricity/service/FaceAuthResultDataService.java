package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.FaceAuthResultData;

import java.util.List;

/**
 * (FaceAuthResultData)表服务接口
 *
 * @author zzlong
 * @since 2023-02-03 11:03:24
 */
public interface FaceAuthResultDataService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    FaceAuthResultData selectByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    FaceAuthResultData selectByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<FaceAuthResultData> selectByPage(int offset, int limit);

    /**
     * 新增数据
     *
     * @param faceAuthResultData 实例对象
     * @return 实例对象
     */
    FaceAuthResultData insert(FaceAuthResultData faceAuthResultData);

    /**
     * 修改数据
     *
     * @param faceAuthResultData 实例对象
     * @return 实例对象
     */
    Integer update(FaceAuthResultData faceAuthResultData);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

}
