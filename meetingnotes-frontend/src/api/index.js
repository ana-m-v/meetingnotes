import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

export const notesApi = {
  ingest: (title, content) => api.post('/notes', { title, content }),
  list: () => api.get('/notes'),
  get: (id) => api.get(`/notes/${id}`),
  getActions: (id) => api.get(`/notes/${id}/actions`),
}

export const actionsApi = {
  update: (id, payload) => api.patch(`/actions/${id}`, payload),
}

export const remindersApi = {
  list: () => api.get('/reminders'),
}
