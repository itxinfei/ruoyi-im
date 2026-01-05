export default [
  {
    path: '/im',
    component: () => import('@/views/im/ImChatLayoutOptimized.vue'),
    meta: {
      title: '聊天',
      icon: 'chat',
      keepAlive: true,
    },
    children: [
      {
        path: 'chat/:id?',
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
        path: 'contacts',
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
        path: 'contact/:id',
        name: 'ContactDetail',
        component: () => import('@/views/im/contacts/detail.vue'),
        meta: {
          title: '联系人详情',
          hidden: true,
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
          activeMenu: '/im/group',
        },
      },
      {
        path: 'group/create',
        name: 'GroupCreate',
        component: () => import('@/views/im/group/create.vue'),
        meta: {
          title: '创建群组',
          hidden: true,
        },
      },
      {
        path: 'group/:id',
        name: 'GroupDetail',
        component: () => import('@/views/im/group/detail.vue'),
        meta: {
          title: '群组详情',
          hidden: true,
        },
      },
      {
        path: 'group/:id/manage',
        name: 'GroupManage',
        component: () => import('@/views/im/group/manage.vue'),
        meta: {
          title: '群组管理',
          hidden: true,
        },
      },
      {
        path: 'file',
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
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/im/settings/index.vue'),
        meta: {
          title: '设置',
          icon: 'settings',
          keepAlive: false,
          activeMenu: '/im/settings',
        },
      },
    ],
  },
]
