<script setup>
import Navbar from './components/Navbar.vue'
import Foot from './components/Footer.vue';
</script>

<template>
  <Navbar />
  <main class="pt-16">
    <router-view />
  </main>
  <Foot />
</template>
