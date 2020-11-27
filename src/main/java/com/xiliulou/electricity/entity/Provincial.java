package com.xiliulou.electricity.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
/**
 * (Provincial)实体类
 *
 * @author makejava
 * @since 2020-11-25 16:20:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("Provincial")
public class Provincial {
    
    private Integer pid;
    
    private String provincial;


}