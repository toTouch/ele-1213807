package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (SplitAccountFailRecord)实体类
 *
 * @author lxc
 * @since 2021-09-13 20:09:25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_split_account_fail_record")
public class SplitAccountFailRecord {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    /**
     * 分账类型1--商户， 2--代理商
     */
    private Integer type;
    /**
     * 商户或者代理尚id
     */
    private Long accountId;
    /**
     * 这次订单收益
     */
    private Double payAmount;
    /**
     * 应该分润比例
     */
    private Integer percent;

    private Long createTime;

    public static final Integer TYPE_STORE = 1;
    public static final Integer TYPE_FRANCHISEE = 2;




}
