<template>
  <div class="layout-demo">
    <a-layout>
      <a-layout-header>
      <div style="margin:1.5vh 0 0 1vw; width: 100%; display: flex;">
        <a-button
            @click="triggerUpload"
            style="margin-right: 10px">
          上传项目
        </a-button>
        <input
            type="file"
            ref="projectInput"
            style="display: none"
            webkitdirectory
            multiple
            @change="handleFileUpload"
        />

        <div style="margin-left: auto;margin-right: 10vh;display: flex;justify-content: flex-end">
          <CalPage
              :userID="userId"
              style="width: 90%"
          />
        </div>

      </div>
    </a-layout-header>

      <a-layout>
        <!-- 左侧Sider：项目列表 -->
        <a-layout-sider :resize-directions="['right']">
          <a-card title="项目列表">
            <a-menu
                :selected-keys="[selectedProjectId]"
                @menu-item-click="handleProjectClick"
            >
              <a-menu-item
                  v-for="item in projectList"
                  :key="item.programId"
              >
                {{ item.programName }}
              </a-menu-item>
            </a-menu>
          </a-card>
        </a-layout-sider>

        <!-- 右侧Sider：文件结构 -->
        <a-layout-sider :resize-directions="['right']">
          <a-card title="项目结构">
            <a-tree
                :data="fileTree"
                :default-expand-all="true"
                :field-names="{ title: 'title', key: 'key', children: 'children' }"
                :show-line="showLine"
                @select="onFileSelected"
            />
          </a-card>
        </a-layout-sider>

        <!-- 主内容区域 Content -->
        <a-layout-content>
          <a-split direction="vertical" :default-size="500" style="height: 100%">
            <template #first>
              <div class="content-section">
                <a-card title="代码可视化">
                  <CodeViewer
                      :program-id="selectedProjectId"
                      :file-path="selectedFilePath"
                      :test-case="selectedTestCaseName"
                  />
                </a-card>
              </div>
            </template>
            <template #second>
              <div class="content-section">
                <!-- 下部分可以放置其他内容，例如日志输出等 -->
                <a-card title="覆盖率结果">
                  <pre style="white-space: pre-wrap; word-wrap: break-word;">{{ logOutput }}</pre>
                </a-card>
              </div>
            </template>
          </a-split>
        </a-layout-content>
      </a-layout>
    </a-layout>

    <a-modal
        v-model:visible="showRunModal"
        title="选择要运行的项目"
        @ok="handleRunProjectOk"       @cancel="handleRunProjectCancel" :mask-closable="false"         width="auto"                   >
      <div v-if="projectList.length > 0">
        <a-radio-group v-model="selectedProjectToRunId" direction="vertical">
          <a-radio v-for="item in projectList" :key="item.programId" :value="item.programId">
            {{ item.programName }} <span style="color: #999;">(ID: {{ item.programId }})</span>
          </a-radio>
        </a-radio-group>
      </div>
      <div v-else>
        <a-empty description="当前没有可运行的项目" />
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import {ref, onMounted, computed} from 'vue';
import axios from "axios";
import { Message} from '@arco-design/web-vue';
import CodeViewer from '@/components/CodeViewer.vue'
import CalPage from '@/components/CalPage.vue'; // 引入新创建的组件
import { useStore } from 'vuex';

//响应式变量
const store = useStore();
// const userId = store.state.userId;
const userId = computed(() => store.state.userId)
console.log("当前登录用户ID:", userId);

const selectedFilePath = ref(null);
const selectedTestCaseName = ref(null)

const projectList = ref([]);

const selectedProjectId = ref(null);

const fileTree = ref([]);
const showLine = ref(true);

const projectInput = ref(null);

//加载项目
const fetchProjects = async () => {
  console.log("刷新user_id传到后端："+userId.value);
  try {
    const res = await axios.get('/all',{
      params: {
        // userId: userId.value // 默认用户ID为2
        userId: 2 // 默认用户ID为2
      }
    })
    projectList.value = res.data
  } catch (err) {
    Message.error('项目列表加载失败')
  }
}

// 点击项目加载文件结构
const handleProjectClick = async (key) => {
  selectedProjectId.value = key;
  try {
    const res = await axios.get('/flat', {
      params: { programId: key },
    });
    //fetchTestProjects()//获取对应的测试用例列表
    fileTree.value = buildTreeFromPaths(res.data);
    Message.success('文件结构加载成功');
  } catch (err) {
    Message.error('文件结构加载失败');
  }
};

// 路径数组转 treeData
const buildTreeFromPaths=(paths)=> {
  const root = []
  for (const path of paths) {
    const parts = path.split('/')
    let current = root
    for (let i = 0; i < parts.length; i++) {
      const name = parts[i]
      const key = parts.slice(0, i + 1).join('/')
      let existing = current.find(node => node.key === key)
      if (!existing) {
        existing = { title: name, key, children: [] }
        current.push(existing)
      }
      current = existing.children
    }
  }
  return root
}

// 文件路径选择
const onFileSelected = (selectedKeys, { node }) => {
  if (!node.children || node.children.length === 0) {
    selectedFilePath.value = selectedKeys[0]
    selectedTestCaseName.value = null // 清除测试用例展示
  }
}

//触发上传
const triggerUpload = () => {
  projectInput.value && projectInput.value.click();
  //document.querySelector('input[type="file"]').click();
}


//上传项目
const handleFileUpload = async(event)=> {
  const files = event.target.files;
  if (!files.length) return;

  const formData = new FormData();
  const programName = prompt("请输入项目名称");
  if (!programName) return;

  formData.append("programName", programName);
  formData.append("user_id", 2);
  // formData.append("user_id", userId.value);
  for (let file of files) {
    formData.append("files", file, file.webkitRelativePath);
  }

  try {
    await axios.post('/uploadProject', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    Message.success('上传成功！');
    location.reload();
  } catch (e) {
    console.log(e)
    Message.error('上传失败');
  }
}

const logOutput = ref(''); // 日志输出内容

onMounted(() => {
  fetchProjects()
})

</script>

<style scoped>
.layout-demo {
  /* 确保 .layout-demo 占据整个视口高度 */
  height: 98vh;
  display: flex;
  flex-direction: column;
}

/* 确保最外层的 a-layout 占据整个 .layout-demo 的高度 */
.layout-demo :deep(.arco-layout) {
  flex: 1; /* 让最外层 layout 填充剩余空间 */
  min-height: 0; /* 允许其收缩 */
  display: flex;
  flex-direction: column;
}

.layout-demo :deep(.arco-layout-header),
.layout-demo :deep(.arco-layout-sider-children),
.layout-demo :deep(.arco-layout-content) {
  /* 保持 flex-direction: column，因为父元素是 column */
  /* color: var(--color-white); */ /* 这些属性在深层选择器中不总是必要 */
  /* font-size: 20px; */
  /* font-stretch: condensed; */
  /* text-align: center; */
}

.layout-demo :deep(.arco-layout-sider-children) {
  justify-content: flex-start;
  margin: 10px;
  /* 确保 sider 内部的 card 内容也可以滚动 */
  height: 100%; /* Sider 的 children 占据 Sider 的全部高度 */
  display: flex;
  flex-direction: column; /* 让 Card 在 sider 内部垂直布局 */
  overflow: hidden; /* 隐藏 Sider 自身的滚动条，让 Card 内部滚动 */
}

/* 确保 Sider 内部的 Card Body 能够滚动 */
.layout-demo :deep(.arco-layout-sider-children .arco-card) {
  flex: 1; /* Card 填充 Sider 内部空间 */
  min-height: 0; /* 允许 Card 收缩 */
  display: flex;
  flex-direction: column; /* 让 Card 的 Header/Body 垂直布局 */
}

.layout-demo :deep(.arco-layout-sider-children .arco-card-body) {
  flex: 1; /* Card Body 填充 Card 剩余空间 */
  overflow: auto; /* 让 Card Body 内部内容滚动 */
  min-height: 0; /* 允许 Card Body 收缩 */
  padding: 0; /* 移除默认 padding，如果菜单或其他内容有自己的 padding */
}


.layout-demo :deep(.arco-tree) {
  font-family: 'Microsoft YaHei', sans-serif;
}

.layout-demo :deep(.arco-layout-header) {
  height: 7vh; /* 固定 Header 高度 */
  background-color: rgba(1, 25, 85, 0.8);
}

/* 关键修改：让内部的 arco-layout 填充除 header 外的剩余高度 */
.layout-demo :deep(.arco-layout > .arco-layout) {
  flex: 1; /* 让这个内部的 layout 填充其父 layout 的剩余空间 */
  min-height: 0; /* 允许其收缩，避免撑开外部容器 */
  display: flex; /* 它已经是 flex 容器，但明确一下 */
  flex-direction: row; /* 它的子元素 (sider, content) 是横向排列的 */
}


.layout-demo :deep(.arco-layout-sider) {
  width: 206px;
  min-width: 150px;
  max-width: 25vw;
  /* min-height: 93vh; -- 这行可以移除，因为父级 layout 已经有明确高度 */
  background-color: rgba(246, 246, 246, 0.8);
  font-family: 'Microsoft YaHei', sans-serif;
}

/* 主内容区域 Content */
.layout-demo :deep(.arco-layout-content) {
  background-color: rgba(246, 246, 246, 0.8);
  padding: 10px;
  flex: 1; /* 让 content 填充其父 flex 容器 (内部 arco-layout) 的剩余宽度 */
  min-height: 0; /* 允许其收缩 */
  display: flex;
  flex-direction: column; /* 让内部的 split 垂直布局 */
}

/* 针对 a-layout-split 内部的两个 content-section */
.content-section {
  /* 这些 section 是 a-layout-split 的直接子元素，split 会为它们分配高度 */
  padding: 10px;
  background-color: rgba(255, 255, 255, 0.9);
  border: 1px solid #ddd;
  border-radius: 4px;

  flex: 1; /* 让 content-section 填充其分配到的高度 */
  min-height: 0; /* 允许内容收缩 */
  box-sizing: border-box; /* 确保 padding 不会增加总高度 */
  display: flex; /* 让其内部的 a-card 垂直布局 */
  flex-direction: column;
  overflow: hidden; /* 默认隐藏滚动条，让 card-body 滚动 */
}

/* 确保 a-card 本身在 content-section 内部能够正确伸缩 */
.content-section :deep(.arco-card) {
  flex: 1; /* 让 card 填充 content-section 的所有空间 */
  min-height: 0; /* 允许 card 收缩 */
  display: flex;
  flex-direction: column; /* card 内部的 header 和 body 垂直布局 */
}

/* 调整 a-card 内部，让 card 的内容区域可以滚动 */
.content-section :deep(.arco-card-body) {
  flex: 1; /* 让 card body 填充剩余空间 */
  overflow: auto; /* **关键**：让 card body 内部的内容滚动 */
  min-height: 0; /* 允许 card body 收缩 */
  padding: 0; /* 移除 Arco Card Body 默认 padding，或者根据需要调整 */
  /* 为了让 pre 标签的 padding 生效，可以给 pre 标签加 padding */
}

/* 确保 pre 标签内的内容不会溢出 */
.content-section pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  padding: 10px; /* 给 pre 标签添加内边距，使内容不贴边 */
}

</style>