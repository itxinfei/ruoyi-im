import { FileText, Calendar, CheckSquare, Clock, TrendingUp, Mail, Newspaper, Megaphone, Gift, Settings, HelpCircle, MessageCircle, Camera, Folder, Download, Shield } from "lucide-react";
import { ScrollArea } from "@/app/components/ui/scroll-area";
import { Card, CardContent } from "@/app/components/ui/card";

interface AppItem {
  id: string;
  icon: React.ElementType;
  label: string;
  color: string;
  badge?: string;
}

const workApps: AppItem[] = [
  { id: "approval", icon: CheckSquare, label: "审批", color: "from-orange-400 to-orange-600" },
  { id: "attendance", icon: Clock, label: "考勤打卡", color: "from-green-400 to-green-600" },
  { id: "report", icon: FileText, label: "日志", color: "from-blue-400 to-blue-600" },
  { id: "mail", icon: Mail, label: "邮箱", color: "from-purple-400 to-purple-600", badge: "3" },
  { id: "notice", icon: Newspaper, label: "公告", color: "from-red-400 to-red-600", badge: "New" },
  { id: "tasks", icon: CheckSquare, label: "待办", color: "from-cyan-400 to-cyan-600", badge: "5" },
];

const businessApps: AppItem[] = [
  { id: "crm", icon: TrendingUp, label: "客户管理", color: "from-indigo-400 to-indigo-600" },
  { id: "project", icon: Folder, label: "项目", color: "from-pink-400 to-pink-600" },
  { id: "hr", icon: Shield, label: "智能人事", color: "from-teal-400 to-teal-600" },
  { id: "finance", icon: TrendingUp, label: "财务", color: "from-amber-400 to-amber-600" },
];

const toolApps: AppItem[] = [
  { id: "scan", icon: Camera, label: "扫一扫", color: "from-blue-400 to-blue-600" },
  { id: "download", icon: Download, label: "收藏", color: "from-yellow-400 to-yellow-600" },
  { id: "feedback", icon: MessageCircle, label: "意见反馈", color: "from-gray-400 to-gray-600" },
  { id: "help", icon: HelpCircle, label: "帮助中心", color: "from-emerald-400 to-emerald-600" },
];

export function MorePanel() {
  return (
    <div className="flex-1 bg-[#F7F8FA]">
      <ScrollArea className="h-full">
        <div className="max-w-6xl mx-auto p-6 space-y-6">
          {/* Header */}
          <div>
            <h1 className="text-2xl mb-1">工作台</h1>
            <p className="text-sm text-gray-500">快速访问常用应用和工具</p>
          </div>

          {/* Work Apps */}
          <div>
            <h2 className="text-base font-medium mb-4">工作应用</h2>
            <div className="grid grid-cols-6 gap-4">
              {workApps.map((app) => {
                const Icon = app.icon;
                return (
                  <Card
                    key={app.id}
                    className="cursor-pointer hover:shadow-md transition-shadow relative"
                  >
                    <CardContent className="flex flex-col items-center justify-center p-4 gap-2">
                      <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${app.color} flex items-center justify-center relative`}>
                        <Icon className="w-6 h-6 text-white" />
                        {app.badge && (
                          <div className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full min-w-5 h-5 flex items-center justify-center px-1">
                            {app.badge}
                          </div>
                        )}
                      </div>
                      <span className="text-xs text-center">{app.label}</span>
                    </CardContent>
                  </Card>
                );
              })}
            </div>
          </div>

          {/* Business Apps */}
          <div>
            <h2 className="text-base font-medium mb-4">业务应用</h2>
            <div className="grid grid-cols-6 gap-4">
              {businessApps.map((app) => {
                const Icon = app.icon;
                return (
                  <Card
                    key={app.id}
                    className="cursor-pointer hover:shadow-md transition-shadow"
                  >
                    <CardContent className="flex flex-col items-center justify-center p-4 gap-2">
                      <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${app.color} flex items-center justify-center`}>
                        <Icon className="w-6 h-6 text-white" />
                      </div>
                      <span className="text-xs text-center">{app.label}</span>
                    </CardContent>
                  </Card>
                );
              })}
            </div>
          </div>

          {/* Tools */}
          <div>
            <h2 className="text-base font-medium mb-4">工具</h2>
            <div className="grid grid-cols-6 gap-4">
              {toolApps.map((app) => {
                const Icon = app.icon;
                return (
                  <Card
                    key={app.id}
                    className="cursor-pointer hover:shadow-md transition-shadow"
                  >
                    <CardContent className="flex flex-col items-center justify-center p-4 gap-2">
                      <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${app.color} flex items-center justify-center`}>
                        <Icon className="w-6 h-6 text-white" />
                      </div>
                      <span className="text-xs text-center">{app.label}</span>
                    </CardContent>
                  </Card>
                );
              })}
            </div>
          </div>

          {/* Quick Actions */}
          <div>
            <h2 className="text-base font-medium mb-4">快捷功能</h2>
            <div className="grid grid-cols-3 gap-4">
              <Card className="cursor-pointer hover:shadow-md transition-shadow">
                <CardContent className="p-4">
                  <div className="flex items-center gap-3">
                    <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center">
                      <Megaphone className="w-6 h-6 text-white" />
                    </div>
                    <div>
                      <p className="font-medium">发起直播</p>
                      <p className="text-xs text-gray-500">开启在线直播</p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card className="cursor-pointer hover:shadow-md transition-shadow">
                <CardContent className="p-4">
                  <div className="flex items-center gap-3">
                    <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-green-400 to-green-600 flex items-center justify-center">
                      <Gift className="w-6 h-6 text-white" />
                    </div>
                    <div>
                      <p className="font-medium">福利中心</p>
                      <p className="text-xs text-gray-500">查看福利活动</p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card className="cursor-pointer hover:shadow-md transition-shadow">
                <CardContent className="p-4">
                  <div className="flex items-center gap-3">
                    <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-purple-400 to-purple-600 flex items-center justify-center">
                      <Settings className="w-6 h-6 text-white" />
                    </div>
                    <div>
                      <p className="font-medium">应用管理</p>
                      <p className="text-xs text-gray-500">管理我的应用</p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        </div>
      </ScrollArea>
    </div>
  );
}
