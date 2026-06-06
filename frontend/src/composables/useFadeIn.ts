import { ref, onMounted } from 'vue'

interface UseFadeInOptions {
  delay?: number
  duration?: number
  distance?: number
}

export function useFadeIn(options: UseFadeInOptions = {}) {
  const { delay = 0, duration = 600, distance = 30 } = options
  
  const isVisible = ref(false)
  const el = ref<HTMLElement | null>(null)

  onMounted(() => {
    if (!el.value) return

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            setTimeout(() => {
              isVisible.value = true
            }, delay)
            observer.unobserve(entry.target)
          }
        })
      },
      { threshold: 0.1 }
    )

    observer.observe(el.value)
  })

  const style = {
    opacity: isVisible.value ? 1 : 0,
    transform: isVisible.value ? 'translateY(0)' : `translateY(${distance}px)`,
    transition: `all ${duration}ms cubic-bezier(0.4, 0, 0.2, 1)`,
  }

  return {
    el,
    isVisible,
    style
  }
}

// 交错动画列表
export function useStaggeredFadeIn(count: number, options: UseFadeInOptions = {}) {
  const { delay: baseDelay = 0, duration = 600, distance = 30 } = options
  const delayIncrement = 100 // 每个元素延迟增量

  return Array.from({ length: count }, (_, i) => ({
    delay: baseDelay + i * delayIncrement,
    duration,
    distance
  }))
}
