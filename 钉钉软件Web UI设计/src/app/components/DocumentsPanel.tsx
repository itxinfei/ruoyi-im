import { Search, FolderPlus, Upload, MoreHorizontal, FileText, Image as ImageIcon, File, Star, Clock, Users } from "lucide-react";
import { useState } from "react";
import { Input } from "@/app/components/ui/input";
import { Button } from "@/app/components/ui/button";
import { ScrollArea } from "@/app/components/ui/scroll-area";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/app/components/ui/tabs";
import { Badge } from "@/app/components/ui/badge";

interface Document {
  id: string;
  name: string;
  type: "doc" | "image" | "file";
  size: string;
  updatedAt: string;
  updatedBy: string;
  isStarred?: boolean;
}

const mockDocuments: Document[] = [
  {
    id: "1",
    name: "产品需求文档V2.0.docx",
    type: "doc",
    size: "2.3 MB",
    updatedAt: "2小时前",
    updatedBy: "张伟",
    isStarred: true,
  },
  {
    id: "2",
    name: "UI设计规范.pdf",
    type: "doc",
    size: "5.7 MB",
    updatedAt: "5小时前",
    updatedBy: "李明",
    isStarred: true,
  },
  {
    id: "3",
    name: "项目进度表.xlsx",
    type: "file",
    size: "1.2 MB",
    updatedAt: "昨天",
    updatedBy: "王芳",
  },
  {
    id: "4",
    name: "产品原型图.fig",
    type: "image",
    size: "8.4 MB",
    updatedAt: "昨天",
    updatedBy: "李明",
  },
  {
    id: "5",
    name: "Q1季度总结报告.pptx",
    type: "doc",
    size: "12.5 MB",
    updatedAt: "2天前",
    updatedBy: "刘洋",
  },
  {
    id: "6",
    name: "技术架构图.png",
    type: "image",
    size: "3.1 MB",
    updatedAt: "3天前",
    updatedBy: "刘洋",
  },
  {
    id: "7",
    name: "用户调研报告.docx",
    type: "doc",
    size: "4.6 MB",
    updatedAt: "3天前",
    updatedBy: "陈晨",
  },
  {
    id: "8",
    name: "竞品分析.pdf",
    type: "doc",
    size: "6.8 MB",
    updatedAt: "星期一",
    updatedBy: "张伟",
  },
];

interface Folder {
  id: string;
  name: string;
  count: number;
  color: string;
}

const mockFolders: Folder[] = [
  { id: "1", name: "产品文档", count: 25, color: "from-blue-400 to-blue-600" },
  { id: "2", name: "设计资源", count: 48, color: "from-purple-400 to-purple-600" },
  { id: "3", name: "开发文档", count: 32, color: "from-green-400 to-green-600" },
  { id: "4", name: "项目资料", count: 18, color: "from-orange-400 to-orange-600" },
];

export function DocumentsPanel() {
  const [selectedDoc, setSelectedDoc] = useState<Document | null>(null);

  const getFileIcon = (type: string) => {
    switch (type) {
      case "doc":
        return <FileText className="w-5 h-5 text-blue-600" />;
      case "image":
        return <ImageIcon className="w-5 h-5 text-purple-600" />;
      default:
        return <File className="w-5 h-5 text-gray-600" />;
    }
  };

  return (
    <div className="flex-1 bg-white flex">
      {/* Left Sidebar */}
      <div className="w-64 border-r border-gray-200 flex flex-col">
        <div className="p-4 border-b border-gray-200">
          <h2 className="text-xl mb-3">文档</h2>
          <Button className="w-full bg-[#0089FF] hover:bg-[#0078E8]">
            <Upload className="w-4 h-4 mr-2" />
            上传文件
          </Button>
        </div>

        <ScrollArea className="flex-1">
          <div className="p-4 space-y-1">
            <button className="w-full flex items-center gap-3 p-2 rounded-lg hover:bg-gray-50 transition-colors text-left">
              <Clock className="w-4 h-4 text-gray-600" />
              <span className="text-sm">最近使用</span>
            </button>
            <button className="w-full flex items-center gap-3 p-2 rounded-lg hover:bg-gray-50 transition-colors text-left bg-blue-50 text-blue-600">
              <FileText className="w-4 h-4" />
              <span className="text-sm">我的文档</span>
            </button>
            <button className="w-full flex items-center gap-3 p-2 rounded-lg hover:bg-gray-50 transition-colors text-left">
              <Users className="w-4 h-4 text-gray-600" />
              <span className="text-sm">共享文档</span>
            </button>
            <button className="w-full flex items-center gap-3 p-2 rounded-lg hover:bg-gray-50 transition-colors text-left">
              <Star className="w-4 h-4 text-gray-600" />
              <span className="text-sm">星标文档</span>
            </button>
          </div>

          <div className="p-4 border-t border-gray-100">
            <div className="flex items-center justify-between mb-3">
              <span className="text-sm text-gray-600">文件夹</span>
              <Button variant="ghost" size="icon" className="h-6 w-6">
                <FolderPlus className="w-4 h-4 text-gray-500" />
              </Button>
            </div>
            <div className="space-y-1">
              {mockFolders.map((folder) => (
                <button
                  key={folder.id}
                  className="w-full flex items-center gap-3 p-2 rounded-lg hover:bg-gray-50 transition-colors text-left"
                >
                  <div className={`w-8 h-8 rounded-lg bg-gradient-to-br ${folder.color} flex items-center justify-center flex-shrink-0`}>
                    <File className="w-4 h-4 text-white" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm truncate">{folder.name}</p>
                    <p className="text-xs text-gray-500">{folder.count}个文件</p>
                  </div>
                </button>
              ))}
            </div>
          </div>
        </ScrollArea>
      </div>

      {/* Main Content */}
      <div className="flex-1 bg-[#F7F8FA] flex flex-col">
        <div className="bg-white border-b border-gray-200 p-4">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-medium">我的文档</h3>
            <div className="flex items-center gap-2">
              <div className="relative w-64">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                <Input
                  placeholder="搜索文档"
                  className="pl-9 bg-gray-50 border-0"
                />
              </div>
              <Button variant="outline" size="icon">
                <MoreHorizontal className="w-4 h-4" />
              </Button>
            </div>
          </div>

          <Tabs defaultValue="list">
            <TabsList className="bg-gray-100">
              <TabsTrigger value="list">列表视图</TabsTrigger>
              <TabsTrigger value="grid">网格视图</TabsTrigger>
            </TabsList>
          </Tabs>
        </div>

        <ScrollArea className="flex-1">
          <div className="p-6">
            {/* Document List */}
            <div className="bg-white rounded-lg overflow-hidden">
              <table className="w-full">
                <thead className="bg-gray-50 border-b border-gray-200">
                  <tr>
                    <th className="text-left text-xs font-medium text-gray-600 p-3">文件名</th>
                    <th className="text-left text-xs font-medium text-gray-600 p-3">大小</th>
                    <th className="text-left text-xs font-medium text-gray-600 p-3">更新时间</th>
                    <th className="text-left text-xs font-medium text-gray-600 p-3">更新人</th>
                    <th className="text-right text-xs font-medium text-gray-600 p-3">操作</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {mockDocuments.map((doc) => (
                    <tr
                      key={doc.id}
                      className="hover:bg-gray-50 cursor-pointer transition-colors"
                      onClick={() => setSelectedDoc(doc)}
                    >
                      <td className="p-3">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-lg bg-gray-50 flex items-center justify-center">
                            {getFileIcon(doc.type)}
                          </div>
                          <div className="flex items-center gap-2">
                            <span className="text-sm">{doc.name}</span>
                            {doc.isStarred && (
                              <Star className="w-4 h-4 text-yellow-500 fill-yellow-500" />
                            )}
                          </div>
                        </div>
                      </td>
                      <td className="p-3">
                        <span className="text-sm text-gray-600">{doc.size}</span>
                      </td>
                      <td className="p-3">
                        <span className="text-sm text-gray-600">{doc.updatedAt}</span>
                      </td>
                      <td className="p-3">
                        <div className="flex items-center gap-2">
                          <div className="w-6 h-6 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white">
                            <span className="text-xs">{doc.updatedBy[0]}</span>
                          </div>
                          <span className="text-sm text-gray-600">{doc.updatedBy}</span>
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="flex items-center justify-end gap-1">
                          <Button variant="ghost" size="icon" className="h-8 w-8">
                            <Star className="w-4 h-4 text-gray-400" />
                          </Button>
                          <Button variant="ghost" size="icon" className="h-8 w-8">
                            <MoreHorizontal className="w-4 h-4 text-gray-400" />
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </ScrollArea>
      </div>
    </div>
  );
}
