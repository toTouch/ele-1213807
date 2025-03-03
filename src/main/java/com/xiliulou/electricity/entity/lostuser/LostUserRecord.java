package com.xiliulou.electricity.entity.lostuser;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 流失用户记录(LostUserRecord)实体类
 *
 * @author maxiaodong
 * @since 2024-10-09 11:39:06
 */

@Data
@TableName("t_lost_user_record")
public class LostUserRecord {
    private Long id;
    /**
     * 用户uid
     */
    private Long uid;
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 套餐类型：0-电，1-车
     */
    private Integer packageType;
    /**
     * 预估时间
     */
    private Long prospectTime;
    /**
     * 实际时间
     */
    private Long actualTime;
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;
    /**
     * 租户id
     */
    private Integer tenantId;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;
}

