//Navbar.vue
<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUser } from '@/composables/useUser'
import { ensureValidToken } from '@/lib/authUtils'

const router = useRouter()

// Get user data from the composable - no need to fetch separately
const { 
  userRole, 
  userNickname, 
  isLoggedIn, 
  isLoading,
  logout
} = useUser()

const mobileMenuOpen = ref(false)
const isLoggingOut = ref(false)

function goToProfile() {
  router.push({ name: 'Profile'})
  // Close mobile menu if open
  mobileMenuOpen.value = false
}

async function handleLogout() {
  isLoggingOut.value = true
  try {
    await logout()
    mobileMenuOpen.value = false
    router.push( { name: 'ListGallery' }).then( () => router.go(0))
  } catch (error) {
    console.error('Logout failed:', error)
  } finally {
    isLoggingOut.value = false
  }
}


onMounted( async() => {
  await ensureValidToken()
})

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
        <router-link 
          :to="{ name: 'ListGallery'}" 
          @click="$router.push({ name: 'ListGallery' }).then(() => $router.go(0))" 
          class="hover:text-purple-400 transition"
        >
          Shop
        </router-link>
        <router-link 
          v-if="userRole === 'SELLER'" 
          :to="{ name: 'ListSaleItems'}" 
          @click="$router.push({ name: 'ListSaleItems' }).then(() => $router.go(0))"  
          class="hover:text-purple-400 transition"
        >
          Categories
        </router-link>
        <router-link to="/contact" class="hover:text-purple-400 transition">Contact</router-link>
        <span v-if="isLoggedIn">
          <router-link :to="{ name: 'Orders'}" class="hover:text-purple-400 transition">Your Order</router-link>
        </span>

        <!-- Auth Buttons - Dynamic based on login status -->
        <div class="space-x-3">
          <!-- Show loading state while data is being fetched -->
          <template v-if="isLoading">
            <div class="px-4 py-2 text-gray-400">Loading...</div>
          </template>
          
          <template v-else-if="!isLoggedIn">
            <!-- Not logged in - show login/register -->
            <router-link :to="{ name: 'Login'}" 
              @click="ensureValidToken()"
              class="px-4 py-2 rounded-md text-gray-200 border border-gray-500 hover:bg-gray-800 transition">
              Sign In
            </router-link>
            <router-link :to="{ name: 'Register'}" 
              class="itbms-register-button px-4 py-2 rounded-md bg-gradient-to-r from-purple-500 to-indigo-600 text-white font-semibold shadow hover:opacity-90 transition">
              Register
            </router-link>
          </template>
          
          <template v-else>
            <!-- Logged in - show user info and logout -->
            <div class="flex items-center space-x-3">
              <!-- User Profile Button -->
              <button @click="goToProfile()" class="flex items-center space-x-2 focus:outline-none hover:bg-gray-800 px-2 py-1 rounded-md transition">
                <!-- Circle avatar with first letter of nickname -->
                <div class="w-8 h-8 rounded-full bg-purple-600 flex items-center justify-center text-white font-bold">
                  {{ userNickname?.charAt(0).toUpperCase() || 'U' }}
                </div>
                <span class="text-sm text-gray-300">{{ userNickname || 'User' }}</span>
              </button>
              
              <!-- Logout Button -->
              <button 
                @click="handleLogout"
                :disabled="isLoggingOut"
                class="px-4 py-2 rounded-md text-gray-200 bg-red-500 hover:bg-red-700 transition
                       disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <span v-if="!isLoggingOut">Logout</span>
                <span v-else>Logging out...</span>
              </button>
            </div>
          </template>
        </div>
      </div>

      <!-- Mobile Menu Button -->
      <button class="md:hidden text-gray-200" @click="mobileMenuOpen = !mobileMenuOpen">
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path>
        </svg>
      </button>
    </nav>

    <!-- Mobile dropdown -->
    <div v-if="mobileMenuOpen" class="md:hidden px-4 py-3 bg-gray-900 shadow text-center space-y-2 border-t border-gray-700">
      <router-link to="/" @click="mobileMenuOpen = false" class="block py-2 hover:text-purple-400">Home</router-link>
      <router-link :to="{ name: 'ListGallery'}" @click="mobileMenuOpen = false; $router.go(0)" class="block py-2 hover:text-purple-400">Shop</router-link>
      <router-link v-if="userRole === 'SELLER'" :to="{ name: 'ListSaleItems'}" @click="mobileMenuOpen = false; $router.go(0)" class="block py-2 hover:text-purple-400">Categories</router-link>
      <router-link to="/contact" @click="mobileMenuOpen = false" class="block py-2 hover:text-purple-400">Contact</router-link>
      <span v-if="isLoggedIn">
        <router-link :to="{ name: 'Orders'}" @click="mobileMenuOpen = false" class="block py-2 hover:text-purple-400">Your Order</router-link>
      </span>

      <!-- Auth buttons for mobile - Dynamic -->
      <div class="pt-2 border-t border-gray-700 space-y-2">
        <!-- Loading state -->
        <template v-if="isLoading">
          <div class="py-2 text-gray-400">Loading...</div>
        </template>
        
        <template v-else-if="!isLoggedIn">
          <router-link :to="{ name: 'Login'}" 
            @click="mobileMenuOpen = false; ensureValidToken()"
            class="block py-2 border border-gray-600 rounded-md hover:bg-gray-800">
            Sign In
          </router-link>
          <router-link :to="{ name: 'Register'}" 
            @click="mobileMenuOpen = false"
            class="block py-2 bg-gradient-to-r from-purple-500 to-indigo-600 text-white rounded-md">
            Register
          </router-link>
        </template>
        
        <template v-else>
          <!-- Logged in - show user info and logout -->
          <div class="space-y-2">
            <!-- User Profile -->
            <button @click="goToProfile()" class="flex items-center justify-center space-x-2 w-full py-2 hover:bg-gray-800 rounded-md transition">
              <div class="w-6 h-6 rounded-full bg-purple-600 flex items-center justify-center text-white font-bold text-sm">
                {{ userNickname?.charAt(0).toUpperCase() || 'U' }}
              </div>
              <span class="text-sm text-gray-300">{{ userNickname || 'User' }}</span>
            </button>
            
            <!-- Mobile Logout Button -->
            <button 
              @click="handleLogout"
              :disabled="isLoggingOut"
              class="block w-full py-2 bg-red-500 rounded-md hover:bg-red-700 text-gray-200
                     disabled:opacity-50 disabled:cursor-not-allowed transition"
            >
              <span v-if="!isLoggingOut">Logout</span>
              <span v-else>Logging out...</span>
            </button>
          </div>
        </template>
      </div>
    </div>
  </header>
</template>
