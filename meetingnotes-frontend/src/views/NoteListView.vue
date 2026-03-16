<template>
  <div>
    <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:1rem">
      <h2 style="margin:0">Meeting Notes</h2>
      <button class="btn-outline" @click="loadNotes">↻ Refresh</button>
    </div>

    <div v-if="loading" class="muted">Loading...</div>
    <div v-else-if="notes.length === 0" class="muted">No notes uploaded yet. <router-link to="/upload">Upload one →</router-link></div>

    <div v-for="note in notes" :key="note.id" class="note-card">
      <div class="note-header">
        <span class="note-title">{{ note.title }}</span>
        <span :class="['badge', note.parseStatus.toLowerCase()]">{{ note.parseStatus }}</span>
      </div>
      <div class="note-meta">
        <span>Uploaded: {{ fmt(note.uploadedAt) }}</span>
        <span v-if="note.parsedAt">· Parsed: {{ fmt(note.parsedAt) }}</span>
        <span>· {{ note.actionItemCount }} action item{{ note.actionItemCount !== 1 ? 's' : '' }}</span>
        <span class="note-hash" title="Content hash (idempotency key)">🔑 {{ note.contentHash.slice(0, 10) }}…</span>
      </div>
      <router-link :to="`/notes/${note.id}/actions`" class="view-link">View action items →</router-link>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useNotesStore } from '../store/notes.store.js'

const notesStore = useNotesStore()
const { notes, loading } = storeToRefs(notesStore)
const { loadNotes } = notesStore

const fmt = iso => new Date(iso).toLocaleString()

onMounted(loadNotes)
</script>

<style scoped>
h2 { margin-bottom: 1rem; }
.note-card {
  background: white; border-radius: 8px; padding: 1rem 1.25rem;
  box-shadow: 0 1px 4px #0001; margin-bottom: 0.75rem;
}
.note-header { display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.4rem; }
.note-title { font-weight: 600; font-size: 1rem; }
.badge {
  font-size: 0.72rem; padding: 0.2rem 0.5rem; border-radius: 99px;
  font-weight: 600; text-transform: uppercase; letter-spacing: 0.04em;
}
.badge.pending     { background:#e0e7ff;color:#3730a3; }
.badge.in_progress { background:#fef3c7;color:#78350f; }
.badge.done        { background:#d1fae5;color:#065f46; }
.badge.failed      { background:#fee2e2;color:#991b1b; }
.note-meta { font-size: 0.82rem; color: #666; display: flex; gap: 0.75rem; flex-wrap: wrap; margin-bottom: 0.5rem; }
.note-hash { font-family: monospace; background: #f3f4f6; padding: 0.1rem 0.4rem; border-radius: 4px; }
.view-link { font-size: 0.9rem; color: #4f46e5; text-decoration: none; font-weight: 600; }
.muted { color: #888; }
.btn-outline {
  background: white; border: 1px solid #ddd; padding: 0.4rem 0.9rem;
  border-radius: 6px; cursor: pointer; font-size: 0.9rem;
}
</style>
