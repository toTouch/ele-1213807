package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * Description: EleUserOperateHistoryVO
 *
 * @author zhangyongbo
 * @version 1.0
 * @since 2023/12/28 11:45
 */
@Data
public class EleUserOperateHistoryVO {
    /**
     * 主键
     */
    private Integer id;
    
    /**
     * 操作类型 0-账户
     */
    private Integer operateType;
    
    /**
     * 操作模块
     */
    private Integer operateModel;
    
    /**
     * 操作内容
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
    private Long operateUid;
    
    /**
     * 操作人
     */
    private String operatorName;
    
    /**
     * 被操作用户
     */
    private Long uid;
    
    /**
     * 被操作用户
     */
    private String uName;
    
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