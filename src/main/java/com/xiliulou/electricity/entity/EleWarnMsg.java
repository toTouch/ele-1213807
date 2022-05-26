package com.xiliulou.electricity.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
/**
 * 换电柜异常上报信息(TEleWarnMsg)实体类
 *
 * @author makejava
 * @since 2021-03-29 14:12:45
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_warn_msg")
public class EleWarnMsg {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    private Integer electricityCabinetId;

    private String electricityCabinetName;

    private Integer cellNo;

    private String msg;
    /**
    *
    */
    private Integer type;
    /**
    * (1--未读消息，0--已读消息)
    */
    private Integer status;

    private Long operateUid;
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
     * 硬件异常具体分类
     */
    private Integer code;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    //未读通知
    public static final Integer STATUS_UNREAD = 1;
    //已读通知
    public static final Integer STATUS_HAVE_READ = 0;

    //电池被异常取走
    public static final Integer  TYPE_BATTERY_TAKE_EXCEPTION = 1;
    //解析消息失败
    public static final Integer MESSAGE_PARSE_ERROR = 101;
    //串口异常
    public static final Integer SERIAL_PORT_NO_DATA = 102;


}
