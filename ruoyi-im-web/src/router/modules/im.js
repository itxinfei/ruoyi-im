export default [
  {
    path: 'im/chat',
    name: 'Chat',
    component: () => import('@/views/im/chat/ChatContainer.vue'),
    meta: {
      title: '聊天',
      icon: 'chat',
      keepAlive: true,
      activeMenu: '/im/chat',
    },
  },
  {
    path: 'im/contacts',
    name: 'Contacts',
    component: () => import('@/views/im/contacts/index.vue'),
    meta: {
      title: '联系人',
      icon: 'contacts',
      keepAlive: true,
      activeMenu: '/im/contacts',
    },
  },
  {
    path: 'im/group',
    name: 'Group',
    component: () => import('@/views/im/group/index.vue'),
    meta: {
      title: '群组',
      icon: 'group',
      keepAlive: true,
      activeMenu: '/im/group',
    },
  },
  {
    path: 'im/file/list',
    name: 'FileList',
    component: () => import('@/views/im/file/index.vue'),
    meta: {
      title: '文件管理',
      icon: 'file',
      keepAlive: false,
      activeMenu: '/im/file',
    },
  },
  {
    path: 'im/user/list',
    name: 'UserList',
    component: () => import('@/views/im/user/index.vue'),
    meta: {
      title: '用户管理',
      icon: 'user',
      keepAlive: false,
      activeMenu: '/im/user',
    },
  },
  {
    path: 'im/message/list',
    name: 'MessageList',
    component: () => import('@/views/im/message/index.vue'),
    meta: {
      title: '消息管理',
      icon: 'message',
      keepAlive: false,
      activeMenu: '/im/message',
    },
  },
  {
    path: 'im/settings',
    name: 'Settings',
    component: () => import('@/views/im/settings/index.vue'),
    meta: {
      title: '设置',
      icon: 'settings',
      keepAlive: false,
      activeMenu: '/im/settings',
    },
  },
]
