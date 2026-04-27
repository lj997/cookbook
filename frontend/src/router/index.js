import { createRouter, createWebHistory } from 'vue-router'
import RecipeList from '../views/RecipeList.vue'
import RecipeDetail from '../views/RecipeDetail.vue'
import RecipeForm from '../views/RecipeForm.vue'
import ShoppingList from '../views/ShoppingList.vue'
import RandomRecipe from '../views/RandomRecipe.vue'

const routes = [
  {
    path: '/',
    name: 'RecipeList',
    component: RecipeList
  },
  {
    path: '/recipe/:id',
    name: 'RecipeDetail',
    component: RecipeDetail
  },
  {
    path: '/create',
    name: 'CreateRecipe',
    component: RecipeForm
  },
  {
    path: '/edit/:id',
    name: 'EditRecipe',
    component: RecipeForm
  },
  {
    path: '/shopping',
    name: 'ShoppingList',
    component: ShoppingList
  },
  {
    path: '/random',
    name: 'RandomRecipe',
    component: RandomRecipe
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
