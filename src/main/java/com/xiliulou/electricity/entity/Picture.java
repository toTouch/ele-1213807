package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * 图片表(Picture)表实体类
 *
 * @author zzlong
 * @since 2022-12-14 13:54:08
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_picture")
public class Picture {
    /**
     * Id
     */
    private Long id;
    /**
     * 业务ID
     */
    private Long businessId;
    /**
     * 图片
     */
    private String pictureUrl;
    /**
     * 顺序
     */
    private Integer seq;
    /**
     * 图片类型 0:小程序
     */
    private Integer imgType;
    /**
     * 状态(0为启用,1为禁用)
     */
    private Integer status;
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;

    private Integer tenantId;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    public static final Integer STATUS_ENABLE = 0;
    public static final Integer STATUS_DISABLE = 1;
    
    public static final Integer TYPE_CAR_IMG = 3;

}
