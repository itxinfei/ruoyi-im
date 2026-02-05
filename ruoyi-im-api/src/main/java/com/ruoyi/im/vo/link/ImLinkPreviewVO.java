package com.ruoyi.im.vo.link;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 链接预览返回值对象
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "链接预览信息")
public class ImLinkPreviewVO {

    @Schema(description = "链接URL")
    @JsonProperty("url")
    private String url;

    @Schema(description = "页面标题")
    @JsonProperty("title")
    private String title;

    @Schema(description = "页面描述")
    @JsonProperty("description")
    private String description;

    @Schema(description = "预览图片URL")
    @JsonProperty("image")
    private String image;

    @Schema(description = "网站图标URL")
    @JsonProperty("favicon")
    private String favicon;

    @Schema(description = "站点名称")
    @JsonProperty("site_name")
    private String siteName;
}
