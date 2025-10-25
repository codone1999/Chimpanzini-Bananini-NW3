// OrderDetail.vue
<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUser } from '@/composables/useUser'
import { getAccessToken } from '@/lib/authUtils'
import { getOrderDetailByIdWithToken } from '@/lib/fetchUtils'
import phoneImg from '../../../public/phone.png'

const route = useRoute()
const router = useRouter()
const { userId, hasCompleteUserData, isLoading: userIsLoading } = useUser()

const orderId = computed(() => route.params.id)
const isLoadingOrder = ref(false)
const orderData = ref(null)
const errorMessage = ref('')

const isLoading = computed(() => {
  return userIsLoading.value || isLoadingOrder.value
})

const canLoadData = computed(() => {
  return hasCompleteUserData.value && userId.value
})

// Calculate order total from all items
const orderTotal = computed(() => {
  if (!orderData.value?.orderItems) return 0
  return orderData.value.orderItems.reduce((sum, item) => {
    return sum + (item.price * item.quantity)
  }, 0)
})

// Helper function to get item image
function getItemImage(item) {
  if (item.saleItem?.saleItemImages?.[0]?.fileName) {
    return `${import.meta.env.VITE_APP_URL}/sale-items/picture/${item.saleItem.saleItemImages[0].fileName}`
  }
  return phoneImg
}

// Helper function to get item description
function getItemDescription(item) {
  if (item.saleItem) {
    return `${item.saleItem.brandName} ${item.saleItem.model} (${item.saleItem.storageGb}GB, ${item.saleItem.color})`
  }
  return 'Item details unavailable'
}

// Calculate unit price (price per item)
function getUnitPrice(item) {
  return item.price || 0
}

// Calculate total price for an item (unit price * quantity)
function getItemTotalPrice(item) {
  return (item.price || 0) * (item.quantity || 0)
}

async function fetchOrderDetails() {
  if (!canLoadData.value) {
    // console.log('Waiting for user data...', { 
    //   hasCompleteUserData: hasCompleteUserData.value, 
    //   userIsLoading: userIsLoading.value 
    // })
    return
  }
  
  isLoadingOrder.value = true
  errorMessage.value = ''
  
  try {
    const data = await getOrderDetailByIdWithToken(
      `${import.meta.env.VITE_APP_URL2}/orders`, 
      orderId.value, 
      getAccessToken()
    )
    orderData.value = data
    // console.log('Order data:', data)
  } catch (error) {
    console.error('Failed to fetch order details:', error)
    errorMessage.value = 'Failed to load order details. Please try again.'
  } finally {
    isLoadingOrder.value = false
  }
}

function goBack() {
  router.push({ name: 'Orders' })
}

// Watch for user data to become available
watch(canLoadData, (newValue) => {
  if (newValue) {
    // console.log('User data loaded, fetching order details...')
    fetchOrderDetails()
  }
})

onMounted(() => {
  if (canLoadData.value) {
    fetchOrderDetails()
  }
})
</script>

<template>
  <div class="min-h-screen bg-gray-900 text-gray-100 p-6">
    <div class="max-w-5xl mx-auto">
      <!-- Breadcrumb -->
      <div class="mb-6 flex items-center gap-2 text-sm animate-pop-in">
        <button 
          @click="goBack"
          class="text-purple-400 hover:text-purple-300 font-medium transition"
        >
          Your Orders
        </button>
        <span class="text-gray-600">›</span>
        <span class="text-gray-300 font-medium">Order Details</span>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="text-center py-20">
        <div class="animate-spin w-12 h-12 border-4 border-purple-500 border-t-transparent rounded-full mx-auto mb-4"></div>
        <p class="text-gray-400">Loading order details...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="errorMessage" class="bg-red-900/30 border border-red-700 rounded-xl p-6 text-center animate-pop-in">
        <p class="text-red-300 mb-4">{{ errorMessage }}</p>
        <button 
          @click="fetchOrderDetails"
          class="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-500 transition shadow-lg"
        >
          Try Again
        </button>
      </div>

      <!-- Order Details -->
      <div v-else-if="orderData" class="bg-gray-800 border border-gray-700 rounded-xl shadow-2xl">
        <!-- Header -->
        <div class="p-6 border-b border-gray-700 animate-pop-in">
          <h1 class="text-2xl font-bold bg-gradient-to-r from-purple-400 to-indigo-500 bg-clip-text text-transparent">
            Order Details
          </h1>
        </div>

        <!-- Order Information Grid -->
        <div class="p-6 grid grid-cols-2 gap-x-16 gap-y-6 border-b border-gray-700 animate-pop-in">
          <div>
            <div class="text-sm text-gray-400 mb-1">Order No:</div>
            <div class="itbms-order-id font-semibold text-purple-300">{{ orderData.id }}</div>
          </div>
          <div>
            <div class="text-sm text-gray-400 mb-1">Seller:</div>
            <div class="itbms-nickname font-semibold text-gray-100">
              {{ orderData.seller?.[0]?.nickName || orderData.seller?.[0]?.sellerName || 'Unknown' }}
            </div>
          </div>
          
          <div>
            <div class="text-sm text-gray-400 mb-1">Order Date:</div>
            <div class="itbms-order-date font-semibold text-gray-100">
              {{ orderData.orderDate ? new Date(orderData.orderDate).toLocaleDateString() : '-' }}
            </div>
          </div>
          <div>
            <div class="text-sm text-gray-400 mb-1">Payment Date:</div>
            <div class="itbms-payment-date font-semibold text-gray-100">
              {{ orderData.paymentDate ? new Date(orderData.paymentDate).toLocaleDateString() : '-' }}
            </div>
          </div>
          
          <div>
            <div class="text-sm text-gray-400 mb-1">Total:</div>
            <div class="itbms-total-order-price font-bold text-purple-400 text-xl">
              ฿{{ orderTotal.toLocaleString() }}
            </div>
          </div>
          <div>
            <div class="text-sm text-gray-400 mb-1">Status:</div>
            <div :class="[
              'itbms-order-status font-semibold',
              orderData.orderStatus === 'COMPLETED' ? 'text-emerald-400' : 
              orderData.orderStatus === 'CANCELLED' ? 'text-red-400' : 
              'text-yellow-400'
            ]">
              {{ orderData.orderStatus }}
            </div>
          </div>
          
          <div class="col-span-2">
            <div class="text-sm text-gray-400 mb-1">Ship To:</div>
            <div class="Itbms-shipping-address font-medium text-gray-200">
              {{ orderData.shippingAddress || '-' }}
            </div>
          </div>
          
          <div v-if="orderData.orderNote" class="col-span-2">
            <div class="text-sm text-gray-400 mb-1">Note:</div>
            <div class="Itbms-order-note font-medium text-gray-200">{{ orderData.orderNote }}</div>
          </div>
        </div>

        <!-- Order Items -->
        <div class="p-6 animate-pop-in">
          <h2 class="text-xl font-bold text-purple-300 mb-4">Order Items</h2>
          <div class="space-y-4">
            <div 
              v-for="item in orderData.orderItems" 
              :key="item.id"
              class="flex items-center gap-4 p-4 bg-gray-700/50 border border-gray-600 rounded-lg"
            >
              <img
                :src="getItemImage(item)"
                :alt="item.description"
                class="w-20 h-20 object-cover rounded border border-gray-600"
              />
              
              <div class="flex-1">
                <div class="Itbms-item-description font-medium text-gray-100 mb-1">
                  {{ item.description }}
                </div>
              </div>
              
              <div class="text-center min-w-[80px]">
                <div class="text-sm text-gray-400">Qty:</div>
                <div class="Itbms-item-quantity font-semibold text-purple-400">{{ item.quantity }}</div>
              </div>
              
              <div class="text-center min-w-[120px]">
                <div class="text-sm text-gray-400">Unit Price:</div>
                <div class="itbms-item-price font-semibold text-gray-100">
                  ฿{{ getUnitPrice(item).toLocaleString() }}
                </div>
              </div>
              
              <div class="text-right min-w-[140px]">
                <div class="text-sm text-gray-400">Total:</div>
                <div class="itbms-item-total-price font-bold text-purple-400 text-lg">
                  ฿{{ getItemTotalPrice(item).toLocaleString() }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Order Total Summary -->
        <div class="p-6 border-t border-gray-700 bg-gradient-to-r from-purple-900/30 to-indigo-900/30 rounded-b-xl animate-pop-in">
          <div class="flex justify-end items-center gap-4">
            <span class="text-lg font-medium text-gray-300">Order Total:</span>
            <span class="text-3xl font-bold bg-gradient-to-r from-purple-400 to-indigo-400 bg-clip-text text-transparent">
              ฿{{ orderTotal.toLocaleString() }}
            </span>
          </div>
        </div>
      </div>

      <!-- No Data -->
      <div v-else class="bg-gray-800 border border-gray-700 rounded-xl shadow-2xl p-12 text-center animate-pop-in">
        <p class="text-gray-400 mb-4">Order not found.</p>
        <button 
          @click="goBack"
          class="px-6 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-500 transition shadow-lg"
        >
          Back to Orders
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
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

.animate-pop-in {
  animation: popIn 0.5s ease-out forwards;
}
</style>
