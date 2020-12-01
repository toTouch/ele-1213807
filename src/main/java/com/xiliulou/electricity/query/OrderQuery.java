package com.xiliulou.electricity.query;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;


/**
 * 订单表(TElectricityCabinetOrder)实体类
 *
 * @author makejava
 * @since 2020-11-26 16:00:45
 */
@Data
@Builder
public class OrderQuery {

    /**
    * 换电柜id
    */
    @NotNull(message = "换电柜id不能为空!")
    private Integer electricityCabinetId;
    /**
    * 下单来源 1--微信公众号 2--小程序
    */
    @NotNull(message = "下单来源不能为空!")
    private Integer source;

    //微信公众号来源
    public static final Integer SOURCE_WX_MP = 1;
    //微信小程序来源
    public static final Integer SOURCE_WX_RPO = 2;

}