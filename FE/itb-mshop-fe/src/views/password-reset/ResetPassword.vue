// ResetPassword.vue
<script setup>
import { addItem } from '@/lib/fetchUtils'
import { validateRegistrationPassword } from '@/lib/validateInput'
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const email = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const error = ref('')
const isLoading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)

// Get email from sessionStorage when component mounts
onMounted(() => {
  const storedEmail = sessionStorage.getItem('reset_email')
  
  email.value = storedEmail  || ''
  
  // If no email found, redirect back
  if (!email.value) {
    error.value = 'Session expired. Please start the reset process again.'
    setTimeout(() => {
      router.push({ name: 'ResetEmail' })
    }, 2000)
  }
})

// Password validation using the custom validator
const passwordValidation = computed(() => {
  if (!newPassword.value) return null
  return validateRegistrationPassword(newPassword.value)
})

const isPasswordValid = computed(() => {
  return newPassword.value && passwordValidation.value === null
})

// Password strength indicator
const passwordStrength = computed(() => {
  const password = newPassword.value
  if (!password) return { level: 0, text: '', color: '' }
  
  let strength = 0
  if (password.length >= 8) strength++
  if (password.length >= 12) strength++
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength++
  if (/[0-9]/.test(password)) strength++
  if (/[@$!%*?&/\\#]/.test(password)) strength++
  
  if (strength <= 2) return { level: strength, text: 'Weak', color: 'text-red-400' }
  if (strength <= 3) return { level: strength, text: 'Medium', color: 'text-amber-400' }
  return { level: strength, text: 'Strong', color: 'text-emerald-400' }
})

const passwordsMatch = computed(() => {
  return confirmPassword.value && newPassword.value === confirmPassword.value
})

async function handleResetPassword() {
  error.value = ''
  
  // Validation
  if (!newPassword.value || !confirmPassword.value) {
    error.value = 'Please fill in all fields'
    return
  }
  
  // Use the validation function
  const passwordError = validateRegistrationPassword(newPassword.value)
  if (passwordError) {
    error.value = passwordError
    return
  }
  
  if (newPassword.value !== confirmPassword.value) {
    error.value = 'Passwords do not match'
    return
  }
  
  isLoading.value = true
  
  try {
    const data = {
      email: email.value,
      newPassword: newPassword.value
    }

    const response = await fetch(`${import.meta.env.VITE_APP_URL2}/auth/reset-password`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    })
    
    if (response.ok || response.status === 200) {
      // Clear the stored data
      sessionStorage.removeItem('reset_email')
      
      router.push({ name: 'Login' })
    } else {
      error.value = 'Failed to reset password. Please try again.'
    }
  } catch (err) {
    error.value = 'An error occurred. Please try again.'
    console.error(err)
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
        <!-- Back Button - Top Left -->
        <div class="mb-6">
          <router-link
            :to="{ name: 'ResetEmail' }"
            class="inline-flex items-center text-sm text-purple-400 hover:text-purple-300 transition group"
          >
            <svg class="w-4 h-4 mr-1 group-hover:-translate-x-0.5 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
            Back To Sign-In
          </router-link>
        </div>

        <!-- Header -->
        <div class="text-center mb-8">
          <h2 class="text-3xl font-extrabold bg-gradient-to-r from-purple-400 to-indigo-500 bg-clip-text text-transparent mb-3">
            Reset Password
          </h2>
          <p class="text-gray-400 text-sm">
            Enter your new password for
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
        <form @submit.prevent="handleResetPassword" class="space-y-6">
          <!-- New Password -->
          <div>
            <label for="newPassword" class="block text-sm font-medium text-purple-300 mb-2">
              New Password
            </label>
            <div class="relative">
              <input
                id="newPassword"
                v-model="newPassword"
                :type="showPassword ? 'text' : 'password'"
                placeholder="Enter new password"
                class="w-full px-4 py-3 bg-gray-700 border border-gray-600 rounded-lg text-gray-100 placeholder-gray-500 focus:outline-none focus:border-purple-500 focus:ring-2 focus:ring-purple-500/20 transition"
                required
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-300 transition"
              >
                <svg v-if="!showPassword" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
                <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                </svg>
              </button>
            </div>
            
            <!-- Password Strength Indicator -->
            <div v-if="newPassword" class="mt-2 flex items-center gap-2">
              <div class="flex-1 h-1.5 bg-gray-700 rounded-full overflow-hidden">
                <div 
                  :class="[
                    'h-full transition-all duration-300',
                    passwordStrength.level <= 2 ? 'bg-red-500' : '',
                    passwordStrength.level === 3 ? 'bg-amber-500' : '',
                    passwordStrength.level >= 4 ? 'bg-emerald-500' : ''
                  ]"
                  :style="{ width: (passwordStrength.level / 5 * 100) + '%' }"
                ></div>
              </div>
              <span :class="['text-xs font-medium', passwordStrength.color]">
                {{ passwordStrength.text }}
              </span>
            </div>

            <!-- Password Validation Error -->
            <div v-if="newPassword && passwordValidation" class="mt-2 text-xs text-red-400">
              {{ passwordValidation }}
            </div>
          </div>

          <!-- Confirm Password -->
          <div>
            <label for="confirmPassword" class="block text-sm font-medium text-purple-300 mb-2">
              Confirm Password
            </label>
            <div class="relative">
              <input
                id="confirmPassword"
                v-model="confirmPassword"
                :type="showConfirmPassword ? 'text' : 'password'"
                placeholder="Confirm new password"
                class="w-full px-4 py-3 bg-gray-700 border border-gray-600 rounded-lg text-gray-100 placeholder-gray-500 focus:outline-none focus:border-purple-500 focus:ring-2 focus:ring-purple-500/20 transition"
                required
              />
              <button
                type="button"
                @click="showConfirmPassword = !showConfirmPassword"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-300 transition"
              >
                <svg v-if="!showConfirmPassword" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
                <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                </svg>
              </button>
            </div>
            
            <!-- Match Indicator -->
            <div v-if="confirmPassword" class="mt-2 flex items-center gap-2 text-xs">
              <svg v-if="passwordsMatch" class="w-4 h-4 text-emerald-400" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
              </svg>
              <svg v-else class="w-4 h-4 text-red-400" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
              </svg>
              <span :class="passwordsMatch ? 'text-emerald-400' : 'text-red-400'">
                {{ passwordsMatch ? 'Passwords match' : 'Passwords do not match' }}
              </span>
            </div>
          </div>

          <!-- Password Requirements -->
          <div class="bg-gray-700/50 rounded-lg p-4 text-xs space-y-1 text-gray-400">
            <p class="font-medium text-gray-300 mb-2">Password must contain:</p>
            <p :class="newPassword.length >= 8 && newPassword.length <= 14 ? 'text-emerald-400' : ''">
              • 8-14 characters
            </p>
            <p :class="/[A-Z]/.test(newPassword) && /[a-z]/.test(newPassword) ? 'text-emerald-400' : ''">
              • Upper and lowercase letters
            </p>
            <p :class="/[0-9]/.test(newPassword) ? 'text-emerald-400' : ''">
              • At least one number
            </p>
            <p :class="/[@$!%*?&/\\#]/.test(newPassword) ? 'text-emerald-400' : ''">
              • Special character (@$!%*?&/\#)
            </p>
          </div>

          <!-- Submit Button -->
           <button
            type="submit"
            :disabled="isLoading || !passwordsMatch || !isPasswordValid || !email"
            class="w-full bg-purple-600 hover:bg-purple-500 disabled:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed text-white px-6 py-3 rounded-lg font-medium transition shadow-md hover:shadow-lg"
          >
            <span v-if="isLoading">Resetting Password...</span>
            <span v-else>Reset Password</span>
          </button>
        </form>
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
