<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUser } from '@/composables/useUser'
import { getAccessToken } from '@/lib/authUtils'

const route = useRoute()
const router = useRouter()
const { userId, hasCompleteUserData, isLoading: userIsLoading } = useUser()

const orderId = computed(() => route.params.id)
const isLoadingOrder = ref(false)
const orderData = ref(null)
const errorMessage = ref('')

// Sample order data - replace with actual API call
const sampleOrder = {
  id: 1,
  orderDate: 'October 1, 2025',
  paymentDate: 'October 1, 2025',
  total: 62700,
  status: 'Completed',
  seller: 'Somsuan',
  shippingAddress: 'Somchai Jaidee, 123/45 Moo 6, T. Bangna, A. Bangna, Bangkok 10260',
  note: 'Dont try this at home',
  items: [
    {
      id: 1,
      image: 'https://images.unsplash.com/photo-1592286927505-b0501739739a?w=100&h=100&fit=crop',
      description: 'Apple iPhone 14 Pro Max (512GB, Space Black)',
      quantity: 1,
      unitPrice: 42900,
      totalPrice: 42900
    },
    {
      id: 2,
      image: 'https://images.unsplash.com/photo-1556656793-08538906a9f8?w=100&h=100&fit=crop',
      description: 'Apple iPhone 13 mini (128GB, Green)',
      quantity: 1,
      unitPrice: 19800,
      totalPrice: 19800
    }
  ]
}

const isLoading = computed(() => {
  return userIsLoading.value || isLoadingOrder.value
})

const canLoadData = computed(() => {
  return hasCompleteUserData.value && userId.value
})

async function fetchOrderDetails() {
  // if (!canLoadData.value) return
  
  isLoadingOrder.value = true
  errorMessage.value = ''
  
  try {
    // TODO: Replace with actual API call
    // const response = await fetch(
    //   `${import.meta.env.VITE_APP_URL2}/orders/${orderId.value}`,
    //   {
    //     headers: {
    //       'Authorization': `Bearer ${getAccessToken()}`
    //     }
    //   }
    // )
    // const data = await response.json()
    // orderData.value = data
    
    // For now, use sample data
    orderData.value = sampleOrder
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

onMounted(() => {
  // if (canLoadData.value) {
    fetchOrderDetails()
  // }
})
</script>

<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="max-w-5xl mx-auto">
      <!-- Breadcrumb -->
      <div class="mb-6 flex items-center gap-2 text-sm">
        <button 
          @click="goBack"
          class="text-blue-600 hover:text-blue-800 font-medium"
        >
          Your Orders
        </button>
        <span class="text-gray-400">›</span>
        <span class="text-gray-700 font-medium">Order Details</span>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="text-center py-20">
        <div class="animate-spin w-12 h-12 border-4 border-purple-500 border-t-transparent rounded-full mx-auto mb-4"></div>
        <p class="text-gray-600">Loading order details...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="errorMessage" class="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
        <p class="text-red-600 mb-4">{{ errorMessage }}</p>
        <button 
          @click="fetchOrderDetails"
          class="px-6 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition"
        >
          Try Again
        </button>
      </div>

      <!-- Order Details -->
      <div v-else-if="orderData" class="bg-white rounded-lg shadow-md border border-gray-200">
        <!-- Order Information Grid -->
        <div class="p-6 grid grid-cols-2 gap-x-16 gap-y-4 border-b border-gray-200">
          <div>
            <div class="text-sm text-gray-500 mb-1">Order No:</div>
            <div class="itbms-order-id font-semibold text-gray-800">{{ orderData.id }}</div>
          </div>
          <div>
            <div class="text-sm text-gray-500 mb-1">Seller:</div>
            <div class="itbms-nickname font-semibold text-gray-800">{{ orderData.seller }}</div>
          </div>
          
          <div>
            <div class="text-sm text-gray-500 mb-1">Order Date:</div>
            <div class="itbms-order-date font-semibold text-gray-800">{{ orderData.orderDate }}</div>
          </div>
          <div>
            <div class="text-sm text-gray-500 mb-1">Payment Date:</div>
            <div class="itbms-payment-date font-semibold text-gray-800">{{ orderData.paymentDate }}</div>
          </div>
          
          <div>
            <div class="text-sm text-gray-500 mb-1">Total:</div>
            <div class="itbms-total-order-price font-bold text-gray-800">{{ orderData.total.toLocaleString() }}</div>
          </div>
          <div>
            <div class="itbms-order-status text-sm text-gray-500 mb-1">Status:</div>
            <div :class="[
              'font-semibold',
              orderData.status === 'Completed' ? 'text-green-600' : 
              orderData.status === 'Cancelled' ? 'text-red-600' : 
              'text-yellow-600'
            ]">
              {{ orderData.status }}
            </div>
          </div>
          
          <div class="col-span-2">
            <div class="text-sm text-gray-500 mb-1">Ship To:</div>
            <div class="Itbms-shipping-address  font-medium text-gray-800">{{ orderData.shippingAddress }}</div>
          </div>
          
          <div v-if="orderData.note" class="col-span-2">
            <div class="text-sm text-gray-500 mb-1">Note:</div>
            <div class="Itbms-order-note font-medium text-gray-800">{{ orderData.note }}</div>
          </div>
        </div>

        <!-- Order Items -->
        <div class="p-6">
          <div class="space-y-4">
            <div 
              v-for="item in orderData.items" 
              :key="item.id"
              class="flex items-center gap-4 p-4 bg-gray-50 rounded-lg"
            >
              <img
                :src="item.image"
                :alt="item.description"
                class="w-20 h-20 object-cover rounded"
              />
              
              <div class="flex-1">
                <div class="Itbms-item-description font-medium text-gray-800 mb-1">{{ item.description }}</div>
              </div>
              
              <div class="text-center min-w-[80px]">
                <div class="text-sm text-gray-500">Qty:</div>
                <div class="Itbms-item-quantity i font-semibold text-gray-800">{{ item.quantity }}</div>
              </div>
              
              <div class="text-center min-w-[120px]">
                <div class="text-sm text-gray-500">Unit Price:</div>
                <div class="itbms-item-price font-semibold text-gray-800">{{ item.unitPrice.toLocaleString() }}</div>
              </div>
              
              <div class="text-right min-w-[140px]">
                <div class="text-sm text-gray-500">Price:</div>
                <div class="itbms-item-total-price font-bold text-gray-800 text-lg">{{ item.totalPrice.toLocaleString() }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- No Data -->
      <div v-else class="bg-white rounded-lg shadow-md border border-gray-200 p-12 text-center">
        <p class="text-gray-500">Order not found.</p>
        <button 
          @click="goBack"
          class="mt-4 px-6 py-2 bg-purple-600 text-white rounded-md hover:bg-purple-700 transition"
        >
          Back to Orders
        </button>
      </div>
    </div>
  </div>
</template>
