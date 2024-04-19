package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.ChannelEmployeeAmount;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * @author BaoYu
 * @description:
 * @date 2024/1/31 20:36
 */
public interface ChannelEmployeeAmountMapper {
    
    ChannelEmployeeAmount selectById(@Param("id") Long id);
    
    ChannelEmployeeAmount selectByUid(@Param("uid") Long uid, @Param("tenantId") Long tenantId);
    
    Integer updateOne(ChannelEmployeeAmount channelEmployeeAmount);
    
    Integer insertOne(ChannelEmployeeAmount channelEmployeeAmount);
    
    Integer addAmountByUid(@Param("income") BigDecimal income, @Param("uid") Long uid, @Param("tenantId") Long tenantId, @Param("updateTime") Long updateTime);
    
    Integer reduceAmountByUid(@Param("income") BigDecimal income, @Param("uid") Long uid, @Param("tenantId") Long tenantId, @Param("updateTime") Long updateTime);

}
