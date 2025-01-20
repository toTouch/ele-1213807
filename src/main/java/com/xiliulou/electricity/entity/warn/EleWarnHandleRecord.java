package com.xiliulou.electricity.entity.warn;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 告警处理操作记录(TEleWarnHandleRecord)实体类
 *
 * @author maxiaodong
 * @since 2024-11-07 13:53:33
 */
@Data
@TableName("t_ele_warn_handle_record")
public class EleWarnHandleRecord implements Serializable {
    
    private static final long serialVersionUID = 465077126139925580L;
    
    private Long id;
    
    /**
     * 批次号
     */
    private String batchNo;
    
    /**
     * 告警消息Id
     */
    private Long warnId;
    
    /**
     * 处理方式：1--处理中 2--已处理  3--忽略 4--转工单
     */
    private Integer status;
    
    /**
     * 备注
     */
    private String remark;
    
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

