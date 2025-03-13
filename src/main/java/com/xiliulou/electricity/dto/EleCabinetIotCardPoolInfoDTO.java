package com.xiliulou.electricity.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;


/**
 * @Description:
 * @Author: RenHang
 * @Date: 2025/03/13
 */

@Data
public class EleCabinetIotCardPoolInfoDTO {
    
    private Integer code;
    
    private CardPoolInfoDTO data;



    @Data
    public static class CardPoolInfoDTO {

        private List<Map<String, Object>> rows;

    }

}
