package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.Region;

import java.util.List;

/**
 * (Region)表服务接口
 *
 * @author zzlong
 * @since 2022-12-12 11:38:20
 */
public interface RegionService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    Region selectByIdFromDB(Integer id);

    /**
     * 通过code查询单条数据
     *
     * @param code
     * @return 实例对象
     */
    Region selectByCodeFromCache(String code);

    /**
     * 通过code查询单条数据
     *
     * @param code
     * @return 实例对象
     */
    Region selectByCode(String code);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<Region> selectByPage(int offset, int limit);

    Region selectByIdFromCache(Integer regionId);

    List<Region> queryRegionListByPid(Integer pid);

    List<Region> selectByRids(List<Integer> rids);
}
