<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { editItem } from '@/lib/fetchUtils'
import { getAccessToken, isAuthenticated, decodeJWT } from '@/lib/authUtils'
import { getProfileByIdAndToken } from '@/lib/fetchUtils'

const router = useRouter()

const form = ref({
  nickName: '',
  email: '',
  password: '',
  fullName: '',
  mobile: '',
  bankAccountNo: '',
  bankName: '',
  nationalCardNo: '',
  role: 'BUYER'
})

// Store original data for comparison
const originalData = ref({
  nickName: '',
  fullName: ''
})

const isLoading = ref(false)
const errorMessage = ref('')
const isDataLoaded = ref(false)

// Load user data from token and/or API
async function loadUserData() {
  if (!isAuthenticated()) {
    router.push({ name: 'Login' })
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    // First, try to get basic data from JWT token
    const token = getAccessToken()
    const tokenData = decodeJWT(token)

    const data = await getProfileByIdAndToken(`${import.meta.env.VITE_APP_URL2}/users`, tokenData.id, token)
    if (data) {
      // Map JWT token data to form (adjust property names based on your JWT structure)
      form.value.nickName = data.nickname || ''
      form.value.email = data.email || ''
      form.value.fullName = data.fullName || ''
      form.value.role = data.role || 'BUYER'
      form.value.mobile = data.phoneNumber || ''
      form.value.bankAccountNo = data.bankAccount || ''
      form.value.bankName = data.bankName || ''

      // Store original editable data for comparison
      originalData.value = {
        nickName: data.nickname || '',
        fullName: data.fullName || ''
      }
    }

  } catch (error) {
    console.error('Failed to load profile data:', error)
    errorMessage.value = 'Failed to load profile data. Please try again.'
    
  } finally {
    isLoading.value = false
    isDataLoaded.value = true
  }
}

// Initialize data on component mount
onMounted(() => {
  loadUserData()
})

// --- Mask helper function ---
function maskNumber(num) {
  if (!num) return ''
  const str = String(num)
  if (str.length <= 4) return str // Don't mask very short numbers
  
  const firstTwo = str.substring(0, 2)
  const lastTwo = str.substring(str.length - 2)
  const middle = 'x'.repeat(str.length - 4)
  
  return firstTwo + middle + lastTwo
}

// Computed property to check if data has been changed
const hasChanges = computed(() => {
  if (!isDataLoaded.value) return false
  
  // Compare only editable fields with original data
  const currentData = {
    nickName: form.value.nickName?.trim() || '',
    fullName: form.value.fullName?.trim() || ''
  }
  
  const original = {
    nickName: originalData.value.nickName?.trim() || '',
    fullName: originalData.value.fullName?.trim() || ''
  }
  
  return currentData.nickName !== original.nickName || 
         currentData.fullName !== original.fullName
})

// Computed property to check if form is valid
const isFormValid = computed(() => {
  return form.value.nickName?.trim() && form.value.fullName?.trim()
})

// Computed property to determine if save button should be disabled
const isSaveDisabled = computed(() => {
  return isLoading.value || !hasChanges.value || !isFormValid.value
})

// Submit - only send editable fields
async function handleSubmit() {
  if (!isAuthenticated()) {
    router.push({ name: 'Login' })
    return
  }

  if (isSaveDisabled.value) {
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    // Only send fields that can be edited
    const updateData = {
      nickName: form.value.nickName?.trim(),
      fullName: form.value.fullName?.trim()
      // Note: Don't send readonly fields like email, mobile, bank info
    }

    const response = await editItem(`${import.meta.env.VITE_APP_URL2}/v2/profile`, updateData)

    if (!response.ok) {
      if (response.status === 401) {
        router.push({ name: 'Login' })
        return
      }
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    // Success - redirect back to profile view
    router.push({ name: 'Profile' })

  } catch (error) {
    console.error('Failed to update profile:', error)
    errorMessage.value = 'Failed to update profile. Please try again.'
  } finally {
    isLoading.value = false
  }
}

// Cancel
function handleCancel() {
  router.push({ name: 'Profile' })
}


// Computed property to check if we should show seller fields
const isSellerRole = computed(() => {
  return form.value.role === 'SELLER'
})
</script>

<template>
  <div class="max-w-lg mx-auto bg-white shadow-md rounded-lg p-6 my-10">
    <h2 class="text-xl font-bold mb-6 text-center">Edit Profile</h2>

    <!-- Loading State -->
    <div v-if="!isDataLoaded" class="text-center py-8">
      <div class="animate-spin w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full mx-auto mb-4"></div>
      <p class="text-gray-600">Loading profile data...</p>
    </div>

    <!-- Error Message -->
    <div v-if="errorMessage" class="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
      {{ errorMessage }}
    </div>

    <!-- Form - only show when data is loaded -->
    <form v-if="isDataLoaded" @submit.prevent="handleSubmit" class="space-y-4">
      <!-- Nickname -->
      <div>
        <label class="block text-sm font-medium">Nickname *</label>
        <input 
          v-model="form.nickName" 
          type="text" 
          required
          :disabled="isLoading"
          class="w-full border px-3 py-2 rounded-md focus:ring focus:ring-blue-200 disabled:bg-gray-100" 
        />
      </div>

      <!-- Email (readonly) -->
      <div>
        <label class="block text-sm font-medium">Email *</label>
        <input 
          v-model="form.email" 
          type="email" 
          readonly 
          class="w-full border px-3 py-2 rounded-md bg-gray-100 text-gray-600" 
        />
      </div>

      <!-- Password (masked, readonly) -->
      <div>
        <label class="block text-sm font-medium">Password</label>
        <div class="flex items-center gap-2">
          <input 
            type="password" 
            value="••••••••" 
            readonly 
            class="flex-1 border px-3 py-2 rounded-md bg-gray-100 text-gray-600" 
          />
          <router-link 
            to="/change-password" 
            class="px-3 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 text-sm"
          >
            Change
          </router-link>
        </div>
      </div>

      <!-- Fullname -->
      <div>
        <label class="block text-sm font-medium">Fullname *</label>
        <input 
          v-model="form.fullName" 
          type="text" 
          required
          :disabled="isLoading"
          class="w-full border px-3 py-2 rounded-md focus:ring focus:ring-blue-200 disabled:bg-gray-100" 
        />
      </div>

      <!-- Seller-only fields -->
      <template v-if="isSellerRole">
        <!-- Mobile (masked, readonly) -->
        <div>
          <label class="block text-sm font-medium">Mobile</label>
          <input 
            :value="maskNumber(form.mobile)" 
            readonly 
            class="w-full border px-3 py-2 rounded-md bg-gray-100 text-gray-600" 
          />
          <p class="text-xs text-gray-500 mt-1">Contact support to change mobile number</p>
        </div>

        <!-- Bank Account No (masked, readonly) -->
        <div>
          <label class="block text-sm font-medium">Bank Account No</label>
          <input 
            :value="maskNumber(form.bankAccountNo)" 
            readonly 
            class="w-full border px-3 py-2 rounded-md bg-gray-100 text-gray-600" 
          />
          <p class="text-xs text-gray-500 mt-1">Contact support to change bank details</p>
        </div>

        <!-- Bank Name (readonly) -->
        <div>
          <label class="block text-sm font-medium">Bank Name</label>
          <input 
            v-model="form.bankName" 
            readonly 
            class="w-full border px-3 py-2 rounded-md bg-gray-100 text-gray-600" 
          />
        </div>

        <!-- National Card No (masked, readonly) -->
        <div>
          <label class="block text-sm font-medium">National Card No</label>
          <input 
            :value="maskNumber(form.nationalCardNo)" 
            readonly 
            class="w-full border px-3 py-2 rounded-md bg-gray-100 text-gray-600" 
          />
          <p class="text-xs text-gray-500 mt-1">Contact support to change national card info</p>
        </div>

        <!-- Photos hidden, just message -->
        <div>
          <label class="block text-sm font-medium">National Card Photos</label>
          <div class="border px-3 py-2 rounded-md bg-gray-100">
            <p class="text-gray-600">✓ Documents verified</p>
            <p class="text-xs text-gray-500 mt-1">Contact support if you need to update documents</p>
          </div>
        </div>
      </template>

      <!-- Buttons -->
      <div class="flex gap-2 pt-4">
        <button 
          type="submit" 
          :disabled="isSaveDisabled" 
          :class="[
            'flex-1 py-2 rounded-md font-medium transition-colors',
            isSaveDisabled 
              ? 'bg-gray-300 text-gray-500 cursor-not-allowed' 
              : 'bg-green-500 text-white hover:bg-green-600'
          ]"
        >
          {{ isLoading ? 'Saving...' : 'Save Changes' }}
        </button>   
        
        <button 
          type="button" 
          @click="handleCancel" 
          :disabled="isLoading"
          class="flex-1 py-2 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400 disabled:bg-gray-200"
        >
          Cancel
        </button>
      </div>

    </form>
  </div>
</template>
