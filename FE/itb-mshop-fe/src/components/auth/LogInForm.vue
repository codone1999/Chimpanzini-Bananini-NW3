// LogInForm.vue
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { validateEmail, validatePassword, isLoginFormValid } from '@/lib/validateInput'
import { setAuthTokens, clearAuthTokens, isAuthenticated, ensureValidToken, isTokenExpired } from '@/lib/authUtils'
import { useUser } from '@/composables/useUser'

const router = useRouter()

const { userRole, loadCompleteUserData } = useUser()

// form state
const form = ref({
  email: '',
  password: ''
})

// states
const errorMessage = ref('')
const successMessage = ref('')
const isRedirecting = ref(false)
const showPassword = ref(false)

// Individual field validation messages
const fieldErrors = ref({
  email: '',
  password: ''
})

// Handle individual field validation on blur
function handleEmailBlur() {
  const error = validateEmail(form.value.email)
  fieldErrors.value.email = error || ''
}

function handlePasswordBlur() {
  const error = validatePassword(form.value.password)
  fieldErrors.value.password = error || ''
}

// Clear field error when user starts typing
function handleEmailInput() {
  if (fieldErrors.value.email) {
    fieldErrors.value.email = ''
  }
}

function handlePasswordInput() {
  if (fieldErrors.value.password) {
    fieldErrors.value.password = ''
  }
}

// Computed property to check if form is valid
const isFormValid = computed(() => {
  return isLoginFormValid(form.value)
})

// Check if button should be disabled
const isButtonDisabled = computed(() => {
  return !isFormValid.value || isRedirecting.value
})

// ---------------- Handlers ---------------- //
function resetForm() {
  form.value = { email: '', password: '' }
  errorMessage.value = ''
  successMessage.value = ''
  isRedirecting.value = false
  fieldErrors.value = { email: '', password: '' }
}

async function handleSubmit() {
  if (isRedirecting.value) return

  // Check if user is already logged in using AuthUtils
  if (isAuthenticated()) {
    successMessage.value = 'You are already logged in!'
    isRedirecting.value = true
    setTimeout(() => {
      router.push({ name: 'ListGallery' }).then( () => router.go(0))
    }, 1500)
    return
  }

  errorMessage.value = ''
  successMessage.value = ''

  // Validate all fields before submit
  const emailError = validateEmail(form.value.email)
  const passwordError = validatePassword(form.value.password)
  
  fieldErrors.value.email = emailError || ''
  fieldErrors.value.password = passwordError || ''

  if (emailError || passwordError) {
    return
  }

  try {
    // Call your login API
    const response = await fetch(`${import.meta.env.VITE_APP_URL2}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email: form.value.email,
        password: form.value.password
      })
    })

    const result = await response.json()

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    // Store tokens using AuthUtils
    setAuthTokens(result.access_token, result.refresh_token)

    await loadCompleteUserData()

    successMessage.value = 'Login successful! Redirecting...'
    isRedirecting.value = true

    if (userRole.value === "SELLER"){
      setTimeout(() => {
        router.push({ name: 'ListSaleItems' })
      }, 1500)
    } else {
      setTimeout(() => {
        resetForm()
        router.push({name: 'ListGallery'}).then( () => router.go(0) )
      }, 1500)
    }

  } catch (error) {
    console.error('Login failed:', error)
    
    // Clear any existing tokens on login failure
    clearAuthTokens()

    let displayMessage = 'Login failed. Please try again.'

    if (error.message.includes('401') || error.message.includes('400')) {
      displayMessage = 'Email or Password is incorrect.'
    } else if (error.message.includes('403')) {
      displayMessage = 'You need to activate your account before signing in.'
    } else {
      displayMessage = 'There is a problem. Please try again later.'
    }

    errorMessage.value = displayMessage
  }
}

onMounted(async () => {
  if(!isTokenExpired()){
    if (userRole.value === "BUYER")
      router.push({ name: 'ListGallery'})
    else 
      router.push({ name: 'ListSaleItems'})
  } else {
    await ensureValidToken()
  }
})
</script>

<template>
  <div class="bg-gray-900 text-gray-100 font-sans flex items-center justify-center px-4 py-12">
    <div class="w-full max-w-lg">
      <!-- Card -->
      <div class="bg-gray-800 border border-gray-700 rounded-2xl shadow-2xl px-8 py-7">
        <!-- Header -->
        <div class="text-center mb-8">
          <h2 class="text-3xl font-extrabold bg-gradient-to-r from-purple-400 to-indigo-500 bg-clip-text text-transparent mb-3">
            Welcome Back
          </h2>
          <p class="text-gray-400 text-sm">
            Sign in to your account
          </p>
        </div>

        <!-- Messages -->
        <transition name="fade">
          <div v-if="successMessage" class="mb-6 p-4 bg-emerald-900/30 border border-emerald-700 text-emerald-300 rounded-lg text-sm">
            {{ successMessage }}
          </div>
        </transition>
        <transition name="fade">
          <div v-if="errorMessage" class="mb-6 p-4 bg-red-900/30 border border-red-700 text-red-300 rounded-lg text-sm">
            {{ errorMessage }}
          </div>
        </transition>

        <!-- Form -->
        <form @submit.prevent="handleSubmit" class="space-y-4">
          <!-- Email Field -->
          <div>
            <label class="block text-sm font-medium text-purple-300 mb-2">
              Email <span class="text-red-400">*</span>
            </label>
            <input
              v-model="form.email"
              type="email"
              required
              maxlength="50"
              :disabled="isRedirecting"
              @blur="handleEmailBlur"
              @input="handleEmailInput"
              :class="[
                'itbms-email',
                'w-full px-4 py-3 bg-gray-700 border rounded-lg text-gray-100 placeholder-gray-500 focus:outline-none focus:ring-2 transition disabled:bg-gray-600 disabled:cursor-not-allowed',
                fieldErrors.email ? 'border-red-500 focus:border-red-500 focus:ring-red-500/20' : 'border-gray-600 focus:border-purple-500 focus:ring-purple-500/20'
              ]"
              placeholder="Enter your email"
            />
            <p v-if="fieldErrors.email" class="text-sm text-red-400 mt-1">
              {{ fieldErrors.email }}
            </p>
          </div>

          <!-- Password Field -->
          <div>
            <label class="block text-sm font-medium text-purple-300 mb-2">
              Password <span class="text-red-400">*</span>
            </label>
            <div class="relative">
              <input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                required
                maxlength="14"
                :disabled="isRedirecting"
                @blur="handlePasswordBlur"
                @input="handlePasswordInput"
                :class="[
                  'itbms-password',
                  'w-full px-4 py-3 bg-gray-700 border rounded-lg text-gray-100 placeholder-gray-500 focus:outline-none focus:ring-2 transition disabled:bg-gray-600 disabled:cursor-not-allowed',
                  fieldErrors.password ? 'border-red-500 focus:border-red-500 focus:ring-red-500/20' : 'border-gray-600 focus:border-purple-500 focus:ring-purple-500/20'
                ]"
                placeholder="Enter your password"
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-300 transition"
                :disabled="isRedirecting"
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
            <p v-if="fieldErrors.password" class="text-sm text-red-400 mt-1">
              {{ fieldErrors.password }}
            </p>
          </div>

          <!-- Submit Button -->
          <div class="pt-2">
            <button
              type="submit"
              :disabled="isButtonDisabled"
              :class="[
                'itbms-signin-button w-full py-3 rounded-lg font-medium transition-all shadow-md',
                isButtonDisabled
                  ? 'bg-gray-600 text-gray-400 cursor-not-allowed'
                  : 'bg-purple-600 text-white hover:bg-purple-500 hover:shadow-lg'
              ]"
            >
              {{ isRedirecting ? 'Redirecting...' : 'Sign In' }}
            </button>
          </div>

          <!-- Sign Up Link -->
          <div class="pt-4 border-t border-gray-700 text-center">
            <p class="text-sm text-gray-400">
              Don't have an account?
              <router-link
                :to="{ name: 'Register' }"
                class="text-purple-400 hover:text-purple-300 hover:underline font-medium transition ml-1"
              >
                Sign up
              </router-link>
            </p>
          </div>

          <!-- Forgot Password Link -->
          <div class="text-center">
            <router-link
              :to="{ name: 'ResetEmail' }"
              class="text-sm text-purple-400 hover:text-purple-300 hover:underline transition"
            >
              Forgot password?
            </router-link>
          </div>
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
