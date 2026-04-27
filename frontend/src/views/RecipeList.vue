<template>
  <div class="recipe-list">
    <div class="page-header">
      <h1>🍳 我的菜谱</h1>
      <p class="subtitle">收藏和管理你喜爱的美食菜谱</p>
    </div>

    <div class="search-section">
      <div class="search-bar">
        <input
          v-model="searchParams.keyword"
          type="text"
          class="form-control"
          placeholder="搜索菜谱名称或食材..."
          @keyup.enter="searchRecipes"
        />
        <button class="btn btn-primary" @click="searchRecipes">
          🔍 搜索
        </button>
      </div>

      <div class="filter-section">
        <div class="filter-group">
          <label class="form-label">菜系</label>
          <select v-model="searchParams.cuisine" class="form-control" @change="searchRecipes">
            <option value="">全部菜系</option>
            <option v-for="cuisine in cuisines" :key="cuisine" :value="cuisine">
              {{ cuisine }}
            </option>
          </select>
        </div>

        <div class="filter-group">
          <label class="form-label">难度</label>
          <select v-model="searchParams.difficulty" class="form-control" @change="searchRecipes">
            <option value="">全部难度</option>
            <option v-for="difficulty in difficulties" :key="difficulty" :value="difficulty">
              {{ difficulty }}
            </option>
          </select>
        </div>

        <div class="filter-group">
          <label class="form-label">烹饪时间</label>
          <select v-model="timeFilter" class="form-control" @change="handleTimeFilter">
            <option value="">全部时间</option>
            <option value="15">15分钟以内</option>
            <option value="30">30分钟以内</option>
            <option value="60">1小时以内</option>
            <option value="more">1小时以上</option>
          </select>
        </div>

        <div class="filter-group">
          <button class="btn btn-outline" @click="resetFilters">
            重置筛选
          </button>
        </div>
      </div>
    </div>

    <div v-if="loading" class="loading">
      <div class="spinner"></div>
    </div>

    <div v-else-if="recipes.length === 0" class="empty-state">
      <div class="empty-state-icon">📝</div>
      <h3 class="empty-state-title">暂无菜谱</h3>
      <p class="empty-state-text">点击"添加菜谱"开始创建你的第一个菜谱吧！</p>
      <router-link to="/create" class="btn btn-primary">
        + 添加菜谱
      </router-link>
    </div>

    <div v-else class="recipes-grid">
      <div v-for="recipe in recipes" :key="recipe.id" class="recipe-card card">
        <router-link :to="`/recipe/${recipe.id}`" class="card-link">
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
            <h3 class="recipe-name">{{ recipe.name }}</h3>
            <div class="recipe-tags">
              <span class="badge badge-info">{{ recipe.cuisine }}</span>
              <span class="badge" :class="getDifficultyClass(recipe.difficulty)">
                {{ recipe.difficulty }}
              </span>
              <span class="badge badge-primary">⏱️ {{ recipe.cookingTime }}分钟</span>
            </div>
            <p v-if="recipe.description" class="recipe-desc">
              {{ truncateText(recipe.description, 60) }}
            </p>
          </div>
        </router-link>
        <div class="recipe-actions">
          <button class="btn btn-sm btn-outline" @click.stop="addToShopping(recipe)">
            🛒 加入购物清单
          </button>
        </div>
      </div>
    </div>

    <div v-if="totalPages > 1" class="pagination">
      <button
        class="btn btn-sm"
        :disabled="currentPage === 0"
        @click="changePage(currentPage - 1)"
      >
        上一页
      </button>
      <span class="page-info">
        第 {{ currentPage + 1 }} / {{ totalPages }} 页
      </span>
      <button
        class="btn btn-sm"
        :disabled="currentPage >= totalPages - 1"
        @click="changePage(currentPage + 1)"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { recipeApi, shoppingApi } from '../api'

const router = useRouter()

const loading = ref(false)
const recipes = ref([])
const cuisines = ref([])
const difficulties = ref([])

const searchParams = reactive({
  keyword: '',
  cuisine: '',
  difficulty: '',
  minTime: null,
  maxTime: null
})

const timeFilter = ref('')

const currentPage = ref(0)
const totalPages = ref(1)
const pageSize = 12

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

const truncateText = (text, maxLength) => {
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

const handleTimeFilter = () => {
  switch (timeFilter.value) {
    case '15':
      searchParams.maxTime = 15
      searchParams.minTime = null
      break
    case '30':
      searchParams.maxTime = 30
      searchParams.minTime = null
      break
    case '60':
      searchParams.maxTime = 60
      searchParams.minTime = null
      break
    case 'more':
      searchParams.minTime = 60
      searchParams.maxTime = null
      break
    default:
      searchParams.minTime = null
      searchParams.maxTime = null
  }
  searchRecipes()
}

const fetchRecipes = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize,
      ...searchParams
    }
    
    const result = await recipeApi.getAll(params)
    recipes.value = result.data.content
    totalPages.value = result.data.totalPages
  } catch (error) {
    console.error('获取菜谱列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchFilters = async () => {
  try {
    const [cuisinesResult, difficultiesResult] = await Promise.all([
      recipeApi.getCuisines(),
      recipeApi.getDifficulties()
    ])
    cuisines.value = cuisinesResult.data || []
    difficulties.value = difficultiesResult.data || []
  } catch (error) {
    console.error('获取筛选条件失败:', error)
  }
}

const searchRecipes = () => {
  currentPage.value = 0
  fetchRecipes()
}

const resetFilters = () => {
  searchParams.keyword = ''
  searchParams.cuisine = ''
  searchParams.difficulty = ''
  searchParams.minTime = null
  searchParams.maxTime = null
  timeFilter.value = ''
  currentPage.value = 0
  fetchRecipes()
}

const changePage = (page) => {
  currentPage.value = page
  fetchRecipes()
}

const addToShopping = async (recipe) => {
  try {
    const result = await shoppingApi.addFromRecipe(recipe.id)
    alert(result.message || `已将"${recipe.name}"的食材添加到购物清单！`)
  } catch (error) {
    alert(error.message || '添加失败')
  }
}

onMounted(() => {
  fetchRecipes()
  fetchFilters()
})
</script>

<style scoped>
.recipe-list {
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

.search-section {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-bar .form-control {
  flex: 1;
}

.filter-section {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: flex-end;
}

.filter-group {
  min-width: 150px;
}

.recipes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.recipe-card {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.card-link {
  text-decoration: none;
  color: inherit;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.recipe-cover {
  width: 100%;
  height: 180px;
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
  font-size: 3rem;
}

.recipe-info {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.recipe-name {
  font-size: 1.2rem;
  margin-bottom: 10px;
  color: #333;
}

.recipe-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}

.recipe-desc {
  color: #6c757d;
  font-size: 0.9rem;
  flex: 1;
}

.recipe-actions {
  padding: 12px 16px 16px;
  border-top: 1px solid #f0f0f0;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 30px;
  padding: 20px;
}

.page-info {
  color: #6c757d;
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .search-bar {
    flex-direction: column;
  }

  .filter-section {
    flex-direction: column;
    gap: 15px;
  }

  .filter-group {
    width: 100%;
  }

  .recipes-grid {
    grid-template-columns: 1fr;
  }
}
</style>
