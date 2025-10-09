// composables/useUser.js
import { ref, computed, watch } from 'vue'
import { getAccessToken, decodeJWT, isAuthenticated, logoutFromServer } from '@/lib/authUtils'
import { getProfileByIdAndToken } from '@/lib/fetchUtils'
import { useShippingAddress } from './useShippingAddress'
import { useCart } from './useCart'

const currentUser = ref(null)
const isLoading = ref(false)
const forceUpdate = ref(0)
const apiUserData = ref(null)
const isInitialized = ref(false)

const loadCompleteUserData = async () => {
  if (isLoading.value) return

  isLoading.value = true
  
  try {
    const token = getAccessToken()
    if (!token) {
      currentUser.value = null
      apiUserData.value = null
      return
    }

    const tokenData = decodeJWT(token)
    if (!tokenData?.id) {
      console.warn('Invalid token data')
      currentUser.value = null
      apiUserData.value = null
      return
    }

    const data = await getProfileByIdAndToken(
      `${import.meta.env.VITE_APP_URL2}/users`, 
      tokenData.id, 
      token
    )

    if (data) {
      currentUser.value = data.id

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
      currentUser.value = null
      apiUserData.value = null
    }
  } catch (error) {
    console.error('Failed to load complete user data:', error)
    currentUser.value = null
    apiUserData.value = null
  } finally {
    isLoading.value = false
    isInitialized.value = true
  }
}

let initPromise = null
const initializeUser = async () => {
  if (isInitialized.value) return
  
  if (initPromise) return initPromise
  
  initPromise = (async () => {
    if (isAuthenticated()) {
      await loadCompleteUserData()
    } else {
      isInitialized.value = true
    }
  })()
  
  return initPromise
}

if (!isInitialized.value) {
  initializeUser()
}

export function useUser() {
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
    
    // Sync cart after refreshing user data
    if (userId.value) {
      const { syncWithBackend } = useCart()
      await syncWithBackend(userId.value)
    }
  }

  const logout = async () => {
    // Clear cart from localStorage only - keep backend data
    const { clearCart } = useCart()
    clearCart() // Don't pass userId - this will only clear local storage
    
    // Clear user data
    currentUser.value = null
    apiUserData.value = null
    isInitialized.value = false
    initPromise = null
    
    // Clear shipping address from local storage
    const { clearShippingAddress } = useShippingAddress()
    clearShippingAddress()
    
    // Logout from server (clears tokens)
    await logoutFromServer()
  }

  const waitForInit = async () => {
    if (initPromise) {
      await initPromise
    }
  }

  watch(forceUpdate, async () => {
    if (userId.value) {
      await loadCompleteUserData()
    }
  })

  return {
    currentUser,
    apiUserData,
    isLoading,
    isInitialized,
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
    loadCompleteUserData,
    refreshUser,
    logout,
    waitForInit
  }
}
