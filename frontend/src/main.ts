import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './styles/cyber.css'
import './styles/highlight-cyber.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
