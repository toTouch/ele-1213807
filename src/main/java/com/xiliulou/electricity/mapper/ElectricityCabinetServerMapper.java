package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ElectricityCabinetServer;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (ElectricityCabinetServer)表数据库访问层
 *
 * @author zgw
 * @since 2022-09-26 11:40:33
 */
public interface ElectricityCabinetServerMapper extends BaseMapper<ElectricityCabinetServer> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetServer queryById(@Param("id") Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<ElectricityCabinetServer> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 通过实体作为筛选条件查询
     *
     * @param electricityCabinetServer 实例对象
     * @return 对象列表
     */
    List<ElectricityCabinetServer> queryAll(ElectricityCabinetServer electricityCabinetServer);

    /**
     * 新增数据
     *
     * @param electricityCabinetServer 实例对象
     * @return 影响行数
     */
    int insertOne(ElectricityCabinetServer electricityCabinetServer);

    /**
     * 修改数据
     *
     * @param electricityCabinetServer 实例对象
     * @return 影响行数
     */
    int update(ElectricityCabinetServer electricityCabinetServer);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    List<ElectricityCabinetServer> queryList(@Param("eleName") String eleName, @Param("deviceName") String deviceName,
        @Param("tenantName") String tenantName, @Param("serverTimeStart") Long beginServerTimeStart,
        @Param("serverTimeEnd") Long beginServerTimeEnd, @Param("offset") Long offset, @Param("size") Long size);

    Long queryCount(@Param("eleName") String eleName, @Param("deviceName") String deviceName,
        @Param("tenantName") String tenantName, @Param("serverTimeStart") Long beginServerTimeStart,
        @Param("serverTimeEnd") Long beginServerTimeEnd);

    ElectricityCabinetServer queryByProductKeyAndDeviceName(@Param("productKey") String productKey,
        @Param("deviceName") String deviceName);

    ElectricityCabinetServer selectByEid(@Param("eid") Integer id);

    Integer deleteByEid(@Param("eid") Integer eid);
    
    Integer updateByEid(ElectricityCabinetServer electricityCabinetServer);
}
