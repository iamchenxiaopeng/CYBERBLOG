<template>
  <div class="not-found-view">
    <div class="nf-content">
      <h1 class="nf-code neon-pink glitch" data-text="404">404</h1>
      <p class="nf-msg">// 神经链路断开 // 目标节点不存在</p>
      <RouterLink to="/" class="btn-cyber">返回主节点</RouterLink>
    </div>
  </div>
</template>

<style scoped>
.not-found-view {
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}
.nf-content { display: flex; flex-direction: column; align-items: center; gap: 24px; }
.nf-code {
  font-family: var(--font-cyber);
  font-size: clamp(5rem, 15vw, 10rem);
  font-weight: 900;
  line-height: 1;
  letter-spacing: 8px;
}
.nf-msg {
  font-family: var(--font-mono);
  font-size: 14px;
  color: var(--cyber-text-muted);
  letter-spacing: 3px;
}
</style>
