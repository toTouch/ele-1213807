package com.xiliulou.electricity.query.operate;


import lombok.Data;

/**
 * <p>
 * Description: This class is OperateQuery!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/3/29
 **/
@Data
public class OperateQuery {
    
    private Long offset = 0L;
    
    private Long size = 10L;
    
    private String operateType;
    
    private Long uid;
    
    private Long startTime;
    
    private Long endTime;
    
    private Long tenantId;
    
    public Long getSize() {
        if (this.size >= 50 || this.size<=0){
            return 50L;
        }
        return this.size;
    }
    
    public Long getOffset() {
        if (this.offset <=0 ){
            return 0L;
        }
        return offset;
    }
}
