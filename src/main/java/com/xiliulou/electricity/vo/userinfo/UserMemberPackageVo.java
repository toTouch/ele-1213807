package com.xiliulou.electricity.vo.userinfo;

import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户套餐信息
 *
 * @author xiaohui.song
 **/
@Data
public class UserMemberPackageVo implements Serializable {

    private static final long serialVersionUID = -2496503959219267500L;

    /**
     * 加盟商ID
     */
    private Integer franchiseeId;

    /**
     * 单电套餐会员期限信息
     */
    private UserMemberBatteryPackageVo batteryPackage;

    /**
     * 单车套餐会员期限信息
     */
    private UserMemberCarPackageVo carPackage;

    /**
     * 车电一体单车套餐会员期限信息
     */
    private UserMemberCarBatteryPackageVo carBatteryPackage;
    
    /**
     * 续费状态 自主续费状态 0:不自主续费, 1:自主续费
     * @see RenewalStatusEnum
     */
    private Integer renewalStatus;
    
}
