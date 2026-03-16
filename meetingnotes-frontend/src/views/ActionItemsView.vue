<template>
  <div>
    <router-link to="/notes" class="back-link">← All Notes</router-link>
    <h2>Action Items</h2>

    <div v-if="noteStatus" class="parse-status-bar">
      Parse status:
      <span :class="['badge', noteStatus.toLowerCase()]">{{ noteStatus }}</span>
      <span v-if="noteStatus === 'IN_PROGRESS'" class="spinner">⏳ Parsing in background…</span>
      <button v-if="noteStatus !== 'DONE'" class="btn-outline small" @click="pollUntilDone">Poll</button>
    </div>

    <div v-if="loading" class="muted">Loading action items...</div>
    <div v-else-if="items.length === 0" class="muted">
      <template v-if="noteStatus === 'DONE'">No action items found in these notes.</template>
      <template v-else>Action items will appear once parsing completes.</template>
    </div>

    <div v-for="item in items" :key="item.id" class="item-card">
      <div class="item-desc">{{ item.description }}</div>

      <div class="item-fields">
        <label>Owner
          <input v-model="item.owner" placeholder="@username" @blur="save(item)" />
        </label>
        <label>Due date
          <input type="date" v-model="item.dueDate" @blur="save(item)" />
        </label>
        <label>Status
          <select v-model="item.status" @change="save(item)">
            <option value="OPEN">Open</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="DONE">Done</option>
          </select>
        </label>
      </div>

      <details class="reminder-section">
        <summary>Schedule reminder</summary>
        <div class="reminder-fields">
          <label>Owner email
            <input v-model="item._reminderEmail" type="email" placeholder="owner@example.com" />
          </label>
          <label>Send at
            <input v-model="item._reminderAt" type="datetime-local" />
          </label>
          <button class="btn-primary small" @click="scheduleReminder(item)">Schedule</button>
          <span v-if="item._reminderSaved" class="ok-msg">✅ Reminder queued</span>
        </div>
      </details>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { notesApi, actionsApi } from '../api/index.js'

const route = useRoute()
const noteId = route.params.id
const DEMO_NOTE_ID = 'demo-note-001'

const demoItems = [
  {
    id: 'demo-action-001',
    description: 'Finalize Q2 roadmap draft and share it with the team',
    owner: 'alice',
    dueDate: '2026-03-14',
    status: 'OPEN',
  },
  {
    id: 'demo-action-002',
    description: 'Review onboarding flow copy with design and support',
    owner: 'bob',
    dueDate: '2026-03-16',
    status: 'IN_PROGRESS',
  },
  {
    id: 'demo-action-003',
    description: 'Prepare customer migration checklist for next release',
    owner: 'carol',
    dueDate: '2026-03-18',
    status: 'DONE',
  },
]

const items = ref([])
const noteStatus = ref(null)
const loading = ref(false)

function withUiFields(actionItems) {
  return actionItems.map(i => ({ ...i, _reminderEmail: '', _reminderAt: '', _reminderSaved: false }))
}

function loadDemoItems() {
  noteStatus.value = 'DONE'
  items.value = withUiFields(demoItems)
}

async function load() {
  loading.value = true
  try {
    if (noteId === DEMO_NOTE_ID) {
      loadDemoItems()
      return
    }

    const [noteResp, itemsResp] = await Promise.all([
      notesApi.get(noteId),
      notesApi.getActions(noteId),
    ])
    noteStatus.value = noteResp.data.parseStatus
    items.value = withUiFields(itemsResp.data)
  } catch {
    noteStatus.value = 'FAILED'
    items.value = []
  } finally {
    loading.value = false
  }
}

async function save(item) {
  if (noteId === DEMO_NOTE_ID) return
  try {
    const resp = await actionsApi.update(item.id, {
      owner: item.owner,
      dueDate: item.dueDate,
      status: item.status,
      version: item.version,
    })
    Object.assign(item, resp.data)
  } catch (e) {
    if (e.response?.status === 409) {
      alert('This item was updated by someone else. Reloading…')
      await load()
      return
    }
    throw e
  }
}

async function scheduleReminder(item) {
  if (!item._reminderEmail) { alert('Enter owner email'); return }
  if (noteId === DEMO_NOTE_ID) {
    item._reminderSaved = true
    setTimeout(() => { item._reminderSaved = false }, 3000)
    return
  }
  try {
    const resp = await actionsApi.update(item.id, {
      scheduleReminder: true,
      ownerEmail: item._reminderEmail,
      reminderAt: item._reminderAt ? new Date(item._reminderAt).toISOString() : null,
      version: item.version,
    })
    Object.assign(item, resp.data)
    item._reminderSaved = true
    setTimeout(() => { item._reminderSaved = false }, 3000)
  } catch (e) {
    if (e.response?.status === 409) {
      alert('This item was updated by someone else. Reloading…')
      await load()
      return
    }
    throw e
  }
}

async function pollUntilDone() {
  if (noteId === DEMO_NOTE_ID) {
    noteStatus.value = 'DONE'
    return
  }
  const resp = await notesApi.get(noteId)
  noteStatus.value = resp.data.parseStatus
  if (noteStatus.value === 'DONE') await load()
}

onMounted(load)
</script>

<style scoped>
.back-link { color: #4f46e5; text-decoration: none; font-size: 0.9rem; }
h2 { margin: 0.5rem 0 1rem; }
.parse-status-bar {
  display: flex; align-items: center; gap: 0.75rem;
  background: white; padding: 0.6rem 1rem; border-radius: 6px;
  margin-bottom: 1rem; font-size: 0.9rem; box-shadow: 0 1px 3px #0001;
}
.spinner { color: #78350f; }
.item-card {
  background: white; border-radius: 8px; padding: 1rem 1.25rem;
  box-shadow: 0 1px 4px #0001; margin-bottom: 0.75rem;
}
.item-desc { font-weight: 500; margin-bottom: 0.75rem; line-height: 1.4; }
.item-fields { display: flex; gap: 1rem; flex-wrap: wrap; }
.item-fields label, .reminder-fields label {
  display: flex; flex-direction: column; font-size: 0.8rem;
  font-weight: 600; color: #555; gap: 0.25rem;
}
.item-fields input, .item-fields select,
.reminder-fields input { padding: 0.4rem 0.6rem; border: 1px solid #ddd; border-radius: 5px; font-size: 0.9rem; }
.reminder-section { margin-top: 0.75rem; }
.reminder-section summary { font-size: 0.85rem; color: #4f46e5; cursor: pointer; }
.reminder-fields {
  display: flex; gap: 0.75rem; flex-wrap: wrap; align-items: flex-end;
  margin-top: 0.5rem; padding: 0.75rem; background: #f8f9ff; border-radius: 6px;
}
.badge {
  font-size: 0.72rem; padding: 0.2rem 0.5rem; border-radius: 99px;
  font-weight: 600; text-transform: uppercase;
}
.badge.pending     { background:#e0e7ff;color:#3730a3; }
.badge.in_progress { background:#fef3c7;color:#78350f; }
.badge.done        { background:#d1fae5;color:#065f46; }
.badge.failed      { background:#fee2e2;color:#991b1b; }
.btn-primary {
  background: #4f46e5; color: white; border: none; padding: 0.5rem 1rem;
  border-radius: 6px; cursor: pointer; font-size: 0.9rem; align-self: flex-end;
}
.btn-outline {
  background: white; border: 1px solid #ddd; border-radius: 5px; cursor: pointer; font-size: 0.85rem; padding: 0.3rem 0.7rem;
}
.btn-primary.small, .btn-outline.small { padding: 0.35rem 0.8rem; font-size: 0.82rem; }
.ok-msg { color: #065f46; font-size: 0.85rem; }
.muted { color: #888; margin-top: 1rem; }
</style>
