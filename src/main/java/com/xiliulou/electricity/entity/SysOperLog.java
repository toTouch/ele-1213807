package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * 操作日志记录(SysOperLog)表实体类
 *
 * @author zzlong
 * @since 2022-10-11 19:47:27
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_sys_oper_log")
public class SysOperLog {
    /**
     * 日志主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 操作内容
     */
    private String title;
    /**
     * 请求ID
     */
    private String requestId;
    /**
     * 操作人员
     */
    private Long operatorUid;
    /**
     * 请求IP
     */
    private String operIp;
    /**
     * 操作状态（0正常 1异常）
     */
    private Integer status;
    /**
     * 错误消息
     */
    private String errorMsg;
    /**
     * 租户id
     */
    private Long tenantId;
    /**
     * 其它信息
     */
    private String attr;
    /**
     * 操作时间
     */
    private Long operTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    /**
     * 操作状态（0正常 1异常）
     */
    public static final Integer STATUS_SUCCESS = 0;
    public static final Integer STATUS_FAIL = 1;

}
