<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ref, onMounted, computed, watch } from 'vue'
import { getItemById, deleteItemByIdAndToken } from '@/lib/fetchUtils'
import { handleQueryAlerts } from '@/lib/alertMessage'
import { getAccessToken } from '@/lib/authUtils'
import { useUser } from '@/composables/useUser'
import { useCart } from '@/composables/useCart'
import phoneImg from '../../../public/phone.png'
import DeleteSaleItem from './DeleteSaleItem.vue'
import Search from './controls/Search.vue'
import ShoppingCart from './controls/ShoppingCart.vue'

const route = useRoute()
const router = useRouter()
const id = route.params.id

const { userId, isLoading: userLoading, loadCompleteUserData } = useUser()
const { addToCart: addItemToCart, cartItems } = useCart()

const url = `${import.meta.env.VITE_APP_URL}/sale-items`
const url2 = `${import.meta.env.VITE_APP_URL2}/sale-items`
const deleteUrl = `${import.meta.env.VITE_APP_URL2}/seller/${userId.value}/sale-item`

const showModal = ref(false)
const product = ref()
const images = ref([])
const showSuccessMessage = ref(false)
const successMessage = ref('')

// Cart functionality
const cartQuantity = ref(1)

// Function to get current cart quantity for this product
const currentCartQuantity = computed(() => {
  if (!product.value) return 0
  const cartItem = cartItems.value.find(item => item.id === product.value.id)
  return cartItem ? cartItem.quantity : 0
})

// Maximum quantity that can be added (stock - already in cart)
const maxAddableQuantity = computed(() => {
  if (!product.value) return 0
  return Math.max(0, product.value.quantity - currentCartQuantity.value)
})

// Check if current user is the owner of the item
const isOwner = computed(() => {
  // Don't show owner buttons while user data is loading
  if (userLoading.value) return false
  if (!userId.value || !product.value) return false
  return product.value.sellerId === userId.value
})

// Show cart buttons for non-logged-in users, buyers, or sellers viewing other's items
const showCartButtons = computed(() => {
  // Show cart buttons while loading
  if (userLoading.value) return true
  
  // Show cart buttons if no user is logged in (not authenticated)
  if (!userId.value) return true
  
  // Show cart buttons if user is logged in but not the owner
  if (!product.value) return false
  return !isOwner.value
})

// Show edit/delete buttons for owners only
const showOwnerButtons = computed(() => {
  return isOwner.value
})

// Watch for user data changes and re-check ownership
watch([userId, userLoading], async ([newUserId, newLoading]) => {
  // When user data finishes loading and we have a userId
  if (!newLoading && newUserId && product.value) {
    // Force re-computation of isOwner by triggering reactivity
    // (This happens automatically with computed, but watch ensures timing)
  }
})

// Watch for changes in max addable quantity and adjust cartQuantity if needed
watch(maxAddableQuantity, (newMax) => {
  if (cartQuantity.value > newMax) {
    cartQuantity.value = Math.max(1, newMax)
  }
})

function decreaseQuantity() {
  if (cartQuantity.value > 1) {
    cartQuantity.value--
  }
}

function increaseQuantity() {
  // Can't exceed stock or what's already available after cart items
  if (cartQuantity.value < maxAddableQuantity.value) {
    cartQuantity.value++
  }
}

async function addToCart() {
  try {
    // Wait for user data if still loading
    if (userLoading.value) {
      await loadCompleteUserData()
    }

    // Check if user is logged in - redirect to login if not
    if (!userId.value) {
      router.push({ name: 'Login' })
      return
    }
    
    // Check if there's enough stock available
    if (maxAddableQuantity.value === 0) {
      alert('Cannot add more items. You already have the maximum available quantity in your cart.')
      return
    }
    
    // Adjust quantity if it exceeds available
    const quantityToAdd = Math.min(cartQuantity.value, maxAddableQuantity.value)
    
    if (quantityToAdd < cartQuantity.value) {
      alert(`Only ${quantityToAdd} items can be added. You already have ${currentCartQuantity.value} in your cart.`)
    }
    
    addItemToCart(product.value, quantityToAdd)
    
    // Reset quantity to 1 after adding
    cartQuantity.value = 1
  } catch (error) {
    console.error('Failed to add to cart:', error)
    alert('Failed to add item to cart')
  }
}

function confirmDelete() {
  showModal.value = true
}

async function handleDelete() {
  try {
    const item = await deleteItemByIdAndToken(deleteUrl, id, getAccessToken())
    if (typeof item === 'number') {
      showModal.value = false
      router.push({ name: 'ListGallery', query: {failed_delete: true} })
      return
    }
  } catch (error) {
    console.error('Failed to fetch product:', error)
  }

  showModal.value = false
  router.push({ name: 'ListGallery', query: {deleted: true} })
}

async function loadImages() {
  if (!product.value?.saleItemImages) {
    console.warn('No images found for this product')
    return
  }

  images.value = []

  const sortedImages = [...product.value.saleItemImages]
    .sort((a, b) => (a.imageViewOrder || 0) - (b.imageViewOrder || 0))

  for (let i = 0; i < sortedImages.length; i++) {
    try {
      if (sortedImages[i] && sortedImages[i].fileName) {
        images.value.push(`${url}/picture/${sortedImages[i].fileName}`)
      }
    } catch (error) {
      console.warn(`Failed to load image ${i}:`, error)
    }
  }
}

onMounted(async () => {
  handleQueryAlerts(
   { edited: 'The sale item has been updated.'},
   showSuccessMessage,
   successMessage
  )
  
  try {
    // Ensure user data is loaded first
    if (userLoading.value) {
      await loadCompleteUserData()
    }

    const item = await getItemById(url2, id)
    if (typeof item === 'number') {
      router.push({name: 'ListGallery'})
      alert('The requested sale item does not exist.')
      return
    }
    product.value = item

    await loadImages()

  } catch (error) {
    console.error('Failed to fetch product:', error)
  }
})
</script>

<template>
  <div class="min-h-screen bg-gray-900 text-gray-100 font-sans">
    <div class="max-w-6xl mx-auto py-12 px-4 itbms-row">
      <!-- Success Message -->
      <transition name="fade">
        <div 
          v-if="showSuccessMessage" 
          class="itbms-message mb-6 p-4 text-green-800 bg-green-100 border border-green-300 rounded-lg shadow-sm text-sm font-medium"
        >
          {{ successMessage }}
        </div>
      </transition>

      <h2 class="text-3xl font-extrabold mb-10 text-center bg-gradient-to-r from-purple-400 to-indigo-500 bg-clip-text text-transparent">
        Product Details
      </h2>

      <!-- Search Box with Cart -->
      <div class="flex mb-10 relative max-w-xl mx-auto">
        <Search
          v-model="search"
          @search="handleSearch"
        />
        <div class="absolute -right-16">
          <ShoppingCart />
        </div>
      </div>

      <!-- Loading -->
      <div v-if="isLoading || !product" class="text-center text-gray-400 py-10">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-purple-500"></div>
        <p class="mt-2">Loading user data...</p>
      </div>
  
      <template v-else>
        <!-- Breadcrumb + Title -->
        <div class="mb-6 text-xl text-gray-500 flex items-center space-x-2">
          <router-link
            :to="{ name: 'ListGallery' }"
            class="itbms-home-button text-purple-400 hover:underline font-medium"
          >
            Home
          </router-link>
          <span>/</span>
          <div class="text-gray-200 font-medium" >
              <span>{{ product.model }}</span>&thinsp;
              <span>{{ product.ramGb ?? "" }}</span>/<span>{{ product.storageGb ?? "" }}</span>GB
              <span>{{ product.color ?? "" }}</span>
          </div>
        </div>
        
        <!-- Product Layout -->
        <div class="grid grid-cols-1 md:grid-cols-2 bg-gray-800 border-gray-700 rounded-2xl shadow-2xl px-8 py-6 gap-8">
          <!-- Image Section -->
          <div class="flex flex-col items-center space-y-5">
            <img
              :src="phoneImg"
              class="w-full object-cover bg-gray-700 rounded-lg shadow-md border border-gray-700"
            />
            <div class="flex space-x-3 items-center justify-center">
              <img
                v-for="(image, index) in images" 
                :key="index"
                :src="image"
                :alt="`Product View ${index + 1}`"
                class="w-20 h-20 rounded-lg bg-gray-800 border border-gray-700 object-cover cursor-pointer hover:border-purple-500 transition"
              />
            </div>
          </div>
  
          <!-- Product Details -->
          <div class="space-y-6 mt-6 md:mt-10">
            <!-- Header -->
            <div>
              <p class="text-sm text-purple-400 uppercase tracking-wide">{{ product.brandName }}</p>
              <h2 class="text-2xl font-bold text-gray-100">{{ product.model }}</h2>
              <p class="itbms-description text-gray-400">{{ product.description }}</p>
            </div>
  
            <!-- Price + Stock -->
            <div class="flex items-center justify-between border-t border-gray-700 pt-4">
              <h3 class="itbms-price text-2xl font-bold bg-gradient-to-r from-purple-400 to-indigo-400 bg-clip-text text-transparent">
                {{ product.price.toLocaleString() }} Baht
              </h3>
              <span
                class="itbms-quantity flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-900/40 text-emerald-300 border border-emerald-700 text-sm font-medium"
              >
                {{ product.quantity }} items in stock
              </span>
            </div>
  
            <!-- Product Description -->
            <h3 class="text-lg font-semibold text-gray-100">Product Description</h3>
            <div class="divide-y divide-gray-700 border border-gray-700 rounded-lg">
              <div class="flex justify-between px-4 py-3">
                <span class="text-sm font-medium text-purple-300">Brand</span>
                <span class="itbms-brand text-gray-200">{{ product.brandName }}</span>
              </div>
              <div class="flex justify-between px-4 py-3">
                <span class="text-sm font-medium text-purple-300">Model</span>
                <span class="itbms-model text-gray-200">{{ product.model }}</span>
              </div>
              <div class="flex justify-between px-4 py-3">
                <span class="text-sm font-medium text-purple-300">Storage</span>
                <span class="text-gray-200">
                  <span class="itbms-storageGb">{{ product.storageGb ?? "-" }}</span>
                  <span class="itbms-storageGb-unit ml-2">GB</span>
                </span>
              </div>
              <div class="flex justify-between px-4 py-3">
                <span class="text-sm font-medium text-purple-300">RAM</span>
                <span class="text-gray-200">
                  <span class="itbms-ramGb">{{ product.ramGb ?? "-" }}</span>
                  <span class="itbms-ramGb-unit ml-2">GB</span>
                </span>
              </div>
              <div class="flex justify-between px-4 py-3">
                <span class="text-sm font-medium text-purple-300">Screen Size</span>
                <span class="text-gray-200">
                  <span class="itbms-screenSizeInch">{{ product.screenSizeInch ?? "-" }}</span>
                  <span class="itbms-screenSizeInch-unit ml-2">Inches</span>
                </span>
              </div>
              <div class="flex justify-between px-4 py-3">
                <span class="text-sm font-medium text-purple-300">Color</span>
                <span class="itbms-color text-gray-200">{{ product.color ?? "-" }}</span>
              </div>
            </div>
  
            <!-- Buttons - Owner View (Edit/Delete) -->
            <div v-if="showOwnerButtons" class="flex flex-wrap gap-4">
              <router-link
                :to="{ name: 'EditItem', params: { id: product.id }, query: { from: 'Gallery' } }"
                class="itbms-edit-button bg-purple-600 hover:bg-purple-500 text-white px-6 py-2 rounded-lg font-medium transition shadow-md hover:shadow-lg"
              >
                Edit
              </router-link>
              <button
                @click="confirmDelete()"
                class="itbms-delete-button bg-gray-200 hover:bg-gray-300 text-gray-700 px-6 py-2 rounded-lg font-medium transition"
              >
                Delete
              </button>
            </div>
  
            <!-- Buttons - Buyer/Non-Owner/Non-Logged-In View (Quantity Controls + Add to Cart) -->
            <div v-else-if="showCartButtons" class="space-y-4">
              <!-- Stock and Cart Info -->
              <div v-if="currentCartQuantity > 0" class="text-sm bg-gray-700/50 px-4 py-2 rounded-lg border border-gray-600">
                <div class="flex items-center justify-between">
                  <span class="text-gray-300">
                    <span class="text-purple-400 font-medium">{{ currentCartQuantity }}</span> 
                    {{ currentCartQuantity === 1 ? 'item' : 'items' }} already in cart
                  </span>
                  <span v-if="maxAddableQuantity > 0" class="text-emerald-400 font-medium">
                    +{{ maxAddableQuantity }} available
                  </span>
                  <span v-else class="text-amber-400 font-medium flex items-center gap-1">
                    <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-11a1 1 0 10-2 0v2H7a1 1 0 100 2h2v2a1 1 0 102 0v-2h2a1 1 0 100-2h-2V7z" clip-rule="evenodd"/>
                    </svg>
                    Maximum reached
                  </span>
                </div>
              </div>
  
              <div class="flex justify-between space-x-4">
                <!-- Quantity Controls -->
                <div v-if="maxAddableQuantity > 0" class="flex items-center gap-4">
                  <span class="text-sm font-medium text-gray-300">Quantity:</span>
                  <div class="flex items-center gap-3 bg-gray-700 rounded-lg">
                    <button
                      @click="decreaseQuantity"
                      :disabled="cartQuantity <= 1"
                      class="itbms-dec-qty-button px-3 py-2 flex items-center justify-center rounded bg-gray-600 hover:bg-gray-500 disabled:opacity-50 disabled:cursor-not-allowed text-white font-bold transition"
                    >
                      −
                    </button>
                    <span class="itbms-cart-quantity px-6 text-center font-semibold text-gray-100">
                      {{ cartQuantity }}
                    </span>
                    <button
                      @click="increaseQuantity"
                      :disabled="cartQuantity >= maxAddableQuantity"
                      class="itbms-inc-qty-button px-3 py-2 flex items-center justify-center rounded bg-gray-600 hover:bg-gray-500 disabled:opacity-50 disabled:cursor-not-allowed text-white font-bold transition"
                    >
                      +
                    </button>
                  </div>
                </div>
  
                <!-- Add to Cart Button -->
                <button
                  @click="addToCart"
                  :disabled="maxAddableQuantity === 0"
                  :class="[
                    'itbms-add-to-cart-quantity px-5 py-3 rounded-lg font-medium transition shadow-md',
                    maxAddableQuantity === 0
                      ? 'bg-gray-600 text-gray-400 cursor-not-allowed'
                      : 'bg-purple-600 hover:bg-purple-500 text-white hover:shadow-lg'
                  ]"
                >
                  <template v-if="product.quantity === 0">
                    Out of Stock
                  </template>
                  <template v-else-if="maxAddableQuantity === 0">
                    <span class="flex items-center gap-2">
                      <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                      </svg>
                      Maximum Reached
                    </span>
                  </template>
                  <template v-else>
                    Add to Cart
                  </template>
                </button>
              </div>
            </div>
          </div>
        </div>
      </template>

    </div>
  
    <!-- Delete Confirmation Modal -->
    <DeleteSaleItem 
      :show-modal="showModal"
      :not-show-model="function(){showModal = false}"
      :handle-delete="handleDelete"
    />
  </div>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
