package com.xiliulou.electricity.query.car;

import lombok.Data;

import java.io.Serializable;

/**
 * 冻结套餐购买订单请求数据模型
 *
 * @author xiaohui.song
 **/
@Data
public class FreezeRentOrderoptReq implements Serializable {

    private static final long serialVersionUID = -5249695230292571002L;

    /**
     * 套餐购买订单编码
     */
    private String packageOrderNo;

    /**
     * 申请期限(天)
     */
    private Integer applyTerm;

    /**
     * 申请理由
     */
    private String applyReason;
}
