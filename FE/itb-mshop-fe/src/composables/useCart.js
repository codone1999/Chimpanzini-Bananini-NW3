// composables/useCart.js
import { ref, computed, watch } from 'vue'

const CART_STORAGE_KEY = 'shopping_cart'

// Shared state across all component instances
const cartItems = ref([])
let isInitialized = false

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

// Save cart to localStorage whenever it changes
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
  } catch (error) {
    console.error('Failed to remove cart to localStorage:', error)
  }
}

// Initialize on first load
if (!isInitialized) {
  initCart()
  isInitialized = true
  
  // Listen for storage events from other tabs/windows
  if (typeof window !== 'undefined') {
    window.addEventListener('storage', (e) => {
      if (e.key === CART_STORAGE_KEY && e.newValue) {
        try {
          cartItems.value = JSON.parse(e.newValue)
        } catch (error) {
          console.error('Failed to sync cart from storage event:', error)
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

  // Methods
  function addToCart(product, quantity) {
    const existingItem = cartItems.value.find(item => item.id === product.id)
    
    if (existingItem) {
      // Calculate new total quantity
      const newQuantity = existingItem.quantity + quantity
      
      // Check if new quantity exceeds available stock
      if (newQuantity > product.quantity) {
        // Set to maximum available quantity instead
        existingItem.quantity = product.quantity
        alert(`Cannot add more items. Maximum available quantity is ${product.quantity}`)
      } else {
        existingItem.quantity = newQuantity
      }
    } else {
      // Check if requested quantity exceeds stock
      const quantityToAdd = Math.min(quantity, product.quantity)
      
      if (quantity > product.quantity) {
        alert(`Only ${product.quantity} items available in stock`)
      }
      
      // Add new item
      cartItems.value.push({
        id: product.id,
        model: product.model,
        price: product.price,
        quantity: quantityToAdd,
        maxQuantity: product.quantity, // Store max available quantity
        sellerId: product.sellerId,
        sellerName: product.sellerName,
        selected: false,
        image: product.saleItemImages?.[0]?.fileName 
          ? `${import.meta.env.VITE_APP_URL}/sale-items/picture/${product.saleItemImages[0].fileName}`
          : null,
        // Store additional product details
        brandName: product.brandName,
        color: product.color,
        storageGb: product.storageGb,
        ramGb: product.ramGb,
        screenSizeInch: product.screenSizeInch
      })
    }
    
    // Force save immediately after adding
    saveCart()
  }

  function removeFromCart(itemId) {
    const index = cartItems.value.findIndex(item => item.id === itemId)
    if (index > -1) {
      cartItems.value.splice(index, 1)
      saveCart()
    } else {
      removeCart()
    }
  }

  function updateQuantity(itemId, newQuantity) {
    const item = cartItems.value.find(i => i.id === itemId)
    if (item) {
      if (newQuantity <= 0) {
        removeFromCart(itemId)
      } else if (item.maxQuantity && newQuantity > item.maxQuantity) {
        // Don't allow quantity to exceed max available
        alert(`Cannot add more items. Maximum available quantity is ${item.maxQuantity}`)
        item.quantity = item.maxQuantity
        saveCart()
      } else {
        item.quantity = newQuantity
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

  function clearCart() {
    cartItems.value = []
    saveCart()
  }

  function clearSelectedItems() {
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
    clearSelectedItems
  }
}
