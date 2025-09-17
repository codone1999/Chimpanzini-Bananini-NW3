//ListDetails
<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ref, onMounted } from 'vue'
import { getItemById, deleteItemById } from '@/lib/fetchUtils'
import { handleQueryAlerts } from '@/lib/alertMessage'
import phoneImg from '../../../public/phone.png';
import DeleteSaleItem from './DeleteSaleItem.vue';

const url = `${import.meta.env.VITE_APP_URL}/sale-items`
const url2 = `${import.meta.env.VITE_APP_URL2}/sale-items`

const route = useRoute()
const router = useRouter()
const id = route.params.id

const showModal = ref(false)
const product = ref()

const images = ref([]) // pull image filename from v2 and then show image in v1

const showSuccessMessage = ref(false)
const successMessage = ref('')

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
  // Check if product and images array exist
  if (!product.value?.saleItemImages) {
    console.warn('No images found for this product');
    return;
  }

  // Clear existing images
  images.value = [];

  // Sort images by imageViewOrder to display in correct order
  const sortedImages = [...product.value.saleItemImages]
    .sort((a, b) => (a.imageViewOrder || 0) - (b.imageViewOrder || 0));

  // Load each image with error handling
  for (let i = 0; i < sortedImages.length; i++) {
    try {
      if (sortedImages[i] && sortedImages[i].fileName) {
        // Use the sorted order, not the original array index
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

          <!-- Buttons -->
          <div class="flex flex-wrap gap-4 pt-6">
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
