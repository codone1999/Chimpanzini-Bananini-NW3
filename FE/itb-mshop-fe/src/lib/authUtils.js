// lib/authUtils.js
import Cookies from 'js-cookie'

// Cookie configuration
const COOKIE_CONFIG = {
  ACCESS_TOKEN: {
    name: 'access_token',
    expires: 30 / (60 * 24), // 30 minutes (30 min / 60 min/hour / 24 hours/day)
    secure: false, // Allow HTTP for development
    sameSite: 'lax', // Less restrictive
    httpOnly: false
  },
  REFRESH_TOKEN: {
    name: 'refresh_token', 
    expires: 1, // 1 day
    secure: false, // Allow HTTP for development
    sameSite: 'lax', // Less restrictive
    httpOnly: false
  }
}

/**
 * Set authentication tokens in cookies
 * @param {string} accessToken - JWT access token
 * @param {string} refreshToken - JWT refresh token
 */
export function setAuthTokens(accessToken, refreshToken) {
  if (accessToken) {
    Cookies.set(COOKIE_CONFIG.ACCESS_TOKEN.name, accessToken, {
      expires: COOKIE_CONFIG.ACCESS_TOKEN.expires,
      secure: COOKIE_CONFIG.ACCESS_TOKEN.secure,
      sameSite: COOKIE_CONFIG.ACCESS_TOKEN.sameSite,
      httpOnly: COOKIE_CONFIG.ACCESS_TOKEN.httpOnly
    })
  }
  
  if (refreshToken) {
    Cookies.set(COOKIE_CONFIG.REFRESH_TOKEN.name, refreshToken, {
      expires: COOKIE_CONFIG.REFRESH_TOKEN.expires,
      secure: COOKIE_CONFIG.REFRESH_TOKEN.secure,
      sameSite: COOKIE_CONFIG.REFRESH_TOKEN.sameSite,
      httpOnly: COOKIE_CONFIG.REFRESH_TOKEN.httpOnly
    })
  }
}

/**
 * Get access token from cookies
 * @returns {string|undefined} Access token
 */
export function getAccessToken() {
  return Cookies.get(COOKIE_CONFIG.ACCESS_TOKEN.name)
}

/**
 * Get refresh token from cookies
 * @returns {string|undefined} Refresh token
 */
export function getRefreshToken() {
  return Cookies.get(COOKIE_CONFIG.REFRESH_TOKEN.name)
}

/**
 * Check if user is authenticated
 * @returns {boolean} True if access token exists
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
 * Refresh access token using refresh token
 * @param {string} apiUrl - Base API URL
 * @returns {Promise<boolean>} True if refresh successful
 */
export async function refreshAccessToken(apiUrl) {
  const refreshToken = getRefreshToken()
  
  if (!refreshToken) {
    clearAuthTokens()
    return false
  }
  
  try {
    const response = await fetch(`${apiUrl}/auth/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${refreshToken}`
      }
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const result = await response.json()
    
    if (result.access_token) {
      setAuthTokens(result.access_token, refreshToken)
      return true
    }
    
    return false
  } catch (error) {
    console.error('Token refresh failed:', error)
    clearAuthTokens()
    return false
  }
}

/**
 * Create authorization header for API requests
 * @returns {Object} Authorization header object
 */
export function getAuthHeaders() {
  const token = getAccessToken()
  return token ? { 'Authorization': `Bearer ${token}` } : {}
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
      // Handle HTTP error responses
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
      // Network error (fetch throws TypeError for network issues)
      throw new Error('Network error - please check your connection')
    } else {
      // Re-throw other errors (including our custom errors above)
      throw error
    }
  }
}

/**
 * Decode JWT token to extract user information
 * @param {string} token - JWT token
 * @returns {Object|null} Decoded token payload or null if invalid
 */
export function decodeJWT(token) {
  if (!token) return null
  
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null
    
    // Decode payload (middle part)
    const payload = parts[1]
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    const parsed = JSON.parse(decoded)
    
    // Check if token is expired
    if (parsed.exp && parsed.exp < Math.floor(Date.now() / 1000)) {
      clearAuthTokens() // Auto-clear expired tokens
      return null
    }
    
    return parsed
  } catch (error) {
    console.error('Error decoding JWT:', error)
    return null
  }
}
