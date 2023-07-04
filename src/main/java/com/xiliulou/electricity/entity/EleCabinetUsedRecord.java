package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Kenneth
 * @Date: 2023/7/3 10:23
 * @Description:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EleCabinetUsedRecord {

    private String name;

    private String phone;

    private String type;

    private String orderId;

    private String returnBatterySn;

    private String returnCellNo;

    private String leaseBatterySn;

    private String leaseCellNo;

    private String createTime;

}
