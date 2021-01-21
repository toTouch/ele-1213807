package com.xiliulou.electricity.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.google.gson.reflect.TypeToken;
import com.xiliulou.core.json.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Map;

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
    @TableId(value = "id",type = IdType.AUTO)
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
    * 可用状态（0-可用，1-禁用）
    */
    private Integer usableStatus;
    /**
    * 状态（0-有电池，1-无电池
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

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;
    //可用
    public static final Integer ELECTRICITY_CABINET_BOX_USABLE = 0;
    //禁用
    public static final Integer ELECTRICITY_CABINET_BOX_UN_USABLE = 1;
    //有电池
    public static final Integer STATUS_ELECTRICITY_BATTERY = 0;
    //无电池
    public static final Integer STATUS_NO_ELECTRICITY_BATTERY = 1;
    //关门
    public static final Integer CLOSE_DOOR = 1;
    //开门
    public static final Integer OPEN_DOOR = 0;
    //关风扇
    public static final Integer CLOSE_FAN = 1;
    //开风扇
    public static final Integer OPEN_FAN = 0;
    //关加热
    public static final Integer CLOSE_HEAT = 0;
    //开加热
    public static final Integer OPEN_HEAT = 1;
    //红灯
    public static final Integer RED_LIGHT = 0x03;
    //绿灯
    public static final Integer GREEN_LIGHT = 0x05;
    //黄灯
    public static final Integer YELLOW_LIGHT = 0x07;

}