// useUser.js
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getAccessToken, decodeJWT, isAuthenticated } from '@/lib/authUtils'
import { getProfileByIdAndToken } from '@/lib/fetchUtils'

const currentUser = ref(null)
const isLoading = ref(false)
const forceUpdate = ref(0)
const apiUserData = ref(null) // Store full API user data

export function useUser() {
  const router = useRouter()

  // Enhanced computed properties that prefer API data over JWT token data
  const userId = computed(() => {
    // Get userId from JWT token first (always available)
    const tokenData = decodeJWT(getAccessToken())
    return tokenData?.id || null
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

  // Load complete user data from API
  const loadCompleteUserData = async () => {
    if (!userId.value) {
      console.warn('No user ID available for API call')
      return false
    }

    isLoading.value = true
    
    try {
      const token = getAccessToken()
      if (!token) {
        throw new Error('No access token available')
      }

      const data = await getProfileByIdAndToken(
        `${import.meta.env.VITE_APP_URL2}/users`, 
        userId.value, 
        token
      )

      if (data && data.id) {
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
        return true
      } else {
        console.warn('Invalid API response structure')
        return false
      }
    } catch (error) {
      console.error('Failed to load complete user data:', error)
      
      // If API fails but we have token data, that's okay
      // The computed properties will fall back to token data
      return false
    } finally {
      isLoading.value = false
    }
  }

  const requireAuth = () => {
    if (!isLoggedIn.value) {
      router.push({ name: 'Login' })
      return false
    }
    return true
  }

  const refreshUser = async () => {
    forceUpdate.value++
    
    // Also refresh API data if we have a user ID
    if (userId.value) {
      await loadCompleteUserData()
    }
  }

  const logout = () => {
    localStorage.removeItem('accessToken')
    currentUser.value = null
    apiUserData.value = null
    router.push({ name: 'Login' })
  }

  // Initialize user data when composable is first used
  const initializeUser = async () => {
    
    // Load complete data from API if we have a user ID and don't have API data yet
    if (userId.value && !apiUserData.value) {
      await loadCompleteUserData()
    }
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
    logout,
    initializeUser
  }
}
