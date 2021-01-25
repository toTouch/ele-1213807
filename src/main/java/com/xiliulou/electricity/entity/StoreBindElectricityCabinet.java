package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * (StoreBindElectricityCabinetBind)实体类
 *
 * @author lxc
 * @since 2020-11-25 11:00:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_store_bind_electricity_cabinet")
public class StoreBindElectricityCabinet {
    /**
     * 换电柜Id
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /**
     * 门店Id
     */
    private Integer storeId;
    /**
     * 换电柜Id
     */
    private Integer electricityCabinetId;

}