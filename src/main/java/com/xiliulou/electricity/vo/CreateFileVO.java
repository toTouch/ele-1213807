package com.xiliulou.electricity.vo;

import com.xiliulou.esign.entity.query.SignFieldPositionQuery;
import lombok.Data;

import java.util.List;

/**
 * @author: Kenneth
 * @Date: 2023/7/19 20:11
 * @Description:
 */

@Data
public class CreateFileVO {

    private String fileId;
    
    private List<SignFieldPositionQuery> signFieldPositionList;

}
