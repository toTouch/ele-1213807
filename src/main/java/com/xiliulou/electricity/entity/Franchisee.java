package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * (Franchisee)实体类
 *
 * @author lxc
 * @since 2020-11-25 11:00:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_franchisee")
public class Franchisee {
    /**
     * Id
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /**
     * 城市Id
     */
    private Integer cid;
    /**
     * uid
     */
    private Long uid;
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;
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