// composables/useCart.js
import { ref, computed, watch } from 'vue'
import { getAccessToken } from '@/lib/authUtils'
import { getItemsWithToken, addItemWithToken, editItemWithToken, deleteItemWithToken } from '@/lib/fetchUtils'

const CART_STORAGE_KEY = 'shopping_cart'

// Shared state across all component instances
const cartItems = ref([])
let isInitialized = false
let isSyncing = false

// Initialize cart from localStorage
function initCart() {
  try {
    const savedCart = localStorage.getItem(CART_STORAGE_KEY)
    if (savedCart) {
      cartItems.value = JSON.parse(savedCart)
    }
  } catch (error) {
    console.error('Failed to load cart from localStorage:', error)
    cartItems.value = []
  }
}

// Save cart to localStorage
function saveCart() {
  try {
    localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(cartItems.value))
  } catch (error) {
    console.error('Failed to save cart to localStorage:', error)
  }
}

function removeCart() {
  try {
    localStorage.removeItem(CART_STORAGE_KEY)
    cartItems.value = [] // Also clear the reactive state
  } catch (error) {
    console.error('Failed to remove cart from localStorage:', error)
  }
}

// Initialize on first load
if (!isInitialized) {
  initCart()
  isInitialized = true
  
  // Listen for storage events from other tabs/windows
  if (typeof window !== 'undefined') {
    window.addEventListener('storage', (e) => {
      if (e.key === CART_STORAGE_KEY) {
        if (e.newValue) {
          try {
            cartItems.value = JSON.parse(e.newValue)
          } catch (error) {
            console.error('Failed to sync cart from storage event:', error)
          }
        } else {
          // Cart was cleared in another tab
          cartItems.value = []
        }
      }
    })
  }
}

// Watch for changes and save to localStorage
watch(cartItems, () => {
  saveCart()
}, { deep: true })

export function useCart() {
  // Computed properties
  const totalQuantity = computed(() => {
    return cartItems.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  const totalPrice = computed(() => {
    return cartItems.value.reduce((sum, item) => sum + (item.price * item.quantity), 0)
  })

  const selectedItems = computed(() => {
    return cartItems.value.filter(item => item.selected)
  })

  const selectedTotalQuantity = computed(() => {
    return selectedItems.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  const selectedTotalPrice = computed(() => {
    return selectedItems.value.reduce((sum, item) => sum + (item.price * item.quantity), 0)
  })

  async function syncWithBackend(userId) {
    if (!userId || isSyncing) return
    
    isSyncing = true
    try {
      const token = getAccessToken()
      if (!token) return

      // Get cart from backend using getItemsWithToken
      const backendCart = await getItemsWithToken(
        `${import.meta.env.VITE_APP_URL2}/carts?accountId=${userId}`,
        token
      )

      if (Array.isArray(backendCart)) {
        // Convert backend cart format to local cart format
        const convertedBackendCart = backendCart.map(item => ({
          id: item.saleItemId,
          cartId: item.id,
          model: item.itemDescription || 'Unknown',
          price: item.priceEach || 0,
          quantity: item.quantity || 0,
          maxQuantity: item.availableQuantity || 0,
          sellerId: null,
          sellerName: item.sellerName || 'Unknown Seller',
          selected: false,
          image: null,
          note: item.note || ''
        }))

        // Merge localStorage cart with backend cart
        const localCart = [...cartItems.value]
        const mergedCart = [...convertedBackendCart]

        // Add items from localStorage that aren't in backend
        for (const localItem of localCart) {
          const existsInBackend = mergedCart.find(item => item.id === localItem.id)
          if (!existsInBackend) {
            // Add to backend
            try {
              const response = await addItemWithToken(
                `${import.meta.env.VITE_APP_URL2}/carts`,
                {
                  accountId: userId,
                  saleItemId: localItem.id,
                  quantity: localItem.quantity,
                  note: localItem.note || ''
                },
                token
              )
              // Add to merged cart with backend cart id
              mergedCart.push({
                ...localItem,
                cartId: response.id
              })
            } catch (error) {
              console.error('Failed to sync local item to backend:', error)
            }
          } else {
            // Merge local cart details with backend data
            const backendItem = mergedCart.find(item => item.id === localItem.id)
            if (backendItem) {
              backendItem.image = localItem.image
              backendItem.brandName = localItem.brandName
              backendItem.color = localItem.color
              backendItem.storageGb = localItem.storageGb
              backendItem.ramGb = localItem.ramGb
              backendItem.screenSizeInch = localItem.screenSizeInch
              backendItem.sellerId = localItem.sellerId
              backendItem.model = localItem.model || backendItem.model
            }
          }
        }

        cartItems.value = mergedCart
        saveCart()
      }
    } catch (error) {
      console.error('Failed to sync with backend:', error)
    } finally {
      isSyncing = false
    }
  }

  async function addToBackend(userId, product, quantity, note = '') {
    try {
      const token = getAccessToken()
      if (!token || !userId) return null

      const response = await addItemWithToken(
        `${import.meta.env.VITE_APP_URL2}/carts`,
        {
          accountId: userId,
          saleItemId: product.id,
          quantity: quantity,
          note: note
        },
        token
      )
      console.log(response)

      return response
    } catch (error) {
      console.error('Failed to add to backend cart:', error)
      return null
    }
  }

  async function updateBackend(userId, cartId, saleItemId, newQuantity, note = '') {
    try {
      const token = getAccessToken()
      if (!token || !userId || !cartId) return null

      const response = await editItemWithToken(
        `${import.meta.env.VITE_APP_URL2}/carts/${cartId}`,
        {
          accountId: userId,
          saleItemId: saleItemId,
          quantity: newQuantity,
          note: note
        },
        token
      )

      return response
    } catch (error) {
      console.error('Failed to update backend cart:', error)
      return null
    }
  }

  async function deleteFromBackend(userId, cartId) {
    try {
      const token = getAccessToken()
      if (!token || !userId || !cartId) return

      await deleteItemWithToken(
        `${import.meta.env.VITE_APP_URL2}/carts/${cartId}?accountId=${userId}`,
        token
      )
    } catch (error) {
      console.error('Failed to delete from backend cart:', error)
    }
  }

  // Methods
  async function addToCart(product, quantity, userId = null, note = '') {
    const existingItem = cartItems.value.find(item => item.id === product.id)
    
    if (existingItem) {
      const newQuantity = existingItem.quantity + quantity
      
      if (newQuantity > product.quantity) {
        existingItem.quantity = product.quantity
        alert(`Cannot add more items. Maximum available quantity is ${product.quantity}`)
      } else {
        existingItem.quantity = newQuantity
      }

      if (userId && existingItem.cartId) {
        await updateBackend(userId, existingItem.cartId, product.id, existingItem.quantity)
      }
    } else {
      const quantityToAdd = Math.min(quantity, product.quantity)
      
      if (quantity > product.quantity) {
        alert(`Only ${product.quantity} items available in stock`)
      }

      let cartId = null
      if (userId) {
        const response = await addToBackend(userId, product, quantityToAdd, note)
        cartId = response?.id
      }
      
      cartItems.value.push({
        id: product.id,
        cartId: cartId,
        model: product.model,
        price: product.price,
        quantity: quantityToAdd,
        maxQuantity: product.quantity,
        sellerId: product.sellerId,
        sellerName: product.sellerName,
        selected: false,
        image: product.saleItemImages?.[0]?.fileName 
          ? `${import.meta.env.VITE_APP_URL}/sale-items/picture/${product.saleItemImages[0].fileName}`
          : null,
        brandName: product.brandName,
        color: product.color,
        storageGb: product.storageGb,
        ramGb: product.ramGb,
        screenSizeInch: product.screenSizeInch,
        note: note
      })
    }
    
    saveCart()
  }

  async function removeFromCart(itemId, userId = null) {
    const index = cartItems.value.findIndex(item => item.id === itemId)
    if (index > -1) {
      const item = cartItems.value[index]
      
      if (userId && item.cartId) {
        await deleteFromBackend(userId, item.cartId)
      }

      cartItems.value.splice(index, 1)
      saveCart()
    }
  }

  async function updateQuantity(itemId, newQuantity, userId = null) {
    const item = cartItems.value.find(i => i.id === itemId)
    if (item) {
      if (newQuantity <= 0) {
        await removeFromCart(itemId, userId)
      } else if (item.maxQuantity && newQuantity > item.maxQuantity) {
        alert(`Cannot add more items. Maximum available quantity is ${item.maxQuantity}`)
        item.quantity = item.maxQuantity
        
        if (userId && item.cartId) {
          await updateBackend(userId, item.cartId, itemId, item.maxQuantity)
        }
        saveCart()
      } else {
        item.quantity = newQuantity
        
        if (userId && item.cartId) {
          await updateBackend(userId, item.cartId, itemId, newQuantity)
        }
        saveCart()
      }
    }
  }

  function toggleSelection(itemId) {
    const item = cartItems.value.find(i => i.id === itemId)
    if (item) {
      item.selected = !item.selected
      saveCart()
    }
  }

  async function clearCart(userId = null) {
    // Clear both reactive state and localStorage
    cartItems.value = []
    removeCart()
  }

  async function clearSelectedItems(userId = null) {
    // Just remove selected items locally
    cartItems.value = cartItems.value.filter(item => !item.selected)
    saveCart()
  }

  return {
    cartItems,
    totalQuantity,
    totalPrice,
    selectedItems,
    selectedTotalQuantity,
    selectedTotalPrice,
    addToCart,
    removeFromCart,
    updateQuantity,
    toggleSelection,
    clearCart,
    clearSelectedItems,
    syncWithBackend
  }
}
