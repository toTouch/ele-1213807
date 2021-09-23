package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class CallBackQuery {
    private List<String> fileNameList;
    private Long otherId;
    @NotNull(message = "fileType不能为空!", groups = {UpdateGroup.class})
    private Integer fileType;
}
