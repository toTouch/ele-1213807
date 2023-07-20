package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author: Kenneth
 * @Date: 2023/7/19 20:11
 * @Description:
 */

@Data
public class CreateFileVO {

    private String fileId;

    private Float componentPositionX;

    private Float componentPositionY;

    private Integer componentPageNum;

}
