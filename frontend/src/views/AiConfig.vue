<template>
  <div class="ai-config-page">
    <div class="page-header">
      <h1>🤖 AI 模型配置</h1>
      <p class="subtitle">配置大语言模型，实现智能化菜谱生成</p>
    </div>

    <div v-if="loading" class="loading">
      <div class="spinner"></div>
    </div>

    <div v-else class="config-container">
      <div class="tabs">
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'presets' }"
          @click="activeTab = 'presets'"
        >
          预设配置
        </button>
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'my-configs' }"
          @click="activeTab = 'my-configs'"
        >
          我的配置
        </button>
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'create' }"
          @click="activeTab = 'create'"
        >
          + 新建配置
        </button>
      </div>

      <div class="tab-content">
        <div v-if="activeTab === 'presets'" class="presets-section">
          <div class="section-info">
            <p>以下是预设的大模型配置，您可以复制它们并填入您的 API Key 来使用。</p>
          </div>
          
          <div v-if="presets.length === 0" class="empty-section">
            <p>暂无预设配置</p>
          </div>

          <div v-else class="config-cards">
            <div 
              v-for="preset in presets" 
              :key="preset.id" 
              class="config-card"
              :class="{ active: preset.isActive }"
            >
              <div class="card-header">
                <h3>{{ preset.name }}</h3>
                <span v-if="preset.isActive" class="active-badge">当前激活</span>
              </div>
              <div class="card-body">
                <div class="config-field">
                  <span class="field-label">API 地址:</span>
                  <span class="field-value">{{ preset.baseUrl }}</span>
                </div>
                <div class="config-field">
                  <span class="field-label">默认模型:</span>
                  <span class="field-value">{{ preset.model }}</span>
                </div>
                <div v-if="preset.description" class="config-field">
                  <span class="field-label">说明:</span>
                  <span class="field-value">{{ preset.description }}</span>
                </div>
              </div>
              <div class="card-footer">
                <button 
                  class="btn btn-primary btn-sm"
                  @click="duplicatePreset(preset)"
                >
                  📋 使用此配置
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="activeTab === 'my-configs'" class="my-configs-section">
          <div v-if="customConfigs.length === 0" class="empty-section">
            <p>暂无自定义配置，请从"预设配置"复制或创建新配置</p>
          </div>

          <div v-else class="config-cards">
            <div 
              v-for="config in customConfigs" 
              :key="config.id" 
              class="config-card"
              :class="{ active: config.isActive }"
            >
              <div class="card-header">
                <h3>{{ config.name }}</h3>
                <div class="header-actions">
                  <span v-if="config.isActive" class="active-badge">当前激活</span>
                  <span v-else class="inactive-badge">未激活</span>
                </div>
              </div>
              <div class="card-body">
                <div class="config-field">
                  <span class="field-label">API 地址:</span>
                  <span class="field-value">{{ config.baseUrl }}</span>
                </div>
                <div class="config-field">
                  <span class="field-label">模型:</span>
                  <span class="field-value">{{ config.model }}</span>
                </div>
                <div class="config-field">
                  <span class="field-label">API Key:</span>
                  <span class="field-value masked">
                    {{ config.apiKey ? '******' + config.apiKey.slice(-4) : '未设置' }}
                  </span>
                </div>
              </div>
              <div class="card-footer">
                <button 
                  v-if="!config.isActive"
                  class="btn btn-success btn-sm"
                  @click="activateConfig(config)"
                >
                  ✓ 激活
                </button>
                <button 
                  class="btn btn-outline btn-sm"
                  @click="editConfig(config)"
                >
                  ✏️ 编辑
                </button>
                <button 
                  class="btn btn-outline btn-sm"
                  @click="testConfig(config)"
                >
                  🔍 测试连接
                </button>
                <button 
                  class="btn btn-danger btn-sm"
                  @click="deleteConfig(config)"
                >
                  🗑️ 删除
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="activeTab === 'create' || activeTab === 'edit'" class="create-section">
          <div class="form-container card">
            <h2>{{ activeTab === 'edit' ? '编辑配置' : '新建配置' }}</h2>
            
            <form @submit.prevent="handleSubmit">
              <div class="form-group">
                <label class="form-label">配置名称 *</label>
                <input
                  v-model="form.name"
                  type="text"
                  class="form-control"
                  placeholder="例如：我的智谱AI配置"
                  required
                />
              </div>

              <div class="form-group">
                <label class="form-label">API 地址 (Base URL) *</label>
                <input
                  v-model="form.baseUrl"
                  type="text"
                  class="form-control"
                  placeholder="例如：https://open.bigmodel.cn/api/paas/v4/"
                  required
                />
                <small class="form-hint">请确保地址以 / 结尾</small>
              </div>

              <div class="form-group">
                <label class="form-label">API Key *</label>
                <input
                  v-model="form.apiKey"
                  type="password"
                  class="form-control"
                  placeholder="请输入您的 API Key"
                  required
                />
              </div>

              <div class="form-group">
                <label class="form-label">模型名称 *</label>
                <input
                  v-model="form.model"
                  type="text"
                  class="form-control"
                  placeholder="例如：glm-4-flash、deepseek-chat、gpt-3.5-turbo"
                  required
                />
                <small class="form-hint">
                  常用模型：智谱(glm-4-flash, glm-4)、DeepSeek(deepseek-chat, deepseek-coder)、OpenAI(gpt-3.5-turbo, gpt-4)
                </small>
              </div>

              <div class="form-group">
                <label class="form-label">描述（可选）</label>
                <textarea
                  v-model="form.description"
                  class="form-control"
                  placeholder="简单描述此配置的用途..."
                  rows="2"
                ></textarea>
              </div>

              <div class="form-group">
                <label class="checkbox-label">
                  <input
                    v-model="form.isActive"
                    type="checkbox"
                  />
                  <span>设为当前激活配置</span>
                </label>
              </div>

              <div class="form-actions">
                <button 
                  type="button" 
                  class="btn btn-secondary"
                  @click="cancelEdit"
                >
                  取消
                </button>
                <button 
                  type="button" 
                  class="btn btn-outline"
                  @click="testCurrentConfig"
                  :disabled="testingConnection"
                >
                  {{ testingConnection ? '测试中...' : '测试连接' }}
                </button>
                <button 
                  type="submit" 
                  class="btn btn-primary"
                  :disabled="submitting"
                >
                  {{ submitting ? '保存中...' : '保存配置' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { aiConfigApi, aiRecipeApi } from '../api'

const loading = ref(false)
const submitting = ref(false)
const testingConnection = ref(false)
const activeTab = ref('presets')

const presets = ref([])
const customConfigs = ref([])
const editingId = ref(null)

const defaultForm = {
  name: '',
  baseUrl: '',
  apiKey: '',
  model: '',
  description: '',
  isActive: false
}

const form = reactive({ ...defaultForm })

const loadConfigs = async () => {
  loading.value = true
  try {
    const [presetsResult, allResult] = await Promise.all([
      aiConfigApi.getPresets(),
      aiConfigApi.getAll()
    ])
    
    presets.value = presetsResult.data.data || []
    const allConfigs = allResult.data.data || []
    customConfigs.value = allConfigs.filter(c => !c.isPreset)
  } catch (error) {
    console.error('加载配置失败:', error)
    alert('加载配置失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const duplicatePreset = async (preset) => {
  try {
    const result = await aiConfigApi.duplicatePreset(preset.id)
    alert('配置已复制，请填写您的 API Key')
    
    form.name = result.data.data.name
    form.baseUrl = result.data.data.baseUrl
    form.model = result.data.data.model
    form.description = result.data.data.description
    form.apiKey = ''
    form.isActive = false
    editingId.value = result.data.data.id
    
    activeTab.value = 'edit'
  } catch (error) {
    alert('复制失败: ' + error.message)
  }
}

const editConfig = (config) => {
  editingId.value = config.id
  form.name = config.name
  form.baseUrl = config.baseUrl
  form.apiKey = config.apiKey
  form.model = config.model
  form.description = config.description || ''
  form.isActive = config.isActive
  activeTab.value = 'edit'
}

const cancelEdit = () => {
  editingId.value = null
  Object.assign(form, defaultForm)
  activeTab.value = 'my-configs'
}

const activateConfig = async (config) => {
  try {
    await aiConfigApi.activate(config.id)
    alert('配置已激活')
    loadConfigs()
  } catch (error) {
    alert('激活失败: ' + error.message)
  }
}

const deleteConfig = async (config) => {
  if (!confirm(`确定要删除配置 "${config.name}" 吗？`)) {
    return
  }
  
  try {
    await aiConfigApi.delete(config.id)
    alert('配置已删除')
    loadConfigs()
  } catch (error) {
    alert('删除失败: ' + error.message)
  }
}

const testConfig = async (config) => {
  testingConnection.value = true
  try {
    const result = await aiRecipeApi.testConnection(config)
    if (result.data.data) {
      alert('✅ 连接测试成功！')
    } else {
      alert('❌ 连接测试失败')
    }
  } catch (error) {
    alert('连接测试失败: ' + error.message)
  } finally {
    testingConnection.value = false
  }
}

const testCurrentConfig = async () => {
  const testConfigData = {
    baseUrl: form.baseUrl,
    apiKey: form.apiKey,
    model: form.model
  }
  
  if (!testConfigData.baseUrl || !testConfigData.apiKey || !testConfigData.model) {
    alert('请先填写 API 地址、API Key 和模型名称')
    return
  }
  
  testingConnection.value = true
  try {
    const result = await aiRecipeApi.testConnection(testConfigData)
    if (result.data.data) {
      alert('✅ 连接测试成功！')
    } else {
      alert('❌ 连接测试失败')
    }
  } catch (error) {
    alert('连接测试失败: ' + error.message)
  } finally {
    testingConnection.value = false
  }
}

const handleSubmit = async () => {
  if (!form.name.trim()) {
    alert('请输入配置名称')
    return
  }
  if (!form.baseUrl.trim()) {
    alert('请输入 API 地址')
    return
  }
  if (!form.apiKey.trim()) {
    alert('请输入 API Key')
    return
  }
  if (!form.model.trim()) {
    alert('请输入模型名称')
    return
  }

  submitting.value = true
  try {
    const configData = {
      name: form.name.trim(),
      baseUrl: form.baseUrl.trim(),
      apiKey: form.apiKey.trim(),
      model: form.model.trim(),
      description: form.description?.trim() || '',
      isActive: form.isActive,
      isPreset: false
    }

    if (editingId.value) {
      await aiConfigApi.update(editingId.value, configData)
      alert('配置更新成功！')
    } else {
      await aiConfigApi.create(configData)
      alert('配置创建成功！')
    }

    cancelEdit()
    loadConfigs()
  } catch (error) {
    alert('保存失败: ' + error.message)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadConfigs()
})
</script>

<style scoped>
.ai-config-page {
  padding: 20px 0;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 2rem;
  color: #333;
  margin-bottom: 10px;
}

.subtitle {
  color: #6c757d;
  font-size: 1rem;
}

.loading {
  text-align: center;
  padding: 40px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #ff6b6b;
  border-radius: 50%;
  margin: 0 auto;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  border-bottom: 2px solid #e9ecef;
  padding-bottom: 10px;
}

.tab-btn {
  padding: 10px 20px;
  border: none;
  background: none;
  font-size: 1rem;
  cursor: pointer;
  color: #6c757d;
  border-radius: 8px 8px 0 0;
  transition: all 0.3s;
}

.tab-btn:hover {
  background-color: #f8f9fa;
}

.tab-btn.active {
  color: #ff6b6b;
  background-color: #fff5f5;
  font-weight: bold;
}

.section-info {
  background-color: #e7f3ff;
  border-left: 4px solid #007bff;
  padding: 15px;
  margin-bottom: 20px;
  border-radius: 4px;
  color: #004085;
}

.empty-section {
  text-align: center;
  padding: 60px;
  color: #6c757d;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.config-cards {
  display: grid;
  gap: 20px;
}

.config-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  border: 2px solid transparent;
  transition: all 0.3s;
}

.config-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

.config-card.active {
  border-color: #28a745;
  background-color: #f8fff9;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.card-header h3 {
  margin: 0;
  font-size: 1.2rem;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.active-badge {
  background-color: #28a745;
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: bold;
}

.inactive-badge {
  background-color: #6c757d;
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.85rem;
}

.card-body {
  margin-bottom: 15px;
}

.config-field {
  display: flex;
  margin-bottom: 8px;
  font-size: 0.95rem;
}

.field-label {
  color: #6c757d;
  min-width: 80px;
}

.field-value {
  color: #333;
  word-break: break-all;
}

.field-value.masked {
  font-family: monospace;
}

.card-footer {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.form-container {
  max-width: 700px;
  margin: 0 auto;
}

.form-container h2 {
  margin-bottom: 25px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.form-control {
  width: 100%;
  padding: 12px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.form-control:focus {
  outline: none;
  border-color: #ff6b6b;
}

.form-hint {
  display: block;
  margin-top: 5px;
  font-size: 0.85rem;
  color: #6c757d;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.checkbox-label input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e9ecef;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: linear-gradient(135deg, #ff6b6b, #ffa502);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  opacity: 0.9;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #5a6268;
}

.btn-outline {
  background-color: transparent;
  border: 2px solid #6c757d;
  color: #6c757d;
}

.btn-outline:hover:not(:disabled) {
  background-color: #f8f9fa;
}

.btn-success {
  background-color: #28a745;
  color: white;
}

.btn-success:hover:not(:disabled) {
  background-color: #218838;
}

.btn-danger {
  background-color: #dc3545;
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background-color: #c82333;
}

.btn-sm {
  padding: 6px 14px;
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .tabs {
    flex-wrap: wrap;
  }
  
  .tab-btn {
    flex: 1;
    min-width: 100px;
    text-align: center;
  }
  
  .card-footer {
    flex-direction: column;
  }
  
  .card-footer .btn {
    width: 100%;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .form-actions .btn {
    width: 100%;
  }
}
</style>
