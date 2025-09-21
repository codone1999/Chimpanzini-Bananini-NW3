// useUser.js
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getAccessToken, decodeJWT, isAuthenticated, clearAuthTokens } from '@/lib/authUtils'
import { getProfileByIdAndToken } from '@/lib/fetchUtils'

const currentUser = ref(null)
const isLoading = ref(false)
const forceUpdate = ref(0)
const apiUserData = ref(null) // Store full API user data

const loadCompleteUserData = async () => {
  if (isLoading.value) return

  isLoading.value = true
  
  try {
    const token = getAccessToken()
    const tokenData = decodeJWT(token)
    if (!token) {
      return
    }

    const data = await getProfileByIdAndToken(`${import.meta.env.VITE_APP_URL2}/users`, tokenData.id, token)

    if (data) {
      currentUser.value = tokenData.id

      // Store the complete API data
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
  } finally {
    isLoading.value = false
  }
}

const initializeUser = async () => {
    if (!currentUser.value && isAuthenticated()) {
      await loadCompleteUserData()
    }
  }

export function useUser() {
  initializeUser()

  const router = useRouter()

  // Enhanced computed properties that prefer API data over JWT token data
  const userId = computed(() => {
    return apiUserData.value?.id || null
  })

  const userRole = computed(() => {
    return apiUserData.value?.role || null
  })

  const userEmail = computed(() => {
    return apiUserData.value?.email || null
  })

  const userNickname = computed(() => {
    return apiUserData.value?.nickname || null
  })

  const userFullName = computed(() => {
    return apiUserData.value?.fullName || null
  })

  const userMobile = computed(() => {
    return apiUserData.value?.phoneNumber || null
  })

  const userBankAccountNo = computed(() => {
    return apiUserData.value?.bankAccount || null
  })

  const userBankName = computed(() => {
    return apiUserData.value?.bankName || null
  })

  const isLoggedIn = computed(() => !!currentUser.value && isAuthenticated())

  // Check if we have complete user data (from API)
  const hasCompleteUserData = computed(() => !!apiUserData.value)

  const refreshUser = async () => {
    forceUpdate.value++
    
    // Also refresh API data if we have a user ID
    if (userId.value) {
      await loadCompleteUserData()
    }
  }

  const logout = () => {
    clearAuthTokens()
    currentUser.value = null
    apiUserData.value = null
    router.push({ name: 'Login' })
  }


  // Watch for forced updates (when profile is edited)
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
    
    // Enhanced computed properties
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
    requireAuth,
    logout
  }
}
