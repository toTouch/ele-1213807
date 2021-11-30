package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Miss.Li
 * @Date: 2021/11/5 14:04
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_third_access_record")
public class ThirdAccessRecord {

    private Long id;
    /**
     * 请求Id
     */
    private String requestId;
    /**
     * 请求时间
     */
    private Long requestTime;
    /**
     * 柜机响应时间
     */
    private Long responseTime;

    private Long createTime;

    private Long updateTime;

    private Integer tenantId;
    /**
     * 操作类型
     */
    private String operateType;
    /**
     * 额外信息
     */
    private String attrMsg;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
