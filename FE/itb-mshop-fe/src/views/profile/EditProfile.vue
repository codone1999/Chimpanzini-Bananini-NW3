//EditProfile.vue
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { editProfileByIdAndToken } from '@/lib/fetchUtils'
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
  
  // Check if data was passed from router state
  const routerState = history.state?.userData

  if (routerState) {
    // Use data from router state instead of fetching
    form.value.nickName = routerState.nickName || ''
    form.value.email = routerState.email || ''
    form.value.fullName = routerState.fullName || ''
    form.value.role = routerState.role || 'BUYER'
    form.value.mobile = routerState.phoneNumber || ''
    form.value.bankAccountNo = routerState.bankAccount || ''
    form.value.bankName = routerState.bankName || ''

    // Store original data for comparison
    originalData.value = {
      nickName: routerState.nickName || '',
      fullName: routerState.fullName || ''
    }

    isDataLoaded.value = true
    return
  }

  // Fallback to API fetch if no router state
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
  if (!num) return 'Not provided'

  const str = String(num)
  if (str.length <= 4) return str

  const first = 'x'.repeat(str.length - 4)
  const middleThree = str.slice(-4, -1) // last 4 digits, but drop the very last one
  const last = 'x'

  return first + middleThree + last
}

// Computed property to check if data has been changed
const hasChanges = computed(() => {
  if (!isDataLoaded.value) return false
  
  // Compare only editable fields with original data
  const currentData = {
    nickName: form.value.nickName || '',
    fullName: form.value.fullName || ''
  }
  
  const original = {
    nickName: originalData.value.nickName || '',
    fullName: originalData.value.fullName || ''
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
    const token = getAccessToken()
    const tokenData = decodeJWT(token)

    const updateData = {
      nickName: form.value.nickName?.trim(),
      fullName: form.value.fullName?.trim()
    }

    const response = await editProfileByIdAndToken(`${import.meta.env.VITE_APP_URL2}/users`, tokenData.id, token, updateData)

    if (response && response.status === 200) {
      form.value.nickName = response.nickname || updateData.nickName
      form.value.fullName = response.fullName || updateData.fullName
    }

    router.push({ name: 'Profile' }).then(() => router.go(0))
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
          class="itbms-nickname w-full border px-3 py-2 rounded-md focus:ring focus:ring-blue-200 disabled:bg-gray-100" 
        />
      </div>

      <!-- Email (readonly) -->
      <div>
        <label class="block text-sm font-medium">Email *</label>
        <input 
          v-model="form.email" 
          type="email" 
          readonly 
          class="itbms-email w-full border px-3 py-2 rounded-md bg-gray-100 text-gray-600 caret-transparent cursor-default" 
        />
      </div>

      <!-- Fullname -->
      <div>
        <label class="block text-sm font-medium">Fullname *</label>
        <input 
          v-model="form.fullName" 
          type="text" 
          required
          :disabled="isLoading"
          class="itbms-fullname w-full border px-3 py-2 rounded-md focus:ring focus:ring-blue-200 disabled:bg-gray-100" 
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
            class="itbms-mobile w-full border px-3 py-2 rounded-md bg-gray-100 text-gray-600 caret-transparent cursor-default" 
          />
          <p class="text-xs text-gray-500 mt-1">Contact support to change mobile number</p>
        </div>

        <!-- Bank Account No (masked, readonly) -->
        <div>
          <label class="block text-sm font-medium">Bank Account No</label>
          <input 
            :value="maskNumber(form.bankAccountNo)" 
            readonly 
            class="itbms-bankAccount w-full border px-3 py-2 rounded-md bg-gray-100 text-gray-600 caret-transparent cursor-default" 
          />
          <p class="text-xs text-gray-500 mt-1">Contact support to change bank details</p>
        </div>

        <!-- Bank Name (readonly) -->
        <div>
          <label class="block text-sm font-medium">Bank Name</label>
          <input 
            v-model="form.bankName" 
            readonly 
            class="itbms-bankName w-full border px-3 py-2 rounded-md bg-gray-100 text-gray-600 caret-transparent cursor-default" 
          />
        </div>
      </template>

      <!-- Buttons -->
      <div class="flex gap-2 pt-4">
        <button 
          type="submit" 
          :disabled="isSaveDisabled" 
          :class="[
            'itbms-save-button flex-1 py-2 rounded-md font-medium transition-colors',
            isSaveDisabled 
              ? 'bg-green-200 text-gray-500 cursor-not-allowed' 
              : 'bg-green-500 text-white hover:bg-green-600'
          ]"
        >
          {{ isLoading ? 'Saving...' : 'Save Changes' }}
        </button>   
        
        <button 
          type="button" 
          @click="handleCancel" 
          :disabled="isLoading"
          class="itbms-cancel-button flex-1 py-2 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400 disabled:bg-gray-200"
        >
          Cancel
        </button>
      </div>

    </form>
  </div>
</template>
