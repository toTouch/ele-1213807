package com.xiliulou.electricity.model.sysopt.opt;

import com.xiliulou.electricity.enums.SysOptLogTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: Ant
 * @Date 2024/3/6
 * @Description: 系统操作日志操作模型
 **/
@Data
public class SysOptLogOptModel implements Serializable {
    
    private static final long serialVersionUID = 6478498813763209654L;
    
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
