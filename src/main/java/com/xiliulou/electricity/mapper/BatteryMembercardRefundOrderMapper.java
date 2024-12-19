package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.query.BatteryMembercardRefundOrderQuery;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

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
     * 根据更换手机号
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    Integer updatePhoneByUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("newPhone") String newPhone);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    Integer selectPageCount(BatteryMembercardRefundOrderQuery query);

    List<BatteryMembercardRefundOrder> selectByPage(BatteryMembercardRefundOrderQuery query);

    BigDecimal selectUserTotalRefund(@Param("tenantId") Integer tenantId, @Param("uid") Long uid);
    
    List<BatteryMembercardRefundOrder> selectListRefundingOrderByMemberCardOrderNoAndStatus(@Param("memberCardOrderNo") String memberCardOrderNo, @Param("statuses") List<Integer> statuses);
}
