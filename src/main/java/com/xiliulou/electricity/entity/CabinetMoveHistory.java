package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (CabinetMoveHistory)实体类
 *
 * @author Eclair
 * @since 2023-06-15 19:54:29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_cabinet_move_history")
public class CabinetMoveHistory {

    private Long id;
    /**
     * 物联网productKey
     */
    private String productKey;
    /**
     * 物联网deviceName
     */
    private String deviceName;
    /**
     * 迁移前数据
     */
    private String oldInfo;
    /**
     * 迁移后柜机id
     */
    private Long eid;
    /**
     * 操作人uid
     */
    private Long uid;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;
    /**
     * 租户id
     */
    private Integer tenantId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
