package com.xiliulou.electricity.vo;


import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author : renhang
 * @description ElectricityCabinetCardInfoVO
 * @date : 2025-03-13 16:17
 **/
@Data
public class ElectricityCabinetCardInfoVO {

    @ExcelProperty("运营商")
    private String tenantName;

    @ExcelProperty("柜机")
    private String cabinetName;

    @ExcelProperty("物联网卡")
    private String cardNumber;

    @ExcelProperty("总流量")
    private String dataBalance;

    @ExcelProperty("剩余流量")
    private String dataTrafficAmount;


    @ExcelProperty("物联网卡到期时间")
    private String expiryDate;

    @ExcelProperty("物联网卡Code")
    private String code;

    @ExcelProperty("流量模式")
    private String codeType;

    @ExcelProperty("柜机创建时间")
    private String createTime;

    @ExcelProperty("柜机到期时间")
    private String expireTime;


}
