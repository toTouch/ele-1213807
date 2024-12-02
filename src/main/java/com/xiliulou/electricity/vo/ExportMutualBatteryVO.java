package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @ClassName: ExportMutualBatteryVO
 * @description:
 * @author: renhang
 * @create: 2024-11-29 09:29
 */
@Data
public class ExportMutualBatteryVO {
    
    /**
     * 电池编号
     */
    private String sn;
    
    
    private String franchiseeName;
    
    /**
     * 电池在仓状态:在仓或者不在仓
     */
    private String physicsStatus;
    
    /**
     * 所在柜机
     */
    private String electricityCabinetName;
    
    /**
     * 所在仓门
     */
    private Integer cellNo;
    
    /**
     * 归属用户姓名
     */
    private String userName;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 互通的加盟商
     */
    private String mutualFranchiseeName;
}
