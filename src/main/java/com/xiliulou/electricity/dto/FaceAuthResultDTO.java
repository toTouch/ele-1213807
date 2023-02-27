package com.xiliulou.electricity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-03-11:16
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FaceAuthResultDTO {

    private String EidDesKey;

    private String EidCode;

    private String EidSign;

    private String EidUserInfo;

    private String OcrAddress;

    private String OcrBirth;

    private String OcrGender;

    private String OcrNation;
    
    private Long ErrCode;
    
    private String ErrMsg;
    
    private Long LiveStatus;
    
    private String LiveMsg;
}
