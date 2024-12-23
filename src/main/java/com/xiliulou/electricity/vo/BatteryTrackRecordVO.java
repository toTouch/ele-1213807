package com.xiliulou.electricity.vo;


import lombok.Data;

@Data
public class BatteryTrackRecordVO {
    
    /**
     * 电池sn
     */
    private String sn;
    
    /**
     * 涉及的订单号
     */
    private String orderId;
    
    /**
     * 柜机id
     */
    private Long eId;
    
    /**
     * 柜机名称
     */
    private String eName;
    
    /**
     * 柜机格挡
     */
    private Integer eNo;
    
    /**
     * 轨迹类型
     */
    private Integer type;
    
    /**
     * 创建时间
     */
    private String createTime;
    
    /**
     * 用户id
     */
    private Long uid;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 用户姓名
     */
    private String name;
    
    /**
     * 订单类型：1-换电订单 2-租/退电订单
     */
    private Integer orderType;
}
