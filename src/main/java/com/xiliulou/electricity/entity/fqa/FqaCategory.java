package com.xiliulou.electricity.entity.fqa;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("t_fqa_category")
@Builder
public class FqaCategory {
    
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 分类
     */
    private String type;
    
    /**
     * 排序
     */
    private BigDecimal sort;
    
    /**
     * 操作人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long opUser;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createTime;
    
    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateTime;
    
    /**
     * 租户id
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer tenantId;
    
    
}
