package com.xiliulou.electricity.vo.userinfo;

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
}
