package com.xiliulou.electricity.vo.supper;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author: Ant
 * @Date 2024/4/23
 * @Description:
 **/
@Data
public class DelBatteryVo implements Serializable {
    
    private static final long serialVersionUID = 1448212894091527215L;
    
    private List<String> successSnList;
    
    private List<String> failedSnList;
}
