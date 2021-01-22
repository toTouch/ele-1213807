package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 换电柜绑定表(ElectricityCabinetBind)实体类
 *
 * @author lxc
 * @since 2020-11-25 11:00:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_cabinet_bind")
public class ElectricityCabinetBind {
    /**
     * 换电柜Id
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /**
     * uid
     */
    private Long uid;
    /**
     * 换电柜ID
     */
    private Long electricityCabinetId;

}