<template>
  <div class="shopping-list">
    <div class="page-header">
      <h1>🛒 购物清单</h1>
      <p class="subtitle">管理你的购物清单，轻松采购</p>
    </div>

    <div class="stats-section">
      <div class="stat-card">
        <span class="stat-number">{{ stats.unpurchased }}</span>
        <span class="stat-label">待购买</span>
      </div>
      <div class="stat-card">
        <span class="stat-number">{{ stats.purchased }}</span>
        <span class="stat-label">已购买</span>
      </div>
      <div class="stat-card total">
        <span class="stat-number">{{ stats.unpurchased + stats.purchased }}</span>
        <span class="stat-label">总计</span>
      </div>
    </div>

    <div class="add-section card">
      <h3>➕ 手动添加食材</h3>
      <div class="add-form">
        <input
          v-model="newItem.name"
          type="text"
          class="form-control"
          placeholder="食材名称"
        />
        <input
          v-model="newItem.quantity"
          type="text"
          class="form-control quantity-input"
          placeholder="用量（可选）"
        />
        <button class="btn btn-primary" @click="addItem" :disabled="!newItem.name.trim()">
          添加
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading">
      <div class="spinner"></div>
    </div>

    <div v-else-if="items.length === 0" class="empty-state">
      <div class="empty-state-icon">🛒</div>
      <h3 class="empty-state-title">购物清单是空的</h3>
      <p class="empty-state-text">
        你可以在菜谱详情页点击"加入购物清单"，或者手动添加食材。
      </p>
      <router-link to="/" class="btn btn-primary">
        浏览菜谱
      </router-link>
    </div>

    <div v-else class="shopping-sections">
      <section v-if="unpurchasedItems.length > 0" class="items-section">
        <div class="section-header">
          <h2 class="section-title">📋 待购买 ({{ unpurchasedItems.length }})</h2>
        </div>

        <div class="items-list">
          <div
            v-for="item in unpurchasedItems"
            :key="item.id"
            class="item-card card"
          >
            <div class="item-checkbox">
              <input
                type="checkbox"
                :id="`item-${item.id}`"
                @change="togglePurchased(item)"
              />
              <label :for="`item-${item.id}`"></label>
            </div>

            <div class="item-info" v-if="!editingItemId || editingItemId !== item.id">
              <span class="item-name">{{ item.name }}</span>
              <span v-if="item.quantity" class="item-quantity">{{ item.quantity }}</span>
            </div>

            <div class="item-edit-form" v-else>
              <input
                v-model="editForm.name"
                type="text"
                class="form-control"
                placeholder="食材名称"
              />
              <input
                v-model="editForm.quantity"
                type="text"
                class="form-control quantity-input"
                placeholder="用量"
              />
            </div>

            <div class="item-actions">
              <template v-if="editingItemId === item.id">
                <button class="btn btn-sm btn-primary" @click="saveEdit(item)">
                  保存
                </button>
                <button class="btn btn-sm btn-secondary" @click="cancelEdit">
                  取消
                </button>
              </template>
              <template v-else>
                <button class="btn btn-sm btn-outline" @click="startEdit(item)">
                  ✏️
                </button>
                <button class="btn btn-sm btn-danger" @click="deleteItem(item)">
                  🗑️
                </button>
              </template>
            </div>
          </div>
        </div>
      </section>

      <section v-if="purchasedItems.length > 0" class="items-section purchased-section">
        <div class="section-header">
          <h2 class="section-title">✅ 已购买 ({{ purchasedItems.length }})</h2>
          <button class="btn btn-sm btn-outline" @click="clearPurchased">
            清空已购买
          </button>
        </div>

        <div class="items-list">
          <div
            v-for="item in purchasedItems"
            :key="item.id"
            class="item-card card purchased"
          >
            <div class="item-checkbox">
              <input
                type="checkbox"
                :id="`purchased-item-${item.id}`"
                checked
                @change="togglePurchased(item)"
              />
              <label :for="`purchased-item-${item.id}`"></label>
            </div>

            <div class="item-info">
              <span class="item-name">{{ item.name }}</span>
              <span v-if="item.quantity" class="item-quantity">{{ item.quantity }}</span>
            </div>

            <div class="item-actions">
              <button class="btn btn-sm btn-danger" @click="deleteItem(item)">
                🗑️
              </button>
            </div>
          </div>
        </div>
      </section>
    </div>

    <div v-if="items.length > 0" class="action-bar">
      <button class="btn btn-danger" @click="clearAll">
        🗑️ 清空全部
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { shoppingApi } from '../api'

const loading = ref(false)
const items = ref([])
const editingItemId = ref(null)
const stats = reactive({
  unpurchased: 0,
  purchased: 0
})

const newItem = reactive({
  name: '',
  quantity: ''
})

const editForm = reactive({
  name: '',
  quantity: ''
})

const unpurchasedItems = computed(() => {
  return items.value.filter(item => !item.purchased)
})

const purchasedItems = computed(() => {
  return items.value.filter(item => item.purchased)
})

const fetchItems = async () => {
  loading.value = true
  try {
    const result = await shoppingApi.getAll()
    items.value = result.data || []
    updateStats()
  } catch (error) {
    console.error('获取购物清单失败:', error)
  } finally {
    loading.value = false
  }
}

const updateStats = () => {
  stats.unpurchased = items.value.filter(item => !item.purchased).length
  stats.purchased = items.value.filter(item => item.purchased).length
}

const addItem = async () => {
  if (!newItem.name.trim()) return

  try {
    await shoppingApi.create({
      name: newItem.name.trim(),
      quantity: newItem.quantity.trim() || null,
      purchased: false
    })
    
    newItem.name = ''
    newItem.quantity = ''
    fetchItems()
  } catch (error) {
    alert('添加失败: ' + error.message)
  }
}

const togglePurchased = async (item) => {
  try {
    await shoppingApi.togglePurchased(item.id)
    fetchItems()
  } catch (error) {
    alert('操作失败: ' + error.message)
  }
}

const startEdit = (item) => {
  editingItemId.value = item.id
  editForm.name = item.name
  editForm.quantity = item.quantity || ''
}

const cancelEdit = () => {
  editingItemId.value = null
  editForm.name = ''
  editForm.quantity = ''
}

const saveEdit = async (item) => {
  if (!editForm.name.trim()) {
    alert('食材名称不能为空')
    return
  }

  try {
    await shoppingApi.update(item.id, {
      name: editForm.name.trim(),
      quantity: editForm.quantity.trim() || null
    })
    cancelEdit()
    fetchItems()
  } catch (error) {
    alert('保存失败: ' + error.message)
  }
}

const deleteItem = async (item) => {
  if (!confirm(`确定要删除"${item.name}"吗？`)) return

  try {
    await shoppingApi.delete(item.id)
    fetchItems()
  } catch (error) {
    alert('删除失败: ' + error.message)
  }
}

const clearPurchased = async () => {
  if (!confirm('确定要清空所有已购买的商品吗？')) return

  try {
    await shoppingApi.deletePurchased()
    fetchItems()
  } catch (error) {
    alert('操作失败: ' + error.message)
  }
}

const clearAll = async () => {
  if (!confirm('确定要清空整个购物清单吗？此操作不可恢复！')) return

  try {
    await shoppingApi.clearAll()
    fetchItems()
  } catch (error) {
    alert('操作失败: ' + error.message)
  }
}

onMounted(() => {
  fetchItems()
})
</script>

<style scoped>
.shopping-list {
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

.stats-section {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-bottom: 30px;
}

.stat-card {
  text-align: center;
  padding: 20px 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  min-width: 120px;
}

.stat-card.total {
  background: linear-gradient(135deg, #ff6b6b, #ffa502);
  color: white;
}

.stat-number {
  display: block;
  font-size: 2rem;
  font-weight: bold;
}

.stat-label {
  display: block;
  font-size: 0.9rem;
  margin-top: 5px;
}

.add-section {
  padding: 20px;
  margin-bottom: 30px;
}

.add-section h3 {
  margin-bottom: 15px;
  color: #333;
}

.add-form {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.quantity-input {
  max-width: 150px;
}

.shopping-sections {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.items-section {
  max-width: 800px;
  margin: 0 auto;
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

.items-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.item-card {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px 20px;
  transition: all 0.3s;
}

.item-card.purchased {
  opacity: 0.6;
}

.item-card.purchased .item-name {
  text-decoration: line-through;
}

.item-checkbox {
  position: relative;
}

.item-checkbox input[type="checkbox"] {
  width: 24px;
  height: 24px;
  cursor: pointer;
  accent-color: #ff6b6b;
}

.item-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 15px;
}

.item-name {
  font-size: 1rem;
  font-weight: 500;
  color: #333;
}

.item-quantity {
  color: #ff6b6b;
  font-size: 0.9rem;
  font-weight: 500;
  padding: 4px 10px;
  background-color: #fff5f5;
  border-radius: 12px;
}

.item-edit-form {
  flex: 1;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.item-edit-form .form-control {
  padding: 8px 12px;
}

.item-actions {
  display: flex;
  gap: 8px;
}

.purchased-section .section-title {
  color: #6c757d;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e9ecef;
}

@media (max-width: 768px) {
  .stats-section {
    flex-direction: column;
    gap: 15px;
  }

  .stat-card {
    padding: 15px;
  }

  .add-form {
    flex-direction: column;
  }

  .quantity-input {
    max-width: none;
  }

  .item-card {
    flex-wrap: wrap;
  }

  .item-edit-form {
    width: 100%;
    order: 3;
    margin-top: 10px;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
