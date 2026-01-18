import { ChevronLeft, ChevronRight, Plus, Clock, MapPin, Users, MoreHorizontal } from "lucide-react";
import { useState } from "react";
import { Button } from "@/app/components/ui/button";
import { Badge } from "@/app/components/ui/badge";
import { ScrollArea } from "@/app/components/ui/scroll-area";

interface CalendarEvent {
  id: string;
  title: string;
  time: string;
  duration: string;
  location?: string;
  attendees?: string[];
  color: string;
}

const mockEvents: Record<string, CalendarEvent[]> = {
  "2026-01-17": [
    {
      id: "1",
      title: "产品设计评审会",
      time: "10:00",
      duration: "1小时",
      location: "会议室A",
      attendees: ["张伟", "李明", "王芳"],
      color: "blue",
    },
    {
      id: "2",
      title: "技术方案讨论",
      time: "14:30",
      duration: "1.5小时",
      location: "会议室B",
      attendees: ["刘洋", "陈晨"],
      color: "green",
    },
    {
      id: "3",
      title: "周会",
      time: "16:00",
      duration: "1小时",
      color: "purple",
    },
  ],
  "2026-01-20": [
    {
      id: "4",
      title: "客户需求沟通",
      time: "09:30",
      duration: "2小时",
      location: "线上会议",
      color: "orange",
    },
  ],
  "2026-01-22": [
    {
      id: "5",
      title: "项目复盘会议",
      time: "15:00",
      duration: "1小时",
      location: "会议室C",
      color: "red",
    },
  ],
};

export function CalendarPanel() {
  const [currentDate, setCurrentDate] = useState(new Date(2026, 0, 17)); // January 17, 2026 (Saturday)
  const [selectedDate, setSelectedDate] = useState<Date>(new Date(2026, 0, 17));

  const getDaysInMonth = (date: Date) => {
    const year = date.getFullYear();
    const month = date.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const daysInMonth = lastDay.getDate();
    const startingDayOfWeek = firstDay.getDay();

    const days: (Date | null)[] = [];

    // Add empty cells for days before the first day of the month
    for (let i = 0; i < startingDayOfWeek; i++) {
      days.push(null);
    }

    // Add all days of the month
    for (let i = 1; i <= daysInMonth; i++) {
      days.push(new Date(year, month, i));
    }

    return days;
  };

  const formatDate = (date: Date) => {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
  };

  const isToday = (date: Date | null) => {
    if (!date) return false;
    const today = new Date(2026, 0, 17); // Saturday, January 17, 2026
    return (
      date.getDate() === today.getDate() &&
      date.getMonth() === today.getMonth() &&
      date.getFullYear() === today.getFullYear()
    );
  };

  const isSelected = (date: Date | null) => {
    if (!date) return false;
    return (
      date.getDate() === selectedDate.getDate() &&
      date.getMonth() === selectedDate.getMonth() &&
      date.getFullYear() === selectedDate.getFullYear()
    );
  };

  const hasEvents = (date: Date | null) => {
    if (!date) return false;
    const dateStr = formatDate(date);
    return mockEvents[dateStr] && mockEvents[dateStr].length > 0;
  };

  const getEventsForSelectedDate = () => {
    const dateStr = formatDate(selectedDate);
    return mockEvents[dateStr] || [];
  };

  const previousMonth = () => {
    setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1));
  };

  const nextMonth = () => {
    setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1));
  };

  const getColorClass = (color: string) => {
    const colors: Record<string, string> = {
      blue: "bg-blue-100 text-blue-700 border-l-blue-500",
      green: "bg-green-100 text-green-700 border-l-green-500",
      purple: "bg-purple-100 text-purple-700 border-l-purple-500",
      orange: "bg-orange-100 text-orange-700 border-l-orange-500",
      red: "bg-red-100 text-red-700 border-l-red-500",
    };
    return colors[color] || colors.blue;
  };

  const days = getDaysInMonth(currentDate);
  const weekDays = ["日", "一", "二", "三", "四", "五", "六"];
  const monthNames = ["1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"];

  return (
    <div className="flex-1 bg-white flex">
      {/* Calendar Grid */}
      <div className="flex-1 border-r border-gray-200 flex flex-col">
        {/* Calendar Header */}
        <div className="p-4 border-b border-gray-200">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl">
              {currentDate.getFullYear()}年 {monthNames[currentDate.getMonth()]}
            </h2>
            <div className="flex items-center gap-2">
              <Button variant="outline" size="icon" onClick={previousMonth}>
                <ChevronLeft className="w-4 h-4" />
              </Button>
              <Button variant="outline" size="sm" onClick={() => setCurrentDate(new Date(2026, 0, 17))}>
                今天
              </Button>
              <Button variant="outline" size="icon" onClick={nextMonth}>
                <ChevronRight className="w-4 h-4" />
              </Button>
              <Button className="bg-[#0089FF] hover:bg-[#0078E8] ml-2">
                <Plus className="w-4 h-4 mr-2" />
                新建日程
              </Button>
            </div>
          </div>

          {/* Week Days Header */}
          <div className="grid grid-cols-7 gap-2">
            {weekDays.map((day) => (
              <div key={day} className="text-center text-sm font-medium text-gray-600 py-2">
                {day}
              </div>
            ))}
          </div>
        </div>

        {/* Calendar Grid */}
        <div className="flex-1 p-4">
          <div className="grid grid-cols-7 gap-2 h-full">
            {days.map((date, index) => (
              <button
                key={index}
                onClick={() => date && setSelectedDate(date)}
                disabled={!date}
                className={`
                  relative border rounded-lg p-2 text-left transition-all
                  ${!date ? "bg-gray-50 cursor-default" : "hover:bg-gray-50 cursor-pointer"}
                  ${isToday(date) ? "border-blue-500 bg-blue-50" : "border-gray-200"}
                  ${isSelected(date) ? "ring-2 ring-blue-500" : ""}
                `}
              >
                {date && (
                  <>
                    <div className={`text-sm font-medium mb-1 ${isToday(date) ? "text-blue-600" : ""}`}>
                      {date.getDate()}
                    </div>
                    {hasEvents(date) && (
                      <div className="flex gap-1">
                        {mockEvents[formatDate(date)].slice(0, 3).map((event) => (
                          <div
                            key={event.id}
                            className={`w-1.5 h-1.5 rounded-full ${
                              event.color === "blue"
                                ? "bg-blue-500"
                                : event.color === "green"
                                ? "bg-green-500"
                                : event.color === "purple"
                                ? "bg-purple-500"
                                : event.color === "orange"
                                ? "bg-orange-500"
                                : "bg-red-500"
                            }`}
                          />
                        ))}
                      </div>
                    )}
                  </>
                )}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Event List Sidebar */}
      <div className="w-96 bg-[#F7F8FA] flex flex-col">
        <div className="bg-white border-b border-gray-200 p-4">
          <h3 className="text-lg font-medium mb-1">
            {selectedDate.getMonth() + 1}月{selectedDate.getDate()}日
          </h3>
          <p className="text-sm text-gray-500">
            {weekDays[selectedDate.getDay()]}
            {isToday(selectedDate) && " · 今天"}
          </p>
        </div>

        <ScrollArea className="flex-1">
          <div className="p-4 space-y-3">
            {getEventsForSelectedDate().length > 0 ? (
              getEventsForSelectedDate().map((event) => (
                <div
                  key={event.id}
                  className={`${getColorClass(event.color)} rounded-lg p-4 border-l-4 cursor-pointer hover:shadow-sm transition-shadow`}
                >
                  <div className="flex items-start justify-between mb-2">
                    <h4 className="font-medium">{event.title}</h4>
                    <Button variant="ghost" size="icon" className="h-6 w-6 -mt-1 -mr-1">
                      <MoreHorizontal className="w-4 h-4" />
                    </Button>
                  </div>
                  <div className="space-y-2 text-sm">
                    <div className="flex items-center gap-2">
                      <Clock className="w-4 h-4" />
                      <span>
                        {event.time} · {event.duration}
                      </span>
                    </div>
                    {event.location && (
                      <div className="flex items-center gap-2">
                        <MapPin className="w-4 h-4" />
                        <span>{event.location}</span>
                      </div>
                    )}
                    {event.attendees && event.attendees.length > 0 && (
                      <div className="flex items-center gap-2">
                        <Users className="w-4 h-4" />
                        <div className="flex items-center gap-1">
                          {event.attendees.slice(0, 3).map((attendee, index) => (
                            <div
                              key={index}
                              className="w-6 h-6 rounded-full bg-white flex items-center justify-center text-xs font-medium"
                            >
                              {attendee[0]}
                            </div>
                          ))}
                          {event.attendees.length > 3 && (
                            <span className="text-xs ml-1">+{event.attendees.length - 3}</span>
                          )}
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              ))
            ) : (
              <div className="text-center py-12">
                <div className="w-16 h-16 rounded-full bg-gray-100 flex items-center justify-center mx-auto mb-3">
                  <Clock className="w-8 h-8 text-gray-400" />
                </div>
                <p className="text-gray-500 mb-2">暂无日程</p>
                <Button variant="outline" size="sm">
                  <Plus className="w-4 h-4 mr-1" />
                  创建日程
                </Button>
              </div>
            )}
          </div>
        </ScrollArea>

        {/* Quick Stats */}
        <div className="bg-white border-t border-gray-200 p-4">
          <div className="flex items-center justify-between text-sm">
            <span className="text-gray-600">本周日程</span>
            <Badge variant="secondary">8个</Badge>
          </div>
        </div>
      </div>
    </div>
  );
}
