<script setup>
import Navbar from './components/Navbar.vue'
import Foot from './components/Footer.vue';
</script>

<template>
  <Navbar />
  <main>
    <router-view />
  </main>
  <Foot />
</template>
