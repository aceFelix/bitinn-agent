/**
 * v-lazy-load 图片懒加载指令
 * 使用 IntersectionObserver，在图片进入视口时再加载
 *
 * 用法：
 *   <img v-lazy-load="imageUrl" />
 *   <img v-lazy-load="{ src: imageUrl, placeholder: defaultImg }" />
 */
const LazyLoadDirective = {
  mounted(el, binding) {
    if (!('IntersectionObserver' in window)) {
      // 不支持 Observer 时直接设置 src
      setSrc(el, binding.value)
      return
    }

    const value = binding.value
    const { src, placeholder } = normalizeValue(value)

    // 设置占位图
    if (placeholder) {
      el.src = placeholder
    }

    // 先把真实 src 存在 dataset
    el.dataset.src = src

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            const img = entry.target
            const realSrc = img.dataset.src
            if (realSrc) {
              // 渐进加载：先模糊后清晰
              const tempImg = new Image()
              tempImg.onload = () => {
                img.src = realSrc
                img.classList.add('lazy-loaded')
              }
              tempImg.onerror = () => {
                // 加载失败使用占位图
                if (placeholder) img.src = placeholder
              }
              tempImg.src = realSrc
              // 加载完停止观察
              observer.unobserve(img)
            }
          }
        })
      },
      {
        rootMargin: '100px 0px', // 提前 100px 开始加载
        threshold: 0.01,
      }
    )

    el._lazyObserver = observer
    observer.observe(el)
  },

  updated(el, binding) {
    // src 变化时更新
    const newValue = binding.value
    const { src } = normalizeValue(newValue)
    if (el.dataset.src !== src) {
      el.dataset.src = src
      if ('IntersectionObserver' in window && el._lazyObserver) {
        // 重新观察
        el._lazyObserver.unobserve(el)
        el._lazyObserver.observe(el)
      } else {
        setSrc(el, newValue)
      }
    }
  },

  unmounted(el) {
    if (el._lazyObserver) {
      el._lazyObserver.disconnect()
      el._lazyObserver = null
    }
  },
}

function normalizeValue(value) {
  if (typeof value === 'string') {
    return { src: value, placeholder: null }
  }
  return { src: value.src, placeholder: value.placeholder || null }
}

function setSrc(el, value) {
  const { src } = normalizeValue(value)
  el.src = src
}

export default LazyLoadDirective
