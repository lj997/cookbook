<template>
  <div class="random-recipe">
    <div class="page-header">
      <h1 class="title">🎲 今天吃什么？</h1>
      <p class="subtitle">让我来帮你做决定吧！</p>
    </div>

    <div class="action-area">
      <button
        class="btn btn-primary random-btn"
        @click="getRandomRecipe"
        :disabled="loading"
      >
        {{ loading ? '抽取中...' : (recipe ? '再抽一次' : '开始抽取') }}
      </button>
      <p class="hint-text">点击按钮随机推荐一道菜</p>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p class="loading-text">正在为你挑选美食...</p>
    </div>

    <div v-else-if="error" class="error-container">
      <div class="empty-state">
        <div class="empty-state-icon">🍽️</div>
        <h3 class="empty-state-title">{{ error }}</h3>
        <p class="empty-state-text">
          快去添加一些菜谱吧！
        </p>
        <router-link to="/create" class="btn btn-primary">
          + 添加菜谱
        </router-link>
      </div>
    </div>

    <div v-else-if="recipe" class="result-container" :class="{ show: showResult }">
      <div class="recipe-card card">
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
          <h2 class="recipe-name">{{ recipe.name }}</h2>
          
          <p v-if="recipe.description" class="recipe-desc">
            {{ recipe.description }}
          </p>

          <div class="recipe-tags">
            <span class="badge badge-info">{{ recipe.cuisine }}</span>
            <span class="badge" :class="getDifficultyClass(recipe.difficulty)">
              {{ recipe.difficulty }}
            </span>
            <span class="badge badge-primary">⏱️ {{ recipe.cookingTime }}分钟</span>
          </div>

          <div class="ingredients-preview" v-if="recipe.ingredients && recipe.ingredients.length > 0">
            <h4 class="preview-title">🥗 食材清单（{{ recipe.ingredients.length }}种）</h4>
            <div class="ingredients-list">
              <span
                v-for="(ingredient, index) in displayIngredients"
                :key="index"
                class="ingredient-tag"
              >
                {{ ingredient.name }}
              </span>
              <span v-if="recipe.ingredients.length > 5" class="more-tag">
                还有 {{ recipe.ingredients.length - 5 }} 种...
              </span>
            </div>
          </div>
        </div>

        <div class="recipe-actions">
          <button class="btn btn-primary" @click="viewDetail">
            👁️ 查看详情
          </button>
          <button class="btn btn-outline" @click="addToShopping">
            🛒 加入购物清单
          </button>
          <button class="btn btn-secondary" @click="exportPdf">
            📄 导出PDF
          </button>
        </div>
      </div>

      <div class="suggestion-box">
        <p class="suggestion-text">
          这道菜怎么样？
          <span class="emoji" @click="getRandomRecipe" title="不满意，换一个">👎</span>
        </p>
      </div>
    </div>

    <div v-else class="empty-container">
      <div class="empty-state">
        <div class="empty-state-icon">🎲</div>
        <h3 class="empty-state-title">准备好开始了吗？</h3>
        <p class="empty-state-text">
          点击上方按钮，让我来帮你决定今天吃什么！
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { recipeApi, shoppingApi, exportApi } from '../api'

const router = useRouter()

const loading = ref(false)
const error = ref('')
const recipe = ref(null)
const showResult = ref(false)

const displayIngredients = computed(() => {
  if (!recipe.value?.ingredients) return []
  return recipe.value.ingredients.slice(0, 5)
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

const getRandomRecipe = async () => {
  loading.value = true
  error.value = ''
  showResult.value = false

  try {
    const result = await recipeApi.getRandom()
    recipe.value = result.data
    showResult.value = true
  } catch (err) {
    error.value = err.message || '暂无菜谱，无法推荐'
    recipe.value = null
  } finally {
    loading.value = false
  }
}

const viewDetail = () => {
  if (recipe.value?.id) {
    router.push(`/recipe/${recipe.value.id}`)
  }
}

const addToShopping = async () => {
  if (!recipe.value?.id) return

  try {
    const result = await shoppingApi.addFromRecipe(recipe.value.id)
    alert(result.message || `已将"${recipe.value.name}"的食材添加到购物清单！`)
  } catch (err) {
    alert(err.message || '添加失败')
  }
}

const exportPdf = async () => {
  if (!recipe.value?.id) return

  try {
    await exportApi.downloadPdf(recipe.value.id, `${recipe.value.name}.pdf`)
    alert('PDF导出成功！')
  } catch (err) {
    alert(err.message || '导出PDF失败')
  }
}
</script>

<style scoped>
.random-recipe {
  padding: 20px 0;
  min-height: 80vh;
  display: flex;
  flex-direction: column;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.title {
  font-size: 2.5rem;
  color: #333;
  margin-bottom: 15px;
  background: linear-gradient(135deg, #ff6b6b, #ffa502);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  color: #6c757d;
  font-size: 1.1rem;
}

.action-area {
  text-align: center;
  margin-bottom: 40px;
}

.random-btn {
  font-size: 1.3rem;
  padding: 15px 50px;
  border-radius: 50px;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.4);
  animation: pulse 2s infinite;
}

.random-btn:disabled {
  animation: none;
  opacity: 0.7;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 4px 15px rgba(255, 107, 107, 0.4);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 6px 25px rgba(255, 107, 107, 0.5);
  }
}

.hint-text {
  margin-top: 15px;
  color: #6c757d;
  font-size: 0.9rem;
}

.loading-container {
  text-align: center;
  padding: 60px;
}

.loading-text {
  margin-top: 20px;
  color: #6c757d;
  font-size: 1.1rem;
}

.empty-container,
.error-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.result-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.5s ease;
}

.result-container.show {
  opacity: 1;
  transform: translateY(0);
}

.recipe-card {
  max-width: 600px;
  width: 100%;
  overflow: hidden;
}

.recipe-cover {
  width: 100%;
  height: 300px;
  overflow: hidden;
  background-color: #f8f9fa;
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
  padding: 25px;
}

.recipe-name {
  font-size: 1.8rem;
  margin-bottom: 15px;
  color: #333;
}

.recipe-desc {
  color: #6c757d;
  margin-bottom: 20px;
  line-height: 1.6;
}

.recipe-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 25px;
}

.ingredients-preview {
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.preview-title {
  font-size: 1rem;
  color: #333;
  margin-bottom: 15px;
}

.ingredients-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.ingredient-tag,
.more-tag {
  padding: 6px 12px;
  background-color: #fff5f5;
  color: #ff6b6b;
  border-radius: 20px;
  font-size: 0.85rem;
}

.more-tag {
  background-color: #f8f9fa;
  color: #6c757d;
}

.recipe-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  padding: 20px 25px 25px;
  border-top: 1px solid #f0f0f0;
  flex-wrap: wrap;
}

.suggestion-box {
  text-align: center;
  margin-top: 30px;
  padding: 20px;
}

.suggestion-text {
  color: #6c757d;
  font-size: 1rem;
}

.emoji {
  font-size: 1.5rem;
  cursor: pointer;
  margin-left: 10px;
  transition: transform 0.2s;
  display: inline-block;
}

.emoji:hover {
  transform: scale(1.2) rotate(10deg);
}

@media (max-width: 768px) {
  .title {
    font-size: 1.8rem;
  }

  .random-btn {
    font-size: 1.1rem;
    padding: 12px 35px;
  }

  .recipe-cover {
    height: 200px;
  }

  .recipe-name {
    font-size: 1.4rem;
  }

  .recipe-actions {
    flex-direction: column;
  }

  .recipe-actions .btn {
    width: 100%;
  }
}
</style>
