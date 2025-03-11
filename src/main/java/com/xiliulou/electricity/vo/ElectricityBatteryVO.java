package com.xiliulou.electricity.vo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.enums.battery.BatteryLabelEnum;
import com.xiliulou.electricity.vo.battery.ElectricityBatteryLabelVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;

/**
 * 换电柜电池表(ElectricityBattery)实体类
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ElectricityBatteryVO {

    private Long id;

    /**
     * sn码
     */
    private String sn;
    private String name;//前端组件需要使用
    /**
     * 电池型号
     */
    private String model;
    /**
     * 3.0之后，model需要转为短型号显示，则使用originalModel来代替原始型号信息
     */
    private String originalModel;
    /**
     * 电池电量
     */
    private Double power;
    /**
     * 电压
     */
    private Integer voltage;
    /**
     * 电池容量,单位(mah)
     */
    private Integer capacity;
    /**
     * 电池物理状态 0：在仓，1：不在仓
     */
    private Integer physicsStatus;
    /**
     * 电池业务状态：1：已录入，2：租借，3：归还，4：异常交换
     */
    private Integer businessStatus;

    private Long createTime;

    private Long updateTime;

    private Integer delFlag;


    /**
     * 0：正常 1：故障
     */
    private Integer healthStatus;
    /**
     * 0--空闲 1--正在开机 2--充电中 3--充满电 4--限额充电 -1 未充电
     */
    private Integer chargeStatus;

    /**
     * 地址经度
     */
    private Double longitude;
    /**
     * 地址纬度
     */
    private Double latitude;

    //租户id
    private Integer tenantId;

    /**
     * 所属换电柜柜Id
     */
    private Integer electricityCabinetId;

    /**
     * 所属换电柜柜
     */
    private String electricityCabinetName;

    /**
     * 所属加盟商ID
     */
    private Long franchiseeId;

    /**
     * 所属加盟商
     */
    private String franchiseeName;
    /**
     * 所属用户id
     */
    private Long uid;

    //所属用户
    private String userName;

    //是否绑定加盟商
    private Boolean isBind;

    //电池的标称电压
    private Double batteryV;

    /**
     * 物联网卡号
     */
    private String iotCardNumber;

    /**
     * 电池充电电流
     */
    private Double batteryChargeA;

    /**
     * 电池服务费
     */
    private BigDecimal batteryServiceFee;

    /**
     * 用户所产生的电池服务费
     */
    private BigDecimal userBatteryServiceFee;
    /**
     * gps上报电流
     */
    private Double sumA;

    /**
     * gps
     */
    private Double sumV;
    
    /**
     * 库存状态；0,库存；1,已出库
     */
    private Integer stockStatus;
    
    /**
     * 库房Id
     */
    private Long warehouseId;
    
    /**
     * 库房Id
     */
    private String warehouseName;
    
    /**
     * 品牌型号
     */
    private String brandAndModelName;
    
    /**
     * 电池型号ID
     */
    private Long modelId;
    /**
     * 电池温度
     */
    private String batteryTemperature;
    
    /**
     * 电池标签
     * @see BatteryLabelEnum
     */
    private Integer label;
    
    /**
     * 电池标签关联数据
     */
    private ElectricityBatteryLabelVO labelVO;
}
