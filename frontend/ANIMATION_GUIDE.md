# CYBERBLOG 动画效果使用指南

## 概述

本文档说明如何使用项目中的各种动画效果和动态背景组件。

## 动态背景组件

### CyberBackground.vue

全屏动态背景组件，包含以下效果：

- **粒子星空**: Canvas 绘制的闪烁星星
- **流动网格**: 赛博朋克风格的透视网格
- **霓虹光线**: 水平移动的霓虹光线
- **光晕效果**: 脉冲式的彩色光晕
- **漂浮粒子**: 从下往上的漂浮发光粒子
- **扫描线**: 屏幕从上到下的扫描光线
- **噪点效果**: 细微的噪点纹理

**使用方式**:

```vue
<template>
  <div>
    <CyberBackground />
    <!-- 你的内容 -->
  </div>
</template>

<script setup>
import CyberBackground from '@/components/CyberBackground.vue'
</script>
```

已在 `App.vue` 中全局引入，所有页面都会显示动态背景。

## 页面过渡动画

### PageTransition.vue

页面切换时的滑动过渡效果。

**使用方式**:

```vue
<template>
  <PageTransition>
    <RouterView />
  </PageTransition>
</template>

<script setup>
import PageTransition from '@/components/PageTransition.vue'
</script>
```

已集成到 `App.vue` 中。

## 动画 CSS 类

在 `cyber.css` 中定义了大量可复用的动画类：

### 1. 浮动动画
```html
<div class="animate-float">悬浮元素</div>
```

### 2. 脉冲光晕
```html
<div class="animate-pulse-glow">呼吸灯效果</div>
```

### 3. 霓虹闪烁
```html
<div class="animate-neon-flicker">闪烁文字</div>
```

### 4. 滑入动画
```html
<div class="animate-slide-up">向上滑入</div>
```

### 5. 缩放动画
```html
<div class="animate-scale-in">缩放进入</div>
```

### 6. 旋转动画
```html
<div class="animate-rotate">持续旋转</div>
```

### 7. 按钮悬停效果
```html
<button class="btn-cyber btn-animate">带光效的按钮</button>
```

### 8. 卡片3D悬停效果
```html
<div class="cyber-card animate-card-3d">3D悬停卡片</div>
```

### 9. 打字机效果
```html
<span class="typing-effect">打字机效果</span>
```

### 10. 渐变边框
```html
<div class="gradient-border">渐变边框</div>
```

### 11. 延迟动画
```html
<div class="animate-slide-up delay-100">延迟0.1秒</div>
<div class="animate-slide-up delay-200">延迟0.2秒</div>
<div class="animate-slide-up delay-300">延迟0.3秒</div>
```

### 12. 加载动画
```html
<div class="loader"></div>
```

## Vue 组件动画

### 1. 入场动画组合式函数

在 `composables/useFadeIn.ts` 中提供了入场动画功能：

```vue
<template>
  <div ref="el" :style="style">渐入元素</div>
</template>

<script setup lang="ts">
import { useFadeIn } from '@/composables/useFadeIn'

const { el, style } = useFadeIn({
  delay: 0,        // 延迟时间（毫秒）
  duration: 600,   // 动画时长（毫秒）
  distance: 30     // 位移距离（像素）
})
</script>
```

### 2. 交错动画

为列表元素创建交错入场效果：

```vue
<template>
  <div 
    v-for="(item, index) in items" 
    :key="item.id"
    :style="getAnimationStyle(index)"
  >
    {{ item.name }}
  </div>
</template>

<script setup lang="ts">
import { useStaggeredFadeIn } from '@/composables/useFadeIn'

const animations = useStaggeredFadeIn(items.length, {
  baseDelay: 0,
  duration: 600,
  distance: 40
})

function getAnimationStyle(index: number) {
  const anim = animations[index]
  return {
    opacity: 0,
    transform: `translateY(${anim.distance}px)`,
    transition: `all ${anim.duration}ms cubic-bezier(0.4, 0, 0.2, 1)`,
    transitionDelay: `${anim.delay}ms`
  }
}

// 在 onMounted 中设置延迟后设置为可见状态
import { ref, onMounted } from 'vue'
const isVisible = ref(false)

onMounted(() => {
  setTimeout(() => {
    isVisible.value = true
  }, 100)
})
</script>
```

## 应用示例

### 首页动画流程

参考 `HomeView.vue` 的实现：

1. 创建动画状态
2. 定义动画样式
3. 在 onMounted 中延迟触发动画
4. 使用 CSS transition 实现平滑过渡

```vue
<template>
  <div class="home-view">
    <h1 :style="titleStyle">CYBERBLOG</h1>
    <p :style="contentStyle">内容文字</p>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

const isLoaded = ref(false)

const titleStyle = computed(() => ({
  opacity: isLoaded.value ? 1 : 0,
  transform: isLoaded.value ? 'translateY(0)' : 'translateY(30px)',
  transition: 'all 0.8s cubic-bezier(0.4, 0, 0.2, 1)',
  transitionDelay: '0.2s'
}))

const contentStyle = computed(() => ({
  opacity: isLoaded.value ? 1 : 0,
  transform: isLoaded.value ? 'translateY(0)' : 'translateY(20px)',
  transition: 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)',
  transitionDelay: '0.4s'
}))

onMounted(() => {
  setTimeout(() => {
    isLoaded.value = true
  }, 100)
})
</script>
```

## 关键帧动画

在 `cyber.css` 中定义的关键帧动画：

- `float`: 上下浮动
- `pulse-glow`: 脉冲光晕
- `neon-flicker`: 霓虹闪烁
- `rotate`: 持续旋转
- `slideUp`: 向上滑入
- `scaleIn`: 缩放进入
- `typing`: 打字机效果
- `blink`: 光标闪烁
- `border-flow`: 边框流动

## 最佳实践

1. **性能优化**: 避免在页面中同时使用过多动画
2. **动画时长**: 推荐 300-600ms，过长会让用户感到拖沓
3. **缓动函数**: 使用 `cubic-bezier(0.4, 0, 0.2, 1)` 实现流畅效果
4. **渐进增强**: 动画应作为视觉增强，不应影响核心功能
5. **移动端**: 考虑为移动设备减少或禁用某些复杂动画

## 自定义动画

如果需要创建自定义动画，可以在 `cyber.css` 中添加：

```css
@keyframes customAnimation {
  0% {
    opacity: 0;
    transform: translateY(20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-custom {
  animation: customAnimation 0.5s ease-out forwards;
}
```

## 兼容性

这些动画效果基于现代 CSS 特性，在以下浏览器中测试通过：

- Chrome 80+
- Firefox 75+
- Safari 13+
- Edge 80+

对于不支持的浏览器，动画会优雅降级为立即显示最终状态。
