package com.xiliulou.electricity.entity.faq;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class FaqCategory {
    
    /**
     * id
     */
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
    private Long opUser;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 修改时间
     */
    private Long updateTime;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    
}
