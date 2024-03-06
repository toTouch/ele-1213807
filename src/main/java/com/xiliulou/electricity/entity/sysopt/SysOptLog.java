package com.xiliulou.electricity.entity.sysopt;

import com.baomidou.mybatisplus.annotation.TableName;
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
}
