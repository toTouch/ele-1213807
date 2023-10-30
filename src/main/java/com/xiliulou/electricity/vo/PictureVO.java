package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-22-17:25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PictureVO {

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

    private String pictureOSSUrl;

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
}
