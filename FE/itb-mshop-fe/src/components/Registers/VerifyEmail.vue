<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const errMessage = ref('')
const status = ref(500)

onMounted(async () => {
  const token = route.query.token
  
  let res
  try {
    if (!token) throw new Error('Token is missing')
    
    const response = await fetch(`${import.meta.env.VITE_APP_URL2}/users/verify-email?token=${encodeURIComponent(token)}`, {
      method: "POST",
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    const result = await response.json()
    status.value = response.status
  } catch (error) {
    console.error('Email verification error:', error)
  }
  
  // Handle response
  if (status.value === 200) {
    // Success - email verified
    console.log('Email verification successful')
  } else if (status.value === 400) {
    errMessage.value = 'Invalid verification token.'
  } else if (status.value === 409) {
    errMessage.value = 'Email already verified.'
  } else {
    errMessage.value = res.result?.message || 'Something went wrong.'
  }

})
</script>

<template>
  <div class="min-h-screen bg-gray-100 flex items-center justify-center px-4">
    <div class="max-w-md w-full bg-white rounded-lg shadow-md p-8 text-center">
      <!-- Success State (when no error message) -->
      <div v-if="!errMessage" class="space-y-6">
        <div class="text-green-500">
          <svg class="w-16 h-16 mx-auto" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
          </svg>
        </div>
        <h2 class="text-2xl font-bold text-gray-800">Email Verified!</h2>
        <p class="text-gray-600">Your email has been successfully verified! Redirecting you shortly...</p>
      </div>

      <!-- Error State -->
      <div v-else class="space-y-6">
        <div class="text-red-500">
          <svg class="w-16 h-16 mx-auto" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
          </svg>
        </div>
        <h2 class="text-2xl font-bold text-gray-800">Verification Failed</h2>
        <p class="text-gray-600">{{ errMessage }}</p>
        <p class="text-sm text-gray-500">You will be redirected shortly...</p>
      </div>
    </div>
  </div>
</template>
