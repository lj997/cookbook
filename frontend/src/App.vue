<template>
  <div id="app">
    <nav class="navbar">
      <div class="container">
        <router-link to="/" class="logo">
          <span class="logo-icon">🍳</span>
          <span class="logo-text">菜谱管理</span>
        </router-link>
        <div class="nav-links">
          <router-link to="/" class="nav-link" :class="{ active: $route.path === '/' }">
            菜谱列表
          </router-link>
          <router-link to="/shopping" class="nav-link" :class="{ active: $route.path === '/shopping' }">
            购物清单
          </router-link>
          <router-link to="/random" class="nav-link" :class="{ active: $route.path === '/random' }">
            随机推荐
          </router-link>
          <router-link to="/create" class="nav-link create-btn">
            + 添加菜谱
          </router-link>
        </div>
      </div>
    </nav>
    
    <main class="main-content">
      <div class="container">
        <router-view />
      </div>
    </main>
    
    <footer class="footer">
      <div class="container">
        <p>个人菜谱收藏与管理工具 © 2026</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
</script>

<style>
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding: 20px 0;
}
</style>
