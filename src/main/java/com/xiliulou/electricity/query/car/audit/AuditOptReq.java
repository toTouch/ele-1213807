package com.xiliulou.electricity.query.car.audit;

import com.xiliulou.electricity.enums.YesNoEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 审核操作数据模型
 *
 * @author xiaohui.song
 **/
@Data
public class AuditOptReq implements Serializable {

    private static final long serialVersionUID = 6192101484685696787L;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 原因
     */
    private String reason;

    /**
     * 金额
     */
    private BigDecimal amount;
    
    /**
     * 强制线下退款
     * <pre>
     *     0: 是
     *     1: 否
     * </pre>
     *
     * @see YesNoEnum#getCode()
     */
    private Integer compelOffLine;
}
