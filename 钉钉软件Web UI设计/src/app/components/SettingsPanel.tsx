import { User, Bell, Lock, Eye, Globe, Palette, HelpCircle, Info, LogOut, ChevronRight } from "lucide-react";
import { ScrollArea } from "@/app/components/ui/scroll-area";
import { Switch } from "@/app/components/ui/switch";
import { Button } from "@/app/components/ui/button";
import { Separator } from "@/app/components/ui/separator";

interface SettingItem {
  id: string;
  icon: React.ElementType;
  label: string;
  description?: string;
  type: "link" | "toggle";
  value?: boolean;
}

const accountSettings: SettingItem[] = [
  { id: "profile", icon: User, label: "个人资料", description: "修改头像、昵称等信息", type: "link" },
  { id: "account", icon: Lock, label: "账号与安全", description: "密码、手机号、邮箱", type: "link" },
];

const notificationSettings: SettingItem[] = [
  { id: "message", icon: Bell, label: "消息通知", value: true, type: "toggle" },
  { id: "meeting", icon: Bell, label: "会议通知", value: true, type: "toggle" },
  { id: "approval", icon: Bell, label: "审批通知", value: true, type: "toggle" },
  { id: "sound", icon: Bell, label: "声音提醒", value: false, type: "toggle" },
];

const privacySettings: SettingItem[] = [
  { id: "online", icon: Eye, label: "在线状态", description: "显示我的在线状态", type: "link" },
  { id: "read", icon: Eye, label: "已读回执", value: true, type: "toggle" },
  { id: "typing", icon: Eye, label: "正在输入", value: true, type: "toggle" },
];

const generalSettings: SettingItem[] = [
  { id: "language", icon: Globe, label: "语言", description: "简体中文", type: "link" },
  { id: "theme", icon: Palette, label: "主题", description: "跟随系统", type: "link" },
];

const otherSettings: SettingItem[] = [
  { id: "help", icon: HelpCircle, label: "帮助与反馈", type: "link" },
  { id: "about", icon: Info, label: "关于钉钉", description: "版本 7.0.0", type: "link" },
];

export function SettingsPanel() {
  return (
    <div className="flex-1 bg-[#F7F8FA]">
      <ScrollArea className="h-full">
        <div className="max-w-4xl mx-auto p-6 space-y-6">
          {/* Header */}
          <div>
            <h1 className="text-2xl mb-1">设置</h1>
            <p className="text-sm text-gray-500">管理你的账号和应用偏好设置</p>
          </div>

          {/* Account Settings */}
          <div className="bg-white rounded-lg p-4">
            <h2 className="text-base font-medium mb-4">账号设置</h2>
            <div className="space-y-1">
              {accountSettings.map((setting, index) => {
                const Icon = setting.icon;
                return (
                  <div key={setting.id}>
                    <button className="w-full flex items-center justify-between p-3 rounded-lg hover:bg-gray-50 transition-colors">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center">
                          <Icon className="w-5 h-5 text-blue-600" />
                        </div>
                        <div className="text-left">
                          <p className="text-sm font-medium">{setting.label}</p>
                          {setting.description && (
                            <p className="text-xs text-gray-500">{setting.description}</p>
                          )}
                        </div>
                      </div>
                      <ChevronRight className="w-5 h-5 text-gray-400" />
                    </button>
                    {index < accountSettings.length - 1 && <Separator />}
                  </div>
                );
              })}
            </div>
          </div>

          {/* Notification Settings */}
          <div className="bg-white rounded-lg p-4">
            <h2 className="text-base font-medium mb-4">通知设置</h2>
            <div className="space-y-1">
              {notificationSettings.map((setting, index) => {
                const Icon = setting.icon;
                return (
                  <div key={setting.id}>
                    <div className="flex items-center justify-between p-3 rounded-lg hover:bg-gray-50 transition-colors">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-lg bg-green-50 flex items-center justify-center">
                          <Icon className="w-5 h-5 text-green-600" />
                        </div>
                        <div className="text-left">
                          <p className="text-sm font-medium">{setting.label}</p>
                        </div>
                      </div>
                      <Switch defaultChecked={setting.value} />
                    </div>
                    {index < notificationSettings.length - 1 && <Separator />}
                  </div>
                );
              })}
            </div>
          </div>

          {/* Privacy Settings */}
          <div className="bg-white rounded-lg p-4">
            <h2 className="text-base font-medium mb-4">隐私设置</h2>
            <div className="space-y-1">
              {privacySettings.map((setting, index) => {
                const Icon = setting.icon;
                return (
                  <div key={setting.id}>
                    <div className="flex items-center justify-between p-3 rounded-lg hover:bg-gray-50 transition-colors">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-lg bg-purple-50 flex items-center justify-center">
                          <Icon className="w-5 h-5 text-purple-600" />
                        </div>
                        <div className="text-left">
                          <p className="text-sm font-medium">{setting.label}</p>
                          {setting.description && (
                            <p className="text-xs text-gray-500">{setting.description}</p>
                          )}
                        </div>
                      </div>
                      {setting.type === "toggle" ? (
                        <Switch defaultChecked={setting.value} />
                      ) : (
                        <ChevronRight className="w-5 h-5 text-gray-400" />
                      )}
                    </div>
                    {index < privacySettings.length - 1 && <Separator />}
                  </div>
                );
              })}
            </div>
          </div>

          {/* General Settings */}
          <div className="bg-white rounded-lg p-4">
            <h2 className="text-base font-medium mb-4">通用设置</h2>
            <div className="space-y-1">
              {generalSettings.map((setting, index) => {
                const Icon = setting.icon;
                return (
                  <div key={setting.id}>
                    <button className="w-full flex items-center justify-between p-3 rounded-lg hover:bg-gray-50 transition-colors">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-lg bg-orange-50 flex items-center justify-center">
                          <Icon className="w-5 h-5 text-orange-600" />
                        </div>
                        <div className="text-left">
                          <p className="text-sm font-medium">{setting.label}</p>
                          {setting.description && (
                            <p className="text-xs text-gray-500">{setting.description}</p>
                          )}
                        </div>
                      </div>
                      <ChevronRight className="w-5 h-5 text-gray-400" />
                    </button>
                    {index < generalSettings.length - 1 && <Separator />}
                  </div>
                );
              })}
            </div>
          </div>

          {/* Other Settings */}
          <div className="bg-white rounded-lg p-4">
            <h2 className="text-base font-medium mb-4">其他</h2>
            <div className="space-y-1">
              {otherSettings.map((setting, index) => {
                const Icon = setting.icon;
                return (
                  <div key={setting.id}>
                    <button className="w-full flex items-center justify-between p-3 rounded-lg hover:bg-gray-50 transition-colors">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-lg bg-gray-50 flex items-center justify-center">
                          <Icon className="w-5 h-5 text-gray-600" />
                        </div>
                        <div className="text-left">
                          <p className="text-sm font-medium">{setting.label}</p>
                          {setting.description && (
                            <p className="text-xs text-gray-500">{setting.description}</p>
                          )}
                        </div>
                      </div>
                      <ChevronRight className="w-5 h-5 text-gray-400" />
                    </button>
                    {index < otherSettings.length - 1 && <Separator />}
                  </div>
                );
              })}
            </div>
          </div>

          {/* Logout Button */}
          <div className="bg-white rounded-lg p-4">
            <Button variant="outline" className="w-full text-red-600 hover:text-red-700 hover:bg-red-50 border-red-200">
              <LogOut className="w-4 h-4 mr-2" />
              退出登录
            </Button>
          </div>
        </div>
      </ScrollArea>
    </div>
  );
}
