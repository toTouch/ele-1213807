package com.xiliulou.electricity.entity.faq;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class Faq {
    
    /**
     * id
     */
    private Long id;
    
    /**
     * 分类id
     */
    private Long typeId;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 答案
     */
    private String answer;
    
    /**
     * 上下架 1表示上架  0表示下架
     */
    private Integer onShelf;
    
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
