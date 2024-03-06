package com.xiliulou.electricity.vo.sysopt;

import com.xiliulou.electricity.enums.SysOptLogTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: Ant
 * @Date 2024/3/6
 * @Description: 系统操作日志 VO
 **/
@Data
public class SysOptLogVO implements Serializable {
    
    private static final long serialVersionUID = 1222121523213066132L;
    
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
    
    /**
     * 创建人姓名
     */
    private String creatorName;
}
