import { MoreVertical, Phone, Video, Send, Smile, Paperclip, Image as ImageIcon, Mic } from "lucide-react";
import { Button } from "@/app/components/ui/button";
import { Textarea } from "@/app/components/ui/textarea";
import { ScrollArea } from "@/app/components/ui/scroll-area";
import { useState } from "react";

interface ChatMessage {
  id: string;
  type: "sent" | "received";
  content: string;
  time: string;
  sender?: string;
}

const mockChatMessages: ChatMessage[] = [
  {
    id: "1",
    type: "received",
    content: "大家好，今天我们讨论一下新项目的设计方案",
    time: "10:23",
    sender: "李明",
  },
  {
    id: "2",
    type: "received",
    content: "我已经准备好了初步的原型图",
    time: "10:24",
    sender: "李明",
  },
  {
    id: "3",
    type: "sent",
    content: "好的，我现在查看一下",
    time: "10:25",
  },
  {
    id: "4",
    type: "received",
    content: "整体风格采用扁平化设计，主色调是蓝色系",
    time: "10:26",
    sender: "李明",
  },
  {
    id: "5",
    type: "sent",
    content: "看起来不错，我觉得可以适当增加一些渐变效果",
    time: "10:28",
  },
  {
    id: "6",
    type: "received",
    content: "好主意！我稍后会更新一版",
    time: "10:29",
    sender: "李明",
  },
];

export function ChatArea() {
  const [inputMessage, setInputMessage] = useState("");

  return (
    <div className="flex-1 bg-[#F7F8FA] flex flex-col">
      {/* Chat Header */}
      <div className="h-14 bg-white border-b border-gray-200 flex items-center justify-between px-4 flex-shrink-0">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-lg bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white shadow-sm">
            <span className="text-sm">产</span>
          </div>
          <div>
            <h3 className="font-medium text-sm">产品设计团队</h3>
            <span className="text-xs text-gray-500">12人</span>
          </div>
        </div>
        <div className="flex items-center gap-1">
          <Button variant="ghost" size="icon" className="h-9 w-9">
            <Phone className="w-4 h-4" />
          </Button>
          <Button variant="ghost" size="icon" className="h-9 w-9">
            <Video className="w-4 h-4" />
          </Button>
          <Button variant="ghost" size="icon" className="h-9 w-9">
            <MoreVertical className="w-4 h-4" />
          </Button>
        </div>
      </div>

      {/* Chat Messages */}
      <ScrollArea className="flex-1 p-4">
        <div className="space-y-4 max-w-4xl mx-auto">
          {mockChatMessages.map((message) => (
            <div
              key={message.id}
              className={`flex ${message.type === "sent" ? "justify-end" : "justify-start"}`}
            >
              <div
                className={`flex gap-3 max-w-[60%] ${
                  message.type === "sent" ? "flex-row-reverse" : "flex-row"
                }`}
              >
                {/* Avatar */}
                <div className="w-9 h-9 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white flex-shrink-0 shadow-sm">
                  <span className="text-xs">{message.type === "sent" ? "我" : "李"}</span>
                </div>

                {/* Message Content */}
                <div className={`flex flex-col ${message.type === "sent" ? "items-end" : "items-start"}`}>
                  {message.sender && (
                    <span className="text-xs text-gray-500 mb-1 px-1">{message.sender} {message.time}</span>
                  )}
                  {!message.sender && message.type === "sent" && (
                    <span className="text-xs text-gray-500 mb-1 px-1">{message.time}</span>
                  )}
                  <div
                    className={`px-3 py-2 rounded-lg shadow-sm ${
                      message.type === "sent"
                        ? "bg-[#0089FF] text-white"
                        : "bg-white text-gray-900"
                    }`}
                  >
                    <p className="text-sm leading-relaxed">{message.content}</p>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </ScrollArea>

      {/* Input Area */}
      <div className="bg-white border-t border-gray-200 flex-shrink-0">
        {/* Toolbar */}
        <div className="flex items-center gap-1 px-4 py-2 border-b border-gray-100">
          <Button variant="ghost" size="icon" className="h-8 w-8">
            <Smile className="w-4 h-4 text-gray-600" />
          </Button>
          <Button variant="ghost" size="icon" className="h-8 w-8">
            <Paperclip className="w-4 h-4 text-gray-600" />
          </Button>
          <Button variant="ghost" size="icon" className="h-8 w-8">
            <ImageIcon className="w-4 h-4 text-gray-600" />
          </Button>
          <Button variant="ghost" size="icon" className="h-8 w-8">
            <Mic className="w-4 h-4 text-gray-600" />
          </Button>
        </div>

        {/* Input */}
        <div className="p-4 flex items-end gap-3">
          <Textarea
            placeholder="输入消息..."
            value={inputMessage}
            onChange={(e) => setInputMessage(e.target.value)}
            className="flex-1 min-h-[80px] max-h-[120px] resize-none border-0 focus-visible:ring-0 focus-visible:ring-offset-0 bg-transparent"
            onKeyDown={(e) => {
              if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                // Handle send message
                setInputMessage("");
              }
            }}
          />
          <Button className="bg-[#0089FF] hover:bg-[#0078E8] h-9 px-6 flex-shrink-0">
            <Send className="w-4 h-4 mr-1" />
            发送
          </Button>
        </div>
      </div>
    </div>
  );
}