import { defineStore } from 'pinia'
import { notesApi } from '../api/index.js'

const demoNote = {
  id: 'demo-note-001',
  title: 'Sample: Weekly Product Sync',
  parseStatus: 'DONE',
  uploadedAt: '2026-03-09T10:00:00.000Z',
  parsedAt: '2026-03-09T10:00:03.000Z',
  actionItemCount: 3,
  contentHash: 'demo09f8e1a5b6c7d8e9f00112233445566',
}

export const useNotesStore = defineStore('notes', {
  state: () => ({
    notes: [demoNote],
    loading: false,
    error: null,
  }),
  actions: {
    async loadNotes() {
      this.loading = true
      this.error = null
      try {
        const response = await notesApi.list()
        const apiNotes = Array.isArray(response.data) ? response.data : []
        this.notes = apiNotes.length > 0 ? apiNotes : [demoNote]
      } catch (error) {
        this.error = error.response?.data?.message || error.message
        this.notes = [demoNote]
      } finally {
        this.loading = false
      }
    },
  },
})
