package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Kenneth
 * @Date: 2023/7/7 22:57
 * @Description:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_tenant_esign_config")
public class EleEsignConfig {

    private Integer id;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * AppID
     */
    private String appId;

    /**
     * AppSecret
     */
    private String appSecret;

    /**
     * 签署文件模板ID
     */
    private String docTemplateId;

    /**
     * 签署文件名
     */
    private String signFileName;

    /**
     * 签署流程名
     */
    private String signFlowName;

    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long updateTime;


}
