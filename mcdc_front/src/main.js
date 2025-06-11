import { createApp } from 'vue'
import ArcoVue from '@arco-design/web-vue';
import App from './App.vue';
import '@arco-design/web-vue/dist/arco.css';
import { router } from './router'; // 导入路由配置
import axios from "./axios";
import store from './store';  // ✅ 引入 Vuex store


const app = createApp(App);
app.config.globalProperties.$axios = axios//全局引入axios，所有组件均可使用axios
app.use(router); // 使用路由中间件
app.use(ArcoVue);
app.mount('#app'); // 挂载 Vue 应用到 DOM 元素中
app.use(store);               // ✅ 注册 Vuex