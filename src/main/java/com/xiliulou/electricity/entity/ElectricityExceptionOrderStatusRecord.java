package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 订单表(TElectricityExceptionOrderStatusRecord)实体类
 *
 * @author makejava
 * @since 2022-07-21 16:00:45
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_exception_order_status_record")
public class ElectricityExceptionOrderStatusRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 订单编号
     */
    private String orderId;

    //订单状态序号
    private Double orderSeq;
    /**
     * 订单的状态
     */
    private String status;
    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    //租户id
    private Integer tenantId;

    /**
     * 是否已自助开仓 0--未自助开仓 1--自助开仓
     */
    private Integer isSelfOpenCell;

    /**
     * 仓门号
     */
    private Integer cellNo;


    /**
     * 自助开仓状态  0--成功 1--失败
     */
    private Integer openCellStatus;

    public static final Integer OPEN_CELL_SUCCESS = 0;
    public static final Integer OPEN_CELL_FAIL = 1;


    public static final Integer SELF_OPEN_CELL = 1;
    public static final Integer NOT_SELF_OPEN_CELL = 0;

    public static String STATUS_SUCCESS = "SUCCESS";
    public static String STATUS_OPEN_FAIL = "OPEN_FAIL";
    public static String BATTERY_NOT_MATCH = "BATTERY_NOT_MATCH";

}
