<script setup>
import { ref } from 'vue'

const mobileMenuOpen = ref(false)
</script>

<template>
  <header class="bg-gray-900 text-gray-100 shadow w-full top-0 z-50">
    <nav class="max-w-7xl mx-auto px-6 py-4 flex justify-between items-center">
      <router-link to="/" class="flex items-center space-x-2">
        <img src="/public/favicon.ico" alt="Logo" class="h-8 w-8" />
        <span class="text-lg font-semibold text-white">ITB MShop</span>
      </router-link>

      <!-- Desktop Links -->
      <div class="hidden md:flex space-x-8 items-center text-sm font-medium">
        <router-link to="/" class="hover:text-purple-400 transition">Home</router-link>
        <router-link :to="{ name: 'ListGallery'}" class="hover:text-purple-400 transition">Shop</router-link>
        <router-link :to="{ name: 'ListSaleItems'}" class="hover:text-purple-400 transition">Categories</router-link>
        <router-link to="/contact" class="hover:text-purple-400 transition">Contact</router-link>

        <!-- Auth Buttons -->
        <div class="space-x-3">
          <router-link to="/signIn" 
            class="px-4 py-2 rounded-md text-gray-200 border border-gray-500 hover:bg-gray-800 transition">
            Sign In
          </router-link>
          <router-link :to="{ name: 'Register'}" 
            class="itbms-register-button px-4 py-2 rounded-md bg-gradient-to-r from-purple-500 to-indigo-600 text-white font-semibold shadow hover:opacity-90 transition">
            Register
          </router-link>
        </div>
      </div>

      <!-- Mobile -->
      <button class="md:hidden text-gray-200" @click="mobileMenuOpen = !mobileMenuOpen">
        <span class="material-icons">menu</span>
      </button>
    </nav>

    <!-- Mobile dropdown -->
    <div v-if="mobileMenuOpen" class="md:hidden px-4 py-3 bg-gray-900 shadow text-center space-y-2 border-t border-gray-700">
      <router-link to="/" class="block py-2 hover:text-purple-400">Home</router-link>
      <router-link :to="{ name: 'ListGallery'}" class="block py-2 hover:text-purple-400">Shop</router-link>
      <router-link :to="{ name: 'ListSaleItems'}" class="block py-2 hover:text-purple-400">Categories</router-link>
      <router-link to="/contact" class="block py-2 hover:text-purple-400">Contact</router-link>

       <!-- Auth buttons for mobile -->
      <router-link to="/signIn" class="block py-2 border border-gray-600 rounded-md hover:bg-gray-800">Sign In</router-link>
      <router-link :to="{ name: 'Register'}" class="block py-2 bg-gradient-to-r from-purple-500 to-indigo-600 text-white rounded-md">Register</router-link>
    </div>
  </header>
</template>
