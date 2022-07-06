package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 柜机核心板上报数据(EleCabinetCoreData)实体类
 *
 * @author zzlong
 * @since 2022-07-06 14:20:37
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_cabinet_core_data")
public class EleCabinetCoreData {
    /**
     * id
     */
    private Long id;
    /**
     * product_key
     */
    private String productKey;
    /**
     * 仓门是否开启;0：开启，1：关闭
     */
    private Integer lockOpen;
    /**
     * 烟雾报警是否开启;0：开启，1：关闭
     */
    private Integer smokeSensorOpen;
    /**
     * 仓口照明灯灯是否开启;0：开启，1：关闭
     */
    private Integer lightOpen;
    /**
     * 风扇是否开启;0：开启，1：关闭
     */
    private Integer fanOpen;
    /**
     * 灭火装置是否开启;0：开启，1：关闭
     */
    private Integer extinguisherOpen;
    /**
     * 电压
     */
    private Double v;
    /**
     * 电流
     */
    private Double a;
    /**
     * 功率
     */
    private Double power;
    /**
     * 功率因素
     */
    private Double powerFactor;
    /**
     * 有功电能
     */
    private Double activeElectricalEnergy;
    /**
     * 水泵是否开启;0：开启，1：关闭
     */
    private Integer waterPumpOpen;
    /**
     * 水位报警是否开启;0：开启，1：关闭
     */
    private Integer waterLevelWarning;
    /**
     * 温度
     */
    private Double temp;
    /**
     * 湿度
     */
    private Double humidity;
    /**
     * 柜内水浸状态
     */
    private Integer waterLeachingWarning;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;


    public static final Integer STSTUS_YES = 0;
    public static final Integer STSTUS_NO = 1;

}
