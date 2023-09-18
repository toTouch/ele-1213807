package com.xiliulou.electricity.vo;

import java.util.List;
import lombok.Data;

/**
 * 换电柜仓门表(TElectricityCabinetBox)实体类
 *
 * @author makejava
 * @since 2020-11-25 11:00:37
 */
@Data
public class ElectricityCabinetBoxVO {
    /**
    * 仓门Id
    */
    private Long id;
    /**
    * 所属换电柜柜Id
    */
    private Integer electricityCabinetId;
    /**
    * 仓门号
    */
    private String cellNo;
    /**
    * 可用状态（0-禁用，1-可用）
    */
    private Integer usableStatus;
    /**
    * 状态（0-无电池，1-有电池）
    */
    private Integer status;
    /**
     * 门锁状态（0-开门，1-关门）
     */
    private Integer isLock;
    /**
     * 风扇状态（0-开，1-关）
     * */
    private Integer isFan;
    /**
     * 温度
     */
    private String temperature;
    /**
     * 加热状态（0-关，1-开）
     */
    private Integer isHeat;
    /**
     * 指示灯状态（3-红灯亮，5-绿灯亮，7-黄灯亮）
     */
    private Integer isLight;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 更新时间
    */
    private Long updateTime;
    /**
    * 是否删除（0-正常，1-删除）
    */
    private Integer delFlag;
    //电池编号
    private String sn;
    /**
     * 电池电量
     */
    private Double power;
    /**
     * 0--空闲 1--正在开机 2--充电中 3--充满电 4--限额充电 -1 未充电
     */
    private Integer chargeStatus;
    /**
     * 仓门电池类型
     */
    private String batteryType;
    /**
     * 仓门电池短类型
     */
    private String batteryShortType;
    /**
     * 电池电压
     */
    private Double batteryV;
    /**
     * 电池电流
     */
    private Double batteryA;
    /**
     * 充电器电压
     */
    private Double chargeV;
    /**
     * 充电器电流
     */
    private Double chargeA;

    /**
     * 是否可换电 0:是；1:否
     */
    private Integer exchange;

    /**
     * 锁仓类型 0--人为锁仓 1--系统锁仓
     */
    private Integer lockType;
    
    /**
     * 锁仓原因
     */
    private Integer lockReason;
    
    /**
     * 备注
     */
    private String remark;

    /**
     * 锁仓/解锁时间
     */
    private Long lockStatusChangeTime;

    
    /**
     * 空闲开始时间
     */
    private Long emptyGridStartTime;
    
    public static final Integer EXCHANGE_YES=0;
    public static final Integer EXCHANGE_NO=1;
}
