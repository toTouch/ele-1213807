package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author : eclair
 * @date : 2021/9/26 5:02 下午
 */
@Data
public class FaqQuery {
    @NotNull(message = "id不能为空", groups = UpdateGroup.class)
    private Integer id;

    @NotNull(message = "标题不能为空", groups = CreateGroup.class)
    private String title;

    @NotNull(message = "内容不能为空", groups = CreateGroup.class)
    private String content;

    private String pic;
}
