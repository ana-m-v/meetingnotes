import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia } from 'pinia'
import App from './App.vue'
import UploadView from './views/UploadView.vue'
import NoteListView from './views/NoteListView.vue'
import ActionItemsView from './views/ActionItemsView.vue'
import RemindersView from './views/RemindersView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/upload' },
    { path: '/upload', component: UploadView },
    { path: '/notes', component: NoteListView },
    { path: '/notes/:id/actions', component: ActionItemsView },
    { path: '/reminders', component: RemindersView },
  ]
})

const app = createApp(App)
const pinia = createPinia()

app.use(router)
app.use(pinia)
app.mount('#app')
