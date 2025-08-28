//EmailVerificationStatus.vue
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const status = ref('') // 'success' | 'failed' | 'expired' | 'invalid'
const message = ref('')
const userEmail = ref('')
const isLoading = ref(true)

onMounted(async () => {
  await verifyEmailToken()
})

async function verifyEmailToken() {
  try {
    // Get token from URL parameters
    const token = route.query.token
    const email = route.query.email // Optional: if email is passed in URL
    
    if (!token) {
      status.value = 'invalid'
      message.value = 'Invalid verification link. No token provided.'
      isLoading.value = false
      return
    }

    // Call your backend API to verify the token
    const response = await fetch(`${import.meta.env.VITE_APP_URL2}/users/verify-email`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ 
        token: token,
        email: email // Optional
      })
    })

    const result = await response.json()

    if (response.ok) {
      status.value = 'success'
      message.value = result.message || 'Your email has been successfully verified! You can now log in to your account.'
      userEmail.value = result.email || email || ''
    } else {
      // Handle different error cases
      if (response.status === 400) {
        status.value = 'invalid'
        message.value = result.message || 'Invalid verification token.'
      } else if (response.status === 410) {
        status.value = 'expired'
        message.value = result.message || 'Verification link has expired. Please request a new one.'
      } else {
        status.value = 'failed'
        message.value = result.message || 'Email verification failed. Please try again.'
      }
    }

  } catch (error) {
    console.error('Verification error:', error)
    status.value = 'failed'
    message.value = 'An error occurred during verification. Please try again later.'
  } finally {
    isLoading.value = false
  }
}

async function resendVerificationEmail() {
  if (!userEmail.value) {
    alert('Email address not found. Please register again.')
    return
  }

  isLoading.value = true

  try {
    const response = await fetch('/api/resend-verification', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ 
        email: userEmail.value 
      })
    })

    const result = await response.json()

    if (response.ok) {
      message.value = 'A new verification email has been sent to your email address.'
    } else {
      message.value = result.message || 'Failed to resend verification email.'
    }

  } catch (error) {
    console.error('Resend error:', error)
    message.value = 'An error occurred while resending email.'
  } finally {
    isLoading.value = false
  }
}

</script>

<template>
  <div class="min-h-screen bg-gray-100 flex items-center justify-center px-4">
    <div class="max-w-md w-full bg-white rounded-lg shadow-md p-8 text-center">
      
      <!-- Loading State -->
      <div v-if="!isLoading" class="space-y-2">
        <div class="animate-spin rounded-full h-16 w-16 border-b-4 border-green-500 mx-auto"></div>
        <h1 class="text-xl font-semibold text-gray-700 leading-9 mt-4 truncate">Verifying <br> <b>{{ userEmail }}</b> </h1>
        <p class="text-gray-500">Please check your email address while we verify. </p>
      </div>

      <!-- Success State -->
      <div v-else-if="status === 'success'" class="space-y-6">
        <div class="text-green-500">
          <svg class="w-16 h-16 mx-auto" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
          </svg>
        </div>
        <h2 class="text-2xl font-bold text-gray-800">Email Verified!</h2>
        <p class="text-gray-600">{{ message }}</p>
        <div v-if="userEmail" class="text-sm text-gray-500">
          Verified email: <strong>{{ userEmail }}</strong>
        </div>
        <div class="flex flex-col space-y-3">
          <router-link 
            :to="{ name: 'Login' }"
            class="w-full bg-green-500 text-white py-3 px-4 rounded-md hover:bg-green-600 transition font-semibold"
          >
            Go to Login
          </router-link>
          <router-link 
            to="/"
            class="w-full bg-gray-300 text-gray-700 py-2 px-4 rounded-md hover:bg-gray-400 transition"
          >
            Back to Home
          </router-link>
        </div>
      </div>

      <!-- Failed State -->
      <div v-else-if="status === 'failed'" class="space-y-6">
        <div class="text-red-500">
          <svg class="w-16 h-16 mx-auto" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
          </svg>
        </div>
        <h2 class="text-2xl font-bold text-gray-800">Verification Failed</h2>
        <p class="text-gray-600">{{ message }}</p>
        <div class="flex flex-col space-y-3">
          <button 
            v-if="userEmail"
            @click="resendVerificationEmail"
            :disabled="isLoading"
            class="w-full bg-orange-500 text-white py-3 px-4 rounded-md hover:bg-orange-600 transition font-semibold disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ isLoading ? 'Sending...' : 'Resend Verification Email' }}
          </button>
          <router-link 
            :to="{ name: 'Register'}"
            class="w-full bg-blue-500 text-white py-2 px-4 rounded-md hover:bg-blue-600 transition"
          >
            Register Again
          </router-link>
          <router-link 
            to="/"
            class="w-full bg-gray-300 text-gray-700 py-2 px-4 rounded-md hover:bg-gray-400 transition"
          >
            Back to Home
          </router-link>
        </div>
      </div>

      <!-- Expired State -->
      <div v-else-if="status === 'expired'" class="space-y-6">
        <div class="text-yellow-500">
          <svg class="w-16 h-16 mx-auto" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
          </svg>
        </div>
        <h2 class="text-2xl font-bold text-gray-800">Link Expired</h2>
        <p class="text-gray-600">{{ message }}</p>
        <div class="flex flex-col space-y-3">
          <button 
            v-if="userEmail"
            @click="resendVerificationEmail"
            :disabled="isLoading"
            class="w-full bg-orange-500 text-white py-3 px-4 rounded-md hover:bg-orange-600 transition font-semibold disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ isLoading ? 'Sending...' : 'Send New Verification Email' }}
          </button>
          <router-link 
            :to="{ name: 'Register' }"
            class="w-full bg-blue-500 text-white py-2 px-4 rounded-md hover:bg-blue-600 transition"
          >
            Register Again
          </router-link>
          <router-link 
            to="/"
            class="w-full bg-gray-300 text-gray-700 py-2 px-4 rounded-md hover:bg-gray-400 transition"
          >
            Back to Home
          </router-link>
        </div>
      </div>

      <!-- Invalid State -->
      <div v-else-if="status === 'invalid'" class="space-y-6">
        <div class="text-red-500">
          <svg class="w-16 h-16 mx-auto" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
          </svg>
        </div>
        <h2 class="text-2xl font-bold text-gray-800">Invalid Link</h2>
        <p class="text-gray-600">{{ message }}</p>
        <div class="flex flex-col space-y-3">
          <router-link  
            :to="{ name: 'Register' }"
            class="w-full bg-blue-500 text-white py-3 px-4 rounded-md hover:bg-blue-600 transition font-semibold"
          >
            Register New Account
          </router-link>
          <router-link 
            to="/"
            class="w-full bg-gray-300 text-gray-700 py-2 px-4 rounded-md hover:bg-gray-400 transition"
          >
            Back to Home
          </router-link>
        </div>
      </div>

    </div>
  </div>
</template>
