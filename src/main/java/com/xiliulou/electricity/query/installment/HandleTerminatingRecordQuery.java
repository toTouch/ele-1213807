package com.xiliulou.electricity.query.installment;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/9/5 17:39
 */
@Data
public class HandleTerminatingRecordQuery {
    
    @NotNull(message = "解约申请id不可为空")
    private Long id;
    
    @NotNull(message = "审核状态不可为空")
    private Integer status;
    
    private String opinion;
}
