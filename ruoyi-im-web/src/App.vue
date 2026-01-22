<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
// Root component just provides the router view
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
</style>
