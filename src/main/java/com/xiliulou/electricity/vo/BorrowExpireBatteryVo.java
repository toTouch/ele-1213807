package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author Hardy
 * @date 2021/12/2 16:50
 * @mood
 */
@Data
public class BorrowExpireBatteryVo {
    private String sn;
    private String electricityCabinetName;
    private String borrowExpireTime;
    private String name;
    private String phone;
    private String status;
    private Integer tenantId;
}
