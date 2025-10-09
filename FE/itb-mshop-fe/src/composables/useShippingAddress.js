import { ref, watch } from 'vue'

const STORAGE_KEY = 'user_shipping_address'

// Shared state
const shippingAddress = ref(localStorage.getItem(STORAGE_KEY) || '')

export function useShippingAddress() {
  // Save to localStorage whenever the address changes
  watch(shippingAddress, (newAddress) => {
    if (newAddress) {
      localStorage.setItem(STORAGE_KEY, newAddress)
    } else {
      localStorage.removeItem(STORAGE_KEY)
    }
  })

  const saveShippingAddress = (address) => {
    shippingAddress.value = address
  }

  const getShippingAddress = () => {
    return shippingAddress.value
  }

  const clearShippingAddress = () => {
    shippingAddress.value = ''
    localStorage.removeItem(STORAGE_KEY)
  }

  return {
    shippingAddress,
    saveShippingAddress,
    getShippingAddress,
    clearShippingAddress
  }
}
