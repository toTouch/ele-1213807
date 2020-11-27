package com.xiliulou.electricity.query;

import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PageQuery {
    @NotNull(message = "分页参数不能为空!")
    private Long offset;
    @NotNull(message = "分页参数不能为空!")
    private Long size;
}
