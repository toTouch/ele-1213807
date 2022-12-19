package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * 车辆型号标签表(CarModelTag)表实体类
 *
 * @author zzlong
 * @since 2022-12-14 15:55:53
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_car_model_tag")
public class CarModelTag {
    /**
     * Id
     */
    private Long id;
    /**
     * 标签
     */
    private String title;
    /**
     * 车辆型号id
     */
    private Long carModelId;
    /**
     * 排序
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

}
