package com.xiliulou.electricity.vo;


import lombok.Data;

/**
 * @author : renhang
 * @description FreeDepositExpireRecordVO
 * @date : 2025-02-25 14:10
 **/
@Data
public class FreeDepositExpireRecordVO {

    private Long id;

    private String realName;

    private String phone;

    private Integer depositType;

    private Double transAmt;

    private Long depositTime;

    private Long franchiseeId;

    private String franchiseeName;

    private String remark;

    /**
     * 处理人
     */
    private Long operateUid;


    /**
     * 处理人
     */
    private String operateName;

    /**
     * 处理时间
     */
    private Long operateTime;


}
