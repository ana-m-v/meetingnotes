<template>
  <div>
    <h2>Upload Meeting Notes</h2>

    <div class="card">
      <label>Title</label>
      <input v-model="title" placeholder="e.g. Sprint Planning 2024-06-01" />

      <label style="margin-top:1rem">Notes content</label>
      <textarea
        v-model="content"
        rows="14"
        placeholder="Paste your meeting notes here...

Examples of lines that will be parsed as action items:
- [ ] Fix login bug @alice by: 2024-06-10
TODO: Update documentation owner: bob
ACTION: Deploy to staging by: 2024-06-15
1. Review PR #42 assigned to: carol"
      />

    <div class="file-row">
      <span class="file-label">Or upload a .txt file:</span>
      <label class="file-btn">
        Choose file
        <input type="file" accept=".txt" @change="onFileUpload" />
      </label>
      <span class="file-name">{{ fileName || 'No file selected' }}</span>
    </div>

      <button class="btn-primary" :disabled="loading" @click="submit" style="margin-top:1rem">
        {{ loading ? 'Uploading...' : 'Submit Notes' }}
      </button>
    </div>

    <div v-if="result" :class="['result-box', result.duplicate ? 'warn' : 'ok']">
      <template v-if="result.duplicate">
        ⚠️ These exact notes were already uploaded (duplicate detected by content hash).
      </template>
      <template v-else>
        ✅ Notes submitted! Parsing is running in the background.
      </template>
      <div v-if="result?.note?.id" class="result-link">
        <router-link :to="`/notes/${result.note.id}/actions`">
          {{ result.duplicate ? 'View existing actions →' : 'View action items →' }}
        </router-link>
      </div>
      <details class="result-raw">
        <summary>Show server response</summary>
        <pre>{{ result }}</pre>
      </details>
    </div>

    <div v-if="error" class="result-box error">❌ {{ error }}</div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { notesApi } from '../api/index.js'

const title = ref('')
const content = ref('')
const loading = ref(false)
const result = ref(null)
const error = ref(null)
const fileName = ref('')

async function submit() {
  if (!title.value.trim() || !content.value.trim()) {
    error.value = 'Title and content are required.'
    return
  }
  loading.value = true
  error.value = null
  result.value = null
  try {
    const resp = await notesApi.ingest(title.value, content.value)
    result.value = resp.data
  } catch (e) {
    error.value = e.response?.data?.message || e.message
  } finally {
    loading.value = false
  }
}

function onFileUpload(e) {
  const file = e.target.files[0]
  if (!file) return
  fileName.value = file.name
  const reader = new FileReader()
  reader.onload = ev => { content.value = ev.target.result }
  reader.readAsText(file)
  if (!title.value) title.value = file.name.replace(/\.[^.]+$/, '')
}
</script>

<style scoped>
h2 { margin-bottom: 1rem; }
.card { background: white; border-radius: 8px; padding: 1.5rem; box-shadow: 0 1px 4px #0001; }
label { display: block; font-weight: 600; margin-bottom: 0.3rem; font-size: 0.9rem; }
input[type=text], input:not([type]), textarea {
  width: 100%; padding: 0.5rem 0.75rem; border: 1px solid #ddd;
  border-radius: 6px; font-size: 0.95rem; font-family: inherit;
}
textarea { resize: vertical; font-family: monospace; }
.btn-primary {
  background: #4f46e5; color: white; border: none; padding: 0.6rem 1.4rem;
  border-radius: 6px; font-size: 0.95rem; cursor: pointer;
}
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.file-row {
  margin-top: 0.75rem;
  display: flex;
  align-items: center;
  gap: 0.6rem;
  flex-wrap: wrap;
  font-size: 0.85rem;
  color: #666;
}
.file-label { font-weight: 600; color: #555; }
.file-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.4rem 0.9rem;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  color: #111827;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease;
}
.file-btn:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
}
.file-btn input {
  display: none;
}
.file-name {
  font-family: monospace;
  background: #f3f4f6;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  color: #374151;
}
.result-box {
  margin-top: 1rem; padding: 0.75rem 1rem; border-radius: 6px; font-size: 0.95rem;
}
.result-link { margin-top: 0.35rem; }
.result-raw {
  margin-top: 0.5rem;
  font-size: 0.8rem;
}
.result-raw pre {
  white-space: pre-wrap;
  background: #ffffffb3;
  padding: 0.5rem;
  border-radius: 6px;
  border: 1px dashed #ddd;
}
.ok   { background: #d1fae5; color: #065f46; }
.warn { background: #fef3c7; color: #78350f; }
.error { background: #fee2e2; color: #991b1b; }
a { color: inherit; font-weight: 600; }
</style>
