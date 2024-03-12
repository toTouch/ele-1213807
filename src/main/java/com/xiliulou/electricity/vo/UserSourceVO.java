package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-12-18:19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSourceVO {

    private Long uid;

    /**
     * 用户来源 1：扫码，2：邀请，3：其它
     */
    private Integer source;

    /**
     * 用户名字
     */
    private String name;

    /**
     * 手机
     */
    private String phone;

    /**
     * 柜机
     */
    private Integer electricityCabinetId;

    private String electricityCabinetName;

    /**
     * 首次购买套餐柜机名称
     */
    private String firstBuyMemberCardEleName;

    /**
     * 所属加盟商
     */
    private Long franchiseeId;

    private String franchiseeName;

    private Long storeId;

    private String storeName;

    private Long createTime;
    
    /**
     * 邀请人名称
     */
    private String inviterUserName;
    
    /**
     * t_user表数据对应的租户id
     */
    private Integer tenantId;


}
