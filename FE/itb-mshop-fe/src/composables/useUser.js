// composables/useUser.js
import { ref, computed, watch } from 'vue'
import { decodeJWT, isAuthenticated, logoutFromServer, ensureValidToken } from '@/lib/authUtils'
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
    const token = await ensureValidToken()
    if (!token) {
      currentUser.value = null
      apiUserData.value = null
      return // Will be handled in finally block
    }

    const tokenData = decodeJWT(token)
    if (!tokenData?.id) {
      console.warn('Invalid token data')
      currentUser.value = null
      apiUserData.value = null
      return // Will be handled in finally block
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
      // Not authenticated - mark as initialized immediately
      isInitialized.value = true
      isLoading.value = false
    }
  })()
  
  return initPromise
}

// Initialize on module load
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
    // Clear cart from localStorage only
    const { clearCart } = useCart()
    clearCart()
    
    // Clear user data
    currentUser.value = null
    apiUserData.value = null
    initPromise = null
    
    // Clear shipping address
    const { clearShippingAddress } = useShippingAddress()
    clearShippingAddress()
    
    // Logout from server
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
