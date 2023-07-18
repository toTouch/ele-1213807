package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 用户停卡绑定(TServiceFeeUserInfo)实体类
 *
 * @author makejava
 * @since 2022-11-17 16:00:45
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_service_fee_user_info")
public class ServiceFeeUserInfo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 停卡单号
     */
    private String disableMemberCardNo;

    /**
     * 是否存在电池服务费 (1--存在电池服务费)
     */
//    private Integer existBatteryServiceFee;

    /**
     * 用户Id
     */
    private Long uid;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;
    //租户id
    private Integer tenantId;

    /**
     * 加盟商Id
     */
    private Long franchiseeId;

    private Integer delFlag;

    /**
     * 服务费产生时间
     */
    private Long serviceFeeGenerateTime;


    public static final Integer NOT_EXIST_SERVICE_FEE = 0;
    public static final Integer EXIST_SERVICE_FEE = 1;


    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
