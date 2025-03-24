package com.xiliulou.electricity.vo.userinfo;

import com.xiliulou.electricity.vo.DetailsBatteryInfoProVO;
import com.xiliulou.electricity.vo.EleBatteryServiceFeeVO;
import com.xiliulou.electricity.vo.EleDepositRefundVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 运维小程序换电会员列表
 * @date 2025/1/3 09:55:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEleInfoProVO {
    
    /**
     * 用户uid
     */
    private Long uid;
    
    /**
     * 用户姓名
     */
    private String name;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 套餐id
     */
    private Long memberCardId;
    
    /**
     * 换电套餐名称
     */
    private String memberCardName;
    
    /**
     * 套餐到期时间
     */
    private Long memberCardExpireTime;
    
    /**
     * 0:不限制,1:限制
     */
    private Integer limitCount;
    
    
    /**
     * 套餐总剩余次数
     */
    private Long remainingNumber;
    
    /**
     * 电池押金状态
     */
    private Integer batteryDepositStatus;
    
    /**
     * 是否免押:0-非免押 1-免押
     */
    private Integer freeDeposit;
    
    private EnterpriseChannelUserVO enterpriseChannelUserInfo;
    
    /**
     * 电池租赁状态
     */
    private Integer batteryRentStatus;
    
    /**
     * 可用状态（0-禁用，1-可用）
     */
    private Integer usableStatus;
    
    /**
     * 租金是否可退
     */
    private Boolean rentRefundFlag;
    
    UserBasicInfoEleProVO basicInfo;
    
    DetailsBatteryInfoProVO batteryInfo;
    
    /**
     * 滞纳金
     */
    private EleBatteryServiceFeeVO batteryServiceFee;
    
    /**
     * 退押
     */
    private EleDepositRefundVO eleDepositRefund;
    
    /**
     * 用户状态:0-正常,1-已删除, 2-已注销
     */
    private Integer userStatus;
    
    /**
     * 删除/注销时间
     */
    private Long delTime;


    /**
     * 是否展示线下履约，0否，1是
     */
    private Integer isViewOffLineAgree = 0;
}
