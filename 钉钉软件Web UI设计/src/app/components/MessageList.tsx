import { Search, Star, Plus } from "lucide-react";
import { Input } from "@/app/components/ui/input";
import { ScrollArea } from "@/app/components/ui/scroll-area";
import { Badge } from "@/app/components/ui/badge";
import { Button } from "@/app/components/ui/button";

interface Message {
  id: string;
  name: string;
  avatar: string;
  lastMessage: string;
  time: string;
  unread?: number;
  isPinned?: boolean;
}

const mockMessages: Message[] = [
  {
    id: "1",
    name: "产品设计团队",
    avatar: "产",
    lastMessage: "李明: 新版本UI已经完成，请查看",
    time: "14:32",
    unread: 5,
    isPinned: true,
  },
  {
    id: "2",
    name: "张伟",
    avatar: "张",
    lastMessage: "今天的会议改到下午3点",
    time: "13:15",
    unread: 2,
  },
  {
    id: "3",
    name: "项目组A",
    avatar: "项",
    lastMessage: "王芳: 项目进度更新完毕",
    time: "11:20",
  },
  {
    id: "4",
    name: "刘洋",
    avatar: "刘",
    lastMessage: "文件已发送，请查收",
    time: "昨天",
  },
  {
    id: "5",
    name: "研发部",
    avatar: "研",
    lastMessage: "代码审查会议通知",
    time: "昨天",
    unread: 1,
  },
  {
    id: "6",
    name: "陈晨",
    avatar: "陈",
    lastMessage: "好的，明天见",
    time: "星期一",
  },
  {
    id: "7",
    name: "销售团队",
    avatar: "销",
    lastMessage: "本月业绩统计表已完成",
    time: "星期一",
  },
  {
    id: "8",
    name: "人事部",
    avatar: "人",
    lastMessage: "请填写本月考勤表",
    time: "星期日",
  },
];

interface MessageListProps {
  selectedMessageId: string;
  onSelectMessage: (id: string) => void;
}

export function MessageList({ selectedMessageId, onSelectMessage }: MessageListProps) {
  return (
    <div className="w-80 bg-white border-r border-gray-200 flex flex-col">
      {/* Header */}
      <div className="p-4 border-b border-gray-200">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-xl">消息</h2>
          <Button variant="ghost" size="icon" className="h-8 w-8">
            <Plus className="w-5 h-5" />
          </Button>
        </div>
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
          <Input
            placeholder="搜索"
            className="pl-9 bg-gray-50 border-0 h-9"
          />
        </div>
      </div>

      {/* Messages */}
      <ScrollArea className="flex-1">
        <div className="divide-y divide-gray-100">
          {mockMessages.map((message) => (
            <div
              key={message.id}
              onClick={() => onSelectMessage(message.id)}
              className={`p-3 hover:bg-gray-50 cursor-pointer transition-colors ${
                selectedMessageId === message.id ? "bg-blue-50" : ""
              }`}
            >
              <div className="flex items-start gap-3">
                {/* Avatar */}
                <div className="w-11 h-11 rounded-lg bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white flex-shrink-0 shadow-sm">
                  <span className="text-sm">{message.avatar}</span>
                </div>

                {/* Content */}
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between mb-1">
                    <div className="flex items-center gap-1">
                      <span className="font-medium text-sm truncate">{message.name}</span>
                      {message.isPinned && (
                        <Star className="w-3 h-3 text-yellow-500 fill-yellow-500 flex-shrink-0" />
                      )}
                    </div>
                    <span className="text-xs text-gray-500 flex-shrink-0 ml-2">{message.time}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <p className="text-sm text-gray-500 truncate pr-2 leading-tight">{message.lastMessage}</p>
                    {message.unread && (
                      <Badge className="bg-red-500 text-white rounded-full h-5 min-w-5 px-1.5 flex items-center justify-center text-xs flex-shrink-0 hover:bg-red-500">
                        {message.unread}
                      </Badge>
                    )}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </ScrollArea>
    </div>
  );
}