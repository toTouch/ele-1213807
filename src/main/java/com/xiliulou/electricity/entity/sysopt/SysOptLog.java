package com.xiliulou.electricity.entity.sysopt;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.enums.SysOptLogTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: Ant
 * @Date 2024/3/6
 * @Description: 系统操作日志
 **/
@Data
@TableName("t_sys_opt_log")
public class SysOptLog implements Serializable {
    
    private static final long serialVersionUID = -8004318052077087997L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 创建人
     */
    private Long createUid;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 请求IP
     */
    private String optIp;
    
    /**
     * 类型
     *
     * @see SysOptLogTypeEnum
     */
    private Integer type;
    
    /**
     * 内容
     */
    private String content;
}
