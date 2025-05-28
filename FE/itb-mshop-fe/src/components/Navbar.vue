<script setup>
import { ref } from 'vue'

const mobileMenuOpen = ref(false)
</script>

<template>
  <header class="bg-white shadow w-full top-0 z-50">
    <nav class="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
      <router-link to="/" class="flex items-center space-x-2">
        <img src="/public/favicon.ico" alt="Logo" class="h-8 w-8" />
        <span class="text-lg font-bold text-gray-800">ITB MShop</span>
      </router-link>

      <!-- Desktop Links -->
      <div class="hidden md:flex space-x-6 text-sm font-medium text-gray-700">
        <router-link to="/" class="hover:text-purple-600">Home</router-link>
        <router-link :to="{ name: 'ListGallery'}" class="hover:text-purple-600">Shop</router-link>
        <router-link :to="{ name: 'ListSaleItem'}" class="hover:text-purple-600">Categories</router-link>
        <router-link to="/contact" class="hover:text-purple-600">Contact</router-link>
      </div>

      <!-- Mobile -->
      <button class="md:hidden" @click="mobileMenuOpen = !mobileMenuOpen">
        <span class="material-icons">menu</span>
      </button>
    </nav>

    <!-- Mobile dropdown -->
    <div v-if="mobileMenuOpen" class="md:hidden px-4 py-2 bg-white shadow">
      <router-link to="/" class="block py-2 text-gray-700 hover:text-purple-600">Home</router-link>
      <router-link to="/shop" class="block py-2 text-gray-700 hover:text-purple-600">Shop</router-link>
      <router-link to="/categories" class="block py-2 text-gray-700 hover:text-purple-600">Categories</router-link>
      <router-link to="/contact" class="block py-2 text-gray-700 hover:text-purple-600">Contact</router-link>
    </div>
  </header>
</template>
