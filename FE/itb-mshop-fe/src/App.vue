<script setup>
import Navbar from './components/common/Navbar.vue'
import Foot from './components/common/Footer.vue';
</script>

<template>
  <Navbar />
  <main>
    <router-view />
  </main>
  <Foot />
</template>
