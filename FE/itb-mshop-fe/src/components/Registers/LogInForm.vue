//LoginForm.vue
<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { validateEmail, validatePassword, isLoginFormValid } from '@/lib/validateInput'

const router = useRouter()

// form state
const form = ref({
  email: '',
  password: ''
})

// states
const errorMessage = ref('')
const successMessage = ref('')
const isRedirecting = ref(false)

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

  // Check if user is already logged in
  const existingToken = localStorage.getItem('access_token')
  if (existingToken) {
    successMessage.value = 'You are already logged in!'
    isRedirecting.value = true
    setTimeout(() => {
      router.push({ name: 'ListGallery' })
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
    const response = await fetch(`${import.meta.env.VITE_APP_URL2}/users/authentications`, {
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
    
    // Store tokens in localStorage
    localStorage.setItem('access_token', result.access_token)
    localStorage.setItem('refresh_token', result.refresh_token)

    successMessage.value = 'Login successful! Redirecting...'
    isRedirecting.value = true

    setTimeout(() => {
      resetForm()
      router.push({name: 'ListGallery'}).then( () => router.go(0) )
    }, 1500)

  } catch (error) {
    console.error('Login failed:', error)

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
</script>

<template>
  <div class="max-w-md mx-auto bg-white shadow-md rounded-lg p-6 my-20">
    <!-- Header -->
    <div class="text-center mb-6">
      <h1 class="text-3xl font-bold bg-gradient-to-r from-purple-600 to-indigo-600 bg-clip-text text-transparent mb-2">
        Welcome To ITB-MShop
      </h1>
      <p class="text-gray-600 text-sm">Sign in to your account</p>
    </div>
    
    <!-- Success Message -->
    <div v-if="successMessage" class="mb-4 p-3 bg-green-100 border border-green-400 text-green-700 rounded">
      {{ successMessage }}
    </div>

    <!-- Error Message -->
    <div v-if="errorMessage" class="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
      {{ errorMessage }}
    </div>

    <form @submit.prevent="handleSubmit" class="space-y-4">
      <div>
        <label class="block text-sm font-medium">Email *</label>
        <input
          v-model="form.email"
          type="email"
          required
          maxlength="50"
          :disabled="isRedirecting"
          @blur="handleEmailBlur"
          @input="handleEmailInput"
          :class="[
            'mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100',
            fieldErrors.email ? 'border-red-500' : 'border-gray-300'
          ]"
          placeholder="Enter your email address"
        />
        <p v-if="fieldErrors.email" class="text-sm text-red-600 mt-1">
          {{ fieldErrors.email }}
        </p>
      </div>

      <div>
        <label class="block text-sm font-medium">Password *</label>
        <input
          v-model="form.password"
          type="password"
          required
          maxlength="14"
          :disabled="isRedirecting"
          @blur="handlePasswordBlur"
          @input="handlePasswordInput"
          :class="[
            'mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100',
            fieldErrors.password ? 'border-red-500' : 'border-gray-300'
          ]"
          placeholder="Enter your password 14 characters"
        />
        <p v-if="fieldErrors.password" class="text-sm text-red-600 mt-1">
          {{ fieldErrors.password }}
        </p>
      </div>

      <!-- Buttons -->
      <div class="pt-4">
        <button
          type="submit"
          :disabled="isButtonDisabled"
          :class="[
            'w-full py-2 rounded-md transition-colors',
            isButtonDisabled
              ? 'bg-gray-400 text-gray-600 cursor-not-allowed'
              : 'bg-green-500 text-white hover:bg-green-600'
          ]"
        >
          {{ isRedirecting ? 'Redirecting...' : 'Login' }}
        </button>
      </div>
    </form>
  </div>
</template>
