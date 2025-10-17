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
    return `${import.meta.env.VITE_APP_URL}/sale-items/picture/${item.saleItem.saleItemImages[0].fileName}`
  }
  return phoneImg
}

onMounted(async () => {
  if (userId.value) {
    loadViewedOrderIds()
  }
  
  if (canLoadData.value && hasCompleteUserData.value) {
    await fetchOrders()
  }
})

watch(canLoadData, (newValue) => {
  if (newValue && hasCompleteUserData.value) {
    fetchOrders()
  }
})

watch(activeTab, () => {
  // Could fetch different data based on tab if needed
})

watch(userId, (newUserId, oldUserId) => {
  if (newUserId && newUserId !== oldUserId) {
    loadViewedOrderIds()
  }
})
</script>

<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="max-w-6xl mx-auto">
      <h1 class="text-3xl font-bold text-gray-800 mb-6">Your Orders</h1>

      <div v-if="isLoading || !canLoadData" class="text-center py-20">
        <div class="animate-spin w-12 h-12 border-4 border-purple-500 border-t-transparent rounded-full mx-auto mb-4"></div>
        <p class="text-gray-600">Loading your orders...</p>
      </div>

      <template v-else>
        <!-- Tab: New Orders, Cancelled, All Orders -->
        <div class="flex gap-4 mb-6 border-b border-gray-300">
          <button
            @click="activeTab = 'NEW'"
            :class="[
              'pb-3 px-4 font-medium transition-colors',
              activeTab === 'NEW'
                ? 'text-purple-600 border-b-2 border-purple-600'
                : 'text-gray-600 hover:text-gray-800'
            ]"
          >
            New Orders
            <span v-if="orders.filter(o => !viewedOrderIds.has(o.id)).length + cancelledOrders.filter(o => !viewedOrderIds.has(o.id)).length > 0" 
              class="ml-2 bg-red-500 text-white text-xs px-2 py-1 rounded-full">
              {{ orders.filter(o => !viewedOrderIds.has(o.id)).length + cancelledOrders.filter(o => !viewedOrderIds.has(o.id)).length }}
            </span>
          </button>
          <button
            @click="activeTab = 'CANCELLED'"
            :class="[
              'pb-3 px-4 font-medium transition-colors',
              activeTab === 'CANCELLED'
                ? 'text-purple-600 border-b-2 border-purple-600'
                : 'text-gray-600 hover:text-gray-800'
            ]"
          >
            Cancelled
          </button>
          <button
            @click="activeTab = 'ALL'"
            :class="[
              'pb-3 px-4 font-medium transition-colors',
              activeTab === 'ALL'
                ? 'text-purple-600 border-b-2 border-purple-600'
                : 'text-gray-600 hover:text-gray-800'
            ]"
          >
            All Orders
          </button>
        </div>

        <!-- Filter & Sort -->
        <div class="flex flex-col md:flex-row justify-between gap-4 items-start w-full mb-8 bg-gray-100 p-4 rounded-lg shadow-sm">
          <div class="flex gap-2 items-center">
            <label class="text-sm font-medium text-gray-700">Sort by:</label>
            <select 
              v-model="sortField" 
              @change="changeSort(sortField)"
              class="px-3 py-2 border border-gray-300 rounded-lg bg-white text-gray-800 focus:outline-none focus:ring-2 focus:ring-purple-500"
            >
              <option value="createdOn">Order Date</option>
              <option value="paymentDate">Payment Date</option>
            </select>
            
            <button
              @click="sortDirection = 'asc'; fetchOrders()"
              :class="[
                'px-3 py-2 text-lg rounded-lg transition',
                sortDirection === 'asc'
                  ? 'bg-purple-600 text-white'
                  : 'bg-white text-purple-600 border border-purple-600 hover:bg-purple-50'
              ]"
              title="Sort Ascending"
            >
              <span class="material-icons">north</span>
            </button>
            
            <button
              @click="sortDirection = 'desc'; fetchOrders()"
              :class="[
                'px-3 py-2 text-lg rounded-lg transition',
                sortDirection === 'desc'
                  ? 'bg-purple-600 text-white'
                  : 'bg-white text-purple-600 border border-purple-600 hover:bg-purple-50'
              ]"
              title="Sort Descending"
            >
              <span class="material-icons">south</span>
            </button>
          </div>
          
          <div class="flex items-center gap-2">
            <label class="text-sm font-medium text-gray-700">Show:</label>
            <select 
              :value="pageSize"
              @change="handlePageSizeChange"
              class="px-3 py-2 border border-gray-300 rounded-lg bg-white text-gray-800 focus:outline-none focus:ring-2 focus:ring-purple-500"
            >
              <option :value="5">5</option>
              <option :value="10">10</option>
              <option :value="20">20</option>
              <option :value="50">50</option>
            </select>
          </div>
        </div>

        <!-- Loading  -->
        <div v-if="isLoadingOrders" class="text-center text-gray-600 py-10">
          <div class="inline-block animate-spin rounded-full h-6 w-6 border-b-2 border-purple-500"></div>
          <p class="mt-2">Loading orders...</p>
        </div>

        <div v-else class="itbms-row space-y-6">
          <div 
            v-for="order in displayOrders" 
            :key="order.id" 
            class="Itbms-item-row bg-white rounded-lg shadow-md p-6 border border-gray-200"
            :class="{ 'ring-2 ring-purple-400': !viewedOrderIds.has(order.id) && activeTab === 'NEW' }"
          >
            <div class="flex justify-between items-start mb-4 pb-4 border-b border-gray-200">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-full bg-purple-600 flex items-center justify-center text-white font-bold">
                  {{ (order.seller[0].sellerName || 'U').charAt(0).toUpperCase() }}
                </div>
                <span class="itbms-nickname font-semibold text-gray-800">
                  {{ order.seller[0].sellerName || 'Unknown' }}
                </span>
                <span v-if="!viewedOrderIds.has(order.id)" 
                  class="bg-red-500 text-white text-xs px-2 py-1 rounded-full font-medium">
                  NEW
                </span>
              </div>
              <div>
                <div class="text-gray-500 mb-1">Order No:</div>
                <div class="itbms-order-id font-medium text-gray-800">{{ order.id }}</div>
              </div>
              <div>
                <div class="text-gray-500 mb-1">Order Date:</div>
                <div class="itbms-order-date font-medium text-gray-800">
                  {{ order.orderDate ? new Date(order.orderDate).toLocaleDateString() : '-' }}
                </div>
              </div>
              <div>
                <div class="text-gray-500 mb-1">Payment Date:</div>
                <div class="itbms-payment-date font-medium text-gray-800">
                  {{ order.paymentDate ? new Date(order.paymentDate).toLocaleDateString() : '-' }}
                </div>
              </div>
              <div>
                <div class="text-gray-500 mb-1">Total:</div>
                <div class="tbms-total-order-price font-bold text-gray-800">
                  ฿{{ calculateOrderTotal(order).toLocaleString() }}
                </div>
              </div>
              <div>
                <div class="text-gray-500 mb-1">Status:</div>
                <div :class="[
                  'itbms-order-status font-medium',
                  order.orderStatus === 'COMPLETED' ? 'text-green-600' : 'text-red-600'
                ]">
                  {{ order.orderStatus }}
                </div>
              </div>
            </div>

            <div class="mb-4 text-sm">
              <span class="font-medium text-gray-700">Ship To: </span>
              <span class="Itbms-shipping-address text-gray-600">{{ order.shippingAddress || '-' }}</span>
            </div>

            <div v-if="order.orderNote" class="mb-4 text-sm">
              <span class="font-medium text-gray-700">Note: </span>
              <span class="Itbms-order-note text-gray-600">{{ order.orderNote }}</span>
            </div>

            <div class="space-y-3">
              <div 
                v-for="item in order.orderItems" 
                :key="item.id" 
                class="flex items-center gap-4 p-3 bg-gray-50 rounded-lg"
              >
                <img
                  :src="getItemImage(item)"
                  :alt="item.saleItem?.model || 'Product'"
                  class="w-16 h-16 object-cover rounded"
                />
                <div class="flex-1">
                  <div class="Itbms-item-description font-medium text-gray-800">
                    <span>{{ item.description }}</span>
                  </div>
                </div>
                <div class="text-sm text-gray-600">
                  Qty: <span class="Itbms-item-quantity font-medium">{{ item.quantity }}</span>
                </div>
                <div class="font-bold text-gray-800 min-w-[120px] text-right">
                  Price: <span class="itbms-item-total-price">฿{{ (item.price * item.quantity).toLocaleString() }}</span>
                </div>
              </div>
            </div>

            <!-- View Details Button -->
            <div class="mt-4 pt-4 border-t border-gray-200 flex justify-end">
              <button
                @click="goToOrderDetails(order.id)"
                class="px-6 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition duration-200 font-medium"
              >
                View Details
              </button>
            </div>
          </div>
        </div>

        <div v-if="displayOrders.length === 0" class="text-center py-12 text-gray-500">
          <p class="text-lg">No {{ activeTab === 'NEW' ? 'new' : activeTab === 'CANCELLED' ? 'cancelled' : '' }} orders found.</p>
        </div>

        <div 
          class="flex justify-center items-center mt-10 gap-2 flex-wrap"
          :class="totalPages > 1 ? 'visible': 'invisible' "
        >
          <button 
            class="itbms-page-first px-5 py-2 text-lg rounded-lg border text-white bg-purple-600 
              disabled:opacity-50 hover:bg-purple-700 hover:shadow-md transition duration-200"
            @click="goToPage(1)" 
            :disabled="currentPage === 1"
          >
            First
          </button>

          <button 
            class="itbms-page-prev px-5 py-2 text-lg rounded-lg border text-white bg-purple-600 
            disabled:opacity-50 hover:bg-purple-700 hover:shadow-md transition duration-200"
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
                'bg-purple-600 text-white hover:bg-purple-700 hover:shadow-md': page === currentPage,
                'bg-white text-purple-600 border-purple-600 hover:bg-purple-50 hover:shadow-md': page !== currentPage
              },
              totalPages === 1 ? 'invisible' : 'visible'
            ]"
          >
            {{ page }}
          </button>

          <button 
            class="itbms-page-next px-5 py-2 text-lg rounded-lg border text-white bg-purple-600 
              disabled:opacity-50 hover:bg-purple-700 hover:shadow-md transition duration-200"
            @click="goToPage(currentPage + 1)" 
            :disabled="currentPage === totalPages"
          >
            Next
          </button>

          <button 
            class="itbms-page-last px-5 py-2 text-lg rounded-lg border text-white bg-purple-600 
              disabled:opacity-50 hover:bg-purple-700 hover:shadow-md transition duration-200"
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
