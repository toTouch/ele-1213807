package com.xiliulou.electricity.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
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
    private Integer id;
    /**
    * 型号名称
    */
    private String name;
    /**
    * 柜子的仓门数量
    */
    private Integer num;
    /**
    * 电压
    */
    private Integer voltage;
    /**
    * 0--未删除，1--删除
    */
    private Object delFlag;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 更新时间
    */
    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}