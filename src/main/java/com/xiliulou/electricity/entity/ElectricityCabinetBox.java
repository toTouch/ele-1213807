package com.xiliulou.electricity.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
/**
 * 换电柜仓门表(TElectricityCabinetBox)实体类
 *
 * @author makejava
 * @since 2020-11-25 11:00:37
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_cabinet_box")
public class ElectricityCabinetBox {
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
    * 电池Id
    */
    private Long electricityBatteryId;
    /**
    * 可用状态（0-禁用，1-可用）
    */
    private Object usableStatus;
    /**
    * 仓门状态（0-开门，1-关门）
    */
    private Object boxStatus;
    /**
    * 状态（0-无电池，1-有电池）
    */
    private Object status;
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
    private Object delFlag;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;
    //可用
    public static final Integer COURIER_BOX_USABLE = 1;
    public static final Integer COURIER_BOX_UN_USABLE = 0;
    //分配状态
    public static final Integer STATUS_DISTRIBUTE = 1;
    public static final Integer STATUS_UN_DISTRIBUTE = 0;

}