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
 */
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
 * Check if user has a refresh token (can potentially refresh session)
 */
export function hasRefreshToken() {
  return !!getRefreshToken()
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
      // Store new access token, keep existing refresh token
      setAuthTokens(result.access_token, refreshToken)
      return true
    }
    
    clearAuthTokens()
    return false
  } catch (error) {
    console.error('Token refresh failed:', error)
    clearAuthTokens()
    return false
  }
}

/**
 * Attempts to refresh access token if only refresh token exists
 */
export async function initializeAuth(apiUrl) {
  const accessToken = getAccessToken()
  const refreshToken = getRefreshToken()
  
  // If we have access token, we're good
  if (accessToken) {
    return true
  }
  
  // If we have refresh token but no access token, try to refresh
  if (refreshToken && !accessToken) {
    console.log('Access token expired, attempting refresh...')
    return await refreshAccessToken(apiUrl)
  }
  
  // No tokens at all
  return false
}

/**
 * Create authorization header for API requests
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
