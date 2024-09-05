package com.xiliulou.electricity.query.installment;

import com.xiliulou.electricity.entity.installment.InstallmentDeductionRecord;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/27 17:40
 */
@Data
public class InstallmentDeductionRecordQuery {
    
    private Integer offset;
    
    private Integer size;
    
    /**
     * 请求签约用户uid
     */
    private Long uid;
    
    /**
     * 请求签约号，唯一
     */
    private String externalAgreementNo;
    
    /**
     * 扣款订单号
     */
    private String payNo;
    
    /**
     * 还款计划号，我方生成的调用接口的参数，不与项目内其他数据关联
     */
    private String repaymentPlanNo;
    
    /**
     * 被扣款人姓名
     */
    private String userName;
    
    /**
     * 被扣款人手机号
     */
    private String mobile;
    
    /**
     * 扣款金额
     */
    private BigDecimal amount;
    
    /**
     * 扣款状态
     */
    private Integer status;
    
    /**
     * 扣款期次
     */
    private Integer issue;
    
    /**
     * 订单标题
     */
    private String subject;
    
    private Integer tenantId;
    
    private Long franchiseeId;
    
    private Long createTime;
    
    private Long updateTime;
    
    private List<Long> franchiseeIds;
    
    private List<Long> storeIds;
}
