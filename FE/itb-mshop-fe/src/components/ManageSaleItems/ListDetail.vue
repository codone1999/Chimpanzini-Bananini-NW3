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
const product = ref({
    "id": 1,
    "model": "Galaxy S23 Ultra",
    "brandName": "Samsung",
    "price": 39600,
    "storageGb": 512,
    "ramGb": null,
    "color": null
  },)

const images = ref([phoneImg, phoneImg, phoneImg, phoneImg]) // pull image filename from v2 and then show image in v1

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

  // Load each image with error handling
  for (let i = 0; i < Math.min(4, product.value.saleItemImages.length); i++) {
    try {
      // Only try to load if the image ID exists
      if (product.value.saleItemImages[i]) {
        images.value[i] = `${url}/picture/${product.value.saleItemImages[i].fileName}`;
      } else {
        images.value[i] = null; // No image at this index
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
  <div class="min-h-screen bg-gray-100">
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
          class="itbms-home-button text-purple-600 hover:underline font-medium"
        >
          Home
        </router-link>
        <span>/</span>
        <div class="text-gray-800 font-medium" >
            <span>{{ product.model }}</span>&thinsp;
            <span>{{ product.ramGb ?? "" }}</span>/<span>{{ product.storageGb ?? "" }}</span>GB
            <span>{{ product.color ?? "" }}</span>
        </div>
      </div>
  
      <!-- Product Layout -->
      <div class="grid grid-cols-1 md:grid-cols-2 bg-white rounded-2xl shadow-lg px-8 py-3 gap-8">
        <!-- Image Section -->
        <div class="flex flex-col items-center space-y-5">
          <img
            :src="phoneImg"
            class="w-full object-cover bg-gray-200 rounded-lg"
          />
          <div class="flex space-x-3 items-center justify-center">
            <img
              v-for="(image, index) in images" 
              :key="index"
              :src="image"
              :alt="`Product View ${index + 1}`"
              class="w-3/13 rounded bg-gray-100 object-cover"
            />
          </div>
        </div>

        <!-- Product Details -->
        <div class="space-y-4 pt-10 text-gray-700 text-base">
          <p><strong>Brand:</strong> <span class="itbms-brand">{{ product.brandName }}</span></p>
          <p><strong>Model:</strong> <span class="itbms-model">{{ product.model }}</span></p>
          <p><strong>Price:</strong> <span class="itbms-price">{{ product.price.toLocaleString() }}</span> Baht</p>
          <p><strong>Description:</strong> <span class="itbms-description">{{ product.description }}</span></p>
          <p>
            <strong>RAM:</strong> <span class="itbms-ramGb">{{ product.ramGb ?? "-" }}</span>
            <span class="itbms-ramGb-unit"> GB</span>
          </p>
          <p>
            <strong>Screen Size:</strong> <span class="itbms-screenSizeInch">{{ product.screenSizeInch ?? "-" }}</span>
            <span class="itbms-screenSizeInch-unit"> Inches</span>
          </p>
          <p>
            <strong>Storage:</strong> <span class="itbms-storageGb">{{ product.storageGb ?? "-" }}</span>
            <span class="itbms-storageGb-unit"> GB</span>
          </p>
          <p><strong>Color:</strong> <span class="itbms-color">{{ product.color ?? "-" }}</span></p>
          <p><strong>Available Quantity:</strong> <span class="itbms-quantity">{{ product.quantity }}</span> units</p>

          <!-- Buttons -->
          <div class="flex flex-wrap gap-4 pt-4">
            <router-link
              :to="{ name: 'EditItem', params: { id: product.id }, query: { from: 'Gallery' } }"
              class="itbms-edit-button bg-purple-600 hover:bg-purple-700 text-white px-6 py-2 rounded-lg font-medium transition"
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
