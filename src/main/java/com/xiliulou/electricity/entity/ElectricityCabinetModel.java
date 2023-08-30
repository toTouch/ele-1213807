package com.xiliulou.electricity.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 换电柜型号表(TElectricityCabinetModel)实体类
 *
 * @author makejava
 * @since 2020-11-25 11:01:04
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_cabinet_model")
public class ElectricityCabinetModel {
    /**
    * 型号Id
    */
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /**
    * 型号名称
    */
    @NotEmpty(message = "型号名称不能为空!")
    private String name;
    /**
    * 柜子的仓门数量
    */
    @NotNull(message = "柜子的仓门数量不能为空!")
    private Integer num;
    /**
    * 电压
    */
    private String voltage;
    /**
    * 0--未删除，1--删除
    */
    private Integer delFlag;
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

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
