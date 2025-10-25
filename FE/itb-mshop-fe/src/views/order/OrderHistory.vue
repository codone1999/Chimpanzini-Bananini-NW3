// OrderHistory.vue
<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUser } from '@/composables/useUser'
import { getOrdersByIdWithToken } from '@/lib/fetchUtils'
import { getAccessToken } from '@/lib/authUtils'
import phoneImg from '../../../public/phone.png'

const router = useRouter()
const { userId, hasCompleteUserData, isLoading: userIsLoading } = useUser()

const activeTab = ref('NEW')
const isLoadingOrders = ref(false)

// Pagination state
const currentPage = ref(1)
const pageSize = ref(10)
const sortField = ref('createdOn')
const sortDirection = ref('desc')
const totalPages = ref(1)
const totalElements = ref(0)

// Order data
const orders = ref([])
const cancelledOrders = ref([])
const viewedOrderIds = ref(new Set())

// Computed property to determine when we can load data
const canLoadData = computed(() => {
  return !userIsLoading.value || !getAccessToken()
})

const displayOrders = computed(() => {
  if (activeTab.value === 'NEW') {
    // Show only orders that haven't been viewed yet
    return orders.value.filter(o => !viewedOrderIds.value.has(o.id))
  } else if (activeTab.value === 'CANCELLED') {
    // Show only cancelled orders
    return cancelledOrders.value
  } else {
    // ALL - show all orders (cancelledOrders is already included in orders)
    return orders.value
  }
})

const isLoading = computed(() => {
  return userIsLoading.value || isLoadingOrders.value
})

// Visible Pages for pagination
const visiblePages = computed(() => {
  const total = totalPages.value
  const current = currentPage.value
  const maxVisible = 10
  const half = Math.floor(maxVisible / 2)

  let start = Math.max(1, current - half)
  let end = Math.min(total, current + half)

  if (end - start + 1 < maxVisible) {
    if (start === 1) {
      end = Math.min(total, start + maxVisible - 1)
    } else if (end === total) {
      start = Math.max(1, total - maxVisible + 1)
    }
  }

  const pages = []
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
})

// Load viewed order IDs from localStorage
function loadViewedOrderIds() {
  try {
    const stored = localStorage.getItem('viewedOrderIds')
    if (stored) {
      const allData = JSON.parse(stored)
      const userKey = `user_${userId.value}`
      
      // Check if data structure is the new format (object with multiple users)
      if (allData[userKey]) {
        // Remove the "order_" prefix when loading into Set
        viewedOrderIds.value = new Set(
          allData[userKey].map(orderId => {
            if (typeof orderId === 'string' && orderId.startsWith('order_')) {
              // Remove "order_" prefix and convert to number
              const id = orderId.replace('order_', '')
              return isNaN(id) ? id : Number(id)
            }
            return orderId
          })
        )
      } else {
        viewedOrderIds.value = new Set()
      }
    }
  } catch (error) {
    console.error('Error loading viewed order IDs:', error)
    viewedOrderIds.value = new Set()
  }
}

// Save viewed order IDs to localStorage
function saveViewedOrderIds() {
  try {
    // Get existing data from localStorage
    const stored = localStorage.getItem('viewedOrderIds')
    let allData = {}
    
    if (stored) {
      allData = JSON.parse(stored)
    }
    
    // Create readable user key (e.g., "user_123" instead of just "123")
    const userKey = `user_${userId.value}`
    
    // Update only this user's viewed orders with readable order IDs
    allData[userKey] = [...viewedOrderIds.value].map(orderId => `order_${orderId}`)
    
    // Save back to localStorage with pretty formatting (2 spaces indentation)
    localStorage.setItem('viewedOrderIds', JSON.stringify(allData, null, 2))
  } catch (error) {
    console.error('Error saving viewed order IDs:', error)
  }
}

async function fetchOrders() {
  if (!hasCompleteUserData.value || userIsLoading.value) {
    return
  }

  if (currentPage.value < 1) currentPage.value = 1
  
  isLoadingOrders.value = true
  
  try {
    let url = `${import.meta.env.VITE_APP_URL2}/users/${userId.value}/orders?`
    
    const query = []
    query.push('page=' + (currentPage.value - 1))
    if (pageSize.value) query.push('size=' + pageSize.value)
    if (sortField.value) query.push('sortField=' + encodeURIComponent(sortField.value))
    if (sortDirection.value) query.push('sortDirection=' + sortDirection.value)
    
    const finalUrl = url + query.join('&')
    
    const response = await getOrdersByIdWithToken(finalUrl, getAccessToken())
    
    if (response && response.content && Array.isArray(response.content)) {
      orders.value = response.content
      cancelledOrders.value = response.content.filter(order => order.orderStatus === 'CANCELLED')
      totalPages.value = response.totalPages || 1
      totalElements.value = response.totalElements || 0
    } else if (Array.isArray(response)) {
      orders.value = response
      cancelledOrders.value = response.filter(order => order.orderStatus === 'CANCELLED')
      totalPages.value = 1
      totalElements.value = response.length
    }
    
    if (currentPage.value > totalPages.value && totalPages.value > 0) {
      currentPage.value = totalPages.value
    }
    
  } catch (error) {
    console.error('Failed to fetch orders:', error)
    orders.value = []
    cancelledOrders.value = []
  } finally {
    isLoadingOrders.value = false
  }
}

function goToPage(page) {
  currentPage.value = page
  fetchOrders()
}

function changeSort(field) {
  if (sortField.value === field) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortDirection.value = 'desc'
  }
  currentPage.value = 1
  fetchOrders()
}

function handlePageSizeChange(event) {
  const value = parseInt(event.target.value)
  pageSize.value = value
  currentPage.value = 1
  fetchOrders()
}

function goToOrderDetails(orderId) {
  // Mark this order as viewed
  viewedOrderIds.value.add(orderId)
  saveViewedOrderIds()
  
  router.push({ name: 'OrderDetail', params: { id: orderId } })
}

function calculateOrderTotal(order) {
  if (!order.orderItems || !Array.isArray(order.orderItems)) return 0
  return order.orderItems.reduce((sum, item) => sum + (item.price * item.quantity), 0)
}

function getItemImage(item) {
  if (item.saleItem?.saleItemImages?.[0]?.fileName) {
    return `${import.meta.env.VITE_APP_URL2}/sale-items-images/${item.saleItem.saleItemImages[0].fileName}`
  }
  return phoneImg
}

watch(userId, (newUserId) => {
  if (newUserId && canLoadData.value) {
    loadViewedOrderIds()
    fetchOrders()
  }
}, { immediate: true })

watch(activeTab, () => {
  currentPage.value = 1
})

onMounted(() => {
  if (userId.value && canLoadData.value) {
    loadViewedOrderIds()
    fetchOrders()
  }
})
</script>

<template>
  <div class="bg-gray-900 text-gray-100 font-sans min-h-screen py-8 px-4">
    <div class="max-w-7xl mx-auto">
      <template v-if="isLoading">
        <div class="text-center py-20">
          <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-purple-500"></div>
          <p class="mt-4 text-gray-400">Loading user data...</p>
        </div>
      </template>

      <template v-else>
        <!-- Header -->
        <div class="text-center mb-8">
          <h1 class="text-4xl font-extrabold bg-gradient-to-r from-purple-400 to-indigo-500 bg-clip-text text-transparent mb-3">
            Order History
          </h1>
          <p class="text-gray-400">
            Track and manage your orders
          </p>
        </div>

        <!-- Tabs -->
        <div class="flex justify-center gap-4 mb-8">
          <button
            @click="activeTab = 'NEW'"
            :class="[
              'px-6 py-3 rounded-lg font-medium transition-all duration-200 animate-bounce-in',
              activeTab === 'NEW'
                ? 'bg-purple-600 text-white shadow-lg'
                : 'bg-gray-800 text-gray-400 border border-gray-700 hover:bg-gray-700 hover:text-gray-200'
            ]"
            style="animation-delay: 0s"
          >
            New Orders
            <span v-if="orders.filter(o => !viewedOrderIds.has(o.id)).length + cancelledOrders.filter(o => !viewedOrderIds.has(o.id)).length > 0" 
              class="ml-2 bg-red-500 text-white text-xs px-2 py-1 rounded-full">
              {{ orders.filter(o => !viewedOrderIds.has(o.id)).length + cancelledOrders.filter(o => !viewedOrderIds.has(o.id)).length }}
            </span>
          </button>
          <button
            @click="activeTab = 'ALL'"
            :class="[
              'px-6 py-3 rounded-lg font-medium transition-all duration-200 animate-bounce-in',
              activeTab === 'ALL'
                ? 'bg-purple-600 text-white shadow-lg'
                : 'bg-gray-800 text-gray-400 border border-gray-700 hover:bg-gray-700 hover:text-gray-200'
            ]"
            style="animation-delay: 0.15s"
          >
            All Orders
          </button>
          <button
            @click="activeTab = 'CANCELLED'"
            :class="[
              'px-6 py-3 rounded-lg font-medium transition-all duration-200 animate-bounce-in',
              activeTab === 'CANCELLED'
                ? 'bg-purple-600 text-white shadow-lg'
                : 'bg-gray-800 text-gray-400 border border-gray-700 hover:bg-gray-700 hover:text-gray-200'
            ]"
            style="animation-delay: 0.3s"
          >
            Cancelled
            <span v-if="cancelledOrders.length > 0"
                  class="ml-2 bg-red-500 text-white text-xs px-2 py-1 rounded-full">
              {{ cancelledOrders.length }}
            </span>
          </button>
        </div>

        <!-- Filter and Sort Controls -->
        <div class="bg-gray-800 border border-gray-700 rounded-xl shadow-lg p-6 mb-8 flex flex-wrap justify-between items-center gap-4">
          <div class="flex items-center gap-3">
            <!-- Sort by -->
            <label class="text-sm font-medium text-purple-300">Sort by:</label>
            <select 
              v-model="sortField"
              @change="changeSort(sortField)"
              class="px-4 py-2 border border-gray-600 bg-gray-700 rounded-lg text-gray-100 focus:outline-none focus:ring-2 focus:ring-purple-500"
            >
              <option value="createdOn">Created On</option>
              <option value="orderDate">Order Date</option>
              <option value="paymentDate">Payment Date</option>
            </select>
            
            <!-- Ascending -->
            <button
              @click="sortDirection = 'asc'; fetchOrders()"
              :class="[
                'px-3 py-2 text-lg rounded-lg transition',
                sortDirection === 'asc'
                  ? 'bg-purple-600 text-white'
                  : 'bg-gray-700 text-purple-400 border border-purple-600 hover:bg-gray-600'
              ]"
              title="Sort Ascending"
            >
              <span class="material-icons">north</span>
            </button>
            
            <!-- Descending -->
            <button
              @click="sortDirection = 'desc'; fetchOrders()"
              :class="[
                'px-3 py-2 text-lg rounded-lg transition',
                sortDirection === 'desc'
                  ? 'bg-purple-600 text-white'
                  : 'bg-gray-700 text-purple-400 border border-purple-600 hover:bg-gray-600'
              ]"
              title="Sort Descending"
            >
              <span class="material-icons">south</span>
            </button>
          </div>
          
          <!-- Page Size -->
          <div class="flex items-center gap-2">
            <label class="text-sm font-medium text-purple-300">Show:</label>
            <select 
              :value="pageSize"
              @change="handlePageSizeChange"
              class="px-3 py-2 border border-gray-600 bg-gray-700 rounded-lg text-gray-100 focus:outline-none focus:ring-2 focus:ring-purple-500"
            >
              <option :value="5">5</option>
              <option :value="10">10</option>
              <option :value="20">20</option>
              <option :value="50">50</option>
            </select>
          </div>
        </div>

        <!-- Loading  -->
        <div v-if="isLoadingOrders" class="text-center text-gray-400 py-10">
          <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-purple-500"></div>
          <p class="mt-2">Loading orders...</p>
        </div>

        <div v-else class="itbms-row space-y-6 animate-slide-to-top">
          <div 
            v-for="order in displayOrders" 
            :key="order.id" 
            class="Itbms-item-row bg-gray-800 border border-gray-700 rounded-xl shadow-lg p-6"
            :class="{ 'ring-2 ring-purple-500': !viewedOrderIds.has(order.id) && activeTab === 'NEW' }"
          >
            <div class="flex justify-between items-start mb-4 pb-4 border-b border-gray-700">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-full bg-purple-600 flex items-center justify-center text-white font-bold">
                  {{ (order.seller[0].sellerName || 'U').charAt(0).toUpperCase() }}
                </div>
                <span class="itbms-nickname font-semibold text-gray-100">
                  {{ order.seller[0].sellerName || 'Unknown' }}
                </span>
                <span v-if="!viewedOrderIds.has(order.id)" 
                  class="bg-red-500 text-white text-xs px-2 py-1 rounded-full font-medium">
                  NEW
                </span>
              </div>
              <div>
                <div class="text-gray-400 text-sm mb-1">Order No:</div>
                <div class="itbms-order-id font-medium text-purple-300">{{ order.id }}</div>
              </div>
              <div>
                <div class="text-gray-400 text-sm mb-1">Order Date:</div>
                <div class="itbms-order-date font-medium text-gray-200">
                  {{ order.orderDate ? new Date(order.orderDate).toLocaleDateString() : '-' }}
                </div>
              </div>
              <div>
                <div class="text-gray-400 text-sm mb-1">Payment Date:</div>
                <div class="itbms-payment-date font-medium text-gray-200">
                  {{ order.paymentDate ? new Date(order.paymentDate).toLocaleDateString() : '-' }}
                </div>
              </div>
              <div>
                <div class="text-gray-400 text-sm mb-1">Total:</div>
                <div class="tbms-total-order-price font-bold text-purple-400">
                  ฿{{ calculateOrderTotal(order).toLocaleString() }}
                </div>
              </div>
              <div>
                <div class="text-gray-400 text-sm mb-1">Status:</div>
                <div :class="[
                  'itbms-order-status font-medium',
                  order.orderStatus === 'COMPLETED' ? 'text-emerald-400' : 'text-red-400'
                ]">
                  {{ order.orderStatus }}
                </div>
              </div>
            </div>

            <div class="mb-4 text-sm">
              <span class="font-medium text-purple-300">Ship To: </span>
              <span class="Itbms-shipping-address text-gray-300">{{ order.shippingAddress || '-' }}</span>
            </div>

            <div v-if="order.orderNote" class="mb-4 text-sm">
              <span class="font-medium text-purple-300">Note: </span>
              <span class="Itbms-order-note text-gray-300">{{ order.orderNote }}</span>
            </div>

            <div class="space-y-3">
              <div 
                v-for="item in order.orderItems" 
                :key="item.id" 
                class="flex items-center gap-4 p-3 bg-gray-700/50 border border-gray-600 rounded-lg"
              >
                <img
                  :src="getItemImage(item)"
                  :alt="item.saleItem?.model || 'Product'"
                  class="w-16 h-16 object-cover rounded border border-gray-600"
                />
                <div class="flex-1">
                  <div class="Itbms-item-description font-medium text-gray-100">
                    <span>{{ item.description }}</span>
                  </div>
                </div>
                <div class="text-sm text-gray-300">
                  Qty: <span class="Itbms-item-quantity font-medium text-purple-400">{{ item.quantity }}</span>
                </div>
                <div class="font-bold text-gray-100 min-w-[120px] text-right">
                  Price: <span class="itbms-item-total-price text-purple-400">฿{{ (item.price * item.quantity).toLocaleString() }}</span>
                </div>
              </div>
            </div>

            <!-- View Details Button -->
            <div class="mt-4 pt-4 border-t border-gray-700 flex justify-end">
              <button
                @click="goToOrderDetails(order.id)"
                class="px-6 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-500 transition duration-200 font-medium shadow-lg hover:shadow-xl"
              >
                View Details
              </button>
            </div>
          </div>
        </div>

        <div v-if="displayOrders.length === 0" class="text-center py-12 text-gray-400">
          <p class="text-lg">No {{ activeTab === 'NEW' ? 'new' : activeTab === 'CANCELLED' ? 'cancelled' : '' }} orders found.</p>
        </div>

        <div 
          class="flex justify-center items-center mt-10 gap-2 flex-wrap"
          :class="totalPages > 1 ? 'visible': 'invisible' "
        >
          <button 
            class="itbms-page-first px-5 py-2 text-lg rounded-lg border border-gray-700 text-white bg-purple-600 
              disabled:opacity-50 hover:bg-purple-500 hover:shadow-md transition duration-200"
            @click="goToPage(1)" 
            :disabled="currentPage === 1"
          >
            First
          </button>

          <button 
            class="itbms-page-prev px-5 py-2 text-lg rounded-lg border border-gray-700 text-white bg-purple-600 
            disabled:opacity-50 hover:bg-purple-500 hover:shadow-md transition duration-200"
            @click="goToPage(currentPage - 1)"
            :disabled="currentPage === 1"
          >
            Prev
          </button>

          <button
            v-for="page in visiblePages"
            :key="page"
            @click="goToPage(page)"
            class="px-5 py-2 text-lg rounded-lg border font-medium"
            :class="[
              `itbms-page-${page - 1}`,
              {
                'bg-purple-600 text-white border-gray-700 hover:bg-purple-500 hover:shadow-md': page === currentPage,
                'bg-gray-800 text-purple-400 border-purple-600 hover:bg-gray-700 hover:shadow-md': page !== currentPage
              },
              totalPages === 1 ? 'invisible' : 'visible'
            ]"
          >
            {{ page }}
          </button>

          <button 
            class="itbms-page-next px-5 py-2 text-lg rounded-lg border border-gray-700 text-white bg-purple-600 
              disabled:opacity-50 hover:bg-purple-500 hover:shadow-md transition duration-200"
            @click="goToPage(currentPage + 1)" 
            :disabled="currentPage === totalPages"
          >
            Next
          </button>

          <button 
            class="itbms-page-last px-5 py-2 text-lg rounded-lg border border-gray-700 text-white bg-purple-600 
              disabled:opacity-50 hover:bg-purple-500 hover:shadow-md transition duration-200"
            @click="goToPage(totalPages)"
            :disabled="currentPage === totalPages"
          >
            Last
          </button>
        </div>
      </template>
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

@keyframes slideInTop {
  0% {
    opacity: 0;
    transform: translateY(100px);
  }
  100% {
    opacity: 1;
    transform: translateY(0px);
  }
}

@keyframes bounceIn {
  0% {
    opacity: 0;
    transform: scale(0.3) translateY(-50px);
  }
  50% {
    opacity: 1;
    transform: scale(1.05);
  }
  70% {
    transform: scale(0.9);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

.animate-slide-in-left {
  animation: slideInLeft 0.6s ease-out forwards;
}

.animate-slide-in-right {
  animation: slideInRight 0.6s ease-out forwards;
}

.animate-slide-to-top {
  animation: slideInTop 0.6s ease-out forwards;
}

.animate-bounce-in {
  opacity: 0;
  animation: bounceIn 0.6s ease-out forwards;
}
</style>
