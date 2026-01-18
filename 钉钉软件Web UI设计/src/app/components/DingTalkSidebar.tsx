import { MessageSquare, Phone, Users, FileText, Calendar, Settings, MoreHorizontal, User } from "lucide-react";
import { useState } from "react";

interface NavItem {
  id: string;
  icon: React.ElementType;
  label: string;
}

const navItems: NavItem[] = [
  { id: "message", icon: MessageSquare, label: "消息" },
  { id: "phone", icon: Phone, label: "电话" },
  { id: "contacts", icon: Users, label: "通讯录" },
  { id: "docs", icon: FileText, label: "文档" },
  { id: "calendar", icon: Calendar, label: "日历" },
  { id: "more", icon: MoreHorizontal, label: "更多" },
];

interface DingTalkSidebarProps {
  activeTab: string;
  onTabChange: (tab: string) => void;
}

export function DingTalkSidebar({ activeTab, onTabChange }: DingTalkSidebarProps) {
  return (
    <div className="w-16 bg-[#0089FF] flex flex-col items-center py-3 gap-1">
      {/* Logo */}
      <div className="w-10 h-10 bg-white rounded-lg flex items-center justify-center mb-2">
        <span className="text-[#0089FF] font-bold text-lg">钉</span>
      </div>

      {/* Navigation Items */}
      <div className="flex flex-col gap-0.5 flex-1">
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <button
              key={item.id}
              onClick={() => onTabChange(item.id)}
              className={`w-12 h-12 rounded-lg flex flex-col items-center justify-center gap-0.5 transition-all ${
                activeTab === item.id
                  ? "bg-white/20 text-white shadow-sm"
                  : "text-white/80 hover:bg-white/10 hover:text-white"
              }`}
            >
              <Icon className="w-5 h-5" />
              <span className="text-[10px] leading-none">{item.label}</span>
            </button>
          );
        })}
      </div>

      {/* Settings */}
      <button 
        onClick={() => onTabChange("settings")}
        className={`w-12 h-12 rounded-lg flex flex-col items-center justify-center gap-0.5 transition-all ${
          activeTab === "settings"
            ? "bg-white/20 text-white shadow-sm"
            : "text-white/80 hover:bg-white/10 hover:text-white"
        }`}
      >
        <Settings className="w-5 h-5" />
        <span className="text-[10px] leading-none">设置</span>
      </button>

      {/* User Avatar */}
      <button
        onClick={() => onTabChange("profile")}
        className={`w-10 h-10 rounded-full flex items-center justify-center transition-all mt-2 ${
          activeTab === "profile"
            ? "bg-white text-[#0089FF] shadow-sm"
            : "bg-white/20 hover:bg-white/30"
        }`}
      >
        <span className="text-white text-sm font-medium">我</span>
      </button>
    </div>
  );
}