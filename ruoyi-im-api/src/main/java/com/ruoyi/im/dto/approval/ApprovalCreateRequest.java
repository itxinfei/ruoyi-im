package com.ruoyi.im.dto.approval;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 发起审批请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "发起审批请求")
public class ApprovalCreateRequest {

    @NotNull(message = "审批模板ID不能为空")
    @Schema(description = "审批模板ID", required = true, example = "1")
    private Long templateId;

    @NotBlank(message = "审批标题不能为空")
    @Schema(description = "审批标题", required = true, example = "请假申请")
    private String title;

    @NotNull(message = "表单数据不能为空")
    @Schema(description = "表单数据", required = true)
    private Map<String, Object> formData;
}