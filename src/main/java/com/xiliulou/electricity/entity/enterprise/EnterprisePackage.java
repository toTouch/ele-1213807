package com.xiliulou.electricity.entity.enterprise;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 企业关联套餐表(EnterprisePackage)实体类
 *
 * @author Eclair
 * @since 2023-09-14 10:15:42
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_enterprise_package")
public class EnterprisePackage {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * 企业id
     */
    private Long enterpriseId;
    /**
     * 套餐id
     */
    private Long packageId;
    /**
     * 套餐类型(1-换电套餐, 2-租车套餐, 3-车电一体套餐)
     */
    private Integer packageType;
    /**
     * 租户ID
     */
    private Integer tenantId;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    //套餐类型(1-换电套餐, 2-租车套餐, 3-车电一体套餐)
    public static final Integer PACKAGE_TYPE_BATTERY = 1;
    public static final Integer PACKAGE_TYPE_CAR = 2;
    public static final Integer PACKAGE_TYPE_BATTERY_CAR = 3;

}
