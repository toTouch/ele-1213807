package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (ElectricityCabinetPhysicsOperRecord)实体类
 *
 * @author zgw
 * @since 2022-08-16 15:31:11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_cabinet_physics_oper_record")
public class ElectricityCabinetPhysicsOperRecord {
    
    private Long id;
    
    private Integer electricityCabinetId;

    private String command;
    
    private String cellNo;
    /**
    * 操作状态 0--初始化 1--成功,2--失败
    */
    private Integer status;
    
    private String msg;
    
    private Long uid;
    
    private String userName;
    /**
    * 操作类型 1--命令下发 2--柜机操作
    */
    private Integer operateType;

    private Long createTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
