package com.xiliulou.electricity.entity;
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
    
    private Long id;
    
    private Integer electricityCabinetId;
    
    private String electricityCabinetName;
    
    private String cellNo;
    
    private String msg;
    /**
    * (1--电柜硬件，2--业务警告，3--)
    */
    private Object type;
    /**
    * (1--未读消息，1--已读消息)
    */
    private Object status;
    
    private Long operateUid;
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