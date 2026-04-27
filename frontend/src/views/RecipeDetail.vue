<template>
  <div class="recipe-detail">
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
    </div>

    <div v-else-if="error" class="error-message">
      <div class="alert alert-danger">
        {{ error }}
      </div>
      <router-link to="/" class="btn btn-primary">
        ← 返回菜谱列表
      </router-link>
    </div>

    <div v-else class="detail-container">
      <div class="detail-header">
        <div class="back-btn">
          <router-link to="/" class="btn btn-outline">
            ← 返回列表
          </router-link>
        </div>
        <div class="action-buttons">
          <button class="btn btn-primary" @click="addToShopping">
            🛒 加入购物清单
          </button>
          <button class="btn btn-outline" @click="exportPdf">
            📄 导出PDF
          </button>
          <router-link :to="`/edit/${recipe.id}`" class="btn btn-secondary">
            ✏️ 编辑
          </router-link>
          <button class="btn btn-danger" @click="deleteRecipe">
            🗑️ 删除
          </button>
        </div>
      </div>

      <div class="recipe-main">
        <div class="recipe-cover">
          <img
            v-if="recipe.coverImage"
            :src="recipe.coverImage"
            :alt="recipe.name"
          />
          <div v-else class="no-cover">
            <span class="no-cover-icon">🍽️</span>
          </div>
        </div>

        <div class="recipe-info">
          <h1 class="recipe-title">{{ recipe.name }}</h1>
          
          <p v-if="recipe.description" class="recipe-description">
            {{ recipe.description }}
          </p>

          <div class="recipe-meta">
            <div class="meta-item">
              <span class="meta-label">菜系</span>
              <span class="badge badge-info">{{ recipe.cuisine }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">难度</span>
              <span class="badge" :class="getDifficultyClass(recipe.difficulty)">
                {{ recipe.difficulty }}
              </span>
            </div>
            <div class="meta-item">
              <span class="meta-label">烹饪时间</span>
              <span class="badge badge-primary">⏱️ {{ recipe.cookingTime }}分钟</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">食材数量</span>
              <span class="badge badge-success">
                {{ recipe.ingredients?.length || 0 }} 种
              </span>
            </div>
            <div class="meta-item">
              <span class="meta-label">步骤数量</span>
              <span class="badge badge-warning">
                {{ recipe.steps?.length || 0 }} 步
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="recipe-sections">
        <section class="ingredients-section">
          <h2 class="section-title">🥗 食材清单</h2>
          <div v-if="recipe.ingredients && recipe.ingredients.length > 0" class="ingredients-list">
            <div
              v-for="(ingredient, index) in recipe.ingredients"
              :key="index"
              class="ingredient-item"
            >
              <span class="ingredient-name">{{ ingredient.name }}</span>
              <span class="ingredient-quantity">{{ ingredient.quantity }}</span>
            </div>
          </div>
          <div v-else class="empty-section">
            <p>暂无食材信息</p>
          </div>
        </section>

        <section class="steps-section">
          <h2 class="section-title">👨‍🍳 烹饪步骤</h2>
          <div v-if="recipe.steps && recipe.steps.length > 0" class="steps-list">
            <div
              v-for="(step, index) in recipe.steps"
              :key="index"
              class="step-item"
            >
              <div class="step-number">
                <span>{{ step.stepNumber }}</span>
              </div>
              <div class="step-content">
                <div v-if="step.image" class="step-image">
                  <img :src="step.image" :alt="`步骤${step.stepNumber}`" />
                </div>
                <p class="step-description">{{ step.description }}</p>
              </div>
            </div>
          </div>
          <div v-else class="empty-section">
            <p>暂无烹饪步骤</p>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { recipeApi, shoppingApi, exportApi } from '../api'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const error = ref('')
const recipe = ref({
  id: null,
  name: '',
  coverImage: '',
  cuisine: '',
  difficulty: '',
  cookingTime: 0,
  description: '',
  ingredients: [],
  steps: []
})

const getDifficultyClass = (difficulty) => {
  switch (difficulty) {
    case '简单':
      return 'badge-success'
    case '中等':
      return 'badge-warning'
    case '困难':
      return 'badge-danger'
    default:
      return 'badge-info'
  }
}

const fetchRecipe = async () => {
  const id = route.params.id
  if (!id) {
    error.value = '菜谱ID不能为空'
    loading.value = false
    return
  }

  loading.value = true
  error.value = ''

  try {
    const result = await recipeApi.getById(id)
    recipe.value = result.data
  } catch (err) {
    error.value = err.message || '获取菜谱信息失败'
  } finally {
    loading.value = false
  }
}

const addToShopping = async () => {
  try {
    const result = await shoppingApi.addFromRecipe(recipe.value.id)
    alert(result.message || `已将"${recipe.value.name}"的食材添加到购物清单！`)
  } catch (err) {
    alert(err.message || '添加失败')
  }
}

const exportPdf = async () => {
  try {
    await exportApi.downloadPdf(recipe.value.id, `${recipe.value.name}.pdf`)
    alert('PDF导出成功！')
  } catch (err) {
    alert(err.message || '导出PDF失败')
  }
}

const deleteRecipe = async () => {
  if (!confirm(`确定要删除菜谱"${recipe.value.name}"吗？此操作不可恢复。`)) {
    return
  }

  try {
    await recipeApi.delete(recipe.value.id)
    alert('菜谱已删除')
    router.push('/')
  } catch (err) {
    alert(err.message || '删除失败')
  }
}

onMounted(() => {
  fetchRecipe()
})
</script>

<style scoped>
.recipe-detail {
  padding: 20px 0;
}

.detail-container {
  max-width: 900px;
  margin: 0 auto;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.recipe-main {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 30px;
  margin-bottom: 40px;
}

.recipe-cover {
  width: 100%;
  height: 300px;
  border-radius: 12px;
  overflow: hidden;
  background-color: #f8f9fa;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.recipe-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-cover {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ffeaa7, #fdcb6e);
}

.no-cover-icon {
  font-size: 5rem;
}

.recipe-info {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.recipe-title {
  font-size: 2rem;
  color: #333;
  margin: 0;
}

.recipe-description {
  color: #6c757d;
  font-size: 1.1rem;
  line-height: 1.8;
  margin: 0;
}

.recipe-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 12px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 120px;
}

.meta-label {
  font-size: 0.85rem;
  color: #6c757d;
  font-weight: 500;
}

.recipe-sections {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.section-title {
  font-size: 1.5rem;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #ff6b6b;
}

.ingredients-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
}

.ingredient-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border-left: 4px solid #ff6b6b;
}

.ingredient-name {
  font-weight: 500;
  color: #333;
}

.ingredient-quantity {
  color: #ff6b6b;
  font-weight: 600;
}

.steps-list {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.step-item {
  display: flex;
  gap: 20px;
  padding: 20px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.step-number {
  width: 50px;
  height: 50px;
  min-width: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff6b6b, #ffa502);
  color: white;
  font-size: 1.2rem;
  font-weight: bold;
  border-radius: 50%;
  box-shadow: 0 4px 10px rgba(255, 107, 107, 0.3);
}

.step-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.step-image {
  width: 100%;
  max-width: 400px;
  border-radius: 8px;
  overflow: hidden;
}

.step-image img {
  width: 100%;
  height: auto;
}

.step-description {
  color: #333;
  font-size: 1rem;
  line-height: 1.8;
  margin: 0;
}

.empty-section {
  text-align: center;
  padding: 40px;
  color: #6c757d;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.error-message {
  text-align: center;
  padding: 40px;
}

@media (max-width: 900px) {
  .recipe-main {
    grid-template-columns: 1fr;
  }

  .recipe-cover {
    height: 250px;
  }

  .step-item {
    flex-direction: column;
  }

  .step-number {
    width: 40px;
    height: 40px;
    min-width: 40px;
    font-size: 1rem;
  }

  .detail-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .action-buttons {
    width: 100%;
  }
}
</style>
