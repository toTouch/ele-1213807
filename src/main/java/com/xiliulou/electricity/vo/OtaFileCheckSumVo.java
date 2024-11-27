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
    
    private String oldCoreSha256HexCloud;
    
    private String oldSubSha256HexCloud;
    
    private String oldCoreVersionCloud;
    
    private String oldSubVersionCloud;
    
    /**
     * 旧六合一版本号
     */
    private String sixVersionCloud;
    
    /**
     * 旧六合一sha256
     */
    private String sixSha256HexCloud;
    
    /**
     * 新六合一版本号
     */
    private String newSixVersionCloud;
    
    /**
     * 新六合一sha256
     */
    private String newSixSha256HexCloud;
    
    private String coreNameEle;
    
    private String subNameEle;
    
    private String coreSha256HexEle;
    
    private String subSha256HexEle;
    
    private Integer fileType;
}
