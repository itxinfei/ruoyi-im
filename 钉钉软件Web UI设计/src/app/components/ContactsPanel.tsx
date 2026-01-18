import { Search, Building2, Users, Phone, Mail, ChevronRight, Star, Video } from "lucide-react";
import { useState } from "react";
import { Input } from "@/app/components/ui/input";
import { ScrollArea } from "@/app/components/ui/scroll-area";
import { Button } from "@/app/components/ui/button";
import { Badge } from "@/app/components/ui/badge";

interface Contact {
  id: string;
  name: string;
  avatar: string;
  position: string;
  department: string;
  phone: string;
  email: string;
  isFavorite?: boolean;
}

const mockContacts: Contact[] = [
  {
    id: "1",
    name: "张伟",
    avatar: "张",
    position: "产品经理",
    department: "产品部",
    phone: "138-1234-5678",
    email: "zhangwei@company.com",
    isFavorite: true,
  },
  {
    id: "2",
    name: "李明",
    avatar: "李",
    position: "UI设计师",
    department: "设计部",
    phone: "139-2345-6789",
    email: "liming@company.com",
    isFavorite: true,
  },
  {
    id: "3",
    name: "王芳",
    avatar: "王",
    position: "前端工程师",
    department: "研发部",
    phone: "136-3456-7890",
    email: "wangfang@company.com",
  },
  {
    id: "4",
    name: "刘洋",
    avatar: "刘",
    position: "后端工程师",
    department: "研发部",
    phone: "137-4567-8901",
    email: "liuyang@company.com",
  },
  {
    id: "5",
    name: "陈晨",
    avatar: "陈",
    position: "测试工程师",
    department: "测试部",
    phone: "135-5678-9012",
    email: "chenchen@company.com",
  },
  {
    id: "6",
    name: "赵敏",
    avatar: "赵",
    position: "人力资源",
    department: "人事部",
    phone: "134-6789-0123",
    email: "zhaomin@company.com",
  },
  {
    id: "7",
    name: "孙强",
    avatar: "孙",
    position: "销售经理",
    department: "销售部",
    phone: "133-7890-1234",
    email: "sunqiang@company.com",
  },
  {
    id: "8",
    name: "周杰",
    avatar: "周",
    position: "运营专员",
    department: "运营部",
    phone: "132-8901-2345",
    email: "zhoujie@company.com",
  },
];

interface Department {
  id: string;
  name: string;
  count: number;
  icon: string;
}

const mockDepartments: Department[] = [
  { id: "1", name: "产品部", count: 12, icon: "产" },
  { id: "2", name: "设计部", count: 8, icon: "设" },
  { id: "3", name: "研发部", count: 35, icon: "研" },
  { id: "4", name: "测试部", count: 15, icon: "测" },
  { id: "5", name: "销售部", count: 20, icon: "销" },
  { id: "6", name: "运营部", count: 10, icon: "运" },
  { id: "7", name: "人事部", count: 6, icon: "人" },
  { id: "8", name: "财务部", count: 5, icon: "财" },
];

export function ContactsPanel() {
  const [selectedContact, setSelectedContact] = useState<Contact | null>(null);
  const [searchQuery, setSearchQuery] = useState("");

  const filteredContacts = mockContacts.filter(contact =>
    contact.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    contact.department.toLowerCase().includes(searchQuery.toLowerCase()) ||
    contact.position.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");

  return (
    <div className="flex-1 bg-white flex">
      {/* Left Panel - Departments & Contacts */}
      <div className="w-80 border-r border-gray-200 flex flex-col relative">
        <div className="p-4 border-b border-gray-200">
          <h2 className="text-xl mb-3">通讯录</h2>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
            <Input
              placeholder="搜索联系人"
              className="pl-9 bg-gray-50 border-0 h-9"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
        </div>

        <ScrollArea className="flex-1">
          {/* Departments Section */}
          {!searchQuery && (
            <div className="p-4 border-b border-gray-100">
              <div className="flex items-center gap-2 mb-3 text-sm font-medium text-gray-700">
                <Building2 className="w-4 h-4" />
                <span>组织架构</span>
              </div>
              <div className="space-y-1">
                {mockDepartments.map((dept) => (
                  <div
                    key={dept.id}
                    className="flex items-center justify-between p-2 rounded-lg hover:bg-gray-50 cursor-pointer transition-colors"
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-9 h-9 rounded-lg bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white shadow-sm">
                        <span className="text-xs">{dept.icon}</span>
                      </div>
                      <span className="text-sm">{dept.name}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs text-gray-500">{dept.count}</span>
                      <ChevronRight className="w-4 h-4 text-gray-400" />
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Contacts List */}
          <div className="p-4">
            <div className="flex items-center gap-2 mb-3 text-sm font-medium text-gray-700">
              <Users className="w-4 h-4" />
              <span>全部联系人</span>
              <Badge variant="secondary" className="ml-auto bg-gray-100">
                {filteredContacts.length}
              </Badge>
            </div>
            <div className="space-y-1">
              {filteredContacts.map((contact) => (
                <div
                  key={contact.id}
                  onClick={() => setSelectedContact(contact)}
                  className={`flex items-center gap-3 p-2 rounded-lg cursor-pointer transition-colors ${
                    selectedContact?.id === contact.id ? "bg-blue-50" : "hover:bg-gray-50"
                  }`}
                >
                  <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white flex-shrink-0 shadow-sm">
                    <span className="text-sm">{contact.avatar}</span>
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-1">
                      <span className="text-sm font-medium truncate">{contact.name}</span>
                      {contact.isFavorite && (
                        <Star className="w-3 h-3 text-yellow-500 fill-yellow-500 flex-shrink-0" />
                      )}
                    </div>
                    <p className="text-xs text-gray-500 truncate leading-tight">
                      {contact.position} · {contact.department}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </ScrollArea>
      </div>

      {/* Right Panel - Contact Details */}
      <div className="flex-1 bg-[#F7F8FA]">
        {selectedContact ? (
          <div className="h-full flex flex-col">
            {/* Header */}
            <div className="bg-white border-b border-gray-200 p-6">
              <div className="flex items-start gap-4">
                <div className="w-20 h-20 rounded-2xl bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white shadow-md">
                  <span className="text-3xl">{selectedContact.avatar}</span>
                </div>
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-2">
                    <h2 className="text-2xl font-medium">{selectedContact.name}</h2>
                    {selectedContact.isFavorite && (
                      <Star className="w-5 h-5 text-yellow-500 fill-yellow-500" />
                    )}
                  </div>
                  <p className="text-gray-600 mb-4">
                    {selectedContact.position} · {selectedContact.department}
                  </p>
                  <div className="flex gap-2">
                    <Button className="bg-[#0089FF] hover:bg-[#0078E8]">
                      <Mail className="w-4 h-4 mr-2" />
                      发消息
                    </Button>
                    <Button variant="outline" className="border-green-200 text-green-600 hover:bg-green-50">
                      <Phone className="w-4 h-4 mr-2" />
                      语音
                    </Button>
                    <Button variant="outline" className="border-blue-200 text-blue-600 hover:bg-blue-50">
                      <Video className="w-4 h-4 mr-2" />
                      视频
                    </Button>
                    <Button variant="outline">
                      {selectedContact.isFavorite ? "取消收藏" : "添加收藏"}
                    </Button>
                  </div>
                </div>
              </div>
            </div>

            {/* Contact Info */}
            <ScrollArea className="flex-1">
              <div className="p-6 space-y-6">
                {/* Basic Info */}
                <div className="bg-white rounded-lg p-5 shadow-sm">
                  <h3 className="font-medium mb-4 text-base">基本信息</h3>
                  <div className="space-y-3">
                    <div className="flex items-start py-2">
                      <span className="text-sm text-gray-500 w-24">姓名</span>
                      <span className="text-sm flex-1">{selectedContact.name}</span>
                    </div>
                    <div className="flex items-start py-2">
                      <span className="text-sm text-gray-500 w-24">职位</span>
                      <span className="text-sm flex-1">{selectedContact.position}</span>
                    </div>
                    <div className="flex items-start py-2">
                      <span className="text-sm text-gray-500 w-24">部门</span>
                      <span className="text-sm flex-1">{selectedContact.department}</span>
                    </div>
                  </div>
                </div>

                {/* Contact Info */}
                <div className="bg-white rounded-lg p-5 shadow-sm">
                  <h3 className="font-medium mb-4 text-base">联系方式</h3>
                  <div className="space-y-3">
                    <div className="flex items-start py-2">
                      <span className="text-sm text-gray-500 w-24">手机</span>
                      <span className="text-sm flex-1 text-blue-600 cursor-pointer hover:underline">{selectedContact.phone}</span>
                    </div>
                    <div className="flex items-start py-2">
                      <span className="text-sm text-gray-500 w-24">邮箱</span>
                      <span className="text-sm flex-1 text-blue-600 cursor-pointer hover:underline">{selectedContact.email}</span>
                    </div>
                  </div>
                </div>

                {/* Additional Info */}
                <div className="bg-white rounded-lg p-5 shadow-sm">
                  <h3 className="font-medium mb-4 text-base">其他信息</h3>
                  <div className="space-y-3">
                    <div className="flex items-start py-2">
                      <span className="text-sm text-gray-500 w-24">工号</span>
                      <span className="text-sm flex-1">2024{selectedContact.id.padStart(4, "0")}</span>
                    </div>
                    <div className="flex items-start py-2">
                      <span className="text-sm text-gray-500 w-24">入职时间</span>
                      <span className="text-sm flex-1">2023-05-15</span>
                    </div>
                  </div>
                </div>
              </div>
            </ScrollArea>
          </div>
        ) : (
          <div className="h-full flex items-center justify-center">
            <div className="text-center">
              <Users className="w-16 h-16 text-gray-300 mx-auto mb-4" />
              <p className="text-gray-400">选择联系人查看详情</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}