package com.xiliulou.electricity.entity.operate;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * <p>
 * Description: This class is UserOperataLogTypeEntity!
 * </p>
 * <p>Project: xiliulou-ele-data-batch</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/3/26
 **/
@Data
@TableName("t_user_operate_log")
public class UserOperateLogEntity {
    
    /**
     * 操作用户id
     **/
    @TableId(value = "id", type = IdType.AUTO)
    private Long uid;
    
    /**
     * 租户id
     **/
    private Long tenantId;
    
    /**
     * 操作类型id
     **/
    private String operateType;
    
    /**
     * 操作人名称
     **/
    private String operateUsername;
    
    /**
     * 操作人ip
     **/
    private String operateIp;
    
    /**
     * 操作记录内容
     **/
    private String operateContent;
    
    /**
     * 操作时间
     **/
    private String operateTime;
    
    ;
}
