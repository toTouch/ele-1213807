package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zgw
 * @date 2022/10/13 11:30
 * @mood
 */
@Data
public class OtaFileCheckSumVo {
    
    private String coreSha256HexCloud;
    
    private String subSha256HexCloud;
    
    private String coreVersionCloud;
    
    private String subVersionCloud;
    
    private String coreNameEle;
    
    private String subNameEle;
    
    private String coreSha256HexEle;
    
    private String subSha256HexEle;
}
