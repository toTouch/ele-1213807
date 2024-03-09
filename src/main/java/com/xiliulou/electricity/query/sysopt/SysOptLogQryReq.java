package com.xiliulou.electricity.query.sysopt;

import com.xiliulou.electricity.enums.SysOptLogTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: Ant
 * @Date 2024/3/6
 * @Description: 系统操作日志
 **/
@Data
public class SysOptLogQryReq implements Serializable {
    
    private static final long serialVersionUID = 3698602536044350558L;
    
    /**
     * 偏移量
     */
    private Integer offset = 0;
    
    /**
     * 取值数量
     */
    private Integer size = 10;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 类型
     *
     * @see SysOptLogTypeEnum
     */
    private Integer type;
    
    /**
     * 创建人
     */
    private Long createUid;
    
    /**
     * 操作时间开始
     */
    private Long beginCreateTime;
    
    /**
     * 操作时间截止
     */
    private Long endCreateTime;
}
