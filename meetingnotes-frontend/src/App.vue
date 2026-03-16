<template>
  <div id="app-root">
    <nav class="nav">
      <span class="nav-brand">📋 MeetingNotes</span>
      <router-link to="/upload">Upload</router-link>
      <router-link to="/notes">All Notes</router-link>
      <router-link to="/reminders">Reminder Log</router-link>
    </nav>
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<style>
.nav {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 0.75rem 2rem;
  background: #1a1a2e;
  color: white;
}
.nav-brand { font-weight: 700; font-size: 1.1rem; margin-right: auto; }
.nav a { color: #a0aec0; text-decoration: none; font-size: 0.95rem; }
.nav a.router-link-active { color: white; font-weight: 600; }
.main { max-width: 900px; margin: 2rem auto; padding: 0 1rem; }
</style>
