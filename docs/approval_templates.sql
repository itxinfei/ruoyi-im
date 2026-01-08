-- =============================================
-- 审批模板初始化数据
-- =============================================

-- 1. 请假申请模板
INSERT INTO im_approval_template (name, code, category, description, form_schema, flow_config, icon, is_system, status, sort_order, create_time, update_time)
VALUES (
    '请假申请',
    'LEAVE_REQUEST',
    '请假',
    '员工请假申请审批流程，包括请假类型、请假时间、请假事由等信息',
    '[
        {"field": "leaveType", "label": "请假类型", "type": "select", "required": true, "options": ["事假", "病假", "年假", "调休", "婚假", "产假", "其他"]},
        {"field": "startDate", "label": "开始日期", "type": "date", "required": true},
        {"field": "endDate", "label": "结束日期", "type": "date", "required": true},
        {"field": "leaveDays", "label": "请假天数", "type": "number", "required": true, "min": 0.5, "max": 365},
        {"field": "reason", "label": "请假事由", "type": "textarea", "required": true, "maxLength": 500},
        {"field": "attachment", "label": "附件", "type": "file", "required": false}
    ]',
    '[
        {"nodeName": "部门主管审批", "nodeType": "APPROVE", "approvers": ["2"], "approveType": "ANY"},
        {"nodeName": "人事审批", "nodeType": "APPROVE", "approvers": ["3"], "approveType": "ANY"}
    ]',
    'leave',
    true,
    'ACTIVE',
    1,
    NOW(),
    NOW()
);

-- 2. 报销申请模板
INSERT INTO im_approval_template (name, code, category, description, form_schema, flow_config, icon, is_system, status, sort_order, create_time, update_time)
VALUES (
    '报销申请',
    'REIMBURSEMENT',
    '报销',
    '费用报销申请审批流程，包括报销类型、报销金额、费用明细等信息',
    '[
        {"field": "reimbursementType", "label": "报销类型", "type": "select", "required": true, "options": ["差旅费", "交通费", "办公用品", "招待费", "通讯费", "其他"]},
        {"field": "amount", "label": "报销金额(元)", "type": "number", "required": true, "min": 0, "precision": 2},
        {"field": "expenseDate", "label": "费用发生日期", "type": "date", "required": true},
        {"field": "description", "label": "费用说明", "type": "textarea", "required": true, "maxLength": 500},
        {"field": "invoiceImages", "label": "发票图片", "type": "file", "required": true, "multiple": true}
    ]',
    '[
        {"nodeName": "部门主管审批", "nodeType": "APPROVE", "approvers": ["2"], "approveType": "ANY"},
        {"nodeName": "财务审批", "nodeType": "APPROVE", "approvers": ["4"], "approveType": "ANY"}
    ]',
    'money',
    true,
    'ACTIVE',
    2,
    NOW(),
    NOW()
);

-- 3. 出差申请模板
INSERT INTO im_approval_template (name, code, category, description, form_schema, flow_config, icon, is_system, status, sort_order, create_time, update_time)
VALUES (
    '出差申请',
    'BUSINESS_TRIP',
    '出差',
    '员工出差申请审批流程，包括出差地点、出差时间、出差事由等信息',
    '[
        {"field": "destination", "label": "出差地点", "type": "text", "required": true, "maxLength": 100},
        {"field": "startDate", "label": "开始日期", "type": "date", "required": true},
        {"field": "endDate", "label": "结束日期", "type": "date", "required": true},
        {"field": "tripDays", "label": "出差天数", "type": "number", "required": true, "min": 1},
        {"field": "transportation", "label": "交通方式", "type": "select", "required": true, "options": ["飞机", "高铁", "火车", "汽车", "其他"]},
        {"field": "estimatedBudget", "label": "预计费用(元)", "type": "number", "required": true, "min": 0},
        {"field": "reason", "label": "出差事由", "type": "textarea", "required": true, "maxLength": 500}
    ]',
    '[
        {"nodeName": "部门主管审批", "nodeType": "APPROVE", "approvers": ["2"], "approveType": "ANY"},
        {"nodeName": "总经理审批", "nodeType": "APPROVE", "approvers": ["5"], "approveType": "ANY"}
    ]',
    'location',
    true,
    'ACTIVE',
    3,
    NOW(),
    NOW()
);

-- 4. 采购申请模板
INSERT INTO im_approval_template (name, code, category, description, form_schema, flow_config, icon, is_system, status, sort_order, create_time, update_time)
VALUES (
    '采购申请',
    'PURCHASE',
    '采购',
    '物品采购申请审批流程，包括采购物品、数量、预算等信息',
    '[
        {"field": "itemCategory", "label": "物品类别", "type": "select", "required": true, "options": ["办公用品", "设备", "软件", "其他"]},
        {"field": "itemNames", "label": "采购物品", "type": "textarea", "required": true, "placeholder": "请详细列出采购物品名称、规格、数量"},
        {"field": "quantity", "label": "数量", "type": "number", "required": true, "min": 1},
        {"field": "budget", "label": "预算金额(元)", "type": "number", "required": true, "min": 0},
        {"field": "urgentLevel", "label": "紧急程度", "type": "select", "required": true, "options": ["普通", "紧急", "非常紧急"]},
        {"field": "reason", "label": "采购理由", "type": "textarea", "required": true, "maxLength": 500}
    ]',
    '[
        {"nodeName": "部门主管审批", "nodeType": "APPROVE", "approvers": ["2"], "approveType": "ANY"},
        {"nodeName": "财务审批", "nodeType": "APPROVE", "approvers": ["4"], "approveType": "ANY"},
        {"nodeName": "总经理审批", "nodeType": "APPROVE", "approvers": ["5"], "approveType": "ANY"}
    ]',
    'shopping',
    true,
    'ACTIVE',
    4,
    NOW(),
    NOW()
);

-- 5. 用印申请模板
INSERT INTO im_approval_template (name, code, category, description, form_schema, flow_config, icon, is_system, status, sort_order, create_time, update_time)
VALUES (
    '用印申请',
    'SEAL_APPLICATION',
    '行政',
    '公章使用申请审批流程，包括用印类型、文件数量、用印事由等信息',
    '[
        {"field": "sealType", "label": "印章类型", "type": "select", "required": true, "options": ["公章", "合同章", "财务章", "法人章", "其他"]},
        {"field": "documentType", "label": "文件类型", "type": "select", "required": true, "options": ["合同", "证明", "报表", "通知", "其他"]},
        {"field": "documentCount", "label": "用印数量", "type": "number", "required": true, "min": 1},
        {"field": "reason", "label": "用印事由", "type": "textarea", "required": true, "maxLength": 500}
    ]',
    '[
        {"nodeName": "部门主管审批", "nodeType": "APPROVE", "approvers": ["2"], "approveType": "ANY"},
        {"nodeName": "行政部审批", "nodeType": "APPROVE", "approvers": ["6"], "approveType": "ANY"}
    ]',
    'edit',
    true,
    'ACTIVE',
    5,
    NOW(),
    NOW()
);

-- 6. 加班申请模板
INSERT INTO im_approval_template (name, code, category, description, form_schema, flow_config, icon, is_system, status, sort_order, create_time, update_time)
VALUES (
    '加班申请',
    'OVERTIME',
    '考勤',
    '员工加班申请审批流程，包括加班时间、加班事由等信息',
    '[
        {"field": "startDate", "label": "加班开始日期", "type": "datetime", "required": true},
        {"field": "endDate", "label": "加班结束日期", "type": "datetime", "required": true},
        {"field": "hours", "label": "加班时长(小时)", "type": "number", "required": true, "min": 0.5, "max": 24},
        {"field": "overtimeType", "label": "加班类型", "type": "select", "required": true, "options": ["工作日加班", "周末加班", "节假日加班"]},
        {"field": "reason", "label": "加班事由", "type": "textarea", "required": true, "maxLength": 500}
    ]',
    '[
        {"nodeName": "部门主管审批", "nodeType": "APPROVE", "approvers": ["2"], "approveType": "ANY"}
    ]',
    'clock',
    true,
    'ACTIVE',
    6,
    NOW(),
    NOW()
);

-- 7. 转正申请模板
INSERT INTO im_approval_template (name, code, category, description, form_schema, flow_config, icon, is_system, status, sort_order, create_time, update_time)
VALUES (
    '转正申请',
    'REGULARIZATION',
    '人事',
    '员工转正申请审批流程，包括试用期总结、自我评价等信息',
    '[
        {"field": "entryDate", "label": "入职日期", "type": "date", "required": true},
        {"field": "probationEnd", "label": "试用期结束日期", "type": "date", "required": true},
        {"field": "workSummary", "label": "试用期工作总结", "type": "textarea", "required": true, "maxLength": 2000},
        {"field": "selfAssessment", "label": "自我评价", "type": "textarea", "required": true, "maxLength": 1000},
        {"field": "futurePlan", "label": "未来工作计划", "type": "textarea", "required": true, "maxLength": 1000}
    ]',
    '[
        {"nodeName": "部门主管审批", "nodeType": "APPROVE", "approvers": ["2"], "approveType": "ANY"},
        {"nodeName": "人事审批", "nodeType": "APPROVE", "approvers": ["3"], "approveType": "ANY"}
    ]',
    'user',
    true,
    'ACTIVE',
    7,
    NOW(),
    NOW()
);

-- 8. 公文发布模板
INSERT INTO im_approval_template (name, code, category, description, form_schema, flow_config, icon, is_system, status, sort_order, create_time, update_time)
VALUES (
    '公文发布',
    'DOCUMENT_PUBLISH',
    '行政',
    '公司公文发布审批流程，包括公文标题、公文内容、发布范围等信息',
    '[
        {"field": "docTitle", "label": "公文标题", "type": "text", "required": true, "maxLength": 100},
        {"field": "docType", "label": "公文类型", "type": "select", "required": true, "options": ["通知", "通告", "决定", "命令", "其他"]},
        {"field": "docContent", "label": "公文内容", "type": "textarea", "required": true, "maxLength": 5000},
        {"field": "publishScope", "label": "发布范围", "type": "select", "required": true, "options": ["全员", "部门", "特定人员"]},
        {"field": "attachment", "label": "附件", "type": "file", "required": false}
    ]',
    '[
        {"nodeName": "部门主管审核", "nodeType": "APPROVE", "approvers": ["2"], "approveType": "ANY"},
        {"nodeName": "办公室审核", "nodeType": "APPROVE", "approvers": ["7"], "approveType": "ANY"},
        {"nodeName": "领导签发", "nodeType": "APPROVE", "approvers": ["5"], "approveType": "ANY"}
    ]',
    'document',
    true,
    'ACTIVE',
    8,
    NOW(),
    NOW()
);
