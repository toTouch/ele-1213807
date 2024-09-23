package com.xiliulou.electricity.query.installment;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/9/5 17:24
 */
@Data
public class CreateTerminatingRecordQuery {
    /**
     * 请求签约号，唯一
     */
    @NotNull(message = "请求签约号不可为空")
    private String externalAgreementNo;
    
    private String reason;
}
