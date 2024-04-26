package com.xiliulou.electricity.query;

import com.xiliulou.esign.entity.query.SignFieldPositionQuery;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author: Kenneth
 * @Date: 2023/7/19 20:23
 * @Description:
 */

@Data
public class SignFileQuery {

    @NotEmpty(message = "文件ID不能为空!")
    private String fileId;
    
    @NotEmpty(message = "组件位置信息不能为空!")
    private List<SignFieldPositionQuery> signFieldPositionList;
    
}
