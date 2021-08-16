package com.xiliulou.electricity.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户迁移历史记录(UserMoveHistory)实体类
 *
 * @author Eclair
 * @since 2021-08-16 09:26:11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_move_history")
public class UserMoveHistory {

    private Long id;

    private Long uid;
    /**
    * 手机号
    */
    private String phone;
    /**
    * 真实姓名
    */
    private String name;
    /**
    * 身份证号
    */
    private String idNumber;
    /**
    * 服务状态 (0--初始化,1--已实名认证，2--已缴纳押金，3--已租电池)
    */
    private Integer serviceStatus;
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
    private Double batteryDeposit;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 租户id
    */
    private Integer tenantId;

    /**
     * 加盟商id
     */
    private Integer franchiseeId;


}
