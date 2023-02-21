package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.FranchiseeMoveRecord;

import java.util.List;

/**
 * 加盟商迁移记录(FranchiseeMoveRecord)表服务接口
 *
 * @author zzlong
 * @since 2023-02-07 17:12:52
 */
public interface FranchiseeMoveRecordService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    FranchiseeMoveRecord selectByIdFromDB(Integer id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    FranchiseeMoveRecord selectByIdFromCache(Integer id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<FranchiseeMoveRecord> selectByPage(int offset, int limit);

    /**
     * 新增数据
     *
     * @param franchiseeMoveRecord 实例对象
     * @return 实例对象
     */
    FranchiseeMoveRecord insert(FranchiseeMoveRecord franchiseeMoveRecord);

    /**
     * 修改数据
     *
     * @param franchiseeMoveRecord 实例对象
     * @return 实例对象
     */
    Integer update(FranchiseeMoveRecord franchiseeMoveRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Integer id);

}
