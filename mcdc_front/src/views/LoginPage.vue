<template>
  <div class="auth-page">
    <div class="form-box">
      <h2>{{ state.isLogin ? '用户登录' : '用户注册' }}</h2>

      <div v-if="state.isLogin">
        <a-input v-model="state.loginForm.username" placeholder="用户名" />
        <a-input v-model="state.loginForm.password" placeholder="密码" type="password" style="margin-top: 10px" />
        <a-button type="primary" :loading="state.loading" long style="margin-top: 20px" @click="handleLogin">登录</a-button>
      </div>

      <div v-else>
        <a-input v-model="state.registerForm.username" placeholder="用户名" />
        <a-input v-model="state.registerForm.email" placeholder="邮箱" style="margin-top: 10px" />

        <div style="display: flex; gap: 10px; margin-top: 10px;">
          <a-input v-model="state.registerForm.emailCode" placeholder="邮箱验证码" style="flex: 1" />
          <a-button :disabled="countDown > 0" @click="sendEmailCode">
            {{ countDown > 0 ? `${countDown}s后重试` : '发送验证码' }}
          </a-button>
        </div>

        <a-input v-model="state.registerForm.password" placeholder="密码" type="password" style="margin-top: 10px" />
        <a-input v-model="state.registerForm.confirmPassword" placeholder="确认密码" type="password" style="margin-top: 10px" />

        <a-button type="primary" :loading="state.loading" long style="margin-top: 20px" @click="handleRegister">注册
        </a-button>
      </div>

      <a-button type="text" size="small" style="margin-top: 10px;" @click="state.isLogin = !state.isLogin">
        {{ state.isLogin ? '没有账号？去注册' : '已有账号？去登录' }}
      </a-button>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref} from 'vue';
import axios from 'axios';
import {Message} from '@arco-design/web-vue';
import { useRouter } from 'vue-router'
import { useStore } from 'vuex';

const router = useRouter()
const store = useStore();

const state = reactive({
  isLogin: true,
  loading: false,
  loginForm: {username: '', password: ''},
  registerForm: {
    username: '',
    password: '',
    confirmPassword: '',
    email: '',
    emailCode: ''
  }
});

const countDown = ref(0);
let timer = null;

// ✅ 发送验证码逻辑
const sendEmailCode = async () => {
  const email = state.registerForm.email;
  if (!email) {
    Message.warning('请先输入邮箱');
    return;
  }

  try {
    await axios.post('/sendEmailCode', {email});
    Message.success('验证码已发送，请检查邮箱');

    countDown.value = 60;
    timer = setInterval(() => {
      countDown.value--;
      if (countDown.value <= 0) clearInterval(timer);
    }, 1000);
  } catch (e) {
    Message.error('发送验证码失败');
  }
};

// ✅ 注册逻辑增加验证码校验
const handleRegister = async () => {
  const {username, password, confirmPassword, email, emailCode} = state.registerForm;
  if (!username || !password || !confirmPassword || !email || !emailCode) {
    Message.error('请填写完整信息');
    return;
  }
  if (password !== confirmPassword) {
    Message.error('两次密码不一致');
    return;
  }

  state.loading = true;
  try {
    const res = await axios.post('/handleRegister', {
      username,
      password,
      confirmPassword,
      email,
      emailCode
    });

    console.log("✅ 注册响应对象 =", res);       // 👈 打印完整 Axios 响应
    console.log("✅ 响应体 =", res.data);        // 👈 打印返回数据

    if (res.data.success) {
      Message.success('注册成功，已登录！');

      // TODO: 登录后跳转逻辑
      // ✅ 提取后端返回的 user_id
      const user_id = res.data.user_id;
      // ✅ 存入 Vuex（需提前在 store 中定义 setUserId mutation）
      store.commit('setUserId', user_id);
      // 跳转页面
      router.push({ path: '/indexPage' });

      state.isLogin = true;
    } else {
      Message.error(res.data.message || '注册失败');
    }
  } catch (err) {
    console.error('❌ 注册请求异常 =', err);       // 👈 捕获异常并打印
    if (err.response) {
      console.error('📛 响应状态码:', err.response.status);
      console.error('📛 响应内容:', err.response.data);
    }
    Message.error('注册请求失败');
  } finally {
    state.loading = false;
  }

};

// ✅ 登陆验证
const handleLogin = async () => {
  const { username, password } = state.loginForm;
  if (!username || !password) {
    Message.error('请输入用户名和密码');
    return;
  }

  state.loading = true;
  try {
    // ✅ 使用请求体 JSON 传参，更安全
    const res = await axios.post('/handleLogin', {
      username,
      password
    });

    console.log("🔥 登录响应 = ", res); // 看一下完整结构
    console.log("✅ 响应体 = ", res.data);

    if (res.data.success) {
      Message.success('登录成功');
      // TODO: 登录后跳转逻辑
      const userId = res.data.user_id; // 提取 user_id
      const role = res.data.role; // 提取 role
      // ✅ 保存 user_id 到 Vuex
      store.commit('setUserId', userId);
      // ✅ 根据角色跳转到对应页面
      if (role === '普通用户') {
        router.push({ path: '/indexPage' });
      } else if (role === '管理员') {
        router.push({ path: '/userManagement' });
      } else {
        Message.warning('未知角色，无法跳转页面');
      }

      // // 登录后跳转并用Vuex传递 user_id
      // router.push({
      //   path: '/indexPage',
      //   query: { user_id: userId }
      // });
    } else {
      Message.error('用户名或密码错误');
    }
  } catch (err) {
    Message.error('登录请求失败');
  } finally {
    state.loading = false;
  }
};

</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f5f5f5;
}

.form-box {
  width: 300px;
  padding: 30px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}
</style>
