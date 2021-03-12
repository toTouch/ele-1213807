package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 门店表(TStore)实体类
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@Data
public class FranchiseeAddAndUpdate {
    /**
    * 加盟商Id
    */
    @NotNull(message = "加盟商Id不能为空!", groups = {UpdateGroup.class})
    private Integer id;
    /**
    * 加盟商名称
    */
    @NotEmpty(message = "加盟商名称不能为空!", groups = {CreateGroup.class})
    private String name;
    /**
    * 城市编号
    */
    @NotNull(message = "城市编号不能为空!", groups = {CreateGroup.class})
    private Integer cid;
    /**
     * 租电池押金
     */
    @NotNull(message = "租电池押金不能为空!", groups = {CreateGroup.class})
    private BigDecimal batteryDeposit;
    /**
    * 0--正常 1--删除
    */
    private Object delFlag;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 更新时间
    */
    private Long updateTime;

    /**
     * uid
     */
    private Long uid;


}