package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import lombok.Data;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-11 18:26
 **/
@Data
public class ElectricityMemberCardOrderVO extends ElectricityMemberCardOrder {
    private String phone;
    private String franchiseeName;
    private String userName;
    private Integer payCount;

    private OldUserActivityVO  oldUserActivityVO;
}
