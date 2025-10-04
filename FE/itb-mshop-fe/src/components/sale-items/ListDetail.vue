// ListDetails
<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ref, onMounted, computed } from 'vue'
import { getItemById, deleteItemById } from '@/lib/fetchUtils'
import { handleQueryAlerts } from '@/lib/alertMessage'
import phoneImg from '../../../public/phone.png';
import DeleteSaleItem from './DeleteSaleItem.vue';
import Search from './gallery/Search.vue';
import ShoppingCart from './gallery/ShoppingCart.vue';
import { useUser } from '@/composables/useUser';

const url = `${import.meta.env.VITE_APP_URL}/sale-items`
const url2 = `${import.meta.env.VITE_APP_URL2}/sale-items`

const route = useRoute()
const router = useRouter()
const id = route.params.id

const { userRole, userId, isLoading } = useUser();

const showModal = ref(false)
const product = ref( {
      "id": 1,
      "model": "iPhone 14 Pro Max",
      "brandName": "Apple",
      "description": "ไอโฟนเรือธงรุ่นล่าสุด มาพร้อม Dynamic Island จอใหญ่สุดในตระกูล กล้องระดับโปร",
      "price": 42900,
      "ramGb": 6,
      "screenSizeInch": 6.7,
      "quantity": 6,
      "storageGb": 512,
      "color": "Space Black",
      "createdOn": "2023-03-01T10:00:00Z",
      "updatedOn": "2025-09-26T11:17:35Z"
    })
const images = ref([])
const showSuccessMessage = ref(false)
const successMessage = ref('')

// Cart functionality
const cartQuantity = ref(1)

// Check if current user is the owner of the item
const isOwner = ref(false) // NOTE - TEST ONLY
// const isOwner = computed(() => {
//   if (!userId.value || !product.value) return false
//   return product.value.sellerId === userId.value
// })

// Show cart buttons for buyers or sellers viewing other's items
const showCartButtons = ref(true) // NOTE - TEST ONLY
// const showCartButtons = computed(() => {
//   if (!userRole.value) return false
//   const role = userRole.value
//   return (role === 'BUYER' || role === 'SELLER') && !isOwner.value
// })


// Show edit/delete buttons for owners only
const showOwnerButtons = computed(() => {
  return isOwner.value
})

function decreaseQuantity() {
  if (cartQuantity.value > 1) {
    cartQuantity.value--
  }
}

function increaseQuantity() {
  if (cartQuantity.value < product.value.quantity) {
    cartQuantity.value++
  }
}

async function addToCart() {
  try {
    // Implement your add to cart logic here
    console.log(`Adding ${cartQuantity.value} items to cart`)
    
    successMessage.value = `Added ${cartQuantity.value} item(s) to cart`
    showSuccessMessage.value = true
    setTimeout(() => {
      showSuccessMessage.value = false
    }, 3000)
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
    const item = await deleteItemById(url2, id)
    if (typeof item === 'number') {
      showModal.value = false
      router.push({ name: 'ListGallery', query: {failed_delete: true} })
      return
    }
  } catch (error) {
    console.error('Failed to fetch product:', error);
  }

  showModal.value = false
  router.push({ name: 'ListGallery', query: {deleted: true} })
}

async function loadImages() {
  if (!product.value?.saleItemImages) {
    console.warn('No images found for this product');
    return;
  }

  images.value = [];

  const sortedImages = [...product.value.saleItemImages]
    .sort((a, b) => (a.imageViewOrder || 0) - (b.imageViewOrder || 0));

  for (let i = 0; i < sortedImages.length; i++) {
    try {
      if (sortedImages[i] && sortedImages[i].fileName) {
        images.value.push(`${url}/picture/${sortedImages[i].fileName}`);
      }
    } catch (error) {
      console.warn(`Failed to load image ${i}:`, error);
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
    const item = await getItemById(url2, id)
    if (typeof item === 'number') {
      router.push({name: 'ListGallery'})
      alert('The requested sale item does not exist.')
      return
    }
    product.value = item;

    await loadImages();

  } catch (error) {
    console.error('Failed to fetch product:', error);
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
          <div v-if="showOwnerButtons" class="flex flex-wrap gap-4 pt-6">
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

          <!-- Buttons - Buyer/Non-Owner View (Quantity Controls + Add to Cart) -->
          <div v-else-if="showCartButtons" class="flex justify-between space-x-4">
            <!-- Quantity Controls -->
            <div v-if="product.quantity > 0" class="flex items-center gap-4">
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
                  :disabled="cartQuantity >= product.quantity"
                  class="itbms-inc-qty-button px-3 py-2 flex items-center justify-center rounded bg-gray-600 hover:bg-gray-500 disabled:opacity-50 disabled:cursor-not-allowed text-white font-bold transition"
                >
                  +
                </button>
              </div>
            </div>

            <!-- Add to Cart Button -->
            <button
              @click="addToCart"
              :disabled="product.quantity === 0"
              class="itbms-add-to-cart-quantity px-5 py-3 bg-purple-600 hover:bg-purple-500 disabled:bg-gray-600 disabled:cursor-not-allowed text-white rounded-lg font-medium transition shadow-md hover:shadow-lg"
            >
              {{ product.quantity === 0 ? 'Out of Stock' : 'Add to Cart' }}
            </button>
          </div>

          <!-- No buttons for non-authenticated users -->
          <div v-else class="pt-6 text-sm text-gray-400">
            Please log in to purchase this item
          </div>
        </div>
      </div>

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
