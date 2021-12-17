package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (Faq)实体类
 *
 * @author Eclair
 * @since 2021-09-26 14:06:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_not_exist_sn")
public class NotExistSn {


    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /**
    * 内容
    */
    private String batteryName;

    private Integer eId;

    /**
     * 换电柜的新仓门号
     */
    private Integer cellNo;

    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;

    private Long createTime;

    private Long updateTime;

    private Integer tenantId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;


}
