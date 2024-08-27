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
@Data
public class FyParams {
    
    @Data
    public static class FreeOfCharge{
        private String thirdOrderNo; //	String(100)	Y	订单号
        private String platformOrderNo; //	String(100)	Y	服务平台订单号
        private String outOrderNo; //	String(100)	N	外部平台订单号，如支付宝
        private String buyerLogonId; //	String(50)	Y	买家账号
        private String buyerBuyerId; //	String(32)	Y	买家 id
        private String tradeTime; //	String(14)	Y	交易时间
        private Long payAmount; //	Long	Y	冻结支付金额，单位分
        private String authNo; //	String(100)	Y	授权单号
    }
    
    @Data
    public static class Withhold{
        private String payNo; //	String(100)	Y	资金处理订单号
        private String thirdOrderNo; //	String(100)	Y	预授权订单号
        private String platformOrderNo; //	String(100)	Y	资金处理服务平台订单号
        private String outOrderNo; //	String(100)	N	资金处理外部平台订单号，如支付宝
        private String tradeTime; //	String(14)	Y	交易时间
        private Long payAmount; //	Long	Y	金额，单位分
        private String tradeType; //	String(20)	Y	交易类型【PAY：转支付；UNFREEZE：解冻】
    }
}
