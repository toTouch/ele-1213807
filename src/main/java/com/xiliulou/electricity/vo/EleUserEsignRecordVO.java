package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author: Kenneth
 * @Date: 2023/7/9 18:31
 * @Description:
 */

@Data
public class EleUserEsignRecordVO {

    private Long id;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 签署流程ID
     */
    private String signFlowId;

    /**
     * 签署完成状态（0-未完成，1-已完成）
     */
    private String signFinishStatus;

    /**
     * 签署文件ID 来源于E签宝
     */
    private String fileId;

    /**
     * 签署文件名称
     */
    private String fileName;

    private Integer delFlag;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long updateTime;

    private String name;

    private String phone;

}
