package com.xiliulou.electricity.entity;


    



                                    
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                    
                                    
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                    

                                                            import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (FreeDepositOrder)实体类
 *
 * @author Eclair
 * @since 2023-02-15 11:39:27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_free_deposit_order")
public class FreeDepositOrder {
    
    private Long id;
    
    private Long uid;
    
    private String orderId;
    /**
    * 授权码
    */
    private String authNo;
    /**
    * 支付宝绑定的手机号
    */
    private String phone;
    /**
    * 身份征号
    */
    private String idCard;
    /**
    * 用户真实姓名
    */
    private String realName;
    /**
    * 免押金额
    */
    private Double transAmt;
    /**
    * 实际支付金额
    */
    private Double payTransAmt;
    /**
    * 授权免押的状态
    */
    private Integer authStatus;
    /**
    * 支付状态
    */
    private Integer payStatus;
    
    private Integer tenantId;
    /**
    * 免押的类型0--支付宝 
    */
    private Integer type;
    
    private Long createTime;
    
    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;
    
    /**
     * 支付宝
     */
    public static final Integer TYPE_ZHIFUBAO = 0;
    
    
    /**
     * 4:待冻结;7:冻结中;10:已冻结;11:解冻中;12:已解冻;13超时关闭
     */
    public static final Integer AUTH_PENDING_FREEZE = 4;
    
    public static final Integer AUTH_FREEZING = 7;
    
    public static final Integer AUTH_FROZEN = 10;
    
    public static final Integer AUTH_UN_FREEZING = 11;
    
    public static final Integer AUTH_UN_FROZEN = 12;
    
    public static final Integer AUTH_TIMEOUT = 13;
    
}
