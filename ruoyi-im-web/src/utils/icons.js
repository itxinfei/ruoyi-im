/**
 * Element Plus 图标统一导出
 * 避免在多个组件中重复导入相同图标
 */
export {
  // 基础图标
  Close,
  Check,
  Delete,
  Edit,
  Search,
  Plus,
  Minus,
  Refresh,
  More,
  MoreFilled,
  Setting,
  Star,
  StarFilled,
  User,
  Users,
  ChatLineSquare,
  Message,
  Phone,
  PhoneFilled,
  VideoCamera,
  VideoCameraFilled,
  Microphone,
  MicrophoneFilled,
  MuteNotification,
  Bell,
  BellFilled,
  Picture,
  PictureRounded,
  Folder,
  FolderOpened,
  Document,
  Link,
  Share,
  Download,
  Upload,
  Paperclip,
  Calendar,
  Clock,
  Location,
  LocationFilled,
  Warning,
  WarningFilled,
  InfoFilled,
  SuccessFilled,
  CircleCheck,
  CircleClose,
  Loading,
  ArrowLeft,
  ArrowRight,
  ArrowUp,
  ArrowDown,
  Back,
  CaretLeft,
  CaretRight,
  CaretUp,
  CaretDown,
  Switch,
  View,
  Hide,
  Lock,
  Unlock,
  Eye,
  EyeFilled,
  ZoomIn,
  ZoomOut,
 FullScreen,
  Crop,
  Filter,
  Sort,
  Grid,
  List,
  Menu,
  MoreFilled as More,
  Operation,
  Tools,
  Notification,
  Memo,
  ChatDotRound,
  ChatDotSquare,
  ChatLineSquare,
  ElementPlus,
  Star as StarOutline,
  Delete as DeleteOutline,
  Edit as EditOutline,
  Close as CloseOutline
} from '@element-plus/icons-vue'

// Material Icons (通过字符串使用，无需导入)
// 在 Element Plus 的 el-icon 组件中使用 class="material-icons-outlined"

/**
 * 常用图标组，便于按需导入
 */
export const CommonIcons = {
  // 操作类
  Close, Check, Delete, Edit, Search, Plus, Minus, Refresh,
  Setting, Share, Download, Upload, View, Hide,

  // 状态类
  Loading, Warning, WarningFilled, InfoFilled, SuccessFilled,
  CircleCheck, CircleClose,

  // 导航类
  ArrowLeft, ArrowRight, ArrowUp, ArrowDown, Back,
  CaretLeft, CaretRight, CaretUp, CaretDown,

  // 用户类
  User, Users, Phone, PhoneFilled, VideoCamera, VideoCameraFilled,
  Microphone, MicrophoneFilled,

  // 内容类
  ChatLineSquare, Message, Picture, PictureRounded, Folder,
  FolderOpened, Document, Link, Paperclip,

  // 其他
  Star, StarFilled, Bell, BellFilled, Calendar, Clock,
  Location, LocationFilled, Lock, Unlock
}

/**
 * 获取图标组件（用于动态图标）
 * @param {string} name - 图标名称
 * @returns {Component|null}
 */
export function getIcon(name) {
  return CommonIcons[name] || null
}
