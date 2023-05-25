package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * (BatteryTrackRecord)实体类
 *
 * @author Eclair
 * @since 2023-01-03 16:24:37
 */
@Setter
@Getter
@Accessors(chain = true)
@TableName("t_battery_track_record")
public class BatteryTrackRecord {
    
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
    private Long createTime;
    
    /**
     * 电池入仓
     */
    public static final Integer TYPE_PHYSICS_IN = 0;
    
    /**
     * 电池出仓
     */
    public static final Integer TYPE_PHYSICS_OUT  = 1;
    
    /**
     *  - 电池换电放入
     */
    public static final Integer TYPE_EXCHANGE_IN = 2;
    
    /**
     *   - 电池换电取走
     */
    public static final Integer TYPE_EXCHANGE_OUT = 3;
    
    /**
     *   - 电池租电取走
     */
    public static final Integer TYPE_RENT_OUT = 4;
    
    /**
     *   - 电池退电放入
     */
    public static final Integer TYPE_RETURN_IN = 5;
    
    /**
     * 离线换电取走
     */
    public static final Integer TYPE_OFFLINE_EXCHANGE_OUT = 6;
    /**
     * 离线换电放入
     */
    public static final Integer TYPE_OFFLINE_EXCHANGE_IN = 7;

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
}
