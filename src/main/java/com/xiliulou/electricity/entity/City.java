package com.xiliulou.electricity.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
/**
 * (City)实体类
 *
 * @author makejava
 * @since 2020-11-25 16:20:18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("City")
public class City {
    
    private Integer cid;
    
    private String city;
    
    private Integer pid;


}