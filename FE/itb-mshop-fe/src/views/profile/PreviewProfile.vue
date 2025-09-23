//PreviewProfile.vue
<script setup>
import { computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUser } from '@/composables/useUser'

const router = useRouter()

// Get all user data from the composable
const {
  userEmail,
  userNickname,
  userFullName,
  userRole,
  userMobile,
  userBankAccountNo,
  userBankName,
  isLoggedIn,
  hasCompleteUserData,
  isLoading,
  loadCompleteUserData
} = useUser()

// Mask sensitive data for display
function maskNumber(num) {
  if (!num) return 'Not provided'

  const str = String(num)
  if (str.length <= 4) return str

  const first = 'x'.repeat(str.length - 4)
  const middleThree = str.slice(-4, -1) // last 4 digits, but drop the very last one
  const last = 'x'

  return first + middleThree + last
}

// Check authentication and redirect if needed
onMounted(() => {
  // If we don't have complete user data and not loading, try to load it
  if (!hasCompleteUserData.value && !isLoading.value) {
    loadCompleteUserData()
  }
})

// Computed properties
const isSellerRole = computed(() => {
  return userRole.value === 'SELLER'
})

const userInitial = computed(() => {
  return userNickname.value?.charAt(0)?.toUpperCase() 
         'U'
})

const showContent = computed(() => {
  return !isLoading.value && hasCompleteUserData.value
})

// Navigation
function goToEdit() {
  router.push({ 
    name: 'EditProfile',
    state: { 
      userData: {
        nickName: userNickname.value || '',
        email: userEmail.value || '',
        fullName: userFullName.value || '',
        role: userRole.value || 'BUYER',
        phoneNumber: userMobile.value || '',
        bankAccount: userBankAccountNo.value || '',
        bankName: userBankName.value || ''
      }
    }
  })
}

function retryLoad() {
  loadCompleteUserData()
}
</script>

<template>
  <div class="max-w-3xl mx-auto bg-white shadow-lg rounded-xl overflow-hidden my-12">
    
    <!-- Loading State -->
    <div v-if="isLoading" class="p-12 text-center">
      <div class="animate-spin w-12 h-12 border-4 border-purple-500 border-t-transparent rounded-full mx-auto mb-4"></div>
      <p class="text-gray-600">Loading your profile...</p>
    </div>

    <!-- Error State - when not logged in or no data -->
    <div v-else-if="!isLoggedIn" class="p-12 text-center">
      <div class="text-red-500 mb-4">
        <svg class="w-16 h-16 mx-auto" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
        </svg>
      </div>
      <h3 class="text-xl font-semibold text-gray-700 mb-2">Not Authenticated</h3>
      <p class="text-gray-600 mb-4">Please log in to view your profile.</p>
      <button 
        @click="router.push({ name: 'Login' })"
        class="px-6 py-2 bg-purple-600 text-white font-medium rounded-md shadow hover:bg-purple-700 transition"
      >
        Go to Login
      </button>
    </div>

    <!-- Data not available state -->
    <div v-else-if="!hasCompleteUserData" class="p-12 text-center">
      <div class="text-yellow-500 mb-4">
        <svg class="w-16 h-16 mx-auto" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
        </svg>
      </div>
      <h3 class="text-xl font-semibold text-gray-700 mb-2">Unable to Load Profile</h3>
      <p class="text-gray-600 mb-4">Your profile data could not be loaded. Please try again.</p>
      <button 
        @click="retryLoad"
        class="px-6 py-2 bg-purple-600 text-white font-medium rounded-md shadow hover:bg-purple-700 transition"
      >
        Try Again
      </button>
    </div>

    <!-- Profile Content -->
    <template v-else-if="showContent">
      <!-- Header -->
      <div class="bg-gradient-to-r from-purple-600 to-indigo-600 p-6 flex items-center space-x-4">
        <!-- Avatar with initial -->
        <div class="w-16 h-16 rounded-full bg-white flex items-center justify-center text-purple-600 font-bold text-2xl shadow-md">
          {{ userInitial }}
        </div>
        <div class="flex-1">
          <h2 class="text-2xl font-bold text-white">{{ userNickname || 'User' }}</h2>
          <p class="text-purple-100 text-sm">@{{ userFullName || 'No full name' }}</p>
          <div class="flex items-center mt-1">
            <span :class="[
              'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
              userRole === 'SELLER' 
                ? 'bg-yellow-100 text-yellow-800' 
                : 'bg-blue-100 text-blue-800'
            ]">
              {{ userRole === 'SELLER' ? 'Seller Account' : 'Buyer Account' }}
            </span>
          </div>
        </div>
      </div>

      <!-- Body -->
      <div class="p-6 space-y-6">
        <!-- Basic Info -->
        <div>
          <h3 class="text-lg font-semibold text-gray-700 border-b pb-2 mb-4">Account Information</h3>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-y-3 gap-x-6 text-gray-600">
            <div>
              <span class="font-medium text-gray-700">Email:</span>
              <p class="text-gray-900">{{ userEmail || 'Not provided' }}</p>
            </div>
            <div>
              <span class="font-medium text-gray-700">Full Name:</span>
              <p class="text-gray-900">{{ userFullName || 'Not provided' }}</p>
            </div>
            
            <!-- Seller-specific fields -->
            <template v-if="isSellerRole">
              <div>
                <span class="font-medium text-gray-700">Mobile:</span>
                <p class="text-gray-900 font-mono">{{ maskNumber(userMobile) }}</p>
              </div>
              <div>
                <span class="font-medium text-gray-700">Bank Account:</span>
                <p class="text-gray-900 font-mono">{{ maskNumber(userBankAccountNo) }}</p>
              </div>
              <div class="sm:col-span-2">
                <span class="font-medium text-gray-700">Bank Name:</span>
                <p class="text-gray-900">{{ userBankName || 'Not provided' }}</p>
              </div>
            </template>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="bg-gray-50 px-6 py-4 flex justify-between items-center">
        <p class="text-sm text-gray-500">
          Last updated: {{ new Date().toLocaleDateString() }}
        </p>
        <button
          @click="goToEdit"
          class="px-6 py-2 bg-purple-600 text-white font-medium rounded-md shadow hover:bg-purple-700 transition focus:outline-none focus:ring-2 focus:ring-purple-500"
        >
          Edit Profile
        </button>
      </div>
    </template>
  </div>
</template>
