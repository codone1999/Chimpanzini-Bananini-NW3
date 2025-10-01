// useUser.js
import { ref, computed, watch } from 'vue'
import { getAccessToken, decodeJWT, isAuthenticated, logoutFromServer } from '@/lib/authUtils'
import { getProfileByIdAndToken } from '@/lib/fetchUtils'

const currentUser = ref(null)
const isLoading = ref(false)
const forceUpdate = ref(0)
const apiUserData = ref(null)
const isInitialized = ref(false) // Track initialization

const loadCompleteUserData = async () => {
  if (isLoading.value) return

  isLoading.value = true
  
  try {
    const token = getAccessToken()
    // Check token first before decoding
    if (!token) {
      currentUser.value = null
      apiUserData.value = null
      return
    }

    const tokenData = decodeJWT(token)
    if (!tokenData?.id) {
      console.warn('Invalid token data')
      return
    }

    const data = await getProfileByIdAndToken(
      `${import.meta.env.VITE_APP_URL2}/users`, 
      tokenData.id, 
      token
    )

    if (data) {
      currentUser.value = data.id // Store the actual user ID

      apiUserData.value = {
        id: data.id,
        nickname: data.nickname || '',
        email: data.email || '',
        fullName: data.fullName || '',
        role: data.role || '',
        phoneNumber: data.phoneNumber || '',
        bankAccount: data.bankAccount || '',
        bankName: data.bankName || ''
      }
    } else {
      console.warn('Invalid API response structure')
    }
  } catch (error) {
    console.error('Failed to load complete user data:', error)
    // Consider clearing user data on error
    currentUser.value = null
    apiUserData.value = null
  } finally {
    isLoading.value = false
  }
}

const initializeUser = async () => {
  // Only initialize once
  if (isInitialized.value) return
  
  if (!currentUser.value && isAuthenticated()) {
    await loadCompleteUserData()
  }
  
  isInitialized.value = true
}

export function useUser() {
  // Only initialize if not already done
  if (!isInitialized.value) {
    initializeUser()
  }

  const userId = computed(() => apiUserData.value?.id || null)
  const userRole = computed(() => apiUserData.value?.role || null)
  const userEmail = computed(() => apiUserData.value?.email || null)
  const userNickname = computed(() => apiUserData.value?.nickname || null)
  const userFullName = computed(() => apiUserData.value?.fullName || null)
  const userMobile = computed(() => apiUserData.value?.phoneNumber || null)
  const userBankAccountNo = computed(() => apiUserData.value?.bankAccount || null)
  const userBankName = computed(() => apiUserData.value?.bankName || null)
  const isLoggedIn = computed(() => !!currentUser.value && isAuthenticated())
  const hasCompleteUserData = computed(() => !!apiUserData.value)

  const refreshUser = async () => {
    forceUpdate.value++
    await loadCompleteUserData()
  }

  const logout = async () => {
    currentUser.value = null
    apiUserData.value = null
    isInitialized.value = false // Reset initialization flag
    
    await logoutFromServer()
  }

  watch(forceUpdate, async () => {
    if (userId.value) {
      await loadCompleteUserData()
    }
  })

  return {
    // State
    currentUser,
    apiUserData,
    isLoading,
    
    // computed properties
    userId,
    userRole,
    userEmail,
    userNickname,
    userFullName,
    userMobile,
    userBankAccountNo,
    userBankName,
    isLoggedIn,
    hasCompleteUserData,
    
    // Methods
    loadCompleteUserData,
    refreshUser,
    logout
  }
}