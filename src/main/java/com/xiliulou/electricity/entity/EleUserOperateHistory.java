package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description: EleUserOperateHistory
 *
 * @author zhangyongbo
 * @version 1.0
 * @since 2023/12/28 11:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_user_operate_history")
public class EleUserOperateHistory {
    /**
     * 主键
     */
    private Long id;
    
    /**
     * 操作类型 1-用户
     */
    private Integer operateType;
    
    /**
     * 操作模块 1-用户账户
     */
    private Integer operateModel;
    
    /**
     * 操作内容 1-解绑微信
     */
    private Integer operateContent;
    
    /**
     * 操作前信息
     */
    private String oldOperateInfo;
    
    /**
     * 操作后信息
     */
    private String newOperateInfo;
    
    /**
     * 操作人
     */
    private String operatorName;
    
    /**
     * 被操作用户
     */
    private Long uid;
    
    /**
     * 租户
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