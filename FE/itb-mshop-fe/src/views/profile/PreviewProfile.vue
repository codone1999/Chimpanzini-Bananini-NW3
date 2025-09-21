<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAccessToken, isAuthenticated, decodeJWT } from '@/lib/authUtils'
import { getProfileByIdAndToken } from '@/lib/fetchUtils'

const router = useRouter()

const user = ref({
  nickName: '',
  email: '',
  fullName: '',
  mobile: '',
  bankAccountNo: '',
  bankName: '',
  nationalCardNo: '',
  nationalCardFront: null,
  nationalCardBack: null,
  role: 'BUYER'
})

const isLoading = ref(true)
const errorMessage = ref('')

// Mask sensitive data for display
function maskNumber(num) {
  if (!num) return 'Not provided'
  const str = String(num)
  if (str.length <= 4) return str
  
  const firstTwo = str.substring(0, 2)
  const lastTwo = str.substring(str.length - 2)
  const middle = '•'.repeat(Math.max(str.length - 4, 3))
  
  return firstTwo + middle + lastTwo
}

// Load user profile data
async function loadUserProfile() {
  if (!isAuthenticated()) {
    router.push({ name: 'Login' })
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    // First, get basic data from JWT token for immediate display
    const token = getAccessToken()
    const tokenData = decodeJWT(token)
    
    const data = await getProfileByIdAndToken(`${import.meta.env.VITE_APP_URL2}/users`, tokenData.id, token)

    if (data && data.id) {
      // Pre-populate with token data
      user.value.nickName = data.nickname || ''
      user.value.email = data.email || ''
      user.value.fullName = data.fullName || ''
      user.value.role = data.role || ''
      user.value.mobile = data.phoneNumber || '',
      user.value.bankAccountNo = data.bankAccount || '',
      user.value.bankName = data.bankName || ''
    }


  } catch (error) {
    console.error('Failed to load profile:', error)
    errorMessage.value = 'Failed to load profile data. Please try again.'
    
    // If API fails but we have token data, continue with that
    if (!user.value.email) {
      router.push({ name: 'Login' })
    }
  } finally {
    isLoading.value = false
  }
}

// Initialize on component mount
onMounted(() => {
  loadUserProfile()
})

// Computed properties
const isSellerRole = computed(() => {
  return user.value.role === 'SELLER'
})

const userInitial = computed(() => {
  return user.value.nickName?.charAt(0)?.toUpperCase() || 
         user.value.fullName?.charAt(0)?.toUpperCase() || 
         'U'
})

// Navigation
function goToEdit() {
  router.push({ name: 'EditProfile' })
}

function retryLoad() {
  loadUserProfile()
}
</script>

<template>
  <div class="max-w-3xl mx-auto bg-white shadow-lg rounded-xl overflow-hidden my-12">
    
    <!-- Loading State -->
    <div v-if="isLoading" class="p-12 text-center">
      <div class="animate-spin w-12 h-12 border-4 border-purple-500 border-t-transparent rounded-full mx-auto mb-4"></div>
      <p class="text-gray-600">Loading your profile...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="errorMessage" class="p-12 text-center">
      <div class="text-red-500 mb-4">
        <svg class="w-16 h-16 mx-auto" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
        </svg>
      </div>
      <h3 class="text-xl font-semibold text-gray-700 mb-2">Unable to Load Profile</h3>
      <p class="text-gray-600 mb-4">{{ errorMessage }}</p>
      <button 
        @click="retryLoad"
        class="px-6 py-2 bg-purple-600 text-white font-medium rounded-md shadow hover:bg-purple-700 transition"
      >
        Try Again
      </button>
    </div>

    <!-- Profile Content -->
    <template v-else>
      <!-- Header -->
      <div class="bg-gradient-to-r from-purple-600 to-indigo-600 p-6 flex items-center space-x-4">
        <!-- Avatar with initial -->
        <div class="w-16 h-16 rounded-full bg-white flex items-center justify-center text-purple-600 font-bold text-2xl shadow-md">
          {{ userInitial }}
        </div>
        <div class="flex-1">
          <h2 class="text-2xl font-bold text-white">{{ user.nickName }}</h2>
          <p class="text-purple-100 text-sm">@{{ user.fullName }}</p>
          <div class="flex items-center mt-1">
            <span :class="[
              'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
              user.role === 'SELLER' 
                ? 'bg-yellow-100 text-yellow-800' 
                : 'bg-blue-100 text-blue-800'
            ]">
              {{ user.role === 'SELLER' ? 'Seller Account' : 'Buyer Account' }}
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
              <p class="text-gray-900">{{ user.email || 'Not provided' }}</p>
            </div>
            <div>
              <span class="font-medium text-gray-700">Full Name:</span>
              <p class="text-gray-900">{{ user.fullName || 'Not provided' }}</p>
            </div>
            
            <!-- Seller-specific fields -->
            <template v-if="isSellerRole">
              <div>
                <span class="font-medium text-gray-700">Mobile:</span>
                <p class="text-gray-900 font-mono">{{ maskNumber(user.mobile) }}</p>
              </div>
              <div>
                <span class="font-medium text-gray-700">Bank Account:</span>
                <p class="text-gray-900 font-mono">{{ maskNumber(user.bankAccountNo) }}</p>
              </div>
              <div class="sm:col-span-2">
                <span class="font-medium text-gray-700">Bank Name:</span>
                <p class="text-gray-900">{{ user.bankName || 'Not provided' }}</p>
              </div>
              <div class="sm:col-span-2">
                <span class="font-medium text-gray-700">National Card No:</span>
                <p class="text-gray-900 font-mono">{{ maskNumber(user.nationalCardNo) }}</p>
              </div>
            </template>
          </div>
        </div>

        <!-- Card Images for Sellers -->
        <div v-if="isSellerRole">
          <h3 class="text-lg font-semibold text-gray-700 border-b pb-2 mb-4">Identity Verification</h3>
          
          <template v-if="user.nationalCardFront || user.nationalCardBack">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <!-- Front Card -->
              <div class="text-center">
                <p class="text-sm font-medium text-gray-500 mb-2">National Card - Front</p>
                <div v-if="user.nationalCardFront" class="relative">
                  <img 
                    :src="user.nationalCardFront" 
                    alt="National Card Front" 
                    class="w-full h-40 object-cover rounded-lg border shadow-sm"
                    @error="$event.target.style.display = 'none'"
                  />
                </div>
                <div v-else class="w-full h-40 bg-gray-100 rounded-lg border flex items-center justify-center">
                  <span class="text-gray-400">Image not available</span>
                </div>
              </div>

              <!-- Back Card -->
              <div class="text-center">
                <p class="text-sm font-medium text-gray-500 mb-2">National Card - Back</p>
                <div v-if="user.nationalCardBack" class="relative">
                  <img 
                    :src="user.nationalCardBack" 
                    alt="National Card Back" 
                    class="w-full h-40 object-cover rounded-lg border shadow-sm"
                    @error="$event.target.style.display = 'none'"
                  />
                </div>
                <div v-else class="w-full h-40 bg-gray-100 rounded-lg border flex items-center justify-center">
                  <span class="text-gray-400">Image not available</span>
                </div>
              </div>
            </div>
          </template>
          
          <template v-else>
            <div class="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
              <div class="flex items-center">
                <svg class="w-5 h-5 text-yellow-600 mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
                </svg>
                <p class="text-yellow-800">Identity documents not uploaded or pending verification.</p>
              </div>
            </div>
          </template>
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
