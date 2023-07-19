package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author: Kenneth
 * @Date: 2023/7/19 20:23
 * @Description:
 */

@Data
public class SignFileQuery {

    @NotEmpty(message = "文件ID不能为空!")
    private String fileId;

    @NotNull(message = "签署坐标X不能为空!")
    private Float componentPositionX;

    @NotNull(message = "签署坐标Y不能为空!")
    private Float componentPositionY;

    @NotNull(message = "签署页码不能为空!")
    private Integer componentPageNum;
}
