import { createStore } from 'vuex';
import createPersistedState from 'vuex-persistedstate'; // 引入插件

const store = createStore({
    state: {
        userId: null
    },
    mutations: {
        setUserId(state, id) {
            state.userId = id;
        }
    },
    plugins: [
        createPersistedState({
            // 默认使用 localStorage，自动保存所有 state
            paths: ['userId'] // 只持久化 userId 字段
        })
    ]
});

export default store;
