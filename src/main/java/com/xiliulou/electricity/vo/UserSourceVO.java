package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

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
     * 所属加盟商
     */
    private Long franchiseeId;
    private String franchiseeName;

    private Long storeId;
    private String storeName;

    private Long createTime;


}
