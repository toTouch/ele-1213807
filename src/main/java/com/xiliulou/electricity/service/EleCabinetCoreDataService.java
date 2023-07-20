package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.EleCabinetCoreData;
import com.xiliulou.electricity.query.EleCabinetCoreDataQuery;

import java.util.List;

/**
 * 柜机核心板上报数据(EleCabinetCoreData)表服务接口
 *
 * @author zzlong
 * @since 2022-07-06 14:20:37
 */
public interface EleCabinetCoreDataService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    EleCabinetCoreData queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    EleCabinetCoreData queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<EleCabinetCoreData> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param eleCabinetCoreData 实例对象
     * @return 实例对象
     */
    EleCabinetCoreData insert(EleCabinetCoreData eleCabinetCoreData);

    /**
     * 修改数据
     *
     * @param eleCabinetCoreData 实例对象
     * @return 实例对象
     */
    Integer update(EleCabinetCoreData eleCabinetCoreData);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    /**
     * 原子更新
     * @param cabinetCoreData
     * @return
     */
    int idempotentUpdateCabinetCoreData(EleCabinetCoreData cabinetCoreData);

    List<EleCabinetCoreData> selectListByQuery(EleCabinetCoreDataQuery eleCabinetCoreDataQuery);

    @Deprecated
    EleCabinetCoreData selectByEleCabinetId(Integer id);

    EleCabinetCoreData selectByEid(Integer id);
}
