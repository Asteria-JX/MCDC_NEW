<template>
  <div class="code-block">
    <div class="header">
      <span>{{ language.toUpperCase() }}</span>
      <button class="copy-button" @click="copyCode">复制</button>
    </div>
    <pre class="prism-block line-numbers"><code ref="codeElement" :class="'language-' + language">{{ code }}</code></pre>
  </div>
</template>

<script>
import Prism from 'prismjs'
import 'prismjs/themes/prism.css' // ✅ GitHub风格（浅色）
//import 'prismjs/themes/prism-tomorrow.css' // ✅ GitHub风格（深色）
import 'prismjs/plugins/line-numbers/prism-line-numbers.css'
import 'prismjs/plugins/line-numbers/prism-line-numbers.js'

// ✅ 引入常用语言
import 'prismjs/components/prism-java'
import 'prismjs/components/prism-python'
import 'prismjs/components/prism-javascript'
import 'prismjs/components/prism-c'
import 'prismjs/components/prism-cpp'

export default {
  props: {
    code: String,
    language: {
      type: String,
      default: 'java'
    }
  },
  mounted() {
    this.highlight()
  },
  updated() {
    this.highlight()
  },
  methods: {
    highlight() {
      if (this.$refs.codeElement) {
        Prism.highlightElement(this.$refs.codeElement)
      }
    },
    copyCode() {
      navigator.clipboard.writeText(this.code).then(() => {
        alert('代码已复制')
      })
    }
  }
}
</script>

<style scoped>
.code-block {
  background-color: #f6f8fa;
  border-radius: 6px;
  overflow: auto;
  margin: 12px 0;
  border: 1px solid #d1d5da;
  position: relative;
}

.header {
  background-color: #eaeef2;
  color: #24292e;
  font-size: 12px;
  font-family: monospace;
  padding: 6px 12px;
  border-bottom: 1px solid #d1d5da;
  display: flex;
  justify-content: space-between;
  align-items: center;
  text-transform: uppercase;
}

.copy-button {
  background-color: transparent;
  border: 1px solid #ccc;
  color: #333;
  padding: 2px 8px;
  font-size: 11px;
  border-radius: 4px;
  cursor: pointer;
}

.copy-button:hover {
  background-color: #d1d5da;
  color: #000;
}

.prism-block {
  padding: 16px;
  font-family: monospace;
  font-size: 14px;
  color: #24292e;
  white-space: pre; /* ✅ 保留缩进和换行 */
  overflow-x: auto;
}
</style>
