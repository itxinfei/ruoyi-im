import { Mail, Phone, Building2, MapPin, Calendar, Briefcase, Edit, QrCode, Share2, MoreHorizontal } from "lucide-react";
import { ScrollArea } from "@/app/components/ui/scroll-area";
import { Button } from "@/app/components/ui/button";
import { Badge } from "@/app/components/ui/badge";
import { Separator } from "@/app/components/ui/separator";

export function UserProfilePanel() {
  return (
    <div className="flex-1 bg-[#F7F8FA]">
      <ScrollArea className="h-full">
        <div className="max-w-4xl mx-auto">
          {/* Cover Image */}
          <div className="h-48 bg-gradient-to-br from-blue-400 via-blue-500 to-blue-600 relative">
            <div className="absolute inset-0 bg-black/10"></div>
          </div>

          {/* Profile Card */}
          <div className="relative px-6 pb-6">
            <div className="bg-white rounded-lg shadow-sm -mt-16 relative z-10 p-6">
              {/* Avatar and Basic Info */}
              <div className="flex items-start gap-6 mb-6">
                <div className="relative">
                  <div className="w-24 h-24 rounded-2xl bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white ring-4 ring-white">
                    <span className="text-4xl">我</span>
                  </div>
                  <div className="absolute -bottom-1 -right-1 w-6 h-6 bg-green-500 rounded-full border-2 border-white"></div>
                </div>

                <div className="flex-1">
                  <div className="flex items-start justify-between mb-3">
                    <div>
                      <div className="flex items-center gap-2 mb-2">
                        <h1 className="text-2xl font-medium">王小明</h1>
                        <Badge className="bg-blue-100 text-blue-700 hover:bg-blue-100">
                          在线
                        </Badge>
                      </div>
                      <p className="text-gray-600">产品设计师 · 产品部</p>
                    </div>
                    <Button variant="outline">
                      <Edit className="w-4 h-4 mr-2" />
                      编辑资料
                    </Button>
                  </div>

                  <div className="flex items-center gap-4">
                    <Button className="bg-[#0089FF] hover:bg-[#0078E8]">
                      <Mail className="w-4 h-4 mr-2" />
                      发送消息
                    </Button>
                    <Button variant="outline">
                      <QrCode className="w-4 h-4 mr-2" />
                      我的二维码
                    </Button>
                    <Button variant="outline" size="icon">
                      <Share2 className="w-4 h-4" />
                    </Button>
                    <Button variant="outline" size="icon">
                      <MoreHorizontal className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              </div>

              <Separator className="my-6" />

              {/* Contact Information */}
              <div>
                <h2 className="text-base font-medium mb-4">联系方式</h2>
                <div className="grid grid-cols-2 gap-4">
                  <div className="flex items-start gap-3">
                    <div className="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center flex-shrink-0">
                      <Phone className="w-5 h-5 text-blue-600" />
                    </div>
                    <div>
                      <p className="text-xs text-gray-500 mb-1">手机号码</p>
                      <p className="text-sm font-medium">138-1234-5678</p>
                    </div>
                  </div>

                  <div className="flex items-start gap-3">
                    <div className="w-10 h-10 rounded-lg bg-purple-50 flex items-center justify-center flex-shrink-0">
                      <Mail className="w-5 h-5 text-purple-600" />
                    </div>
                    <div>
                      <p className="text-xs text-gray-500 mb-1">邮箱地址</p>
                      <p className="text-sm font-medium">wangxiaoming@company.com</p>
                    </div>
                  </div>
                </div>
              </div>

              <Separator className="my-6" />

              {/* Work Information */}
              <div>
                <h2 className="text-base font-medium mb-4">工作信息</h2>
                <div className="grid grid-cols-2 gap-4">
                  <div className="flex items-start gap-3">
                    <div className="w-10 h-10 rounded-lg bg-green-50 flex items-center justify-center flex-shrink-0">
                      <Building2 className="w-5 h-5 text-green-600" />
                    </div>
                    <div>
                      <p className="text-xs text-gray-500 mb-1">所属部门</p>
                      <p className="text-sm font-medium">产品部 / 设计组</p>
                    </div>
                  </div>

                  <div className="flex items-start gap-3">
                    <div className="w-10 h-10 rounded-lg bg-orange-50 flex items-center justify-center flex-shrink-0">
                      <Briefcase className="w-5 h-5 text-orange-600" />
                    </div>
                    <div>
                      <p className="text-xs text-gray-500 mb-1">职位</p>
                      <p className="text-sm font-medium">高级产品设计师</p>
                    </div>
                  </div>

                  <div className="flex items-start gap-3">
                    <div className="w-10 h-10 rounded-lg bg-red-50 flex items-center justify-center flex-shrink-0">
                      <MapPin className="w-5 h-5 text-red-600" />
                    </div>
                    <div>
                      <p className="text-xs text-gray-500 mb-1">办公地点</p>
                      <p className="text-sm font-medium">北京市朝阳区望京SOHO</p>
                    </div>
                  </div>

                  <div className="flex items-start gap-3">
                    <div className="w-10 h-10 rounded-lg bg-cyan-50 flex items-center justify-center flex-shrink-0">
                      <Calendar className="w-5 h-5 text-cyan-600" />
                    </div>
                    <div>
                      <p className="text-xs text-gray-500 mb-1">入职时间</p>
                      <p className="text-sm font-medium">2022年6月15日</p>
                    </div>
                  </div>
                </div>
              </div>

              <Separator className="my-6" />

              {/* Additional Information */}
              <div>
                <h2 className="text-base font-medium mb-4">其他信息</h2>
                <div className="space-y-3">
                  <div className="flex items-center justify-between py-2">
                    <span className="text-sm text-gray-600">工号</span>
                    <span className="text-sm font-medium">20220615001</span>
                  </div>
                  <div className="flex items-center justify-between py-2">
                    <span className="text-sm text-gray-600">直属上级</span>
                    <span className="text-sm font-medium text-blue-600">张伟 (产品总监)</span>
                  </div>
                  <div className="flex items-center justify-between py-2">
                    <span className="text-sm text-gray-600">汇报对象</span>
                    <span className="text-sm font-medium text-blue-600">李明 (设计总监)</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Stats Cards */}
            <div className="grid grid-cols-3 gap-4 mt-6">
              <div className="bg-white rounded-lg p-4">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm text-gray-600">本月考勤</span>
                  <Badge variant="secondary" className="bg-green-100 text-green-700">
                    正常
                  </Badge>
                </div>
                <p className="text-2xl font-semibold">22天</p>
                <p className="text-xs text-gray-500 mt-1">迟到0次 · 早退0次</p>
              </div>

              <div className="bg-white rounded-lg p-4">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm text-gray-600">待办任务</span>
                  <Badge variant="secondary" className="bg-orange-100 text-orange-700">
                    进行中
                  </Badge>
                </div>
                <p className="text-2xl font-semibold">8个</p>
                <p className="text-xs text-gray-500 mt-1">已完成15个</p>
              </div>

              <div className="bg-white rounded-lg p-4">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm text-gray-600">团队协作</span>
                  <Badge variant="secondary" className="bg-blue-100 text-blue-700">
                    活跃
                  </Badge>
                </div>
                <p className="text-2xl font-semibold">156次</p>
                <p className="text-xs text-gray-500 mt-1">本月沟通次数</p>
              </div>
            </div>
          </div>
        </div>
      </ScrollArea>
    </div>
  );
}
