package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (Faq)实体类
 *
 * @author Eclair
 * @since 2021-09-26 14:06:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_faq")
public class Faq {


    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /**
    * 标题
    */
    private String title;
    /**
    * 内容
    */
    private String content;
    /**
    * 图片路径
    */
    private String pic;


    private Long createTime;

    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
