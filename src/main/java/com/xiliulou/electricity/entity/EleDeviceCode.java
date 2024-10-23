package com.xiliulou.electricity.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (EleDeviceCode)实体类
 *
 * @author zhaozhilong
 * @since 2024-10-11 09:27:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_device_code")
public class EleDeviceCode implements Serializable {
    
    private static final long serialVersionUID = 404639289716075901L;
    
    private Long id;
    
    private String productKey;
    
    private String deviceName;
    
    /**
     * 密钥
     */
    private String secret;
    
    /**
     * 在线状态（0--在线，1--离线）
     */
    private Integer onlineStatus;
    
    private String remark;
    
    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;
    
    private Long createTime;
    
    private Long updateTime;
    
    
}

