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

const activeRole = ref('buyer')
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

const isLoading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
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

const validationMap = {
  nickName: validateNickName,
  email: validateRegistrationEmail,
  password: validateRegistrationPassword,
  fullName: validateFullname,
  mobile: validateMobile,
  bankAccountNo: validateBankAccountNo,
  bankName: validateBankName,
  nationalCardNo: validateNationalCardNo
}

function handleFieldBlur(fieldName) {
  const validator = validationMap[fieldName]
  fieldErrors.value[fieldName] = validator ? (validator(form.value[fieldName]) || '') : ''
}

function handleFieldInput(fieldName) {
  if (fieldErrors.value[fieldName]) {
    fieldErrors.value[fieldName] = ''
  }
}

const isBuyerFormValidComputed = computed(() => isBuyerFormValid(form.value))
const isSellerFormValidComputed = computed(() => isSellerFormValid(form.value))
const isSubmitDisabled = computed(() => {
  if (isLoading.value) return true
  return activeRole.value === 'buyer' ? !isBuyerFormValidComputed.value : !isSellerFormValidComputed.value
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
  Object.keys(fieldErrors.value).forEach(key => fieldErrors.value[key] = '')
  errorMessage.value = ''
  successMessage.value = ''
}

function handleFileUpload(event, side) {
  const file = event.target.files[0]
  if (file) {
    form.value[side] = {
      file,
      preview: URL.createObjectURL(file)
    }
  }
}

function removeFile(side) {
  if (form.value[side]?.preview) {
    URL.revokeObjectURL(form.value[side].preview)
  }
  form.value[side] = null
}

async function handleSubmit() {
  if (isLoading.value || isSubmitDisabled.value) return
  
  errorMessage.value = ''
  successMessage.value = ''

  const fieldsToValidate = ['nickName', 'email', 'password', 'fullName']
  if (activeRole.value === 'seller') {
    fieldsToValidate.push('mobile', 'bankAccountNo', 'bankName', 'nationalCardNo')
  }

  let hasErrors = false
  fieldsToValidate.forEach(field => {
    handleFieldBlur(field)
    if (fieldErrors.value[field]) hasErrors = true
  })

  if (hasErrors) return

  isLoading.value = true

  try {
    await registerAccount(`${import.meta.env.VITE_APP_URL2}/auth/register`, form.value, activeRole.value)
    successMessage.value = `${activeRole.value === 'buyer' ? 'Buyer' : 'Seller'} account created successfully!`
    setTimeout(resetForm, 3000)
  } catch (error) {
    console.error('Registration failed:', error)
    
    const errorMessages = {
      'Could not commit JPA transaction': 'Registration failed. This email might already be registered or there\'s a data validation issue.',
      '500': 'Server error occurred. Please check your information and try again.',
      '400': 'Invalid data provided. Please check all required fields.'
    }
    
    errorMessage.value = Object.entries(errorMessages).find(([key]) => error.message.includes(key))?.[1] || error.message || 'Registration failed. Please try again.'
  } finally {
    isLoading.value = false
  }
}

function handleCancel() {
  resetForm()
}

// Common fields for both forms
const commonFields = [
  { name: 'nickName', label: 'Nickname', type: 'text', class: 'itbms-nickname' },
  { name: 'email', label: 'Email', type: 'email', class: 'itbms-email' },
  { name: 'password', label: 'Password', type: 'password', class: 'itbms-password', minlength: 8, pattern: '^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&/\\\\#]).+$' },
  { name: 'fullName', label: 'Fullname', type: 'text', class: 'itbms-fullname', minlength: 4, maxlength: 40 }
]

const sellerFields = [
  { name: 'mobile', label: 'Mobile', type: 'text', class: 'itbms-mobile' },
  { name: 'bankAccountNo', label: 'Bank Account No', type: 'text', class: 'itbms-bankAccount' },
  { name: 'bankName', label: 'Bank Name', type: 'text', class: 'itbms-bankName' },
  { name: 'nationalCardNo', label: 'National Card No', type: 'text', class: 'itbms-card-no' }
]
</script>

<template>
  <div class="max-w-md mx-auto bg-white shadow-md rounded-lg p-6 my-20">
    <!-- Tabs -->
    <div class="itbms-account-type flex mb-6 border-b">
      <button
        v-for="role in ['buyer', 'seller']"
        :key="role"
        @click="setRole(role)"
        :class="[
          'flex-1 py-2 text-center font-semibold rounded-t-lg',
          activeRole === role ? 'bg-orange-500 text-white' : 'bg-gray-200 text-gray-600'
        ]"
        :disabled="isLoading"
      >
        {{ role.charAt(0).toUpperCase() + role.slice(1) }}
      </button>
    </div>

    <!-- Messages -->
    <div v-if="successMessage" class="mb-4 p-3 bg-green-100 border border-green-400 text-green-700 rounded">
      {{ successMessage }}
    </div>
    <div v-if="errorMessage" class="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
      {{ errorMessage }}
    </div>

    <!-- Form -->
    <form @submit.prevent="handleSubmit" class="space-y-4">
      <!-- Common Fields -->
      <div v-for="field in commonFields" :key="field.name">
        <label class="block text-sm font-medium">{{ field.label }} *</label>
        <input 
          v-model="form[field.name]"
          :type="field.type"
          :minlength="field.minlength"
          :maxlength="field.maxlength"
          :pattern="field.pattern"
          :class="[
            field.class,
            'mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100',
            fieldErrors[field.name] ? 'border-red-500' : 'border-gray-300'
          ]"
          required
          :disabled="isLoading"
          @blur="handleFieldBlur(field.name)"
          @input="handleFieldInput(field.name)"
        />
        <p v-if="fieldErrors[field.name]" class="text-sm text-red-600 mt-1">
          {{ fieldErrors[field.name] }}
        </p>
      </div>

      <!-- Seller-only Fields -->
      <template v-if="activeRole === 'seller'">
        <div v-for="field in sellerFields" :key="field.name">
          <label class="block text-sm font-medium">{{ field.label }} *</label>
          <input 
            v-model="form[field.name]"
            :type="field.type"
            :class="[
              field.class,
              'mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200 disabled:bg-gray-100',
              fieldErrors[field.name] ? 'border-red-500' : 'border-gray-300'
            ]"
            required
            :disabled="isLoading"
            @blur="handleFieldBlur(field.name)"
            @input="handleFieldInput(field.name)"
          />
          <p v-if="fieldErrors[field.name]" class="text-sm text-red-600 mt-1">
            {{ fieldErrors[field.name] }}
          </p>
        </div>

        <!-- National Card Photos -->
        <div>
          <label class="block text-sm font-medium mb-2">National Card Photo *</label>
          <div class="flex gap-4">
            <div
              v-for="side in [{ key: 'nationalCardFront', label: 'Front', class: 'itbms-card-photo-front' }, 
                              { key: 'nationalCardBack', label: 'Back', class: 'itbms-card-photo-back' }]"
              :key="side.key"
              class="relative flex-1 h-32 border-2 border-dashed rounded-md flex items-center justify-center text-gray-500 cursor-pointer hover:border-orange-400 overflow-hidden group"
              :class="{ 'opacity-50 cursor-not-allowed': isLoading }"
            >
              <template v-if="form[side.key]">
                <img :src="form[side.key].preview" :alt="`${side.label} Preview`" class="object-cover w-full h-full" />
                <button
                  type="button"
                  @click="removeFile(side.key)"
                  :disabled="isLoading"
                  class="absolute inset-0 bg-red-500 bg-opacity-50 flex items-center justify-center text-white text-xl opacity-0 group-hover:opacity-60 transition disabled:cursor-not-allowed"
                >
                  ✕
                </button>
              </template>
              <template v-else>
                <span>{{ side.label }}</span>
                <input 
                  type="file" 
                  accept="image/*" 
                  :disabled="isLoading"
                  :class="[side.class, 'absolute inset-0 opacity-0 cursor-pointer disabled:cursor-not-allowed']"
                  @change="e => handleFileUpload(e, side.key)" 
                />
              </template>
            </div>
          </div>
        </div>
      </template>

      <!-- Buttons -->
      <div class="flex gap-2 pt-4">
        <button 
          type="submit"
          :disabled="isSubmitDisabled"
          :class="[
            'itbms-submit-button flex-1 py-2 rounded-md transition-colors',
            isSubmitDisabled ? 'bg-red-300 text-gray-600 cursor-not-allowed' : 'bg-green-500 text-white hover:bg-green-600'
          ]"
        >
          {{ isLoading ? 'Registering...' : 'Submit' }}
        </button>
        <router-link 
          to="/"
          @click="handleCancel"
          class="itbms-cancel-button flex-1 text-center bg-gray-300 text-gray-700 py-2 rounded-md hover:bg-gray-400"
        >
          Cancel
        </router-link>
      </div>
    </form>
  </div>
</template>
