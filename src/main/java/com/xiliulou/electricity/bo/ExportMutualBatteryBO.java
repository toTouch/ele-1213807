package com.xiliulou.electricity.bo;

import lombok.Data;

/**
 * @ClassName: ExportMutualBatteryBO
 * @description:
 * @author: renhang
 * @create: 2024-11-29 09:19
 */
@Data
public class ExportMutualBatteryBO {
    
    /**
     * 电池编号
     */
    private String sn;
    
    /**
     * 所属加盟商
     */
    private Long franchiseeId;
    
    private String franchiseeName;
    
    /**
     * 电池在仓状态
     */
    private Integer physicsStatus;
    
    /**
     * 所在柜机
     */
    private String electricityCabinetName;
    
    /**
     * 所在仓门
     */
    private Integer cellNo;

    /**
     * 用户加盟商
     */
    private Long userFranchiseeId;
    
    /**
     * 归属用户姓名
     */
    private String userName;
    
    /**
     * 手机号
     */
    private String phone;
}
