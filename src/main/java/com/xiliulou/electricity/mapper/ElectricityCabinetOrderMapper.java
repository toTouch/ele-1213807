package com.xiliulou.electricity.mapper;

import java.util.HashMap;
import java.util.List;

import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.query.ElectricityCabinetOrderQuery;
import com.xiliulou.electricity.vo.ElectricityCabinetOrderVO;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 订单表(TElectricityCabinetOrder)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
public interface ElectricityCabinetOrderMapper extends BaseMapper<ElectricityCabinetOrder>{

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
    List<ElectricityCabinetOrderVO> queryList(@Param("query") ElectricityCabinetOrderQuery electricityCabinetOrderQuery);


    /**
     * 修改数据
     *
     * @param electricityCabinetOrder 实例对象
     * @return 影响行数
     */
    int update(ElectricityCabinetOrder electricityCabinetOrder);


    List<HashMap<String, String>> homeThree(@Param("startTimeMilliDay") long startTimeMilliDay, @Param("endTimeMilliDay") Long endTimeMilliDay);

    int updateExpiredCancelOrder(@Param("orderId")String orderId,@Param("updateTime") Long updateTime);
}