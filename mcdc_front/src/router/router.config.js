export default [
    {
        path: '/userManagement',// 路由地址
        name: 'userManagement',
        component: () => import('../views/userManagement.vue'),// 文件地址
        children: []
    },
    {
        path: '/',// 路由地址
        name: 'LoginPage',
        component: () => import('../views/LoginPage.vue'),// 文件地址
        children: []
    },
    {
        path: '/indexPage',// 路由地址
        name: 'indexPage',
        component: () => import('../views/indexPage.vue'),// 文件地址
        children: []
    },
    {
        path: '/CodeViewer',// 路由地址
        name: 'CodeViewer',
        component: () => import('../components/CodeViewer.vue'),// 文件地址
        children: []
    },
]