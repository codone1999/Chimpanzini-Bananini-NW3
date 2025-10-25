// Cart.vue
<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCart } from '@/composables/useCart'
import { useUser } from '@/composables/useUser'
import { addItemWithToken } from '@/lib/fetchUtils'
import { getAccessToken } from '@/lib/authUtils'
import { useShippingAddress } from '@/composables/useShippingAddress'

const router = useRouter()
const { saveShippingAddress, getShippingAddress } = useShippingAddress()
const { 
  cartItems, 
  updateQuantity: updateCartQuantity,
  removeFromCart: removeFromCartComposable,
  clearSelectedItems
} = useCart()
const { userId, isLoading: userLoading } = useUser()

const shippingNote = ref('')
const orderShippingAddress = ref(getShippingAddress())

// Popup state
const showRemovePopup = ref(false)
const itemToRemove = ref(null)

onMounted(async () => {
  // Cart is already synced in useUser.js loadCompleteUserData()
  // Just check if cart is empty and redirect
  if (cartItems.value.length === 0) {
    router.push({ name: 'ListGallery' })
  }
})

// Watch for cart becoming empty during usage and redirect
watch(() => cartItems.value.length, (newLength, oldLength) => {
  if (newLength === 0 && oldLength > 0) {
    setTimeout(() => {
      router.push({ name: 'ListGallery' })
    }, 500)
  }
})

// Group items by seller
const groupedBySeller = computed(() => {
  const groups = {}
  cartItems.value.forEach(item => {
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

const updateQuantity = async (itemId, change) => {
  const item = cartItems.value.find(i => i.id === itemId)
  if (!item) return
  
  const newQuantity = item.quantity + change
  
  if (newQuantity === 0 && change < 0) {
    itemToRemove.value = item
    showRemovePopup.value = true
    return
  }
  
  // Check if exceeds max quantity
  if (newQuantity > item.maxQuantity) {
    alert(`Cannot add more items. Maximum available quantity is ${item.maxQuantity}`)
    return
  }
  
  try {
    await updateCartQuantity(itemId, newQuantity, userId.value)
  } catch (error) {
    console.error('Failed to update quantity:', error)
    alert('Failed to update quantity. Please try again.')
  }
}

const removeFromCart = async (itemId) => {
  try {
    await removeFromCartComposable(itemId, userId.value)
  } catch (error) {
    console.error('Failed to remove item:', error)
    alert('Failed to remove item. Please try again.')
  }
}

const confirmRemove = async () => {
  if (itemToRemove.value) {
    await removeFromCart(itemToRemove.value.id)
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
  const selectedItems = cartItems.value.filter(item => item.selected)
  if (selectedItems.length === 0) {
    alert('Please select at least one item to create an order')
    return
  }
  if (!orderShippingAddress.value.trim()) {
    alert('Please enter a shipping address')
    return
  }

  try {
    const ordersBySeller = {}
    
    for (let i = 0; i < selectedItems.length; i++) {
      const item = selectedItems[i]
      const sellerKey = item.sellerName || item.seller || 'Unknown Seller'
      if (!ordersBySeller[sellerKey]) {
        ordersBySeller[sellerKey] = []
      }
      ordersBySeller[sellerKey].push(item)
    }
    
    let globalOrderItemId = 1
    
    for (const seller in ordersBySeller) {
      const items = ordersBySeller[seller]
      
      const orderItems = []
      for (let j = 0; j < items.length; j++) {
        const item = items[j]
        
        orderItems.push({
          id: globalOrderItemId++,
          saleItemId: item.id,
          price: item.price,
          quantity: item.quantity
        })
      }

      const orderData = {
        buyerId: userId.value,
        sellerId: items[0].sellerId || '',
        shippingAddress: orderShippingAddress.value.trim(),
        orderNote: shippingNote.value.trim() || '',
        orderItems: orderItems
      }

      saveShippingAddress(orderData.shippingAddress)
      
      const createdOrder = await addItemWithToken(
        `${import.meta.env.VITE_APP_URL2}/orders`, 
        orderData, 
        getAccessToken()
      )
      console.log(`Order created for seller ${seller}:`, createdOrder)
    }

    alert('Orders created successfully! Redirecting to orders page...')
    clearSelectedItems()
    setTimeout(() => {
      router.push({ name: 'ListOrders' })
    }, 1000)

  } catch (error) {
    console.error('Create order failed:', error)
    alert('Failed to create order. Please try again.')
  }
}

const getItemDisplayText = (item) => {
  if (item.model) return item.model
  if (item.brand) return item.brand
  return 'Product'
}
</script>

<template>
  <div class="bg-gray-900 text-gray-100 font-sans min-h-screen py-12 px-4">
    <div class="max-w-7xl mx-auto">
      <!-- Header -->
      <div class="text-center mb-8">
        <h1 class="text-4xl font-extrabold p-1 bg-gradient-to-r from-purple-400 to-indigo-500 bg-clip-text text-transparent mb-3">
          Shopping Cart
        </h1>
        <p class="text-gray-400">
          Review your items and proceed to checkout
        </p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Cart Items -->
        <div class="lg:col-span-2 space-y-6 animate-slide-in-left">
          <!-- Select All -->
          <div class="bg-gray-800 border border-gray-700 rounded-xl shadow-lg p-4">
            <label class="flex items-center gap-3 cursor-pointer">
              <input 
                type="checkbox" 
                v-model="allSelected"
                class="w-5 h-5 rounded border-gray-600 bg-gray-700 text-purple-600 focus:ring-purple-500 focus:ring-offset-gray-800 cursor-pointer"
              >
              <span class="font-semibold text-purple-300">Select All Items</span>
            </label>
          </div>

          <!-- Items grouped by seller -->
          <div v-for="(items, seller) in groupedBySeller" :key="seller" 
               class="bg-gray-800 border border-gray-700 rounded-xl shadow-lg p-6 space-y-4">
            
            <!-- Seller Checkbox -->
            <div class="border-b border-gray-700 pb-4">
              <label 
                class="flex items-center gap-3 cursor-pointer hover:bg-gray-700/50 p-2 rounded-lg transition"
                @click="toggleSeller(seller)"
              >
                <input 
                  type="checkbox" 
                  :checked="isSellerSelected(seller)"
                  @click.stop="toggleSeller(seller)"
                  class="w-5 h-5 rounded border-gray-600 bg-gray-700 text-purple-600 focus:ring-purple-500 focus:ring-offset-gray-800 cursor-pointer"
                >
                <div class="flex items-center gap-2">
                  <svg class="w-5 h-5 text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"></path>
                  </svg>
                  <span class="font-bold text-gray-100">{{ seller }}</span>
                </div>
              </label>
            </div>

            <!-- Items in this seller group -->
            <div v-for="item in items" :key="item.id" 
                class="bg-gray-700/50 border border-gray-600 rounded-lg p-4 flex gap-4">
              <label class="flex items-start cursor-pointer">
                <input 
                  type="checkbox" 
                  v-model="item.selected"
                  class="w-5 h-5 mt-1 rounded border-gray-600 bg-gray-800 text-purple-600 focus:ring-purple-500 focus:ring-offset-gray-700 cursor-pointer"
                >
              </label>

              <img 
                v-if="item.image"
                :src="item.image" 
                :alt="item.model || 'Product'" 
                class="w-24 h-24 object-cover rounded border border-gray-600"
              >
              <div 
                v-else
                class="w-24 h-24 bg-gray-800 rounded border border-gray-600 flex items-center justify-center"
              >
                <svg class="w-12 h-12 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                </svg>
              </div>
              
              <div class="flex-1">
                <h3 class="text-lg text-gray-100 font-medium">
                  {{ getItemDisplayText(item) }}
                </h3>
                <p class="text-purple-400 font-semibold mt-1">฿{{ item.price.toLocaleString() }}</p>
                <p v-if="item.maxQuantity" class="text-gray-400 text-sm mt-1">
                  Available: {{ item.maxQuantity }}
                </p>
                
                <div class="flex items-center gap-3 mt-3">
                  <button 
                    @click="updateQuantity(item.id, -1)"
                    class="w-8 h-8 rounded bg-gray-600 hover:bg-gray-500 text-gray-100 flex items-center justify-center transition font-bold"
                  >
                    -
                  </button>
                  <span class="font-medium text-gray-100 w-8 text-center">{{ item.quantity }}</span>
                  <button 
                    @click="updateQuantity(item.id, 1)"
                    :disabled="item.quantity >= item.maxQuantity"
                    :class="[
                      'w-8 h-8 rounded flex items-center justify-center transition font-bold',
                      item.quantity >= item.maxQuantity 
                        ? 'bg-gray-800 text-gray-500 cursor-not-allowed' 
                        : 'bg-gray-600 hover:bg-gray-500 text-gray-100'
                    ]"
                  >
                    +
                  </button>
                </div>
              </div>
              
              <div class="text-right flex flex-col justify-between">
                <p class="font-semibold text-lg text-gray-100">
                  ฿{{ (item.price * item.quantity).toLocaleString() }}
                </p>
                <button @click="() => {itemToRemove = item; showRemovePopup = true}"
                        class="ml-auto text-red-400 hover:text-red-300 text-sm font-medium transition">
                  Remove
                </button>
              </div>
            </div>
          </div>
        </div>
          
        <!-- Summary -->
        <div class="lg:col-span-1 animate-slide-in-right">
          <div class="bg-gray-800 border border-gray-700 rounded-xl shadow-2xl p-6 sticky top-8">
            
            <!-- Header -->
            <div class="flex items-center gap-2 mb-6">
              <svg class="w-6 h-6 text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path>
              </svg>
              <h2 class="text-2xl font-bold text-purple-400">Order Summary</h2>
            </div>

            <!-- Ship To -->
            <div class="bg-gray-700/50 border border-gray-600 rounded-lg p-4 mb-4">
              <label for="shipTo" class="block text-purple-300 font-semibold mb-2">
                Ship To <span class="text-red-400">*</span>
              </label>
              <p class="text-gray-400 text-sm mb-2">
                (Address, No, Street, Subdistrict, District, Province, Postal Code)
              </p>
              <textarea 
                id="shipTo"
                v-model="orderShippingAddress"
                rows="5"
                placeholder="Enter full shipping address"
                class="itbms-shipping-address w-full border border-gray-600 bg-gray-800 px-3 py-2 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 resize-none text-gray-100 placeholder-gray-500"
              ></textarea>
            </div>

            <!-- Note -->
            <div class="bg-gray-700/50 border border-gray-600 rounded-lg p-4 mb-6">
              <label for="orderNote" class="block text-purple-300 font-semibold mb-2">
                Note
              </label>
              <textarea 
                id="orderNote"
                v-model="shippingNote"
                rows="4"
                placeholder="Add a note for delivery (optional)"
                class="itbms-order-note w-full border border-gray-600 bg-gray-800 px-3 py-2 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 resize-none text-gray-100 placeholder-gray-500"
              ></textarea>
            </div>

            <!-- Totals -->
            <div class="space-y-4 mb-6">
              <!-- Total Items -->
              <div class="bg-gray-700/50 border border-gray-600 rounded-lg p-4 flex justify-between items-center">
                <div class="flex items-center gap-2">
                  <svg class="w-5 h-5 text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"></path>
                  </svg>
                  <span class="text-gray-300 font-medium">Total Items</span>
                </div>
                <span class="itbms-total-order-items text-xl font-bold text-purple-400">{{ totalItems }}</span>
              </div>

              <!-- Total Price -->
              <div class="bg-gradient-to-r from-purple-600 to-indigo-600 rounded-lg p-5 shadow-lg flex justify-between items-center text-white">
                <div class="flex items-center gap-2">
                  <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                  </svg>
                  <span class="font-semibold text-lg">Total Price</span>
                </div>
                <span class="itbms-total-order-price text-2xl font-bold">฿{{ total.toLocaleString() }}</span>
              </div>
            </div>

            <!-- Create Order Button -->
            <button 
              @click="createOrder"
              :disabled="totalItems === 0 || !orderShippingAddress.trim() || userLoading || !userId"
              :class="[
                'itbms-create-order w-full py-4 px-6 rounded-xl font-bold shadow-lg transition-all duration-200 flex items-center justify-center gap-2',
                totalItems === 0 || !orderShippingAddress.trim() || userLoading || !userId
                  ? 'bg-gray-600 text-gray-400 cursor-not-allowed'
                  : 'bg-gradient-to-r from-emerald-600 to-green-600 hover:from-emerald-500 hover:to-green-500 text-white hover:shadow-xl hover:scale-105'
              ]"
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
    <div v-if="showRemovePopup" class="fixed inset-0 bg-black/70 flex items-center justify-center z-50 backdrop-blur-sm">
      <div class="bg-gray-800 border border-gray-700 rounded-2xl shadow-2xl p-6 max-w-md w-full mx-4 animate-pop-in">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-12 h-12 rounded-full bg-red-900/30 border border-red-700 flex items-center justify-center">
            <svg class="w-6 h-6 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
            </svg>
          </div>
          <h3 class="text-xl font-bold text-gray-100">Remove Item</h3>
        </div>
        
        <p class="text-gray-300 mb-6">
          Do you want to remove the sale Item from the cart?
        </p>
        
        <div class="flex gap-3">
          <button
            @click="cancelRemove"
            class="flex-1 px-4 py-3 bg-gray-700 hover:bg-gray-600 text-gray-100 font-medium rounded-lg transition border border-gray-600"
          >
            Cancel
          </button>
          <button
            @click="confirmRemove"
            class="flex-1 px-4 py-3 bg-red-600 hover:bg-red-500 text-white font-medium rounded-lg transition shadow-lg"
          >
            Confirm
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes slideInLeft {
  0% {
    opacity: 0;
    transform: translateX(-100px);
  }
  100% {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideInRight {
  0% {
    opacity: 0;
    transform: translateX(100px);
  }
  100% {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes popIn {
  0% {
    opacity: 0;
    transform: scale(0.95);
  }
  70% {
    opacity: 1;
    transform: scale(1.02);
  }
  100% {
    transform: scale(1);
  }
}

.animate-slide-in-left {
  animation: slideInLeft 0.6s ease-out forwards;
}

.animate-slide-in-right {
  animation: slideInRight 0.6s ease-out forwards;
}

.animate-pop-in {
  animation: popIn 0.5s ease-out forwards;
}

/* Custom scrollbar for textareas */
textarea::-webkit-scrollbar {
  width: 8px;
}

textarea::-webkit-scrollbar-track {
  background: #374151;
  border-radius: 4px;
}

textarea::-webkit-scrollbar-thumb {
  background: #6b7280;
  border-radius: 4px;
}

textarea::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}
</style>
