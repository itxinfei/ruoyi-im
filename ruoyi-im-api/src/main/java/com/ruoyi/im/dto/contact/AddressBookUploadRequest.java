package com.ruoyi.im.dto.contact;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 通讯录上传请求
 *
 * @author ruoyi
 */
@Schema(description = "通讯录上传请求")
@Validated
public class AddressBookUploadRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 联系人列表
     */
    @Schema(description = "联系人列表", required = true)
    @NotEmpty(message = "联系人列表不能为空")
    @Valid
    private List<ContactItem> contacts;

    /**
     * 获取联系人列表
     */
    public List<ContactItem> getContacts() {
        return contacts;
    }

    /**
     * 设置联系人列表
     */
    public void setContacts(List<ContactItem> contacts) {
        this.contacts = contacts;
    }

    /**
     * 联系人信息
     */
    @Schema(description = "联系人信息")
    @Validated
    public static class ContactItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 姓名
         */
        @Schema(description = "姓名", required = true, example = "张三")
        private String name;

        /**
         * 手机号
         */
        @Schema(description = "手机号", required = true, example = "13800138000")
        private String phone;

        /**
         * 获取姓名
         */
        public String getName() {
            return name;
        }

        /**
         * 设置姓名
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * 获取手机号
         */
        public String getPhone() {
            return phone;
        }

        /**
         * 设置手机号
         */
        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}
