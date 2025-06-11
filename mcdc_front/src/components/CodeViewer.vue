<template>
  <CodeBlock :code="content" :language="language" />
</template>

<script>
import axios from 'axios'
import CodeBlock from './CodeBlock.vue'

export default {
  name: 'CodeViewer',
  components: { CodeBlock },
  props: {
    programId: Number,
    filePath: String,
    testCase: String
  },
  data() {
    return {
      content: '请选择代码文件或测试用例...',
      language: 'plaintext'
    }
  },
  watch: {
    programId: 'loadContent',
    filePath: 'loadContent',
    testCase: {
      handler: 'loadContent',
      immediate: true
    }
  },
  methods: {
    detectLanguage(name) {
      const ext = (name || '').split('.').pop().toLowerCase()
      return {
        java: 'java',
        js: 'javascript',
        py: 'python',
        c: 'c',
        cpp: 'cpp'
      }[ext] || 'plaintext'
    },
    async loadContent() {
      if (this.testCase) {
        // 显示测试用例内容
        this.content = '加载中...'
        this.language = this.detectLanguage(this.testCase)
        console.log("this.testCase:"+this.testCase)
        try {
          const res = await axios.get('/getTestContent', {
            params: {name: this.testCase}
          })
          this.content = res.data || '（内容为空）'
        } catch (e) {
          console.error(e)
          this.content = '测试用例加载失败'
        }
      } else if (this.programId && this.filePath) {
        // 显示项目代码内容
        this.content = '加载中...'
        this.language = this.detectLanguage(this.filePath)
        try {
          const res = await axios.get('/getCodeContent', {
            params: {
              programId: this.programId,
              filePath: this.filePath
            }
          })
          this.content = res.data || '（内容为空）'
        } catch (e) {
          console.error(e)
          this.content = '代码加载失败'
        }
      } else {
        this.content = '请选择代码文件或测试用例...'
      }
    }
  }
}
</script>
