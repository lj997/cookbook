<template>
  <div class="recipe-form">
    <div class="page-header">
      <h1>{{ isEdit ? '✏️ 编辑菜谱' : '➕ 添加菜谱' }}</h1>
      <p class="subtitle">{{ isEdit ? '修改你喜爱的菜谱信息' : '创建一个新的菜谱' }}</p>
    </div>

    <div v-if="loading" class="loading">
      <div class="spinner"></div>
    </div>

    <div v-else class="form-container card">
      <form @submit.prevent="handleSubmit">
        <div class="form-section">
          <h2 class="section-title">📋 基本信息</h2>

          <div class="form-group">
            <label class="form-label">菜谱名称 *</label>
            <input
              v-model="form.name"
              type="text"
              class="form-control"
              placeholder="请输入菜谱名称"
              required
            />
          </div>

          <div class="form-group">
            <label class="form-label">封面图片</label>
            <div class="upload-area">
              <div v-if="form.coverImage" class="image-preview">
                <img :src="form.coverImage" alt="封面预览" />
                <button type="button" class="btn btn-sm btn-danger remove-btn" @click="removeCoverImage">
                  × 移除
                </button>
              </div>
              <div v-else class="upload-placeholder" @click="triggerUpload">
                <span class="upload-icon">📷</span>
                <span class="upload-text">点击上传封面图片</span>
              </div>
              <input
                ref="coverInput"
                type="file"
                accept="image/*"
                style="display: none"
                @change="handleCoverUpload"
              />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="form-label">菜系分类 *</label>
              <select v-model="form.cuisine" class="form-control" required>
                <option value="">请选择菜系</option>
                <option v-for="cuisine in cuisineOptions" :key="cuisine" :value="cuisine">
                  {{ cuisine }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label">难度等级 *</label>
              <select v-model="form.difficulty" class="form-control" required>
                <option value="">请选择难度</option>
                <option value="简单">简单</option>
                <option value="中等">中等</option>
                <option value="困难">困难</option>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label">烹饪时长（分钟）*</label>
              <input
                v-model.number="form.cookingTime"
                type="number"
                class="form-control"
                placeholder="请输入烹饪时长"
                min="1"
                required
              />
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">菜谱描述</label>
            <textarea
              v-model="form.description"
              class="form-control"
              placeholder="简单描述这道菜的特点..."
              rows="3"
            ></textarea>
          </div>
        </div>

        <div class="form-section">
          <div class="section-header">
            <h2 class="section-title">🥗 食材清单</h2>
            <button type="button" class="btn btn-outline btn-sm" @click="addIngredient">
              + 添加食材
            </button>
          </div>

          <div v-if="form.ingredients.length === 0" class="empty-section">
            <p>暂无食材，点击"添加食材"按钮添加</p>
          </div>

          <div v-else class="ingredients-list">
            <div
              v-for="(ingredient, index) in form.ingredients"
              :key="index"
              class="ingredient-row"
            >
              <span class="ingredient-index">{{ index + 1 }}</span>
              <input
                v-model="ingredient.name"
                type="text"
                class="form-control"
                placeholder="食材名称"
              />
              <input
                v-model="ingredient.quantity"
                type="text"
                class="form-control quantity-input"
                placeholder="用量"
              />
              <button
                type="button"
                class="btn btn-danger btn-sm"
                @click="removeIngredient(index)"
              >
                删除
              </button>
            </div>
          </div>
        </div>

        <div class="form-section">
          <div class="section-header">
            <h2 class="section-title">👨‍🍳 烹饪步骤</h2>
            <button type="button" class="btn btn-outline btn-sm" @click="addStep">
              + 添加步骤
            </button>
          </div>

          <div v-if="form.steps.length === 0" class="empty-section">
            <p>暂无烹饪步骤，点击"添加步骤"按钮添加</p>
          </div>

          <div v-else class="steps-list">
            <div
              v-for="(step, index) in form.steps"
              :key="index"
              class="step-row"
            >
              <div class="step-header">
                <span class="step-number">步骤 {{ index + 1 }}</span>
                <button
                  type="button"
                  class="btn btn-danger btn-sm"
                  @click="removeStep(index)"
                >
                  删除步骤
                </button>
              </div>

              <div class="form-group">
                <label class="form-label">步骤描述</label>
                <textarea
                  v-model="step.description"
                  class="form-control"
                  placeholder="详细描述这一步的操作..."
                  rows="3"
                ></textarea>
              </div>

              <div class="form-group">
                <label class="form-label">步骤图片（可选）</label>
                <div class="step-upload">
                  <div v-if="step.image" class="step-image-preview">
                    <img :src="step.image" alt="步骤图片" />
                    <button
                      type="button"
                      class="btn btn-sm btn-danger remove-btn"
                      @click="removeStepImage(index)"
                    >
                      × 移除
                    </button>
                  </div>
                  <div v-else class="step-upload-placeholder" @click="triggerStepUpload(index)">
                    <span>📷 点击上传步骤图片</span>
                  </div>
                  <input
                    ref="stepFileInputs"
                    type="file"
                    accept="image/*"
                    style="display: none"
                    @change="handleStepUpload(index, $event)"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="form-actions">
          <router-link to="/" class="btn btn-secondary">
            取消
          </router-link>
          <button type="submit" class="btn btn-primary" :disabled="submitting">
            {{ submitting ? '提交中...' : (isEdit ? '保存修改' : '创建菜谱') }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { recipeApi, fileApi } from '../api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const coverInput = ref(null)

const isEdit = computed(() => !!route.params.id)

const cuisineOptions = [
  '川菜', '粤菜', '湘菜', '鲁菜', '苏菜', '浙菜', '闽菜', '徽菜',
  '家常菜', '甜品', '烘焙', '汤品', '主食', '凉菜', '西餐', '日料',
  '韩餐', '泰餐', '其他'
]

const defaultForm = {
  name: '',
  coverImage: '',
  cuisine: '',
  difficulty: '',
  cookingTime: null,
  description: '',
  ingredients: [],
  steps: []
}

const form = reactive({ ...defaultForm })

const addIngredient = () => {
  form.ingredients.push({ name: '', quantity: '' })
}

const removeIngredient = (index) => {
  form.ingredients.splice(index, 1)
}

const addStep = () => {
  const stepNumber = form.steps.length + 1
  form.steps.push({ stepNumber, description: '', image: '' })
}

const removeStep = (index) => {
  form.steps.splice(index, 1)
  form.steps.forEach((step, i) => {
    step.stepNumber = i + 1
  })
}

const triggerUpload = () => {
  coverInput.value?.click()
}

const handleCoverUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  try {
    const result = await fileApi.upload(file)
    form.coverImage = result.data.url
  } catch (error) {
    alert('上传失败: ' + error.message)
  }

  event.target.value = ''
}

const removeCoverImage = () => {
  form.coverImage = ''
}

const triggerStepUpload = (index) => {
  const inputs = document.querySelectorAll('input[type="file"]')
  if (inputs.length > index + 1) {
    inputs[index + 1]?.click()
  }
}

const handleStepUpload = async (index, event) => {
  const file = event.target.files[0]
  if (!file) return

  try {
    const result = await fileApi.upload(file)
    form.steps[index].image = result.data.url
  } catch (error) {
    alert('上传失败: ' + error.message)
  }

  event.target.value = ''
}

const removeStepImage = (index) => {
  form.steps[index].image = ''
}

const fetchRecipe = async () => {
  if (!isEdit.value) return

  loading.value = true
  try {
    const result = await recipeApi.getById(route.params.id)
    const data = result.data
    
    form.name = data.name || ''
    form.coverImage = data.coverImage || ''
    form.cuisine = data.cuisine || ''
    form.difficulty = data.difficulty || ''
    form.cookingTime = data.cookingTime || null
    form.description = data.description || ''
    form.ingredients = data.ingredients?.map(i => ({ name: i.name, quantity: i.quantity })) || []
    form.steps = data.steps?.map(s => ({
      stepNumber: s.stepNumber,
      description: s.description,
      image: s.image || ''
    })) || []
  } catch (error) {
    alert('获取菜谱信息失败: ' + error.message)
    router.push('/')
  } finally {
    loading.value = false
  }
}

const validateForm = () => {
  if (!form.name.trim()) {
    alert('请输入菜谱名称')
    return false
  }
  if (!form.cuisine) {
    alert('请选择菜系分类')
    return false
  }
  if (!form.difficulty) {
    alert('请选择难度等级')
    return false
  }
  if (!form.cookingTime || form.cookingTime < 1) {
    alert('请输入有效的烹饪时长')
    return false
  }
  return true
}

const handleSubmit = async () => {
  if (!validateForm()) return

  submitting.value = true
  try {
    const recipeData = {
      name: form.name.trim(),
      coverImage: form.coverImage,
      cuisine: form.cuisine,
      difficulty: form.difficulty,
      cookingTime: form.cookingTime,
      description: form.description,
      ingredients: form.ingredients.filter(i => i.name.trim()),
      steps: form.steps.filter(s => s.description.trim())
    }

    if (isEdit.value) {
      await recipeApi.update(route.params.id, recipeData)
      alert('菜谱修改成功！')
    } else {
      await recipeApi.create(recipeData)
      alert('菜谱创建成功！')
    }
    
    router.push('/')
  } catch (error) {
    alert('操作失败: ' + error.message)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchRecipe()
})
</script>

<style scoped>
.recipe-form {
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

.form-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px;
}

.form-section {
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 1px dashed #e9ecef;
}

.form-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 1.3rem;
  color: #333;
  margin: 0;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.upload-area {
  width: 100%;
}

.upload-placeholder {
  width: 100%;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f8f9fa;
  border: 2px dashed #dee2e6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-placeholder:hover {
  border-color: #ff6b6b;
  background-color: #fff5f5;
}

.upload-icon {
  font-size: 3rem;
  margin-bottom: 10px;
}

.upload-text {
  color: #6c757d;
}

.image-preview {
  position: relative;
  width: 100%;
  max-width: 400px;
}

.image-preview img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
}

.remove-btn {
  position: absolute;
  top: 10px;
  right: 10px;
}

.ingredients-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ingredient-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.ingredient-index {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff6b6b, #ffa502);
  color: white;
  border-radius: 50%;
  font-weight: bold;
  font-size: 0.9rem;
}

.quantity-input {
  max-width: 150px;
}

.steps-list {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.step-row {
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #ff6b6b;
}

.step-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.step-number {
  font-weight: bold;
  font-size: 1.1rem;
  color: #ff6b6b;
}

.step-upload {
  margin-top: 10px;
}

.step-upload-placeholder {
  padding: 20px;
  background-color: #e9ecef;
  border: 2px dashed #dee2e6;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  color: #6c757d;
  transition: all 0.3s;
}

.step-upload-placeholder:hover {
  border-color: #ff6b6b;
  background-color: #fff5f5;
}

.step-image-preview {
  position: relative;
  max-width: 300px;
}

.step-image-preview img {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 8px;
}

.empty-section {
  text-align: center;
  padding: 30px;
  color: #6c757d;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e9ecef;
}

@media (max-width: 768px) {
  .form-container {
    padding: 20px;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .ingredient-row {
    flex-wrap: wrap;
  }

  .ingredient-row .form-control {
    flex: 1 1 100%;
  }

  .quantity-input {
    max-width: none;
  }

  .form-actions {
    flex-direction: column;
  }

  .form-actions .btn {
    width: 100%;
  }
}
</style>
