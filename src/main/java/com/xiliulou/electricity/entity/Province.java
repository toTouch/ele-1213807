package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * (Province)实体类
 *
 * @author Eclair
 * @since 2021-01-21 18:05:46
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_province")
public class Province {
    
    private Integer id;
    /**
    * 省份code
    */
    private String code;
    /**
    * 名称
    */
    private String name;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}