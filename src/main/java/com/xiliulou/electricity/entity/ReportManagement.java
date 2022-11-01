package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 报表管理(ReportManagement)表实体类
 *
 * @author zzlong
 * @since 2022-10-31 15:59:06
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_report_management")
public class ReportManagement {
    /**
     * id
     */
    private Long id;
    /**
     * 任务id
     */
    private String jobId;
    /**
     * 业务类型
     */
    private Integer type;
    /**
     * 任务状态
     */
    private Integer status;
    /**
     * 下载链接
     */
    private String url;
    /**
     * 0--未删除，1--删除
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
     * 创建时间
     */
    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    //状态：0 初始化 ；1导出中；2 导出成功 ；3  导出失败
    public static final Integer STATUS_INIT = 0;
    public static final Integer STATUS_EXPORTING = 1;
    public static final Integer STATUS_SUCCESS = 2;
    public static final Integer STATUS_FAIL = 3;


    //业务类型： 1 故障上报 ； 2 购卡记录
    public static final Integer TYPE_WARN_MESSAGE = 1;
    public static final Integer TYPE_MEMBERCARD_RECORD = 2;


}
