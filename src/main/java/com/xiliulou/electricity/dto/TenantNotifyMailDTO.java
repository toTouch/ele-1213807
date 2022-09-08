package com.xiliulou.electricity.dto;

import com.xiliulou.electricity.vo.TenantNotifyMailVO;
import lombok.Data;

import java.util.List;

@Data
public class TenantNotifyMailDTO {
    private Long tenantId;

    private List<TenantNotifyMailVO> tenantNotifyMailList;
}
