package com.xiliulou.electricity.mapper;

import java.util.List;

import com.xiliulou.electricity.entity.CarLockCtrlHistory;
import com.xiliulou.electricity.vo.CarLockCtrlHistoryVo;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (CarLockCtrlHistory)表数据库访问层
 *
 * @author Hardy
 * @since 2023-04-04 16:22:27
 */
public interface CarLockCtrlHistoryMapper extends BaseMapper<CarLockCtrlHistory> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    CarLockCtrlHistory queryById(Long id);
    
    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<CarLockCtrlHistory> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);
    
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param carLockCtrlHistory 实例对象
     * @return 对象列表
     */
    List<CarLockCtrlHistory> queryAll(CarLockCtrlHistory carLockCtrlHistory);
    
    /**
     * 新增数据
     *
     * @param carLockCtrlHistory 实例对象
     * @return 影响行数
     */
    int insertOne(CarLockCtrlHistory carLockCtrlHistory);
    
    /**
     * 修改数据
     *
     * @param carLockCtrlHistory 实例对象
     * @return 影响行数
     */
    int update(CarLockCtrlHistory carLockCtrlHistory);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    List<CarLockCtrlHistoryVo> queryList(@Param("offset") Long offset, @Param("size") Long size,
            @Param("name") String name, @Param("phone") String phone, @Param("carSn") String carSn,
            @Param("tenantId") Integer tenantId);
    
    Integer queryCount(@Param("name") String name, @Param("phone") String phone, @Param("carSn") String carSn,
            @Param("tenantId") Integer tenantId);
}
