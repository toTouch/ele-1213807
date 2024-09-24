package com.xiliulou.electricity.query.installment;

import lombok.Data;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/9/2 18:39
 */
@Data
public class InstallmentSignNotifyQuery {
    
    /**
     * 请求签约号
     */
    private String externalAgreementNo;
    
    /**
     * 用户签约成功记录编号
     */
    private String agreementNo;
    
    /**
     * 支付宝id
     */
    private String alipayUserId;
    
    /**
     * 协议原本生效时间，格式为 yyyy-MM-dd HH:mm:ss
     */
    private String validTime;
    
    /**
     * 协议原本失效时间
     */
    private String invalidTime;
    
    /**
     * 签约或解约生效时间
     */
    private String effectTime;
    
    /**
     * 状态：1签约成功；2解约成功；
     */
    private String status;
}
