// VerifyResetCode.vue
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const code = ref('')
const email = ref('')
const error = ref('')
const isLoading = ref(false)

// Get email from sessionStorage when component mounts
onMounted(() => {
  const storedEmail = sessionStorage.getItem('reset_email')
  
  email.value = storedEmail || ''
  
  // If no email found, redirect back to request reset page
  if (!email.value) {
    error.value = 'Session expired. Please request a new reset code.'
    setTimeout(() => {
      router.push({ name: 'ResetEmail' })
    }, 2000)
  }
})

async function handleVerifyCode() {
  if (!code.value) {
    error.value = 'Please enter the verification code'
    return
  }
  
  if (code.value.length !== 4) {
    error.value = 'Code must be 4 digits'
    return
  }
  
  error.value = ''
  isLoading.value = true
  

  try {
    const data = {
      email: email.value,
      code: code.value,
    }
    
    const response = await fetch(`${import.meta.env.VITE_APP_URL2}/auth/verify-reset-code`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    })

    if (response.ok || response.status === 200) {
      // Pass to the reset password page
      router.push({ 
        name: 'ResetPassword',
        state: { email: email.value, code: code.value }
      })
    } else {
      error.value = 'Invalid or expired code'
    }
  } catch (err) {
    error.value = 'Failed to verify code. Please try again.'
    console.error(err)
  } finally {
    isLoading.value = false
  }
}

async function handleResendCode() {
  if (!email.value) {
    error.value = 'Email not found. Please start over.'
    return
  }
  
  isLoading.value = true
  error.value = ''
  
  try {
    const response = await fetch(`${import.meta.env.VITE_APP_URL2}/auth/forgot-password`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ email: email.value })
    })
    
    if (response.ok || response.status === 200) {
      error.value = '' // Clear any errors
      // Show success message (you could add a success state variable)
      alert('New code sent to your email!')
    } else {
      const errorData = await response.json().catch(() => ({}))
      error.value = errorData.message || 'Failed to resend code'
    }
  } catch (err) {
    error.value = 'Failed to resend code. Please try again.'
    console.error('Fetch error:', err)
  } finally {
    isLoading.value = false
  }
}

function handleInput(e) {
  // Only allow numbers
  code.value = e.target.value.replace(/[^0-9]/g, '')
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
            :to="{ name: 'ResetEmail' }"
            class="inline-flex items-center text-sm text-purple-400 hover:text-purple-300 transition group"
          >
            <svg class="w-4 h-4 mr-1 group-hover:-translate-x-0.5 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
            Back
          </router-link>
        </div>

        <!-- Header -->
        <div class="text-center mb-8">
          <h2 class="text-3xl font-extrabold bg-gradient-to-r from-purple-400 to-indigo-500 bg-clip-text text-transparent mb-3">
            Verify Reset Code
          </h2>
          <p class="text-gray-400 text-sm">
            Enter the 4-digit code sent to
          </p>
          <p class="text-purple-400 text-sm font-medium mt-1">
            {{ email }}
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
        <form @submit.prevent="handleVerifyCode" class="space-y-6">
          <!-- Code Input -->
          <div>
            <label for="code" class="block text-sm font-medium text-purple-300 mb-2">
              Verification Code
            </label>
            <input
              id="code"
              v-model="code"
              @input="handleInput"
              type="text"
              maxlength="4"
              placeholder="0000"
              class="w-full px-4 py-3 bg-gray-700 border border-gray-600 rounded-lg text-gray-100 placeholder-gray-500 focus:outline-none focus:border-purple-500 focus:ring-2 focus:ring-purple-500/20 transition text-center text-2xl tracking-widest font-mono"
              required
            />
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            :disabled="isLoading || !email"
            class="w-full bg-purple-600 hover:bg-purple-500 disabled:bg-purple-700 disabled:cursor-not-allowed text-white px-6 py-3 rounded-lg font-medium transition shadow-md hover:shadow-lg"
          >
            <span v-if="isLoading">Verifying...</span>
            <span v-else>Verify Code</span>
          </button>
        </form>

        <!-- Resend Code -->
        <div class="mt-6 pt-6 border-t border-gray-700 text-center">
          <p class="text-sm text-gray-400 mb-2">
            Didn't receive the code?
          </p>
          <button
            @click="handleResendCode"
            :disabled="isLoading"
            class="text-sm text-purple-400 hover:text-purple-300 hover:underline font-medium transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Resend Code
          </button>
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
