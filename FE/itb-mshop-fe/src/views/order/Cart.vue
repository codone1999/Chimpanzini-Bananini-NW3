<script setup>
import { ref, computed } from 'vue'
import { useCart } from '@/composables/useCart'
import { useUser } from '@/composables/useUser'
import { addItemWithToken } from '@/lib/fetchUtils'
import { getAccessToken } from '@/lib/authUtils'

const { 
  cartItems, 
  selectedTotalQuantity,
  selectedTotalPrice,
  updateQuantity: updateCartQuantity,
  removeFromCart,
  clearSelectedItems
} = useCart()

const { userId, isLoading: userLoading } = useUser()

const shippingAddress = ref('')
const shippingNote = ref('')

// Popup state
const showRemovePopup = ref(false)
const itemToRemove = ref(null)

// Group items by seller
const groupedBySeller = computed(() => {
  const groups = {}
  cartItems.value.forEach(item => {
    // Use sellerName if available, otherwise use a fallback
    const sellerKey = item.sellerName || 'Unknown Seller'
    if (!groups[sellerKey]) {
      groups[sellerKey] = []
    }
    groups[sellerKey].push(item)
  })
  return groups
})

// Check if all items are selected
const allSelected = computed({
  get() {
    return cartItems.value.length > 0 && cartItems.value.every(item => item.selected)
  },
  set(value) {
    cartItems.value.forEach(item => {
      item.selected = value
    })
  }
})

// Check if all items in a seller group are selected
const isSellerSelected = (seller) => {
  const sellerItems = groupedBySeller.value[seller]
  return sellerItems.every(item => item.selected)
}

// Toggle all items in a seller group
const toggleSeller = (seller) => {
  const sellerItems = groupedBySeller.value[seller]
  const allChecked = isSellerSelected(seller)
  sellerItems.forEach(item => {
    item.selected = !allChecked
  })
}

// Calculate totals based on selected items only
const totalItems = computed(() => {
  return cartItems.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + item.quantity, 0)
})

const total = computed(() => {
  return cartItems.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + (item.price * item.quantity), 0)
})

const updateQuantity = (itemId, change) => {
  const item = cartItems.value.find(i => i.id === itemId)
  if (item) {
    const newQuantity = item.quantity + change
    
    // If decreasing to 0, show confirmation popup
    if (newQuantity === 0 && change < 0) {
      itemToRemove.value = item
      showRemovePopup.value = true
      return
    }
    
    updateCartQuantity(itemId, newQuantity)
  }
}

const confirmRemove = () => {
  if (itemToRemove.value) {
    removeFromCart(itemToRemove.value.id)
  }
  closeRemovePopup()
}

const cancelRemove = () => {
  closeRemovePopup()
}

const closeRemovePopup = () => {
  showRemovePopup.value = false
  itemToRemove.value = null
}

const createOrder = async () => {
  // Validation
  const selectedItems = cartItems.value.filter(item => item.selected)
  if (selectedItems.length === 0) {
    alert('Please select at least one item to create an order')
    return
  }
  if (!shippingAddress.value.trim()) {
    alert('Please enter a shipping address')
    return
  }

  try {
    // Group selected items by seller using plain object
    const ordersBySeller = {}
    
    for (let i = 0; i < selectedItems.length; i++) {
      const item = selectedItems[i]
      // Use sellerName if available, otherwise use a fallback
      const sellerKey = item.sellerName || item.seller || 'Unknown Seller'
      if (!ordersBySeller[sellerKey]) {
        ordersBySeller[sellerKey] = []
      }
      ordersBySeller[sellerKey].push(item)
    }
    
    // Track global order item sequence
    let globalOrderItemId = 1
    
    // Create orders for each seller
    for (const seller in ordersBySeller) {
      const items = ordersBySeller[seller]
      
      // Prepare order items array
      const orderItems = []
      for (let j = 0; j < items.length; j++) {
        const item = items[j]
        
        orderItems.push({
          id: globalOrderItemId++,  // Sequential number across all orders
          saleItemId: item.id,
          price: item.price,
          quantity: item.quantity
        })
      }

      // Prepare order data
        const orderData = {
          buyerId: userId.value,
          sellerId: items[0].sellerId || '',
          orderDate: new Date().toISOString(),
          shippingAddress: shippingAddress.value.trim(),
          orderNote: shippingNote.value.trim() || '',
          orderStatus: 'Completed',
          orderItems: orderItems
        }

      // Submit order to API
      await addItemWithToken(`${import.meta.env.VITE_APP_URL2}/orders`, orderData, getAccessToken())
    }
    
    // Clear selected items from cart after successful order
    clearSelectedItems()
    
    // Reset form
    shippingAddress.value = ''
    shippingNote.value = ''
    
    alert('Order created successfully!')
  } catch (error) {
    console.error('Failed to create order:', error)
    alert('Failed to create order. Please try again.')
  }
}
</script>

<template>
  <div class="min-h-screen py-8 px-4">
    <div class="max-w-7xl mx-auto">
      <h1 class="text-3xl font-bold text-gray-800 mb-8">Shopping Cart</h1>
      
      <div class="grid lg:grid-cols-3 gap-8">
        <!-- Cart Items -->
        <div class="lg:col-span-2 space-y-4">
          <div v-if="cartItems.length === 0" class="bg-white rounded-lg shadow p-8 text-center">
            <p class="text-gray-500 text-lg">Your cart is empty</p>
          </div>

          <!-- Select All -->
          <div v-if="cartItems.length > 0" class="bg-white rounded-lg shadow p-4">
            <label class="flex items-center gap-3 cursor-pointer">
              <input 
                type="checkbox" 
                v-model="allSelected"
                class="w-5 h-5 rounded border-gray-300 text-blue-600 focus:ring-blue-500 cursor-pointer"
              >
              <span class="font-semibold text-gray-800">Select All</span>
            </label>
          </div>

          <!-- Grouped by Seller -->
          <div v-for="(items, seller) in groupedBySeller" :key="seller" class="space-y-3">
            <!-- Seller Header -->
            <div class="bg-gray-50 rounded-lg p-4 border border-gray-200">
              <label class="flex items-center gap-3 cursor-pointer">
                <input 
                  type="checkbox" 
                  :checked="isSellerSelected(seller)"
                  @change="toggleSeller(seller)"
                  class="w-5 h-5 rounded border-gray-300 text-blue-600 focus:ring-blue-500 cursor-pointer"
                >
                <div class="flex items-center gap-2">
                  <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"></path>
                  </svg>
                  <span class="font-bold text-gray-800">{{ seller }}</span>
                </div>
              </label>
            </div>

            <!-- Items in this seller group -->
            <div v-for="item in items" :key="item.id" 
                class="bg-white rounded-lg shadow p-4 flex gap-4 ml-8">
              <label class="flex items-start cursor-pointer">
                <input 
                  type="checkbox" 
                  v-model="item.selected"
                  class="w-5 h-5 mt-1 rounded border-gray-300 text-blue-600 focus:ring-blue-500 cursor-pointer"
                >
              </label>

              <img 
                :src="item.image" 
                :alt="item.model" 
                class="w-24 h-24 object-cover rounded"
              >
              
              <div class="flex-1">
                <h3 class="text-lg text-gray-800">
                  <span><span class="font-semibold">{{ item.brandName }}</span>&nbsp;
                  {{ item.model }}&nbsp;({{item.storageGb}}GB,&nbsp;{{ item.color }})
                  </span>
                </h3>
                <p class="text-gray-600 mt-1">฿{{ item.price.toFixed(2) }}</p>
                
                <div class="flex items-center gap-3 mt-3">
                  <button @click="updateQuantity(item.id, -1)"
                          class="w-8 h-8 rounded bg-gray-200 hover:bg-gray-300 flex items-center justify-center">
                    -
                  </button>
                  <span class="font-medium text-gray-700 w-8 text-center">{{ item.quantity }}</span>
                  <button @click="updateQuantity(item.id, 1)"
                          class="w-8 h-8 rounded bg-gray-200 hover:bg-gray-300 flex items-center justify-center">
                    +
                  </button>
                </div>
              </div>
              
              <div class="text-right flex flex-col justify-between">
                <p class="font-semibold text-lg text-gray-800">
                  ฿{{ (item.price * item.quantity).toFixed(2) }}
                </p>
                <button @click="() => {itemToRemove = item; showRemovePopup = true}"
                        class="ml-auto text-red-500 hover:text-red-700 text-sm font-medium">
                  Remove
                </button>
              </div>
            </div>
          </div>
        </div>
          
        <!-- Summary -->
        <div class="lg:col-span-1">
          <div class="bg-gradient-to-br from-blue-50 to-indigo-50 rounded-xl shadow-lg p-6 sticky top-8 border border-blue-100">
            
            <!-- Header -->
            <div class="flex items-center gap-2 mb-6">
              <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path>
              </svg>
              <h2 class="text-2xl font-bold text-gray-800">Order Summary</h2>
            </div>

            <!-- Ship To -->
            <div class="bg-white rounded-lg p-4 shadow-sm mb-4">
              <label for="shipTo" class="block text-gray-700 font-semibold mb-2">
                Ship To
              </label>
              <p class="text-gray-400 mb-2">
                (Address, No, Street, Subdistrict, District, Province, Postal Code)
              </p>
              <textarea 
                id="shipTo"
                v-model="shippingAddress"
                rows="5"
                placeholder="Enter full shipping address"
                class="itbms-shipping-address w-full border border-gray-300 px-3 py-2 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 resize-none text-gray-700 placeholder-gray-400"
              ></textarea>
            </div>

            <!-- Note -->
            <div class="bg-white rounded-lg p-4 shadow-sm mb-6">
              <label for="orderNote" class="block text-gray-700 font-semibold mb-2">
                Note
              </label>
              <textarea 
                id="orderNote"
                v-model="shippingNote"
                rows="4"
                placeholder="Add a note for delivery (optional)"
                class="itbms-order-note w-full border border-gray-300 px-3 py-2 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 resize-none text-gray-700 placeholder-gray-400"
              ></textarea>
            </div>

            <!-- Totals -->
            <div class="space-y-4 mb-6">
              <!-- Total Items -->
              <div class="bg-white rounded-lg p-4 shadow-sm flex justify-between items-center">
                <div class="flex items-center gap-2">
                  <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"></path>
                  </svg>
                  <span class="text-gray-600 font-medium">Total Items</span>
                </div>
                <span class="itbms-total-order-items text-xl font-bold text-blue-600">{{ totalItems }}</span>
              </div>

              <!-- Total Price -->
              <div class="bg-gradient-to-r from-blue-600 to-indigo-600 rounded-lg p-5 shadow-md flex justify-between items-center text-white">
                <div class="flex items-center gap-2">
                  <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                  </svg>
                  <span class="font-semibold text-lg">Total Price</span>
                </div>
                <span class="itbms-total-order-price text-2xl font-bold">฿{{ total.toFixed(2) }}</span>
              </div>
            </div>

            <!-- Create Order Button -->
            <button 
              @click="createOrder"
              :disabled="totalItems === 0 || !shippingAddress || userLoading || !userId"
              class="itbms-create-order w-full bg-gradient-to-r from-green-500 to-emerald-600 hover:from-green-600 hover:to-emerald-700 disabled:from-gray-300 disabled:to-gray-400 disabled:cursor-not-allowed text-white font-bold py-4 px-6 rounded-xl shadow-lg hover:shadow-xl transition-all duration-200 transform hover:scale-105 disabled:transform-none flex items-center justify-center gap-2"
            >
              <svg v-if="!userLoading" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
              </svg>
              <span v-if="userLoading">Loading...</span>
              <span v-else>Place Order</span>
            </button>
          </div>
        </div>

      </div>
    </div>

    <!-- Remove Confirmation Popup -->
    <div v-if="showRemovePopup" class="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-2xl p-6 max-w-md w-full mx-4">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-12 h-12 rounded-full bg-red-100 flex items-center justify-center">
            <svg class="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
            </svg>
          </div>
          <h3 class="text-xl font-bold text-gray-800">Remove Item</h3>
        </div>
        
        <p class="text-gray-600 mb-6">
          Do you want to remove the sale Item from the cart?
        </p>
        
        <div class="flex gap-3">
          <button
            @click="cancelRemove"
            class="flex-1 px-4 py-2 bg-gray-200 hover:bg-gray-300 text-gray-800 font-medium rounded-lg transition"
          >
            Cancel
          </button>
          <button
            @click="confirmRemove"
            class="flex-1 px-4 py-2 bg-red-600 hover:bg-red-700 text-white font-medium rounded-lg transition"
          >
            Confirm
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
