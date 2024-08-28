package com.xiliulou.electricity.dto.callback;


import lombok.Data;

/**
 * <p>
 * Description: This class is PxzParams!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/8/27
 **/
public class PxzParams {
    @Data
    public static class FreeDepositOrUnfree {
        
        private String transId;
        
        private String authNo;
        
        private Integer authStatus;
    }
    
    @Data
    public static class AuthPay {
        
        private String payNo;
        
        private String orderId;
        
        private Integer orderStatus;
    }
}
