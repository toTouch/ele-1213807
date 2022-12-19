package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * 门店详情表(StoreDetail)表实体类
 *
 * @author zzlong
 * @since 2022-12-14 13:54:40
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_store_detail")
public class StoreDetail {
    /**
     * Id
     */
    private Long id;
    /**
     * 详情
     */
    private String detail;
    /**
     * 其它属性
     */
    private String attr;
    /**
     * 门店Id
     */
    private Long storeId;
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

}
