export default [
  {
    path: 'chat',
    name: 'Chat',
    component: () => import('@/views/im/chat/ChatContainer.vue'),
    meta: {
      title: '聊天',
      icon: 'chat',
      keepAlive: true,
      activeMenu: '/chat'
    },
  },
  {
    path: 'contacts',
    name: 'Contacts',
    component: () => import('@/views/im/contacts/index.vue'),
    meta: {
      title: '联系人',
      icon: 'contacts',
      keepAlive: true,
      activeMenu: '/contacts'
    },
  },
  {
    path: 'group',
    name: 'Group',
    component: () => import('@/views/im/group/index.vue'),
    meta: {
      title: '群组',
      icon: 'group',
      keepAlive: true,
      activeMenu: '/group'
    },
  },
  {
    path: 'files',
    name: 'Files',
    component: () => import('@/views/im/file/index.vue'),
    meta: {
      title: '文件',
      icon: 'file',
      keepAlive: false,
      activeMenu: '/files'
    },
  },
  {
    path: 'settings',
    name: 'Settings',
    component: () => import('@/views/im/settings/index.vue'),
    meta: {
      title: '设置',
      icon: 'settings',
      keepAlive: false,
      activeMenu: '/settings'
    },
  },
]
