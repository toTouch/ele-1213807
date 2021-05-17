package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 加盟商套餐绑定表(TFranchiseeBindCard)实体类
 *
 * @author makejava
 * @since 2021-04-16 15:12:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_franchisee_bind_card")
public class FranchiseeBindCard {
    /**
    * 主键Id
    */
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    /**
    * 加盟商id
    */
    private Integer franchiseeId;
    /**
    * 套餐id
    */
    private Integer cardId;


}
