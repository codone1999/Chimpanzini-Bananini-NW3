<script setup>
import { ref } from 'vue'
import { loginAccount } from '@/lib/fetchUtils'

// form state
const form = ref({
  email: '',
  password: ''
})

// states
const isLoading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// ---------------- Validate ---------------- //
function isValidEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email.trim())
}

function validatePassword(password) {
  return password.length >= 8
}

// ---------------- Handlers ---------------- //
function resetForm() {
  form.value = { email: '', password: '' }
  errorMessage.value = ''
  successMessage.value = ''
}

async function handleSubmit() {
  if (isLoading.value) return

  errorMessage.value = ''
  successMessage.value = ''

  if (!isValidEmail(form.value.email)) {
    errorMessage.value = 'Please enter a valid email address'
    return
  }

  if (!validatePassword(form.value.password)) {
    errorMessage.value = 'Password must be at least 8 characters long'
    return
  }

  isLoading.value = true

  try {
    // console.log('Logging in:', form.value.email)

    const result = await loginAccount(
      `${import.meta.env.VITE_APP_URL2}/users/login`,
      form.value
    )

    successMessage.value = 'Login successful! Redirecting...'

    // Example redirect simulation
    setTimeout(() => {
      resetForm()
      // e.g., use router.push('/dashboard')
    }, 1500)
  } catch (error) {
    console.error('Login failed:', error)

    let displayMessage = 'Login failed. Please try again.'

    if (error.message.includes('401')) {
      displayMessage = 'Invalid email or password.'
    } else if (error.message.includes('500')) {
      displayMessage = 'Server error. Please try again later.'
    } else {
      displayMessage = error.message
    }

    errorMessage.value = displayMessage
  } finally {
    isLoading.value = false
  }
}

function handleCancel() {
  resetForm()
}
</script>

<template>
  <div class="max-w-md mx-auto bg-white shadow-md rounded-lg p-6 my-20">
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
          :disabled="isLoading"
          class="mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100"
        />
      </div>

      <div>
        <label class="block text-sm font-medium">Password *</label>
        <input
          v-model="form.password"
          type="password"
          required
          minlength="8"
          :disabled="isLoading"
          class="mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100"
        />
      </div>

      <!-- Buttons -->
      <div class="flex gap-2 pt-4">
        <button
          type="submit"
          :disabled="isLoading"
          :class="[
            'flex-1 py-2 rounded-md transition-colors',
            isLoading
              ? 'bg-red-300 text-gray-600 cursor-not-allowed'
              : 'bg-green-500 text-white hover:bg-green-600'
          ]"
        >
          {{ isLoading ? 'Logging in...' : 'Login' }}
        </button>
        <router-link
          to="/"
          @click="handleCancel"
          :disabled="isLoading"
          class="flex-1 text-center bg-gray-300 text-gray-700 py-2 rounded-md hover:bg-gray-400 disabled:bg-gray-200"
        >
          Cancel
        </router-link>
      </div>
    </form>
  </div>
</template>
