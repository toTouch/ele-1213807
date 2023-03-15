package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.BatteryGeo;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (BatteryGeo)表数据库访问层
 *
 * @author makejava
 * @since 2023-03-03 08:54:46
 */
public interface BatteryGeoMapper extends BaseMapper<BatteryGeo> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryGeo queryById(Long id);
    
    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<BatteryGeo> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);
    
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param batteryGeo 实例对象
     * @return 对象列表
     */
    List<BatteryGeo> queryAll(BatteryGeo batteryGeo);
    
    /**
     * 新增数据
     *
     * @param batteryGeo 实例对象
     * @return 影响行数
     */
    int insertOrUpdate(BatteryGeo batteryGeo);
    
    /**
     * 修改数据
     *
     * @param batteryGeo 实例对象
     * @return 影响行数
     */
    int update(BatteryGeo batteryGeo);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    
    List<BatteryGeo> queryAllList(@Param("franchiseeIds") List<Long> franchiseeIds,
            @Param("list") List<String> locationsHashList, @Param("tenantId") Integer tenantId,
            @Param("lat") Double lat, @Param("lon") Double lon, @Param("size") Long size);
    
    
    int deleteBySn(@Param("sn") String sn);
}
