package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 设备上下线(TEleOnLineLog)实体类
 *
 * @author makejava
 * @since 2022-08-16 09:28:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_on_line_log")
public class EleOnlineLog {

    private Long id;
    /**
     * 换电柜id
     */
    private Integer electricityId;
    /**
     * 客户端IP
     */
    private String clientIp;
    /**
     * offline下线，online上线
     */
    private String status;
    /**
     * 消息内容
     */
    private String msg;
    /**
     * 上报时间
     */
    private String appearTime;
    /**
     * 创建时间
     */
    private Long createTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
