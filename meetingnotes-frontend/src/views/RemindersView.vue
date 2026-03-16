<template>
  <div>
    <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:1rem">
      <h2 style="margin:0">Reminder Log</h2>
      <button class="btn-outline" @click="load">↻ Refresh</button>
    </div>
    <p class="muted" style="margin-top:-0.5rem;margin-bottom:1rem;font-size:0.85rem">
      Shows outbox events (Transactional Outbox pattern). Pending events are dispatched every 5 seconds.
    </p>

    <div v-if="loading" class="muted">Loading...</div>
    <div v-else-if="events.length === 0" class="muted">No reminder events yet.</div>

    <table v-else class="log-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Type</th>
          <th>Status</th>
          <th>Scheduled at</th>
          <th>Processed at</th>
          <th>Retries</th>
          <th>Payload</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="e in events" :key="e.id">
          <td>{{ e.id }}</td>
          <td>{{ e.eventType }}</td>
          <td><span :class="['badge', e.status.toLowerCase()]">{{ e.status }}</span></td>
          <td>{{ fmt(e.scheduledAt) }}</td>
          <td>{{ e.processedAt ? fmt(e.processedAt) : '—' }}</td>
          <td>{{ e.retryCount }}</td>
          <td><code class="payload">{{ e.payload }}</code></td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { remindersApi } from '../api/index.js'

const events = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try { events.value = (await remindersApi.list()).data }
  finally { loading.value = false }
}

const fmt = iso => new Date(iso).toLocaleString()
onMounted(load)
</script>

<style scoped>
h2 { margin-bottom: 1rem; }
.log-table {
  width: 100%; border-collapse: collapse; background: white;
  border-radius: 8px; overflow: hidden; box-shadow: 0 1px 4px #0001; font-size: 0.85rem;
}
.log-table th {
  text-align: left; padding: 0.6rem 0.75rem; background: #f3f4f6;
  font-size: 0.78rem; color: #555; text-transform: uppercase; letter-spacing: 0.04em;
}
.log-table td { padding: 0.55rem 0.75rem; border-top: 1px solid #f0f0f0; vertical-align: top; }
.payload { font-size: 0.75rem; word-break: break-all; color: #444; }
.badge {
  font-size: 0.7rem; padding: 0.15rem 0.45rem; border-radius: 99px;
  font-weight: 600; text-transform: uppercase;
}
.badge.pending    { background:#e0e7ff;color:#3730a3; }
.badge.processing { background:#fef3c7;color:#78350f; }
.badge.sent       { background:#d1fae5;color:#065f46; }
.badge.failed     { background:#fee2e2;color:#991b1b; }
.btn-outline {
  background: white; border: 1px solid #ddd; padding: 0.4rem 0.9rem;
  border-radius: 6px; cursor: pointer; font-size: 0.9rem;
}
.muted { color: #888; }
</style>
