package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zgw
 * @date 2021/11/30 19:22
 * @mood
 */
@Data
@TableName("t_template_config")
public class TemplateConfigEntity implements Serializable {
    @TableId
    private Integer id;
    /**
     * 电池超时未归还
     */
    private String batteryOuttimeTemplate ;
    /**
     * 低电量模板
     */
    private String electricQuantityRemindTemplate ;
    /**
     * 租户id
     */
    private Integer tenantId;

    private Long createTime;

    private Long updateTime;

}
