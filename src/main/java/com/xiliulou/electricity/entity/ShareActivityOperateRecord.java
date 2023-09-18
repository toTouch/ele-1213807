package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (ShareActivityOperateRecord)实体类
 *
 * @author Eclair
 * @since 2023-05-24 14:47:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_share_activity_operate_record")
public class ShareActivityOperateRecord {
    /**
     * id
     */
    private Long id;
    /**
     * 活动id
     */
    private Long shareActivityId;
    /**
     * 修改人id
     */
    private Long uid;
    /**
     * 活动名称
     */
    private String name;
    /**
     * 活动套餐
     */
    private String memberCard;
    /**
     * 3.0 新增套餐信息
     */
    private String packageInfo;
    /**
     * 租户id
     */
    private Integer tenantId;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
