// lib/authUtils.js
import { useCart } from '@/composables/useCart'
import Cookies from 'js-cookie'

// Cookie configuration
const COOKIE_CONFIG = {
  ACCESS_TOKEN: {
    name: 'access_token',
    expires: 30 / (60 * 24), // 30 minutes (30 min / 60 min/hour / 24 hours/day)
    secure: true,
    sameSite: 'Strict',
    httpOnly: true
  },
  REFRESH_TOKEN: {
    name: 'refresh_token',
    expires: 1, // 1 days 
    secure: true,
    sameSite: 'Strict',
    httpOnly: true
  }
}

export function setAuthTokens(accessToken, refreshToken = null) {
  // Set access token
  Cookies.set(COOKIE_CONFIG.ACCESS_TOKEN.name, accessToken, {
    expires: COOKIE_CONFIG.ACCESS_TOKEN.expires,
    secure: COOKIE_CONFIG.ACCESS_TOKEN.secure,
    sameSite: COOKIE_CONFIG.ACCESS_TOKEN.sameSite
  })
  
  // Set refresh token (if provided)
  if (refreshToken) {
    Cookies.set(COOKIE_CONFIG.REFRESH_TOKEN.name, refreshToken, {
      expires: COOKIE_CONFIG.REFRESH_TOKEN.expires,
      secure: COOKIE_CONFIG.REFRESH_TOKEN.secure,
      sameSite: COOKIE_CONFIG.REFRESH_TOKEN.sameSite
    })
  }
}

export function getAccessToken() {
  return Cookies.get(COOKIE_CONFIG.ACCESS_TOKEN.name)
}

export function getRefreshToken() {
  return Cookies.get(COOKIE_CONFIG.REFRESH_TOKEN.name)
}
/**
 * Check if user is authenticated (has valid access token)
 */
export function isAuthenticated() {
  return !!getAccessToken()
}

/**
 * Clear all authentication tokens
 */
export function clearAuthTokens() {
  Cookies.remove(COOKIE_CONFIG.ACCESS_TOKEN.name)
  Cookies.remove(COOKIE_CONFIG.REFRESH_TOKEN.name)
}

/**
 * Refresh the access token using the refresh token
 * Returns the new access token or null if refresh fails
 */
export async function refreshAccessToken() {
  const refreshToken = getRefreshToken()

  if (!refreshToken) {
    // console.error('No refresh token available')
    return null
  }

  try {
    const response = await fetch(`${import.meta.env.VITE_APP_URL2}/auth/refresh`, {
      method: 'POST',
      credentials: 'include', // Also send any HttpOnly cookies
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${refreshToken}`
      }
    })
    
    if (!response.ok) {
      if (response.status === 400) {
        throw new Error('No refresh token provided')
      } else if (response.status === 401) {
        throw new Error('Invalid or expired refresh token')
      }
      throw new Error('Token refresh failed')
    }

    const data = await response.json()
    
    // Store ONLY Access Token
    setAuthTokens(data.access_token)
    
    return data.access_token
    
  } catch (error) {
    console.error('Token refresh error:', error)
    clearAuthTokens()
    return null
  }
}

/**
 * Check if the access token is expired or about to expire
 * @param {number} bufferSeconds - Refresh if token expires within this many seconds (default: 60)
 */
export function isTokenExpired(bufferSeconds = 60) {
  const token = getAccessToken()
  if (!token) return true
  
  const decoded = decodeJWT(token)
  if (!decoded || !decoded.exp) return true
  
  const expirationTime = decoded.exp
  const currentTime = Math.floor(Date.now() / 1000)
  
  // Return true if token is expired or will expire within buffer time
  return expirationTime - currentTime < bufferSeconds
}

/**
 * Ensure we have a valid access token, refreshing if necessary
 * Returns the valid access token or null if refresh fails
 */
export async function ensureValidToken() {
  // If token is valid and not expired, return it
  if (!isTokenExpired()) {
    return getAccessToken()
  }
  
  // Token is expired or about to expire, try to refresh
  const newToken = await refreshAccessToken()
  
  if (!newToken) {
    const { clearCart } = useCart()
    clearCart()
    // Refresh failed, clear tokens
    clearAuthTokens()
    return null
  }
  
  router.go(0)
  return newToken
}

/**
 * Logout user by clearing tokens
 */
export async function logoutFromServer() {
  try {
    const response = await fetch(`${import.meta.env.VITE_APP_URL2}/auth/logout`, {
      method: 'POST',
      credentials: 'include', // Important: sends cookies including refresh_token
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    if (!response.ok) {
      if (response.status === 400) {
        throw new Error('Refresh token is missing or invalid')
      } else if (response.status === 500) {
        throw new Error('Server error during logout')
      } else {
        throw new Error(`Logout failed: ${response.status}`)
      }
    }

    clearAuthTokens()
  } catch (error) {
    console.error('Logout error:', error)
    
    if (error.name === 'TypeError') {
      throw new Error('Network error - please check your connection')
    } else {
      throw error
    }
  }
}

/**
 * Decode JWT token to extract user information
 */
export function decodeJWT(token) {
  if (!token) return null
  
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null
    
    const payload = parts[1]
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    const parsed = JSON.parse(decoded)
    
    // Check if token is expired
    if (parsed.exp && parsed.exp < Math.floor(Date.now() / 1000)) {
      return null
    }
    
    return parsed
  } catch (error) {
    console.error('Error decoding JWT:', error)
    return null
  }
}
