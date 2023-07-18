package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author: Kenneth
 * @Date: 2023/7/7 23:15
 * @Description:
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EleEsignConfigQuery {

    private Integer id;

    @NotBlank(message = "商户appId不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String appId;

    @NotBlank(message = "商户appSecret不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private String appSecret;

    @NotBlank(message = "商户设定模板ID不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private String docTemplateId;

    @NotBlank(message = "商户设定签署文件名不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private String signFileName;

    @NotBlank(message = "商户设定签署流程名不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private String signFlowName;

}
