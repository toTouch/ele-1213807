package com.xiliulou.electricity.vo.userinfo;

import com.xiliulou.electricity.enums.PayTypeEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @date 2025/2/21 17:34:23
 */
@Data
public class UserCarRentalPackageProV2VO {
    
    /**
     * 用户ID
     */
    private Long uid;
    
    /**
     * 用户名称
     */
    private String name;
    
    /**
     * 用户电话
     */
    private String phone;
    
    /**
     * 用户可用状态 0--启用，1--禁用
     */
    private Integer usableStatus;
    
    /**
     * 租车押金状态 0-未缴纳 1-已缴纳
     */
    private Integer carDepositStatus;
    
    /**
     * 车电一体押金缴纳状态 0-已缴纳 1-未缴纳
     */
    private Integer carBatteryDepositStatus;
    
    /**
     * 电池租赁状态，0：未租电池，1：已租电池
     */
    
    private Integer batteryRentStatus;
    
    /**
     * 押金金额(元)
     */
    private BigDecimal deposit;
    
    /**
     * 交易方式
     * <pre>
     *     1-线上
     *     2-线下
     *     3-免押
     * </pre>
     *
     * @see PayTypeEnum
     */
    private Integer depositPayType;
    
    /**
     * 套餐ID
     */
    private Long packageId;
    
    /**
     * 套餐名称
     */
    private String packageName;
    
    /**
     * 套餐类型
     */
    private Integer packageType;
    
    /**
     * 到期时间
     */
    private Long dueTime;
    
    /**
     * 总计到期时间
     */
    private Long dueTimeTotal;
    
    /**
     * 当前余量
     */
    private Long residue;
    
    /**
     * 滞纳金金额
     */
    private BigDecimal lateFeeAmount;
    
    /**
     * 电池SN信息
     */
    private String batterySn;
    
    /**
     * 车辆SN
     */
    private String carSn;
    
    /**
     * 所属加盟商
     */
    private Long franchiseeId;
    
    /**
     * 所属加盟商名称
     */
    private String franchiseeName;
    
    /**
     * 所属门店
     */
    private Long storeId;
    
    /**
     * 所属门店名称
     */
    private String storeName;
}
