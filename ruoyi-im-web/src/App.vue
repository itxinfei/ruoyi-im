<template>
  <div id="app" class="dingtalk-app">
    <ImHeader />
    <div class="main-container">
      <ImSideNav :active-module="activeModule" @switch-module="handleSwitchModule" />
      <SessionPanel v-if="activeModule === 'chat'" @select-session="handleSelectSession" />
      <WorkbenchPanel v-if="activeModule === 'workbench'" />
      <ContactsPanel v-if="activeModule === 'contacts'" />
      <DocumentsPanel v-if="activeModule === 'drive'" />
      <CalendarPanel v-if="activeModule === 'calendar'" />
      <SettingsPanel v-if="activeModule === 'settings'" />
      <UserProfilePanel v-if="activeModule === 'profile'" />
      <ChatPanel v-if="currentSession" :session="currentSession" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ImHeader from './components/ImHeader/index.vue'
import ImSideNav from './components/ImSideNav/index.vue'
import SessionPanel from './views/SessionPanel.vue'
import WorkbenchPanel from './views/WorkbenchPanel.vue'
import ContactsPanel from './views/ContactsPanel.vue'
import DocumentsPanel from './views/DocumentsPanel.vue'
import CalendarPanel from './views/CalendarPanel.vue'
import SettingsPanel from './views/SettingsPanel.vue'
import UserProfilePanel from './views/UserProfilePanel.vue'
import ChatPanel from './views/ChatPanel.vue'

const activeModule = ref('chat')
const currentSession = ref(null)

const handleSwitchModule = (module) => {
  activeModule.value = module
}

const handleSelectSession = (session) => {
  currentSession.value = session
}
</script>

<style lang="scss">
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 14px;
  color: #262626;
  background-color: #f5f5f5;
}

#app {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

.dingtalk-app {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  background-color: #ffffff;
}

.main-container {
  display: flex;
  flex: 1;
  overflow: hidden;
}
</style>
