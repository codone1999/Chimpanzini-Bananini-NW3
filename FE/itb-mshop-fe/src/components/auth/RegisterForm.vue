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
const showPassword = ref(false)
const hasSubmitted = ref(false)
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

// Password strength indicator
const passwordStrength = computed(() => {
  const password = form.value.password
  if (!password) return { level: 0, text: '', color: '' }
  
  let strength = 0
  if (password.length >= 8) strength++
  if (password.length >= 12) strength++
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength++
  if (/[0-9]/.test(password)) strength++
  if (/[@$!%*?&/\\#]/.test(password)) strength++
  
  if (strength <= 2) return { level: strength, text: 'Weak', color: 'text-red-400' }
  if (strength <= 3) return { level: strength, text: 'Medium', color: 'text-amber-400' }
  return { level: strength, text: 'Strong', color: 'text-emerald-400' }
})

function handleFieldBlur(fieldName) {
  const validator = validationMap[fieldName]
  fieldErrors.value[fieldName] = validator ? (validator(form.value[fieldName]) || '') : ''
}

function handleFieldInput(fieldName) {
  if (fieldErrors.value[fieldName]) {
    fieldErrors.value[fieldName] = ''
  }
  // Re-enable submit button when user starts typing
  if (hasSubmitted.value) {
    hasSubmitted.value = false
  }
}

const isBuyerFormValidComputed = computed(() => isBuyerFormValid(form.value))
const isSellerFormValidComputed = computed(() => isSellerFormValid(form.value))
const isSubmitDisabled = computed(() => {
  if (isLoading.value) return true
  if (hasSubmitted.value) return true
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
  hasSubmitted.value = false
}

function handleFileUpload(event, side) {
  const file = event.target.files[0]
  if (file) {
    form.value[side] = {
      file,
      preview: URL.createObjectURL(file)
    }
  }
  // Re-enable submit button when user uploads a file
  if (hasSubmitted.value) {
    hasSubmitted.value = false
  }
}

function removeFile(side) {
  if (form.value[side]?.preview) {
    URL.revokeObjectURL(form.value[side].preview)
  }
  form.value[side] = null
  // Re-enable submit button when user removes a file
  if (hasSubmitted.value) {
    hasSubmitted.value = false
  }
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
  hasSubmitted.value = true

  try {
    await registerAccount(`${import.meta.env.VITE_APP_URL2}/auth/register`, form.value, activeRole.value)
    successMessage.value = `${form.value.email} account created successfully!`
    hasSubmitted.value = false
    resetForm()
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
  { name: 'email', label: 'Email', type: 'email', class: 'itbms-email' }
]

const additionalCommonFields = [
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
  <div class="bg-gray-900 text-gray-100 font-sans flex items-center justify-center px-4 py-12">
    <div class="w-full max-w-lg">
      <!-- Card -->
      <div class="bg-gray-800 border border-gray-700 rounded-2xl shadow-2xl px-8 py-10">
        <!-- Header -->
        <div class="text-center mb-8">
          <h2 class="text-3xl font-extrabold bg-gradient-to-r from-purple-400 to-indigo-500 bg-clip-text text-transparent mb-3">
            Create Account
          </h2>
          <p class="text-gray-400 text-sm">
            Sign up as a buyer or seller
          </p>
        </div>

        <!-- Tabs -->
        <div class="itbms-account-type flex mb-6 gap-2">
          <button
            v-for="role in ['buyer', 'seller']"
            :key="role"
            @click="setRole(role)"
            :class="[
              'flex-1 py-2.5 text-center font-semibold rounded-lg transition-all',
              activeRole === role 
                ? 'bg-purple-600 text-white shadow-md' 
                : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
            ]"
            :disabled="isLoading"
          >
            {{ role.charAt(0).toUpperCase() + role.slice(1) }}
          </button>
        </div>

        <!-- Messages -->
        <transition name="fade">
          <div v-if="successMessage" class="mb-6 p-4 bg-emerald-900/30 border border-emerald-700 text-emerald-300 rounded-lg text-sm">
            {{ successMessage }}
          </div>
        </transition>
        <transition name="fade">
          <div v-if="errorMessage" class="mb-6 p-4 bg-red-900/30 border border-red-700 text-red-300 rounded-lg text-sm">
            {{ errorMessage }}
          </div>
        </transition>

        <!-- Form -->
        <form @submit.prevent="handleSubmit" class="space-y-4">
          <!-- Common Fields -->
          <div v-for="field in commonFields" :key="field.name">
            <label class="block text-sm font-medium text-purple-300 mb-2">
              {{ field.label }} <span class="text-red-400">*</span>
            </label>
            <input 
              v-model="form[field.name]"
              :type="field.type"
              :minlength="field.minlength"
              :maxlength="field.maxlength"
              :class="[
                field.class,
                'w-full px-4 py-3 bg-gray-700 border rounded-lg text-gray-100 placeholder-gray-500 focus:outline-none focus:ring-2 transition disabled:bg-gray-600 disabled:cursor-not-allowed',
                fieldErrors[field.name] ? 'border-red-500 focus:border-red-500 focus:ring-red-500/20' : 'border-gray-600 focus:border-purple-500 focus:ring-purple-500/20'
              ]"
              :placeholder="`Enter your ${field.label.toLowerCase()}`"
              required
              :disabled="isLoading"
              @blur="handleFieldBlur(field.name)"
              @input="handleFieldInput(field.name)"
            />
            <p v-if="fieldErrors[field.name]" class="text-sm text-red-400 mt-1">
              {{ fieldErrors[field.name] }}
            </p>
          </div>

          <!-- Password Field with Strength Indicator -->
          <div>
            <label class="block text-sm font-medium text-purple-300 mb-2">
              Password <span class="text-red-400">*</span>
            </label>
            <div class="relative">
              <input 
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&/\\#]).+$"
                :class="[
                  'itbms-password',
                  'w-full px-4 py-3 bg-gray-700 border rounded-lg text-gray-100 placeholder-gray-500 focus:outline-none focus:ring-2 transition disabled:bg-gray-600 disabled:cursor-not-allowed',
                  fieldErrors.password ? 'border-red-500 focus:border-red-500 focus:ring-red-500/20' : 'border-gray-600 focus:border-purple-500 focus:ring-purple-500/20'
                ]"
                placeholder="Enter your password"
                required
                :disabled="isLoading"
                @blur="handleFieldBlur('password')"
                @input="handleFieldInput('password')"
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-300 transition"
                :disabled="isLoading"
              >
                <svg v-if="!showPassword" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
                <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                </svg>
              </button>
            </div>
            
            <!-- Password Strength Indicator -->
            <div v-if="form.password" class="mt-2 flex items-center gap-2">
              <div class="flex-1 h-1.5 bg-gray-700 rounded-full overflow-hidden">
                <div 
                  :class="[
                    'h-full transition-all duration-300',
                    passwordStrength.level <= 2 ? 'bg-red-500' : '',
                    passwordStrength.level === 3 ? 'bg-amber-500' : '',
                    passwordStrength.level >= 4 ? 'bg-emerald-500' : ''
                  ]"
                  :style="{ width: (passwordStrength.level / 5 * 100) + '%' }"
                ></div>
              </div>
              <span :class="['text-xs font-medium', passwordStrength.color]">
                {{ passwordStrength.text }}
              </span>
            </div>

            <p v-if="fieldErrors.password" class="text-sm text-red-400 mt-1">
              {{ fieldErrors.password }}
            </p>
          </div>

          <!-- Additional Common Fields -->
          <div v-for="field in additionalCommonFields" :key="field.name">
            <label class="block text-sm font-medium text-purple-300 mb-2">
              {{ field.label }} <span class="text-red-400">*</span>
            </label>
            <input 
              v-model="form[field.name]"
              :type="field.type"
              :minlength="field.minlength"
              :maxlength="field.maxlength"
              :class="[
                field.class,
                'w-full px-4 py-3 bg-gray-700 border rounded-lg text-gray-100 placeholder-gray-500 focus:outline-none focus:ring-2 transition disabled:bg-gray-600 disabled:cursor-not-allowed',
                fieldErrors[field.name] ? 'border-red-500 focus:border-red-500 focus:ring-red-500/20' : 'border-gray-600 focus:border-purple-500 focus:ring-purple-500/20'
              ]"
              :placeholder="`Enter your ${field.label.toLowerCase()}`"
              required
              :disabled="isLoading"
              @blur="handleFieldBlur(field.name)"
              @input="handleFieldInput(field.name)"
            />
            <p v-if="fieldErrors[field.name]" class="text-sm text-red-400 mt-1">
              {{ fieldErrors[field.name] }}
            </p>
          </div>

          <!-- Seller-only Fields -->
          <template v-if="activeRole === 'seller'">
            <div v-for="field in sellerFields" :key="field.name">
              <label class="block text-sm font-medium text-purple-300 mb-2">
                {{ field.label }} <span class="text-red-400">*</span>
              </label>
              <input 
                v-model="form[field.name]"
                :type="field.type"
                :class="[
                  field.class,
                  'w-full px-4 py-3 bg-gray-700 border rounded-lg text-gray-100 placeholder-gray-500 focus:outline-none focus:ring-2 transition disabled:bg-gray-600 disabled:cursor-not-allowed',
                  fieldErrors[field.name] ? 'border-red-500 focus:border-red-500 focus:ring-red-500/20' : 'border-gray-600 focus:border-purple-500 focus:ring-purple-500/20'
                ]"
                :placeholder="`Enter ${field.label.toLowerCase()}`"
                required
                :disabled="isLoading"
                @blur="handleFieldBlur(field.name)"
                @input="handleFieldInput(field.name)"
              />
              <p v-if="fieldErrors[field.name]" class="text-sm text-red-400 mt-1">
                {{ fieldErrors[field.name] }}
              </p>
            </div>

            <!-- National Card Photos -->
            <div>
              <label class="block text-sm font-medium text-purple-300 mb-2">
                National Card Photo <span class="text-red-400">*</span>
              </label>
              <div class="flex gap-4">
                <div
                  v-for="side in [{ key: 'nationalCardFront', label: 'Front', class: 'itbms-card-photo-front' }, 
                                  { key: 'nationalCardBack', label: 'Back', class: 'itbms-card-photo-back' }]"
                  :key="side.key"
                  class="relative flex-1 h-32 border-2 border-dashed rounded-lg flex items-center justify-center text-gray-400 cursor-pointer hover:border-purple-500 overflow-hidden group transition-colors bg-gray-700/30"
                  :class="{ 'opacity-50 cursor-not-allowed border-gray-600': isLoading, 'border-gray-600': !isLoading }"
                >
                  <template v-if="form[side.key]">
                    <img :src="form[side.key].preview" :alt="`${side.label} Preview`" class="object-cover w-full h-full" />
                    <button
                      type="button"
                      @click="removeFile(side.key)"
                      :disabled="isLoading"
                      class="absolute inset-0 bg-red-500 bg-opacity-70 flex items-center justify-center text-white text-xl opacity-0 group-hover:opacity-100 transition disabled:cursor-not-allowed"
                    >
                      ✕
                    </button>
                  </template>
                  <template v-else>
                    <div class="text-center">
                      <svg class="w-8 h-8 mx-auto mb-2 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                      </svg>
                      <span class="text-sm">{{ side.label }}</span>
                    </div>
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
          <div class="flex gap-3 pt-4">
            <button 
              type="submit"
              :disabled="isSubmitDisabled"
              :class="[
                'itbms-submit-button flex-1 py-3 rounded-lg font-medium transition-all shadow-md',
                isSubmitDisabled 
                  ? 'bg-gray-600 text-gray-400 cursor-not-allowed' 
                  : 'bg-purple-600 text-white hover:bg-purple-500 hover:shadow-lg'
              ]"
            >
              {{ isLoading ? 'Registering...' : 'Submit' }}
            </button>
            <router-link 
              to="/"
              @click="handleCancel"
              class="itbms-cancel-button flex-1 text-center bg-gray-700 text-gray-300 py-3 rounded-lg hover:bg-gray-600 font-medium transition-all"
            >
              Cancel
            </router-link>
          </div>

          <!-- Sign In Link -->
          <div class="pt-4 border-t border-gray-700 text-center">
            <p class="text-sm text-gray-400">
              Already have an account?
              <router-link
                :to="{ name: 'Login' }"
                class="text-purple-400 hover:text-purple-300 hover:underline font-medium transition ml-1"
              >
                Sign in
              </router-link>
            </p>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
