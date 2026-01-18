import { Phone, Video, Clock, User, Search, PhoneCall, PhoneMissed, PhoneIncoming, PhoneOutgoing } from "lucide-react";
import { useState } from "react";
import { Input } from "@/app/components/ui/input";
import { Button } from "@/app/components/ui/button";
import { ScrollArea } from "@/app/components/ui/scroll-area";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/app/components/ui/tabs";
import { Badge } from "@/app/components/ui/badge";

interface CallRecord {
  id: string;
  name: string;
  avatar: string;
  type: "incoming" | "outgoing" | "missed";
  callType: "voice" | "video";
  time: string;
  duration?: string;
}

const mockCallRecords: CallRecord[] = [
  {
    id: "1",
    name: "张伟",
    avatar: "张",
    type: "incoming",
    callType: "voice",
    time: "今天 14:32",
    duration: "5分32秒",
  },
  {
    id: "2",
    name: "李明",
    avatar: "李",
    type: "outgoing",
    callType: "video",
    time: "今天 11:20",
    duration: "12分18秒",
  },
  {
    id: "3",
    name: "王芳",
    avatar: "王",
    type: "missed",
    callType: "voice",
    time: "昨天 16:45",
  },
  {
    id: "4",
    name: "刘洋",
    avatar: "刘",
    type: "outgoing",
    callType: "voice",
    time: "昨天 10:15",
    duration: "3分22秒",
  },
  {
    id: "5",
    name: "陈晨",
    avatar: "陈",
    type: "incoming",
    callType: "video",
    time: "星期一 15:30",
    duration: "25分10秒",
  },
];

interface FavoriteContact {
  id: string;
  name: string;
  avatar: string;
}

const mockFavorites: FavoriteContact[] = [
  { id: "1", name: "张伟", avatar: "张" },
  { id: "2", name: "李明", avatar: "李" },
  { id: "3", name: "王芳", avatar: "王" },
  { id: "4", name: "刘洋", avatar: "刘" },
];

export function PhonePanel() {
  const [dialNumber, setDialNumber] = useState("");

  const handleDialPad = (num: string) => {
    setDialNumber(prev => prev + num);
  };

  const handleDelete = () => {
    setDialNumber(prev => prev.slice(0, -1));
  };

  return (
    <div className="flex-1 bg-white flex">
      {/* Left Panel - Dial Pad */}
      <div className="w-96 border-r border-gray-200 flex flex-col">
        <div className="p-6 border-b border-gray-200">
          <h2 className="text-xl mb-4">电话</h2>
          
          {/* Dial Display */}
          <div className="bg-gray-50 rounded-lg p-4 mb-4 min-h-[60px] flex items-center justify-center">
            <span className="text-2xl tracking-widest font-light">{dialNumber || "输入号码"}</span>
          </div>

          {/* Dial Pad */}
          <div className="grid grid-cols-3 gap-3 mb-4">
            {["1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#"].map((num) => (
              <Button
                key={num}
                variant="outline"
                className="h-14 text-lg font-medium hover:bg-gray-50 border-gray-200"
                onClick={() => handleDialPad(num)}
              >
                {num}
              </Button>
            ))}
          </div>

          {/* Action Buttons */}
          <div className="flex gap-2">
            <Button
              variant="outline"
              className="flex-1 border-gray-200"
              onClick={handleDelete}
            >
              删除
            </Button>
            <Button
              className="flex-1 bg-green-500 hover:bg-green-600"
              disabled={!dialNumber}
            >
              <Phone className="w-4 h-4 mr-2" />
              拨打
            </Button>
          </div>
        </div>

        {/* Favorite Contacts */}
        <div className="p-6">
          <h3 className="text-sm font-medium mb-4 text-gray-700">常用联系人</h3>
          <div className="grid grid-cols-4 gap-4">
            {mockFavorites.map((contact) => (
              <div
                key={contact.id}
                className="flex flex-col items-center gap-2 cursor-pointer hover:opacity-80 transition-opacity"
              >
                <div className="w-14 h-14 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white shadow-md hover:shadow-lg transition-shadow">
                  <span className="text-lg">{contact.avatar}</span>
                </div>
                <span className="text-xs text-center truncate w-full">{contact.name}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Right Panel - Call Records */}
      <div className="flex-1 bg-[#F7F8FA]">
        <Tabs defaultValue="all" className="h-full flex flex-col">
          <div className="bg-white border-b border-gray-200 px-6 pt-6 pb-0">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-medium">通话记录</h3>
              <div className="relative w-64">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                <Input
                  placeholder="搜索通话记录"
                  className="pl-9 bg-gray-50 border-0 h-9"
                />
              </div>
            </div>
            <TabsList className="w-full justify-start bg-transparent border-b-0 p-0 h-10">
              <TabsTrigger value="all" className="data-[state=active]:border-b-2 data-[state=active]:border-blue-500 rounded-none bg-transparent">
                全部
              </TabsTrigger>
              <TabsTrigger value="missed" className="data-[state=active]:border-b-2 data-[state=active]:border-blue-500 rounded-none bg-transparent">
                未接来电
              </TabsTrigger>
            </TabsList>
          </div>

          <TabsContent value="all" className="flex-1 m-0">
            <ScrollArea className="h-full">
              <div className="p-6 space-y-2">
                {mockCallRecords.map((record) => (
                  <div
                    key={record.id}
                    className="bg-white rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer border border-gray-100"
                  >
                    <div className="flex items-center gap-4">
                      {/* Avatar */}
                      <div className="w-12 h-12 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white flex-shrink-0 shadow-sm">
                        <span>{record.avatar}</span>
                      </div>

                      {/* Info */}
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 mb-1">
                          <span className="font-medium">{record.name}</span>
                          {record.type === "missed" && (
                            <Badge variant="destructive" className="h-5 text-xs">未接</Badge>
                          )}
                        </div>
                        <div className="flex items-center gap-2 text-sm text-gray-500">
                          {record.callType === "video" ? (
                            <Video className="w-4 h-4" />
                          ) : (
                            <Phone className="w-4 h-4" />
                          )}
                          {record.type === "incoming" && <PhoneIncoming className="w-3 h-3" />}
                          {record.type === "outgoing" && <PhoneOutgoing className="w-3 h-3" />}
                          {record.type === "missed" && <PhoneMissed className="w-3 h-3" />}
                          <span>
                            {record.type === "incoming" && "来电"}
                            {record.type === "outgoing" && "去电"}
                            {record.type === "missed" && "未接"}
                          </span>
                          <span>•</span>
                          <span>{record.time}</span>
                          {record.duration && (
                            <>
                              <span>•</span>
                              <span>{record.duration}</span>
                            </>
                          )}
                        </div>
                      </div>

                      {/* Actions */}
                      <div className="flex gap-2">
                        <Button variant="ghost" size="icon" className="h-9 w-9 text-green-600 hover:bg-green-50">
                          <Phone className="w-4 h-4" />
                        </Button>
                        <Button variant="ghost" size="icon" className="h-9 w-9 text-blue-600 hover:bg-blue-50">
                          <Video className="w-4 h-4" />
                        </Button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </ScrollArea>
          </TabsContent>

          <TabsContent value="missed" className="flex-1 m-0">
            <ScrollArea className="h-full">
              <div className="p-6 space-y-2">
                {mockCallRecords
                  .filter((record) => record.type === "missed")
                  .map((record) => (
                    <div
                      key={record.id}
                      className="bg-white rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer border border-gray-100"
                    >
                      <div className="flex items-center gap-4">
                        <div className="w-12 h-12 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white flex-shrink-0 shadow-sm">
                          <span>{record.avatar}</span>
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 mb-1">
                            <span className="font-medium">{record.name}</span>
                            <Badge variant="destructive" className="h-5 text-xs">未接</Badge>
                          </div>
                          <div className="flex items-center gap-2 text-sm text-gray-500">
                            <Phone className="w-4 h-4" />
                            <PhoneMissed className="w-3 h-3" />
                            <span>未接来电</span>
                            <span>•</span>
                            <span>{record.time}</span>
                          </div>
                        </div>
                        <div className="flex gap-2">
                          <Button variant="ghost" size="icon" className="h-9 w-9 text-green-600 hover:bg-green-50">
                            <Phone className="w-4 h-4" />
                          </Button>
                          <Button variant="ghost" size="icon" className="h-9 w-9 text-blue-600 hover:bg-blue-50">
                            <Video className="w-4 h-4" />
                          </Button>
                        </div>
                      </div>
                    </div>
                  ))}
              </div>
            </ScrollArea>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}