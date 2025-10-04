<script setup>
import { ref, computed } from 'vue'
import { useUser } from '@/composables/useUser'

const { userId, userNickname, hasCompleteUserData, isLoading: userIsLoading } = useUser()

const activeTab = ref('completed')
const isLoadingOrders = ref(false)

// Sample order data - replace with actual API calls
const orders = ref([
  {
    id: 123456,
    nickname: 'Somsuan',
    orderDate: 'October 1, 2025',
    paymentDate: 'October 1, 2025',
    total: 62700,
    status: 'Completed',
    shippingAddress: 'Somchai Jaidee, 123/45 Moo 6, T. Bangna, A. Bangna, Bangkok 10260',
    note: '',
    items: [
      {
        id: 1,
        image: 'https://images.unsplash.com/photo-1592286927505-b0501739739a?w=100&h=100&fit=crop',
        description: 'Apple iPhone 14 Pro Max (512GB, Space Black)',
        quantity: 1,
        price: 42900
      },
      {
        id: 2,
        image: 'https://images.unsplash.com/photo-1556656793-08538906a9f8?w=100&h=100&fit=crop',
        description: 'Apple iPhone 13 mini (128GB, Green)',
        quantity: 1,
        price: 19800
      }
    ]
  },
  {
    id: 123457,
    nickname: 'Somchai',
    orderDate: 'September 28, 2025',
    paymentDate: 'September 28, 2025',
    total: 35900,
    status: 'Completed',
    shippingAddress: 'Somchai Smith, 456/78 Moo 2, T. Sukhumvit, A. Watthana, Bangkok 10110',
    note: 'Please deliver after 5 PM',
    items: [
      {
        id: 1,
        image: 'https://images.unsplash.com/photo-1611472173362-3f53dbd65d80?w=100&h=100&fit=crop',
        description: 'Apple iPhone 12 Pro (256GB, Pacific Blue)',
        quantity: 1,
        price: 35900
      }
    ]
  }
])

const cancelledOrders = ref([
  {
    id: 123458,
    nickname: 'Malee',
    orderDate: 'September 25, 2025',
    paymentDate: '-',
    total: 28900,
    status: 'Cancelled',
    shippingAddress: 'Malee Brown, 789/12 Moo 4, T. Silom, A. Bangrak, Bangkok 10500',
    note: 'Customer requested cancellation',
    items: [
      {
        id: 1,
        image: 'https://images.unsplash.com/photo-1632661674596-df8be070a5c5?w=100&h=100&fit=crop',
        description: 'Apple iPhone 11 (128GB, Purple)',
        quantity: 1,
        price: 28900
      }
    ]
  }
])

const displayOrders = computed(() => {
  return activeTab.value === 'completed' ? orders.value : cancelledOrders.value
})

const isLoading = computed(() => {
  return userIsLoading.value || isLoadingOrders.value
})

// TODO: Replace with actual API call
async function fetchOrders() {
  if (!hasCompleteUserData.value) return
  
  isLoadingOrders.value = true
  try {
    // const response = await fetch(`${import.meta.env.VITE_APP_URL2}/orders/${userId.value}`)
    // const data = await response.json()
    // orders.value = data.completed
    // cancelledOrders.value = data.cancelled
  } catch (error) {
    console.error('Failed to fetch orders:', error)
  } finally {
    isLoadingOrders.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="max-w-6xl mx-auto">
      <h1 class="text-3xl font-bold text-gray-800 mb-6">Your Orders</h1>

      <!-- Loading State -->
      <div v-if="isLoading" class="text-center py-20">
        <div class="animate-spin w-12 h-12 border-4 border-purple-500 border-t-transparent rounded-full mx-auto mb-4"></div>
        <p class="text-gray-600">Loading your orders...</p>
      </div>

      <template v-else>
        <!-- Tabs -->
        <div class="flex gap-4 mb-6 border-b border-gray-300">
          <button
            @click="activeTab = 'completed'"
            :class="[
              'pb-3 px-4 font-medium transition-colors',
              activeTab === 'completed'
                ? 'text-purple-600 border-b-2 border-purple-600'
                : 'text-gray-600 hover:text-gray-800'
            ]"
          >
            Completed
          </button>
          <button
            @click="activeTab = 'cancelled'"
            :class="[
              'pb-3 px-4 font-medium transition-colors',
              activeTab === 'cancelled'
                ? 'text-purple-600 border-b-2 border-purple-600'
                : 'text-gray-600 hover:text-gray-800'
            ]"
          >
            Cancelled
          </button>
        </div>

        <!-- Orders List -->
        <div class="itbms-row space-y-6">
          <div 
            v-for="order in displayOrders" 
            :key="order.id" 
            class="Itbms-item-row bg-white rounded-lg shadow-md p-6 border border-gray-200"
          >
            <!-- Order Header -->
            <div class="flex justify-between items-start mb-4 pb-4 border-b border-gray-200">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-full bg-purple-600 flex items-center justify-center text-white font-bold">
                  {{ order.nickname.charAt(0).toUpperCase() }}
                </div>
                <span class="itbms-nickname font-semibold text-gray-800">{{ order.nickname }}</span>
              </div>
              <!-- Order No -->
              <div>
                <div class="text-gray-500 mb-1">Order No:</div>
                <div class="itbms-order-id font-medium text-gray-800">{{ order.id }}</div>
              </div>
              <!-- Order Date -->
              <div>
                <div class="text-gray-500 mb-1">Order Date:</div>
                <div class="itbms-order-date font-medium text-gray-800">{{ order.orderDate }}</div>
              </div>
              <!-- Payment Date -->
              <div>
                <div class="text-gray-500 mb-1">Payment Date:</div>
                <div class="itbms-payment-date font-medium text-gray-800">{{ order.paymentDate }}</div>
              </div>
              <!-- Total -->
              <div>
                <div class="text-gray-500 mb-1">Total:</div>
                <div class="tbms-total-order-price font-bold text-gray-800">{{ order.total.toLocaleString() }}</div>
              </div>
              <!-- Status -->
              <div class="col-start-4">
                <div class="text-gray-500 mb-1">Status:</div>
                <div :class="[
                  'itbms-order-status font-medium',
                  order.status === 'Completed' ? 'text-green-600' : 'text-red-600'
                ]">
                  {{ order.status }}
                </div>
              </div>
            </div>

            <!-- Shipping Address -->
            <div class="mb-4 text-sm">
              <span class="font-medium text-gray-700">Ship To: </span>
              <span class="Itbms-shipping-address text-gray-600">{{ order.shippingAddress }}</span>
            </div>

            <!-- Note -->
            <div v-if="order.note" class="mb-4 text-sm">
              <span class="font-medium text-gray-700">Note: </span>
              <span class="Itbms-order-note text-gray-600">{{ order.note }}</span>
            </div>

            <!-- Order Items -->
            <div class=" space-y-3">
              <div 
                v-for="item in order.items" 
                :key="item.id" 
                class="flex items-center gap-4 p-3 bg-gray-50 rounded-lg"
              >
                <img
                  :src="item.image"
                  :alt="item.description"
                  class="w-16 h-16 object-cover rounded"
                />
                <div class="flex-1">
                  <div class="Itbms-item-description  font-medium text-gray-800">{{ item.description }}</div>
                </div>
                <div class="text-sm text-gray-600">
                  Qty: <span class="Itbms-item-quantity font-medium">{{ item.quantity }}</span>
                </div>
                <div class="font-bold text-gray-800 min-w-[120px] text-right">
                  Price: <span class="itbms-item-total-price">{{ item.price.toLocaleString() }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div v-if="displayOrders.length === 0" class="text-center py-12 text-gray-500">
          <p class="text-lg">No {{ activeTab }} orders found.</p>
        </div>
      </template>
    </div>
  </div>
</template>
