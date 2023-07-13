package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import java.util.List;

import com.xiliulou.electricity.query.BatteryMembercardRefundOrderQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (BatteryMembercardRefundOrder)表数据库访问层
 *
 * @author zzlong
 * @since 2023-07-12 15:56:43
 */
public interface BatteryMembercardRefundOrderMapper  extends BaseMapper<BatteryMembercardRefundOrder>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryMembercardRefundOrder queryById(Long id);

    /**
     * 修改数据
     *
     * @param batteryMembercardRefundOrder 实例对象
     * @return 影响行数
     */
    int update(BatteryMembercardRefundOrder batteryMembercardRefundOrder);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    Integer selectPageCount(BatteryMembercardRefundOrderQuery query);

    List<BatteryMembercardRefundOrder> selectByPage(BatteryMembercardRefundOrderQuery query);
}
