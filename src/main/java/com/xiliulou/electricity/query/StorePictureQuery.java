package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-14-14:09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StorePictureQuery {

    /**
     * 门店Id
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
     * 状态(0为启用,1为禁用)
     */
    private Integer status;
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;

    private Integer tenantId;

}
