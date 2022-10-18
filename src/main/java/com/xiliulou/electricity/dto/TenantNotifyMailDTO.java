package com.xiliulou.electricity.dto;

import com.xiliulou.electricity.vo.TenantNotifyMailVO;
import lombok.Data;

import java.util.List;

@Data
public class TenantNotifyMailDTO {

    private Long id;

    private String mail;

    private Long tenantId;

    private String tenantName;

    private Long createTime;

    private Long updateTime;
}
