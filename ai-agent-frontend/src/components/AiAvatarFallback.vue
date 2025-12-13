<template>
  <div class="ai-avatar-fallback" :class="type">
    <span v-if="type === 'relamind'">🌱</span>
    <span v-else-if="type === 'super'">🤖</span>
    <span v-else>🤖</span>
  </div>
</template>

<script setup>
defineProps({
  type: {
    type: String,
    default: 'default'
  }
})
</script>

<style scoped>
.ai-avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  border-radius: 50%;
}

.relamind {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.super, .default {
  background: linear-gradient(45deg, #3f51b5, #5677fc);
}
</style> 