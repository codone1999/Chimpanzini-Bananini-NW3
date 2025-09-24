//RegisterForm.vue
<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { registerAccount } from '@/lib/fetchUtils'
import { 
  validateNickName,
  validateRegistrationEmail,
  validateRegistrationPassword,
  validateFullname,
  validateMobile,
  validateBankAccountNo,
  validateBankName,
  validateNationalCardNo,
  isBuyerFormValid,
  isSellerFormValid
} from '@/lib/validateInput'

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

// Individual field validation messages
const fieldErrors = ref({
  nickName: '',
  email: '',
  password: '',
  fullName: '',
  mobile: '',
  bankAccountNo: '',
  bankName: '',
  nationalCardNo: ''
})

// Handle individual field validation on blur
function handleFieldBlur(fieldName) {
  let error = null
  
  switch(fieldName) {
    case 'nickName':
      error = validateNickName(form.value.nickName)
      break
    case 'email':
      error = validateRegistrationEmail(form.value.email)
      break
    case 'password':
      error = validateRegistrationPassword(form.value.password)
      break
    case 'fullName':
      error = validateFullname(form.value.fullName)
      break
    case 'mobile':
      error = validateMobile(form.value.mobile)
      break
    case 'bankAccountNo':
      error = validateBankAccountNo(form.value.bankAccountNo)
      break
    case 'bankName':
      error = validateBankName(form.value.bankName)
      break
    case 'nationalCardNo':
      error = validateNationalCardNo(form.value.nationalCardNo)
      break
  }
  
  fieldErrors.value[fieldName] = error || ''
}

// Clear field error when user starts typing
function handleFieldInput(fieldName) {
  if (fieldErrors.value[fieldName]) {
    fieldErrors.value[fieldName] = ''
  }
}

// ------------------ Computed ----------------------- //
const isBuyerFormValidComputed = computed(() => {
  return isBuyerFormValid(form.value)
})

const isSellerFormValidComputed = computed(() => {
  return isSellerFormValid(form.value)
})

const isSubmitDisabled = computed(() => {
  if (isLoading.value) return true
  
  if (activeRole.value === 'buyer') {
    return !isBuyerFormValidComputed.value
  } else {
    return !isSellerFormValidComputed.value
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
  fieldErrors.value = {
    nickName: '',
    email: '',
    password: '',
    fullName: '',
    mobile: '',
    bankAccountNo: '',
    bankName: '',
    nationalCardNo: ''
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

async function handleSubmit() {
  if (isLoading.value || isSubmitDisabled.value) return
  
  errorMessage.value = ''
  successMessage.value = ''

  // Validate all fields before submit
  const fieldsToValidate = ['nickName', 'email', 'password', 'fullName']
  if (activeRole.value === 'seller') {
    fieldsToValidate.push('mobile', 'bankAccountNo', 'bankName', 'nationalCardNo')
  }

  let hasErrors = false
  fieldsToValidate.forEach(field => {
    handleFieldBlur(field)
    if (fieldErrors.value[field]) {
      hasErrors = true
    }
  })

  if (hasErrors) {
    return
  }

  isLoading.value = true

  try {
    console.log('Submitting registration:', activeRole.value)
    
    const result = await registerAccount(`${import.meta.env.VITE_APP_URL2}/auth/register`, form.value, activeRole.value)
    
    successMessage.value = `${activeRole.value === 'buyer' ? 'Buyer' : 'Seller'} account created successfully!`

    setTimeout(() => {
      resetForm()
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
          @blur="handleFieldBlur('nickName')"
          @input="handleFieldInput('nickName')"
          :class="[
            'mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100',
            fieldErrors.nickName ? 'border-red-500' : 'border-gray-300'
          ]"
        />
        <p v-if="fieldErrors.nickName" class="text-sm text-red-600 mt-1">
          {{ fieldErrors.nickName }}
        </p>
      </div>

      <div>
        <label class="itbms-email block text-sm font-medium">Email *</label>
        <input 
          v-model="form.email" 
          type="email" 
          required
          :disabled="isLoading"
          @blur="handleFieldBlur('email')"
          @input="handleFieldInput('email')"
          :class="[
            'mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100',
            fieldErrors.email ? 'border-red-500' : 'border-gray-300'
          ]"
        />
        <p v-if="fieldErrors.email" class="text-sm text-red-600 mt-1">
          {{ fieldErrors.email }}
        </p>
      </div>

      <div>
        <label class="itbms-password block text-sm font-medium">Password *</label>
        <input 
          v-model="form.password" 
          type="password" 
          required
          minlength="8"
          pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&/\\#]).+$"
          :disabled="isLoading"
          @blur="handleFieldBlur('password')"
          @input="handleFieldInput('password')"
          :class="[
            'mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100',
            fieldErrors.password ? 'border-red-500' : 'border-gray-300'
          ]"
        />
        <p v-if="fieldErrors.password" class="text-sm text-red-600 mt-1">
          {{ fieldErrors.password }}
        </p>
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
          @blur="handleFieldBlur('fullName')"
          @input="handleFieldInput('fullName')"
          :class="[
            'mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100',
            fieldErrors.fullName ? 'border-red-500' : 'border-gray-300'
          ]"
        />
        <p v-if="fieldErrors.fullName" class="text-sm text-red-600 mt-1">
          {{ fieldErrors.fullName }}
        </p>
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
          @blur="handleFieldBlur('nickName')"
          @input="handleFieldInput('nickName')"
          :class="[
            'itbms-nickname mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100',
            fieldErrors.nickName ? 'border-red-500' : 'border-gray-300'
          ]"
        />
        <p v-if="fieldErrors.nickName" class="text-sm text-red-600 mt-1">
          {{ fieldErrors.nickName }}
        </p>
      </div>

      <div>
        <label class="block text-sm font-medium">Email *</label>
        <input 
          v-model="form.email" 
          type="email"
          required
          :disabled="isLoading"
          @blur="handleFieldBlur('email')"
          @input="handleFieldInput('email')"
          :class="[
            'itbms-email mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100',
            fieldErrors.email ? 'border-red-500' : 'border-gray-300'
          ]"
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
          minlength="8"
          pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&/\\#]).+$"
          :disabled="isLoading"
          @blur="handleFieldBlur('password')"
          @input="handleFieldInput('password')"
          :class="[
            'itbms-password mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100',
            fieldErrors.password ? 'border-red-500' : 'border-gray-300'
          ]"
        />
        <p v-if="fieldErrors.password" class="text-sm text-red-600 mt-1">
          {{ fieldErrors.password }}
        </p>
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
          @blur="handleFieldBlur('fullName')"
          @input="handleFieldInput('fullName')"
          :class="[
            'itbms-fullname mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100',
            fieldErrors.fullName ? 'border-red-500' : 'border-gray-300'
          ]"
        />
        <p v-if="fieldErrors.fullName" class="text-sm text-red-600 mt-1">
          {{ fieldErrors.fullName }}
        </p>
      </div>

      <div>
        <label class="block text-sm font-medium">Mobile *</label>
        <input 
          v-model="form.mobile" 
          type="text"
          required
          :disabled="isLoading"
          @blur="handleFieldBlur('mobile')"
          @input="handleFieldInput('mobile')"
          :class="[
            'itbms-mobile mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100',
            fieldErrors.mobile ? 'border-red-500' : 'border-gray-300'
          ]"
        />
        <p v-if="fieldErrors.mobile" class="text-sm text-red-600 mt-1">
          {{ fieldErrors.mobile }}
        </p>
      </div>

      <div>
        <label class="block text-sm font-medium">Bank Account No *</label>
        <input 
          v-model="form.bankAccountNo" 
          type="text"
          required
          :disabled="isLoading"
          @blur="handleFieldBlur('bankAccountNo')"
          @input="handleFieldInput('bankAccountNo')"
          :class="[
            'itbms-bankAccount mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100',
            fieldErrors.bankAccountNo ? 'border-red-500' : 'border-gray-300'
          ]"
        />
        <p v-if="fieldErrors.bankAccountNo" class="text-sm text-red-600 mt-1">
          {{ fieldErrors.bankAccountNo }}
        </p>
      </div>

      <div>
        <label class="block text-sm font-medium">Bank Name *</label>
        <input 
          v-model="form.bankName" 
          type="text"
          required
          :disabled="isLoading"
          @blur="handleFieldBlur('bankName')"
          @input="handleFieldInput('bankName')"
          :class="[
            'itbms-bankName mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100',
            fieldErrors.bankName ? 'border-red-500' : 'border-gray-300'
          ]"
        />
        <p v-if="fieldErrors.bankName" class="text-sm text-red-600 mt-1">
          {{ fieldErrors.bankName }}
        </p>
      </div>

      <div>
        <label class="block text-sm font-medium">National Card No *</label>
        <input 
          v-model="form.nationalCardNo" 
          type="text"
          required
          :disabled="isLoading"
          @blur="handleFieldBlur('nationalCardNo')"
          @input="handleFieldInput('nationalCardNo')"
          :class="[
            'itbms-card-no mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100',
            fieldErrors.nationalCardNo ? 'border-red-500' : 'border-gray-300'
          ]"
        />
        <p v-if="fieldErrors.nationalCardNo" class="text-sm text-red-600 mt-1">
          {{ fieldErrors.nationalCardNo }}
        </p>
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
