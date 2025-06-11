<template>
  <div class="layout-demo">
    <a-layout>
      <a-layout-header>
        <div class="header-content">
          <a-space class="header-left">
            <a-button @click="triggerUpload" style="margin-right: 10px">
              <a-icon type="upload"/>上传项目
            </a-button>
            <input
                type="file"
                ref="fileInput"
                style="display: none"
                webkitdirectory
                multiple
                @change="handleFileUpload"
            />
          </a-space>

          <a-space class="header-left">
            <a-button
                type="primary"
                @click="showTestCaseModal"
                :disabled="!selectedTestProgramId"
            >
              生成测试用例
            </a-button>

            <a-button
                @click="showAnalysisModal"
            >
              进行MC/DC分析
            </a-button>
          </a-space>
        </div>
      </a-layout-header>

      <a-layout>
        <a-layout-sider :resize-directions="['right']">
          Sider
        </a-layout-sider>

        <a-layout-sider :resize-directions="['right']">
          <a-tree
              :default-selected-keys="['0-0-1']"
              :data="treeData"
              :show-line="showLine"
          />
        </a-layout-sider>

        <a-layout-content>Content</a-layout-content>
      </a-layout>
    </a-layout>

    <a-modal
        v-model:visible="testCaseModalVisible"
        title="MC/DC 测试用例生成"
        width="80%"
        :footer="null"
        :maskClosable="false"
    >
      <div class="modal-content">
        <div class="selection-section">
          <label>选择程序:</label>
          <a-select
              v-model:value="selectedTestProgramId"
              style="width: 100%"
              :options="programOptions"
              @change="val => selectedTestProgramId = val"
          />
        </div>

        <div class="action-section" style="display: flex; justify-content: center;">
          <a-button
              type="primary"
              @click="generateTestCases"
              :disabled="!selectedTestProgramId || generating"
              :loading="generating"
              block
              style="margin: 0 auto"
          >
            {{ generating ? '生成中...' : '生成测试用例' }}
          </a-button>
        </div>

        <div v-if="generating" class="loading-container">
          <a-spin size="large" />
          <p>正在生成测试用例...</p>
        </div>

        <div v-else-if="testCases.length > 0" class="test-cases-container" style="max-height: 400px; overflow-y: auto;">
          <h3>生成的MC/DC测试用例 (共 {{ testCases.length }} 个):</h3>
          <table class="custom-table">
            <thead>
            <tr>
              <th>#</th>
              <th>逻辑表达式</th>
              <th>条件组合</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(testCase, index) in testCases" :key="index">
              <td>{{ testCase.index }}</td>
              <td>{{ testCase.expression }}</td>
              <td>
                <div v-for="(value, key) in testCase.conditionValues" :key="key">
                  {{ key }}: {{ value }}
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
        <div v-else class="empty-tip">
          <a-empty description="请选择程序并生成测试用例" />
        </div>
      </div>
    </a-modal>

    <a-modal v-model:visible="analysisModalVisible" @ok="handleOk" @cancel="handleCancel" draggable class="mcdc">
      <template #title>
        MC/DC 覆盖分析
      </template>
      <div class="selection-section">
        <label for="programSelect">选择程序:</label>
        <select id="programSelect" v-model="selectedProgramId" @change="fetchTestResources">
          <option v-for="program in programs" :key="program.programId" :value="program.programId">
            {{ program.programName }} (版本: {{ program.version }})
          </option>
        </select>
      </div>

      <div class="selection-section">
        <label for="testResourceSelect">选择测试套件:</label>
        <select id="testResourceSelect" v-model="selectedTestResourceId">
          <option v-if="testResources.length === 0" value="">此程序无可用测试套件</option>
          <option v-for="resource in testResources" :key="resource.id" :value="resource.id">
            {{ resource.name }}
          </option>
        </select>
      </div>

      <button
          style="width: 40%; margin: 0 auto;"
          @click="runMCDCAnalysis"
          :disabled="!selectedProgramId || !selectedTestResourceId || loading"
      >
        {{ loading ? '分析中...' : '运行 MC/DC 分析' }}
      </button>

      <div v-if="coverageResult" class="results-section">
        <h3>分析结果:</h3>
        <p>MC/DC 覆盖率: {{ coverageResult.mcdcCoverage }}%</p>
        <div v-if="coverageResult.details">
          <h4>覆盖详情:</h4>
          <pre style="max-height: 200px;overflow: auto">{{ JSON.stringify(coverageResult.details, null, 2) }}</pre>
        </div>
        <p v-if="coverageResult.error" class="error-message">{{ coverageResult.error }}</p>
      </div>

      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
      <template #footer>
        <div/>
      </template>
    </a-modal>
  </div>
</template>
<script>
import axios from "axios";
import { Message } from '@arco-design/web-vue';

export default {
  name: 'CalPage',
  components: {},
  data() {
    return {
      programs: [],
      selectedProgramId: null,
      selectedTestProgramId: null,
      testResources: [],
      selectedTestResourceId: null,
      loading: false,
      generating: false,
      testResourcesLoading: false,
      coverageResult: null,
      errorMessage: null,
      testCaseModalVisible: false,
      analysisModalVisible: false,
      testCases: [], // 存储解析后的测试用例数据
      userID: 2,
      treeData: [
        {
          title: 'Trunk 1',
          key: '0-0',
          children: [
            {
              title: 'Trunk 1',
              key: '0-0-0',
              children: [
                {title: 'leaf', key: '0-0-0-0'},
                {
                  title: 'leaf',
                  key: '0-0-0-1',
                  children: [{title: 'leaf', key: '0-0-0-1-0'}],
                },
                {title: 'leaf', key: '0-0-0-2'},
              ],
            },
          ],
        },
      ],
      testCaseColumns: [
        {
          title: '#',
          dataIndex: 'index',
          width: 60,
          align: 'center'
        },
        {
          title: '逻辑表达式',
          dataIndex: 'expression',
          ellipsis: true,
          width: '30%'
        },
        {
          title: '条件组合',
          dataIndex: 'conditionValues',
          width: '70%'
        }
      ],
      showLine: true
    };
  },
  computed: {
    programOptions() {
      return this.programs.map(p => ({
        value: p.programId,
        label: `${p.programName} (v${p.version})`
      }));
    },
    testResourceOptions() {
      return this.testResources.map(r => ({
        value: r.id,
        label: r.name
      }));
    }
  },
  created() {
    this.fetchPrograms();
  },
  methods: {
    // 显示测试用例生成模态框
    showTestCaseModal() {
      this.testCaseModalVisible = true;
      this.testCases = []; // 清空之前的测试用例
    },
    // 显示MC/DC分析模态框
    showAnalysisModal() {
      this.analysisModalVisible = true;
    },
    // 获取用户上传的程序列表
    async fetchPrograms() {
      try {
        const response = await axios.get(`/getProgramsByUserID/${this.userID}`);
        this.programs = response.data;
        if (this.programs.length > 0) {
          // 默认选中第一个程序，用于分析和测试用例生成
          this.selectedProgramId = this.programs[0].programId;
          this.selectedTestProgramId = this.programs[0].programId;
          this.fetchTestResources(); // 获取对应程序的测试资源
        }
      } catch (error) {
        this.errorMessage = '加载程序失败。';
        console.error('获取程序错误:', error);
      }
    },
    // 处理MC/DC分析模块中程序选择的改变
    async handleProgramChange(programId) {
      this.selectedProgramId = programId;
      this.selectedTestResourceId = null; // 清空当前选中的测试套件
      this.testResources = []; // 清空测试套件列表
      await this.fetchTestResources(); // 重新获取新程序的测试资源
    },
    // 获取当前选中程序的测试资源
    async fetchTestResources() {
      if (!this.selectedProgramId) {
        this.testResources = [];
        this.selectedTestResourceId = null;
        return;
      }

      this.testResourcesLoading = true;
      try {
        const response = await axios.get(`/getTestResourceByProgramID/${this.selectedProgramId}`);
        this.testResources = response.data;
        if (this.testResources.length > 0) {
          this.selectedTestResourceId = this.testResources[0].id; // 默认选中第一个测试套件
        }
      } catch (error) {
        this.errorMessage = '加载测试资源失败。';
        console.error('获取测试资源错误:', error);
      } finally {
        this.testResourcesLoading = false;
      }
    },
    // 运行MC/DC分析
    async runMCDCAnalysis() {
      if (!this.selectedProgramId || !this.selectedTestResourceId) {
        this.errorMessage = '请选择程序和测试套件。';
        return;
      }

      this.loading = true;
      this.coverageResult = null;
      this.errorMessage = null;

      try {
        const response = await axios.post('/MCDCAnalysis', {
          programId: this.selectedProgramId,
          testResourceId: this.selectedTestResourceId,
        });
        this.coverageResult = response.data;
        // 正确打印对象，避免 [object Object]
        console.log("MCDCAnalysis:", response.data);
        // 或者使用 JSON.stringify：
        // console.log("MCDCAnalysis:" + JSON.stringify(response.data, null, 2));
      } catch (error) {
        this.errorMessage =
            'MC/DC 分析失败。' +
            (error.response ? error.response.data.message : error.message);
        console.error('MC/DC 分析错误:', error);
      } finally {
        this.loading = false;
      }
    },
    // 生成测试用例
    async generateTestCases() {
      if (!this.selectedTestProgramId) {
        Message.error('请先选择程序');
        return;
      }

      this.generating = true;
      this.testCases = [];
      try {
        const res = await axios.get('/generateTestCase', {
          params: { programId: this.selectedTestProgramId }
        });

        // 直接使用后端返回的数据
        this.testCases = res.data;

        // 添加调试日志
        console.log("原始测试用例数据:", res.data);
        console.log("赋值后的测试用例:", this.testCases);

        // 验证数据格式
        if (!Array.isArray(this.testCases) || this.testCases.length === 0) {
          Message.warning('没有生成任何测试用例或返回数据格式不正确。');
          return;
        }

        // 确保每个测试用例都有索引
        this.testCases.forEach((testCase, index) => {
          if (testCase.index === undefined || testCase.index === null) {
            testCase.index = index + 1;
          }
        });

        Message.success(`成功生成 ${this.testCases.length} 个测试用例`);
      } catch (error) {
        const errorMsg = error.response?.data?.message || error.message;
        Message.error(`生成测试用例失败: ${errorMsg}`);
        console.error('生成失败详情:', error);
      } finally {
        this.generating = false;
      }
    },
    // 触发文件上传输入框点击
    triggerUpload() {
      this.$refs.fileInput.click();
    },
    // 处理文件上传
    async handleFileUpload(event) {
      const files = event.target.files;
      if (!files.length) return;

      const formData = new FormData();
      const programName = prompt("请输入项目名称");
      if (!programName) return; // 如果用户取消输入，则不上传

      formData.append("programName", programName);
      for (let file of files) {
        // webkitRelativePath 用于获取文件在目录中的相对路径
        formData.append("files", file, file.webkitRelativePath);
      }

      try {
        await axios.post('/uploadProject', formData, {
          headers: {'Content-Type': 'multipart/form-data'}
        });
        Message.success('上传成功！');
        this.fetchPrograms(); // 上传成功后刷新程序列表
      } catch (e) {
        console.error('上传失败:', e);
        Message.error('上传失败');
      }
    }
  }
};
</script>
<style scoped>
.layout-demo :deep(.arco-layout-header) {
  height: 7vh;
  background-color: rgba(1, 25, 85, 0.8);
  padding: 0 20px;
  display: flex;
  align-items: center;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  margin-left: auto;
}

.layout-demo :deep(.arco-layout-sider) {
  width: 206px;
  min-width: 150px;
  max-width: 20vw;
  height: 93vh;
  background-color: rgba(246, 246, 246, 0.8);
}

.layout-demo :deep(.arco-layout-content) {
  background-color: rgb(var(--arcoblue-6));
}

.selection-section {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
}

select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 16px;
}

.mcdc button {
  display: block;
  width: 100%;
  padding: 12px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 18px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.mcdc button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.mcdc button:hover:not(:disabled) {
  background-color: #0056b3;
}

.results-section {
  margin-top: 30px;
  padding: 20px;
  background-color: #f9f9f9;
  border: 1px solid #eee;
  border-radius: 5px;

}

.results-section h3 {
  color: #28a745;
  margin-bottom: 15px;
}

.results-section p {
  font-size: 1.1em;
  line-height: 1.6;
}

.error-message {
  color: #dc3545;
  margin-top: 20px;
  text-align: center;
  font-weight: bold;
}

pre {
  background-color: #e9e9e9;
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
}

.selection-section {
  margin: 10px 0;
}

.results-section {
  margin-top: 15px;
}

.error-message {
  color: red;
  margin-top: 10px;
}

.custom-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
}

.custom-table th,
.custom-table td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}

.custom-table th {
  background-color: #f2f2f2;
  font-weight: bold;
}

.custom-table tr:nth-child(even) {
  background-color: #f9f9f9;
}

.custom-table tr:hover {
  background-color: #f1f1f1;
}

.test-cases-container {
  margin-top: 20px;
  padding: 20px;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style>