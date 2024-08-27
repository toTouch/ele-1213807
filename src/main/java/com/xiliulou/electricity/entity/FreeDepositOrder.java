package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * 实际支付金额(剩余的可代扣金额)
     */
    private Double payTransAmt;
    
    /**
     * 支付状态（免押）
     */
    private Integer authStatus;
    
    /**
     * 授权免押的状态(代扣)
     */
    private Integer payStatus;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 门店Id
     */
    private Long storeId;
    
    private Integer tenantId;
    
    /**
     * 免押的类型0--支付宝
     */
    private Integer type;
    
    /**
     * 押金类型 1：电池，2：租车
     */
    private Integer depositType;
    
    private Long createTime;
    
    private Long updateTime;
    
    /**
     * 默认是拍小租
     */
    private Integer channel;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
    /**
     * 支付宝
     */
    public static final Integer TYPE_ZHIFUBAO = 0;
    
    
    /**
     * 支付状态 0:初始化;4:待冻结;7:冻结中;10:已冻结;11:解冻中;12:已解冻;13超时关闭
     */
    public static final Integer AUTH_INIT = 0;
    
    public static final Integer AUTH_PENDING_FREEZE = 4;
    
    public static final Integer AUTH_FREEZING = 7;
    
    public static final Integer AUTH_FROZEN = 10;
    
    public static final Integer AUTH_UN_FREEZING = 11;
    
    public static final Integer AUTH_UN_FROZEN = 12;
    
    public static final Integer AUTH_TIMEOUT = 13;
    
    /**
     * 授权免押状态 -1:初始化;0:交易成功；1:交易处理中；2:交易失败；4:交易关闭
     */
    public static final Integer PAY_STATUS_INIT = -1;
    
    public static final Integer PAY_STATUS_DEAL_SUCCESS = 0;
    
    public static final Integer PAY_STATUS_DEALING = 1;
    
    public static final Integer PAY_STATUS_DEAL_FAIL = 2;
    
    public static final Integer PAY_STATUS_DEAL_CLOSE = 4;
    
    //押金类型 1：电池，2：租车，3：租电池和租车
    public static final Integer DEPOSIT_TYPE_BATTERY = 1;
    
    public static final Integer DEPOSIT_TYPE_CAR = 2;
    
    public static final Integer DEPOSIT_TYPE_CAR_BATTERY = 3;
    
    
    public static final Integer YEAR = 365 * 24 * 60 * 60 * 1000;
    
}
