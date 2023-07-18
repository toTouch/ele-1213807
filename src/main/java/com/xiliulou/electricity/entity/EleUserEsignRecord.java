package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Kenneth
 * @Date: 2023/7/9 18:23
 * @Description:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_user_esign_record")
public class EleUserEsignRecord {

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
    private Integer signFinishStatus;

    /**
     * 签署文件ID 来源于E签宝
     */
    private String fileId;

    /**
     * 签署文件名称
     */
    private String fileName;

    /**
     * 签署结果信息
     */
    private String signResult;

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
