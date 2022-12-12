package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * (Region)表实体类
 *
 * @author zzlong
 * @since 2022-12-12 11:38:20
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_region")
public class Region {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 区域code
     */
    private String code;
    /**
     * pid
     */
    private Integer pid;
    /**
     * 区域名称
     */
    private String name;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
