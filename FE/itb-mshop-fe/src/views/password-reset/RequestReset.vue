// RequestReset.vue
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const email = ref('')
const error = ref('')
const isLoading = ref(false)
const isEmailSend = ref(false)

async function handleSendCode() {
  if (!email.value) {
    error.value = 'Please enter your email address'
    return
  }
  
  // Basic email validation
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(email.value)) {
    error.value = 'Please enter a valid email address'
    return
  }
  
  error.value = ''
  isLoading.value = true
  
  try {
    const response = await fetch(`${import.meta.env.VITE_APP_URL2}/auth/forgot-password`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ email: email.value })
    })
    
    if (response.ok || response.status === 200) {
      
      isEmailSend.value = true
      // Store email in sessionStorage for the reset flow
      sessionStorage.setItem('reset_email', email.value)
      
      // Redirect to verify code page after 2 seconds
      setTimeout(() => {
        router.push({ name: 'ResetCode' })
      }, 2000)
    } else {
      const errorData = await response.json().catch(() => ({}))
      error.value = errorData.message || 'Email not found or failed to send reset code'
    }
  } catch (err) {
    error.value = 'Failed to send reset code. Please try again.'
    console.error('Fetch error:', err)
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-900 text-gray-100 font-sans flex items-center justify-center px-4">
    <div class="w-full max-w-md">
      <!-- Card -->
      <div class="bg-gray-800 border border-gray-700 rounded-2xl shadow-2xl px-8 py-10">
        <!-- Back to Login - Top Left -->
        <div class="mb-6">
          <router-link
            :to="{ name: 'Login' }"
            class="inline-flex items-center text-sm text-purple-400 hover:text-purple-300 transition group"
          >
            <svg class="w-4 h-4 mr-1 group-hover:-translate-x-0.5 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
            Back
          </router-link>
        </div>

        <!-- Success State -->
        <div v-if="isEmailSend" class="text-center py-8">
          <div class="mb-6 flex justify-center">
            <div class="w-16 h-16 bg-emerald-900/40 rounded-full flex items-center justify-center border-2 border-emerald-500">
              <svg class="w-8 h-8 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 19v-8.93a2 2 0 01.89-1.664l7-4.666a2 2 0 012.22 0l7 4.666A2 2 0 0121 10.07V19M3 19a2 2 0 002 2h14a2 2 0 002-2M3 19l6.75-4.5M21 19l-6.75-4.5M3 10l6.75 4.5M21 10l-6.75 4.5m0 0l-1.14.76a2 2 0 01-2.22 0l-1.14-.76" />
              </svg>
            </div>
          </div>
          <h2 class="text-2xl font-bold text-gray-100 mb-3">Email Sent!</h2>
          <p class="text-gray-400 mb-2">
            We've sent a verification code to
          </p>
          <p class="text-purple-400 font-medium mb-6">
            {{ email }}
          </p>
          <p class="text-sm text-gray-500">
            Redirecting to verification page...
          </p>
        </div>

        <!-- Form State -->
        <div v-else>
          <!-- Header -->
          <div class="text-center mb-8">
            <h2 class="text-3xl font-extrabold bg-gradient-to-r from-purple-400 to-indigo-500 bg-clip-text text-transparent mb-3">
              Forgot Password?
            </h2>
            <p class="text-gray-400 text-sm">
              Enter your email address and we'll send you a code to reset your password
            </p>
          </div>

          <!-- Error Message -->
          <transition name="fade">
            <div 
              v-if="error" 
              class="mb-6 p-4 text-red-300 bg-red-900/30 border border-red-700 rounded-lg text-sm"
            >
              {{ error }}
            </div>
          </transition>

          <!-- Form -->
          <form @submit.prevent="handleSendCode" class="space-y-6">
            <!-- Email Input -->
            <div>
              <label for="email" class="block text-sm font-medium text-purple-300 mb-2">
                Email Address
              </label>
              <input
                id="email"
                v-model="email"
                type="email"
                placeholder="your.email@example.com"
                class="w-full px-4 py-3 bg-gray-700 border border-gray-600 rounded-lg text-gray-100 placeholder-gray-500 focus:outline-none focus:border-purple-500 focus:ring-2 focus:ring-purple-500/20 transition"
                required
              />
            </div>

            <!-- Submit Button -->
            <button
              type="submit"
              :disabled="isLoading"
              class="w-full bg-purple-600 hover:bg-purple-500 disabled:bg-purple-700 disabled:cursor-not-allowed text-white px-6 py-3 rounded-lg font-medium transition shadow-md hover:shadow-lg"
            >
              <span v-if="isLoading">Sending Code...</span>
              <span v-else>Send Reset Code</span>
            </button>
          </form>

          <!-- Additional Info -->
          <div class="mt-6 pt-6 border-t border-gray-700 text-center">
            <p class="text-sm text-gray-400">
              Remember your password?
              <router-link
                :to="{ name: 'Login' }"
                class="text-purple-400 hover:text-purple-300 hover:underline font-medium transition ml-1"
              >
                Sign in
              </router-link>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
