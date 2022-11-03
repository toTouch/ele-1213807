package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * 换电柜仓门其它属性(BoxOtherProperties)表实体类
 *
 * @author zzlong
 * @since 2022-11-03 19:51:35
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_box_other_properties")
public class BoxOtherProperties {
    
    /**
     * Id
     */
    private Long id;
    
    /**
     * 所属换电柜柜Id
     */
    private Integer electricityCabinetId;
    
    /**
     * 仓门号
     */
    private String cellNo;
    
    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 锁仓原因
     */
    private Integer lockReason;
    
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
