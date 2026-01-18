import { Calendar, Clock, CheckSquare, Users, TrendingUp, FileText, Bell, ChevronRight, BarChart3 } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/app/components/ui/card";
import { Badge } from "@/app/components/ui/badge";
import { ScrollArea } from "@/app/components/ui/scroll-area";
import { Button } from "@/app/components/ui/button";

interface QuickAction {
  id: string;
  icon: React.ElementType;
  label: string;
  color: string;
}

const quickActions: QuickAction[] = [
  { id: "calendar", icon: Calendar, label: "日程", color: "from-blue-500 to-blue-600" },
  { id: "attendance", icon: Clock, label: "考勤打卡", color: "from-green-500 to-green-600" },
  { id: "approval", icon: CheckSquare, label: "审批", color: "from-orange-500 to-orange-600" },
  { id: "report", icon: FileText, label: "日报", color: "from-purple-500 to-purple-600" },
];

interface TodoItem {
  id: string;
  title: string;
  time: string;
  status: "pending" | "completed";
}

const mockTodos: TodoItem[] = [
  { id: "1", title: "完成项目设计稿", time: "今天 15:00", status: "pending" },
  { id: "2", title: "参加团队会议", time: "今天 16:30", status: "pending" },
  { id: "3", title: "审批报销单", time: "明天 10:00", status: "pending" },
];

interface Notice {
  id: string;
  title: string;
  time: string;
  unread: boolean;
}

const mockNotices: Notice[] = [
  { id: "1", title: "关于调整工作时间的通知", time: "2小时前", unread: true },
  { id: "2", title: "本月绩效考核开始", time: "昨天", unread: true },
  { id: "3", title: "公司年会活动通知", time: "2天前", unread: false },
];

export function WorkbenchPanel() {
  return (
    <div className="flex-1 bg-[#F7F8FA]">
      <ScrollArea className="h-full">
        <div className="p-6 max-w-6xl mx-auto space-y-6">
          {/* Header */}
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl mb-1">工作台</h1>
              <p className="text-sm text-gray-500">欢迎回来，开始高效工作</p>
            </div>
            <div className="text-right">
              <p className="text-sm text-gray-500">今天是</p>
              <p className="text-lg font-medium">2026年1月17日 星期六</p>
            </div>
          </div>

          {/* Quick Actions */}
          <div className="grid grid-cols-4 gap-4">
            {quickActions.map((action) => {
              const Icon = action.icon;
              return (
                <Card
                  key={action.id}
                  className="cursor-pointer hover:shadow-lg transition-all hover:-translate-y-0.5"
                >
                  <CardContent className="flex flex-col items-center justify-center p-6 gap-3">
                    <div className={`w-14 h-14 rounded-xl bg-gradient-to-br ${action.color} flex items-center justify-center shadow-md`}>
                      <Icon className="w-7 h-7 text-white" />
                    </div>
                    <span className="text-sm font-medium">{action.label}</span>
                  </CardContent>
                </Card>
              );
            })}
          </div>

          {/* Main Content Grid */}
          <div className="grid grid-cols-2 gap-6">
            {/* My Todos */}
            <Card className="shadow-sm">
              <CardHeader className="flex flex-row items-center justify-between pb-3">
                <CardTitle className="text-base flex items-center gap-2">
                  <CheckSquare className="w-4 h-4" />
                  我的待办
                </CardTitle>
                <Badge variant="secondary" className="bg-blue-50 text-blue-600 hover:bg-blue-50">
                  {mockTodos.length}
                </Badge>
              </CardHeader>
              <CardContent className="space-y-2">
                {mockTodos.map((todo) => (
                  <div
                    key={todo.id}
                    className="flex items-start gap-3 p-3 rounded-lg hover:bg-gray-50 cursor-pointer transition-colors"
                  >
                    <div className="w-4 h-4 mt-0.5 rounded border-2 border-blue-500 flex-shrink-0 hover:bg-blue-50 transition-colors" />
                    <div className="flex-1 min-w-0">
                      <p className="text-sm mb-1 leading-tight">{todo.title}</p>
                      <div className="flex items-center gap-2 text-xs text-gray-500">
                        <Clock className="w-3 h-3" />
                        <span>{todo.time}</span>
                      </div>
                    </div>
                  </div>
                ))}
                <Button variant="ghost" className="w-full text-sm text-blue-600 hover:text-blue-700 hover:bg-blue-50">
                  查看全部 <ChevronRight className="w-4 h-4 ml-1" />
                </Button>
              </CardContent>
            </Card>

            {/* Notices */}
            <Card className="shadow-sm">
              <CardHeader className="flex flex-row items-center justify-between pb-3">
                <CardTitle className="text-base flex items-center gap-2">
                  <Bell className="w-4 h-4" />
                  公告通知
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-2">
                {mockNotices.map((notice) => (
                  <div
                    key={notice.id}
                    className="flex items-start gap-3 p-3 rounded-lg hover:bg-gray-50 cursor-pointer transition-colors"
                  >
                    {notice.unread && (
                      <div className="w-2 h-2 mt-1.5 rounded-full bg-red-500 flex-shrink-0" />
                    )}
                    {!notice.unread && <div className="w-2 h-2 mt-1.5 flex-shrink-0" />}
                    <div className="flex-1 min-w-0">
                      <p className={`text-sm mb-1 leading-tight ${notice.unread ? 'font-medium' : ''}`}>
                        {notice.title}
                      </p>
                      <span className="text-xs text-gray-500">{notice.time}</span>
                    </div>
                  </div>
                ))}
                <Button variant="ghost" className="w-full text-sm text-blue-600 hover:text-blue-700 hover:bg-blue-50">
                  查看全部 <ChevronRight className="w-4 h-4 ml-1" />
                </Button>
              </CardContent>
            </Card>

            {/* Team Stats */}
            <Card className="shadow-sm">
              <CardHeader className="pb-3">
                <CardTitle className="text-base flex items-center gap-2">
                  <BarChart3 className="w-4 h-4" />
                  团队数据
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between p-3 rounded-lg bg-blue-50">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-lg bg-blue-100 flex items-center justify-center">
                      <Users className="w-5 h-5 text-blue-600" />
                    </div>
                    <div>
                      <p className="text-xs text-gray-600">团队成员</p>
                      <p className="text-xl font-semibold">24人</p>
                    </div>
                  </div>
                  <TrendingUp className="w-5 h-5 text-green-500" />
                </div>
                <div className="flex items-center justify-between p-3 rounded-lg bg-green-50">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-lg bg-green-100 flex items-center justify-center">
                      <CheckSquare className="w-5 h-5 text-green-600" />
                    </div>
                    <div>
                      <p className="text-xs text-gray-600">本周完成任务</p>
                      <p className="text-xl font-semibold">38个</p>
                    </div>
                  </div>
                  <TrendingUp className="w-5 h-5 text-green-500" />
                </div>
              </CardContent>
            </Card>

            {/* Recent Documents */}
            <Card className="shadow-sm">
              <CardHeader className="pb-3">
                <CardTitle className="text-base flex items-center gap-2">
                  <FileText className="w-4 h-4" />
                  最近文档
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-2">
                <div className="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-50 cursor-pointer transition-colors">
                  <div className="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center">
                    <FileText className="w-5 h-5 text-blue-600" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm mb-1 truncate leading-tight">产品需求文档V2.0</p>
                    <span className="text-xs text-gray-500">2小时前</span>
                  </div>
                </div>
                <div className="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-50 cursor-pointer transition-colors">
                  <div className="w-10 h-10 rounded-lg bg-green-50 flex items-center justify-center">
                    <FileText className="w-5 h-5 text-green-600" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm mb-1 truncate leading-tight">Q1季度总结报告</p>
                    <span className="text-xs text-gray-500">昨天</span>
                  </div>
                </div>
                <Button variant="ghost" className="w-full text-sm text-blue-600 hover:text-blue-700 hover:bg-blue-50">
                  查看全部 <ChevronRight className="w-4 h-4 ml-1" />
                </Button>
              </CardContent>
            </Card>
          </div>
        </div>
      </ScrollArea>
    </div>
  );
}