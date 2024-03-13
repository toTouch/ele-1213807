package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xiliulou.electricity.enums.EleCabinetModelHeatingEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 换电柜型号表(TElectricityCabinetModel)实体类
 *
 * @author makejava
 * @since 2020-11-25 11:01:04
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_cabinet_model")
public class ElectricityCabinetModel {
    
    /**
     * 型号Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    /**
     * 厂家名称
     */
    @NotEmpty(message = "厂家名称不能为空!")
    private String manufacturerName;
    
    /**
     * 型号名称
     */
    @NotEmpty(message = "型号名称不能为空!")
    private String name;
    
    /**
     * 柜子的仓门数量
     */
    @NotNull(message = "柜子的仓门数量不能为空!")
    private Integer num;
    
    /**
     * 柜机尺寸
     */
    private String cabinetSize;
    
    /**
     * 仓位尺寸
     */
    private String cellSize;
    
    /**
     * 屏幕尺寸
     */
    private String screenSize;
    
    /**
     * 防水等级
     */
    private String waterproofGrade;
    
    /**
     * 电压
     */
    private String voltage;
    
    /**
     * 0--未删除，1--删除
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
    
    //租户id
    private Integer tenantId;
    
    /**
     * <p>
     * Description: 柜机加热支持
     * <pre>
     *        0:不支持
     *        1:支持
     *    </pre>
     * <h4>排期内快速实现</h4>
     * <a herf="https://benyun.feishu.cn/wiki/GrNjwBNZkipB5wkiws2cmsEDnVU#KZJedQiWgoiJpQxCPM0cErGqndR">12.2 电柜厂家型号（3条优化点）</a>
     * <p>ALTER TABLE t_electricity_cabinet_model ADD COLUMN heating TINYINT NOT NULL DEFAULT 0 COMMENT '柜机加热支持 --0不支持 --1支持';</p>
     *
     * @see EleCabinetModelHeatingEnum
     * </p>
     */
    private Integer heating;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
    
}
