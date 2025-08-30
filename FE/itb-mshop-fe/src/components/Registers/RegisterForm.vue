//RegisterForm.vue
<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { registerAccount } from '@/lib/fetchUtils'

const router = useRouter()

// active tab
const activeRole = ref('buyer') // Set Default to buyer

// form state
const form = ref({
  nickName: '',
  email: '',
  password: '',
  fullName: '',
  mobile: '',
  bankAccountNo: '',
  bankName: '',
  nationalCardNo: '',
  nationalCardFront: null,
  nationalCardBack: null
})

// loading and error states
const isLoading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// ------------------ Computed ----------------------- //
const isBuyerFormValid = computed(() => {
  return form.value.nickName.trim() !== '' &&
         form.value.email.trim() !== '' &&
         form.value.password !== '' &&
         form.value.fullName.trim() !== '' &&
         validatePassword(form.value.password) &&
         validateFullname(form.value.fullName) &&
         isValidEmail(form.value.email)
})

const isSellerFormValid = computed(() => {
  return form.value.nickName?.trim() !== '' &&
         form.value.email?.trim() !== '' &&
         form.value.password !== '' &&
         form.value.fullName?.trim() !== '' &&
         form.value.mobile?.trim() !== '' &&
         form.value.bankAccountNo?.trim() !== '' &&
         form.value.bankName?.trim() !== '' &&
         form.value.nationalCardNo?.trim() !== '' &&
         form.value.nationalCardFront !== null &&
         form.value.nationalCardBack !== null &&
         validatePassword(form.value.password) &&
         validateFullname(form.value.fullName) &&
         isValidEmail(form.value.email)
})

const isSubmitDisabled = computed(() => {
  if (isLoading.value) return true
  
  if (activeRole.value === 'buyer') {
    return !isBuyerFormValid.value
  } else {
    return !isSellerFormValid.value
  }
})

function setRole(role) {
  activeRole.value = role
  resetForm()
}

function resetForm() {
  form.value = {
    nickName: '',
    email: '',
    password: '',
    fullName: '',
    mobile: '',
    bankAccountNo: '',
    bankName: '',
    nationalCardNo: '',
    nationalCardFront: null,
    nationalCardBack: null
  }
  errorMessage.value = ''
  successMessage.value = ''
}

function handleFileUpload(event, side) {
  const file = event.target.files[0] || null
  if (file) {
    form.value[side] = {
      file,
      preview: URL.createObjectURL(file)
    }
  }
}

function removeFile(side) {
  if (form.value[side] && form.value[side].preview) {
    URL.revokeObjectURL(form.value[side].preview)
  }
  form.value[side] = null
}

// ------------------------ Validate -------------------- //
function isValidEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email.trim())
}

function validatePassword(password) {
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).+$/
  return passwordRegex.test(password) && password.length >= 8
}

function validateFullname(fullName) {
  const trimmedName = fullName.trim()
  return trimmedName.length >= 4 && trimmedName.length <= 40
}

async function handleSubmit() {
  if (isLoading.value || isSubmitDisabled.value) return
  
  errorMessage.value = ''
  successMessage.value = ''

  if (!validateFullname(form.value.fullName)) {
    errorMessage.value = 'Full name must be between 4 and 40 characters long'
    return
  }

  if (!validatePassword(form.value.password)) {
    errorMessage.value = 'Password must be at least 8 characters and contain uppercase, lowercase, number, and special character (@$!%*?&)'
    return
  }

  if (!isValidEmail(form.value.email)) {
    errorMessage.value = 'Please enter a valid email address'
    return
  }

  isLoading.value = true

  try {
    console.log('Submitting registration:', activeRole.value)
    
    const result = await registerAccount(`${import.meta.env.VITE_APP_URL2}/users/register`, form.value, activeRole.value)
    
    successMessage.value = `${activeRole.value === 'buyer' ? 'Buyer' : 'Seller'} account created successfully!`
    // console.log('Registration successful:', result)
    
    // Optionally redirect or clear form after successful registration
    setTimeout(() => {
      resetForm()
      router.push({ name: 'VerifyEmail'})
    }, 3000)
    
  } catch (error) {
    console.error('Registration failed:', error)
    
    // Handle specific error messages from backend
    let displayMessage = 'Registration failed. Please try again.'
    
    if (error.message.includes('Could not commit JPA transaction')) {
      displayMessage = 'Registration failed. This email might already be registered or there\'s a data validation issue.'
    } else if (error.message.includes('500')) {
      displayMessage = 'Server error occurred. Please check your information and try again.'
    } else if (error.message.includes('400')) {
      displayMessage = 'Invalid data provided. Please check all required fields.'
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
    <!-- Tabs Buyer/Seller -->
    <div class="itbms-account-type flex mb-6 border-b">
      <button
        @click="setRole('buyer')"
        :class="[
          'flex-1 py-2 text-center font-semibold',
          activeRole === 'buyer'
            ? 'bg-orange-500 text-white rounded-t-lg'
            : 'bg-gray-200 text-gray-600 rounded-t-lg'
        ]"
        :disabled="isLoading"
      >
        Buyer
      </button>
      <button
        @click="setRole('seller')"
        :class="[
          'flex-1 py-2 text-center font-semibold',
          activeRole === 'seller'
            ? 'bg-orange-500 text-white rounded-t-lg'
            : 'bg-gray-200 text-gray-600 rounded-t-lg'
        ]"
        :disabled="isLoading"
      >
        Seller
      </button>
    </div>

    <!-- Success Message -->
    <div v-if="successMessage" class="mb-4 p-3 bg-green-100 border border-green-400 text-green-700 rounded">
      {{ successMessage }}
    </div>

    <!-- Error Message -->
    <div v-if="errorMessage" class="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
      {{ errorMessage }}
    </div>

    <!-- Buyer form -->
    <form v-if="activeRole === 'buyer'" @submit.prevent="handleSubmit" class="space-y-4">
      <div>
        <label class="itbms-nickname block text-sm font-medium">Nickname *</label>
        <input 
          v-model="form.nickName" 
          type="text" 
          required
          :disabled="isLoading"
          class="mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100" 
        />
      </div>

      <div>
        <label class="itbms-email block text-sm font-medium">Email *</label>
        <input 
          v-model="form.email" 
          type="email" 
          required
          :disabled="isLoading"
          class="mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100" 
        />
      </div>

      <div>
        <label class="itbms-password block text-sm font-medium">Password *</label>
        <input 
          v-model="form.password" 
          type="password" 
          required
          minlength="8"
          pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).+$"
          :disabled="isLoading"
          class="mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100" 
        />
        <small class="text-gray-500 text-xs">Must contain uppercase, lowercase, number, and special character (@$!%*?&)</small>
      </div>

      <div>
        <label class="itbms-fullname block text-sm font-medium">Fullname *</label>
        <input 
          v-model="form.fullName" 
          type="text" 
          required
          minlength="4"
          maxlength="40"
          :disabled="isLoading"
          class="mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100" 
        />
        <small class="text-gray-500 text-xs">Must be between 4 and 40 characters</small>
      </div>

      <div class="flex gap-2 pt-4">
        <button 
          type="submit" 
          :disabled="isSubmitDisabled"
          :class="[
            'itbms-submit-button flex-1 py-2 rounded-md transition-colors',
            isSubmitDisabled
              ? 'bg-red-300 text-gray-600 cursor-not-allowed'
              : 'bg-green-500 text-white hover:bg-green-600'
          ]"
        >
          {{ isLoading ? 'Registering...' : 'Submit' }}
        </button>
        <router-link to="/"
            @click="handleCancel"
            :disabled="isLoading" 
            class="itbms-cancel-button flex-1 text-center bg-gray-300 text-gray-700 py-2 rounded-md hover:bg-gray-400 disabled:bg-gray-200"
          >
            Cancel
        </router-link>
      </div>
    </form>

    <!-- Seller form -->
    <form v-else @submit.prevent="handleSubmit" class="space-y-4">
      <div>
        <label class="block text-sm font-medium">Nickname *</label>
        <input 
          v-model="form.nickName" 
          type="text"
          required
          :disabled="isLoading"
          class="itbms-nickname mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100" 
        />
      </div>

      <div>
        <label class="block text-sm font-medium">Email *</label>
        <input 
          v-model="form.email" 
          type="email"
          required
          :disabled="isLoading"
          class="itbms-email mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100" 
        />
      </div>

      <div>
        <label class="block text-sm font-medium">Password *</label>
        <input 
          v-model="form.password" 
          type="password"
          required
          minlength="8"
          pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).+$"
          :disabled="isLoading"
          class="itbms-password mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100" 
        />
        <small class="text-gray-500 text-xs">Must contain uppercase, lowercase, number, and special character (@$!%*?&)</small>
      </div>

      <div>
        <label class="block text-sm font-medium">Fullname *</label>
        <input 
          v-model="form.fullName" 
          type="text"
          required
          minlength="4"
          maxlength="40"
          :disabled="isLoading"
          class="itbms-fullname mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100" 
        />
        <small class="text-gray-500 text-xs">Must be between 4 and 40 characters</small>
      </div>

      <div>
        <label class="block text-sm font-medium">Mobile *</label>
        <input 
          v-model="form.mobile" 
          type="text"
          required
          :disabled="isLoading"
          class="itbms-mobile mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100" 
        />
      </div>

      <div>
        <label class="block text-sm font-medium">Bank Account No *</label>
        <input 
          v-model="form.bankAccountNo" 
          type="text"
          required
          :disabled="isLoading"
          class="itbms-bank-account-no mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100" 
        />
      </div>

      <div>
        <label class="block text-sm font-medium">Bank Name *</label>
        <input 
          v-model="form.bankName" 
          type="text"
          required
          :disabled="isLoading"
          class="itbms-back-name mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100" 
        />
      </div>

      <div>
        <label class="block text-sm font-medium">National Card No *</label>
        <input 
          v-model="form.nationalCardNo" 
          type="text"
          required
          :disabled="isLoading"
          class="itbms-card-no mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100" 
        />
      </div>

      <!-- National Card Photo Section -->
      <div>
        <label class="block text-sm font-medium mb-2">National Card Photo *</label>
        <div class="flex gap-4">
          
          <!-- FRONT -->
          <div
            class="relative flex-1 h-32 border-2 border-dashed rounded-md flex items-center justify-center text-gray-500 cursor-pointer hover:border-orange-400 overflow-hidden group"
            :class="{ 'opacity-50 cursor-not-allowed': isLoading }"
          >
            <!-- Show preview if exists -->
            <template v-if="form.nationalCardFront">
              <img :src="form.nationalCardFront.preview" alt="Front Preview" class="object-cover w-full h-full" />
              <!-- Hover overlay with X -->
              <button
                type="button"
                @click="removeFile('nationalCardFront')"
                :disabled="isLoading"
                class="absolute inset-0 bg-red-500 bg-opacity-50 flex items-center justify-center text-white text-xl opacity-0 hover:opacity-60 transition disabled:cursor-not-allowed"
              >
                ✕
              </button>
            </template>
            <!-- Show placeholder if no image -->
            <template v-else>
              <span>Front</span>
              <input 
                type="file" 
                accept="image/*" 
                :disabled="isLoading"
                class="itbms-card-photo-front absolute inset-0 opacity-0 cursor-pointer disabled:cursor-not-allowed"
                @change="e => handleFileUpload(e, 'nationalCardFront')" 
              />
            </template>
          </div>

          <!-- BACK -->
          <div
            class="relative flex-1 h-32 border-2 border-dashed rounded-md flex items-center justify-center text-gray-500 cursor-pointer hover:border-orange-400 overflow-hidden group"
            :class="{ 'opacity-50 cursor-not-allowed': isLoading }"
          >
            <!-- Show preview if exists -->
            <template v-if="form.nationalCardBack">
              <img :src="form.nationalCardBack.preview" alt="Back Preview" class="object-cover w-full h-full" />
              <!-- Hover overlay with X -->
              <button
                type="button"
                @click="removeFile('nationalCardBack')"
                :disabled="isLoading"
                class="absolute inset-0 bg-red-500 bg-opacity-50 flex items-center justify-center text-white text-xl opacity-0 group-hover:opacity-60 transition disabled:cursor-not-allowed"
              >
                ✕
              </button>
            </template>
            <!-- Show placeholder if no image -->
            <template v-else>
              <span>Back</span>
              <input 
                type="file" 
                accept="image/*" 
                :disabled="isLoading"
                class="itbms-card-photo-back absolute inset-0 opacity-0 cursor-pointer disabled:cursor-not-allowed"
                @change="e => handleFileUpload(e, 'nationalCardBack')" 
              />
            </template>
          </div>

        </div>
      </div>

      <!-- Buttons -->
      <div class="flex gap-2 pt-4">
        <button 
          type="submit"
          :disabled="isSubmitDisabled"
          :class="[
            'flex-1 py-2 rounded-md transition-colors',
            isSubmitDisabled
              ? 'bg-red-300 text-gray-600 cursor-not-allowed'
              : 'bg-green-500 text-white hover:bg-green-600'
          ]"
        >
          {{ isLoading ? 'Registering...' : 'Submit' }}
        </button>
        <router-link to="/"
            @click="handleCancel"
            :disabled="isLoading" 
            class="itbms-cancel-button flex-1 text-center bg-gray-300 text-gray-700 py-2 rounded-md hover:bg-gray-400 disabled:bg-gray-200"
          >
            Cancel
        </router-link>
      </div>
    </form>

  </div>
</template>
