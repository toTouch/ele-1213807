package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * (ThirdConfig)表实体类
 *
 * @author zzlong
 * @since 2023-02-02 18:03:58
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_third_config")
public class ThirdConfig {

    private Integer id;

    private Integer tenantId;
    /**
     * 人脸核身商户号
     */
    private String faceMerchantId;
    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;

    private Long createTime;

    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
