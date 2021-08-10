package com.xiliulou.electricity.query;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 用户绑定列表(TUserInfo)实体类
 *
 * @author makejava
 * @since 2020-12-08 14:17:59
 */
@Data
public class UserMoveQuery {

    /**
     * 手机号
     */
    private String phone;
    /**
     * 用户姓名
     */
    private String name;
    /**
     * 身份证号
     */
    private String idNumber;
    /**
     * 服务状态 (0--初始化,1--已实名认证,2--已缴纳押金，3--已租电池)
     */
    private Integer serviceStatus;
    /**
     * 加盟商id
     */
    private Integer franchiseeId;
    /**
     * 套餐id
     */
    private Integer cardId;
    /**
     * 月卡过期时间
     */
    private Long memberCardExpireTime;
    /**
     * 月卡剩余次数
     */
    private Long remainingNumber;
    /**
     * 租电池押金
     */
    private BigDecimal batteryDeposit;
    /**
     * 押金订单编号
     */
    private String orderId;
}
