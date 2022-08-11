package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (ElectricityCabinetHysicsOperRecord)实体类
 *
 * @author zgw
 * @since 2022-08-08 14:37:18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_cabinet_hysics_oper_record")
public class ElectricityCabinetHysicsOperRecord {
    
    private Long id;
    
    private Long electricityId;
    
    private Integer cellNo;
    
    private Integer type;
    
    private Integer status;
    
    private String msg;
    
    private Long uid;
    
    private String username;
    
    private Long createTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
