package com.xiliulou.electricity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.query.ModelBatteryDeposit;
import com.xiliulou.electricity.validator.CreateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

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
public class FranchiseeVO {
    /**
     * Id
     */
    private Integer id;
    /**
     * 门店名称
     */
    private String name;
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
    //城市名称
    private String cityName;
    //用户名称
    private String userName;

    private BigDecimal batteryDeposit;

    private Integer percent;

    /**
     * 加盟商押金类型 1--老（不分型号） 2--新（分型号）
     * */
    private Integer modelType;

    //新分型号押金
    private List<ModelBatteryDeposit> modelBatteryDepositList;

    //新分型号押金
    private String modelBatteryDeposit;


}
