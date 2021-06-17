package com.xiliulou.electricity.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户绑定列表(FranchiseeUserInfo)实体类
 *
 * @author Eclair
 * @since 2021-06-17 10:10:13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_franchisee_user_info")
public class FranchiseeUserInfo {

    private Long id;

    private Long userInfoId;
    /**
    * 服务状态 (2--已缴纳押金，3--已租电池)
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
    * 套餐名称
    */
    private String cardName;
    /**
    * 类型(0:月卡,1:季卡,2:年卡)
    */
    private Integer cardType;
    /**
    * 月卡过期时间
    */
    private Long memberCardExpireTime;
    /**
    * 月卡剩余次数
    */
    private Integer remainingNumber;
    /**
    * 初始电池编号
    */
    private String initElectricityBatterySn;
    /**
    * 当前电池编号
    */
    private String nowElectricityBatterySn;
    /**
    * 租电池押金
    */
    private Double batteryDeposit;
    /**
    * 租电池订单编号
    */
    private String orderId;
    /**
    * 0--正常 1--删除
    */
    private Integer delFlag;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 更新时间
    */
    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
