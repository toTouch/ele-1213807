package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (UserBatteryMemberCardPackage)实体类
 *
 * @author Eclair
 * @since 2023-07-12 14:44:01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_battery_member_card_package")
public class UserBatteryMemberCardPackage {
    /**
     * 主键
     */
    private Long id;
    /**
     * 用户id
     */
    private Long uid;
    /**
     * 套餐id
     */
    private Long memberCardId;
    /**
     * 套餐订单Id
     */
    private String orderId;

    private Integer tenantId;

    private Long createTime;

    private Long updateTime;
    /**
     * 套餐有效时间
     */
    private Long memberCardExpireTime;
    /**
     * 套餐剩余次数
     */
    private Long remainingNumber;

//    private Integer status;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
