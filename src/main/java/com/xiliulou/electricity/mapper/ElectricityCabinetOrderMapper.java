package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.query.ElectricityCabinetOrderQuery;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
 * 订单表(TElectricityCabinetOrder)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
public interface ElectricityCabinetOrderMapper extends BaseMapper<ElectricityCabinetOrder> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetOrder queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @return 对象列表
     */
    IPage queryList(Page page, @Param("query") ElectricityCabinetOrderQuery electricityCabinetOrderQuery);


    /**
     * 修改数据
     *
     * @param electricityCabinetOrder 实例对象
     * @return 影响行数
     */
    int update(ElectricityCabinetOrder electricityCabinetOrder);


    List<HashMap<String, String>> homeThree(@Param("startTimeMilliDay") long startTimeMilliDay, @Param("endTimeMilliDay") Long endTimeMilliDay);

    int updateExpiredCancelOrder(@Param("orderId") String orderId, @Param("updateTime") Long updateTime);

    Integer homeOneCount(@Param("first") Long first, @Param("now") Long now, @Param("eleIdList") List<Integer> eleIdList);

    Integer homeOneSuccess(@Param("first") Long first, @Param("now") Long now, @Param("eleIdList") List<Integer> eleIdList);
}