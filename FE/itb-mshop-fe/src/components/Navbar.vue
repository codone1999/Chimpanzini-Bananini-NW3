<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const mobileMenuOpen = ref(false)
// Reactive token state
const accessToken = ref(localStorage.getItem('access_token'))

// Function to decode JWT token
function decodeJWT(token) {
  if (!token) return null
  
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null
    
    // Decode payload (middle part)
    const payload = parts[1]
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    const parsed = JSON.parse(decoded)
    
    // Check if token is expired
    if (parsed.exp && parsed.exp < Math.floor(Date.now() / 1000)) {
      return null // Token expired
    }
    
    return parsed
  } catch (error) {
    return null
  }
}

// Get user info from token
const userInfo = computed(() => {
  if (!accessToken.value) return null
  return decodeJWT(accessToken.value)
})

// Get nickname from user info
// const userNickname = computed(() => {
//   if (!userInfo.value) return null
//   // Adjust these property names based on your JWT payload structure
//   return userInfo.value.nickname || 'User_Nickname'
// })

const userNickname = "BOOM"

// Check if user is logged in
const isLoggedIn = computed(() => {
  // return accessToken.value !== null
  return true
})

// Logout function
function handleLogout() {
  // Clear tokens
  localStorage.removeItem('access_token')
  localStorage.removeItem('refresh_token')
  
  // Update reactive state
  accessToken.value = null
  
  // Redirect to home
  router.push('/')
  
  // Close mobile menu if open
  mobileMenuOpen.value = false
}

function goToProfile() {
  router.push({ name: 'Profile'})
}

</script>

<template>
  <header class="bg-gray-950 text-gray-100 shadow w-full top-0 z-50">
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

        <!-- Auth Buttons - Dynamic based on login status -->
        <div class="space-x-3">
          <template v-if="!isLoggedIn">
            <!-- Not logged in - show login/register -->
            <router-link :to="{ name: 'Login'}" 
              class="px-4 py-2 rounded-md text-gray-200 border border-gray-500 hover:bg-gray-800 transition">
              Sign In
            </router-link>
            <router-link :to="{ name: 'Register'}" 
              class="itbms-register-button px-4 py-2 rounded-md bg-gradient-to-r from-purple-500 to-indigo-600 text-white font-semibold shadow hover:opacity-90 transition">
              Register
            </router-link>
          </template>
          
          <template v-else>
            <!-- Logged in - show profile dropdown -->
            <div class="relative">
              <button @click="goToProfile()" class="flex items-center space-x-2 focus:outline-none">
                <!-- Circle avatar with first letter of nickname -->
                <div class="w-8 h-8 rounded-full bg-purple-600 flex items-center justify-center text-white font-bold">
                  {{ userNickname?.charAt(0).toUpperCase() }}
                </div>
                <span class="text-sm text-gray-300">{{ userNickname }}</span>
              </button>
            </div>
          </template>
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

      <!-- Auth buttons for mobile - Dynamic -->
      <div>
        <template v-if="!isLoggedIn">
          <router-link :to="{ name: 'Login'}" 
            class="block py-2 border border-gray-600 rounded-md hover:bg-gray-800 mb-2">
            Sign In
          </router-link>
          <router-link :to="{ name: 'Register'}" 
            class="block py-2 bg-gradient-to-r from-purple-500 to-indigo-600 text-white rounded-md">
            Register
          </router-link>
        </template>
        
        <template v-else>
          <div class="space-y-2">
            <div v-if="userNickname" class="text-center py-2 text-gray-300 text-sm border-b border-gray-700">
              Hello, <b>{{ userNickname }}</b>
            </div>
            <button @click="handleLogout"
              class="block w-full py-2 bg-red-600 text-white rounded-md hover:bg-red-700">
              Logout
            </button>
          </div>
        </template>
      </div>
    </div>
  </header>
</template>
