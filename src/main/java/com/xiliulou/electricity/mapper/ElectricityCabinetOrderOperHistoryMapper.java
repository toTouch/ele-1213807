package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 订单的操作历史记录(TElectricityCabinetOrderOperHistory)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-26 10:57:22
 */
public interface ElectricityCabinetOrderOperHistoryMapper extends BaseMapper<ElectricityCabinetOrderOperHistory>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetOrderOperHistory queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ElectricityCabinetOrderOperHistory> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param electricityCabinetOrderOperHistory 实例对象
     * @return 对象列表
     */
    List<ElectricityCabinetOrderOperHistory> queryAll(ElectricityCabinetOrderOperHistory electricityCabinetOrderOperHistory);

    /**
     * 新增数据
     *
     * @param electricityCabinetOrderOperHistory 实例对象
     * @return 影响行数
     */
    int insertOne(ElectricityCabinetOrderOperHistory electricityCabinetOrderOperHistory);

    /**
     * 修改数据
     *
     * @param electricityCabinetOrderOperHistory 实例对象
     * @return 影响行数
     */
    int update(ElectricityCabinetOrderOperHistory electricityCabinetOrderOperHistory);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

}