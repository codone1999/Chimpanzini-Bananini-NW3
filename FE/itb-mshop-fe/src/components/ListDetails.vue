<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ref, onMounted } from 'vue'
import { getItemById, deleteItemById } from '@/lib/fetchUtils'
import phoneImg from '../../public/phone.jpg';

const route = useRoute()
const router = useRouter()
const id = route.params.id

const showModal = ref(false)
const product = ref()

const showSuccessMessage = ref(false)
const successMessage = ref('')

function confirmDelete() {
  showModal.value = true
}

async function handleDelete() {
  try {
    //const item = await deleteItemById('http://ip24nw3.sit.kmutt.ac.th:8080/v1/sale-items', id)
    const item = await deleteItemById('http://localhost:8080/v1/sale-items', id)
    if (!item || item?.status === 404 || item === 404) {
      // alert('The requested sale item does not exist.')
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

function handleQuerySuccess(type, message) {
  if (route.query[type] === 'true') {
    showSuccessMessage.value = true
    successMessage.value = message

    setTimeout(() => {
      showSuccessMessage.value = false
      const updatedQuery = { ...route.query }
      delete updatedQuery[type] // remove query param
      router.replace({ name: route.name, query: updatedQuery })
    }, 3000)
  }
}


onMounted(async () => {
  handleQuerySuccess('edited', 'The sale item has been updated.')
  
  try {
    //const item = await getItemById('http://ip24nw3.sit.kmutt.ac.th:8080/v1/sale-items', id)
    const item = await getItemById('http://localhost:8080/v1/sale-items', id)
    if (!item || item?.status === 404) {
      router.push('/sale-items')
      // alert('The requested sale item does not exist.')
      return
    }
    product.value = item;
  } catch (error) {
    console.error('Failed to fetch product:', error);
  }
})
</script>

<template>
  <div class="max-w-5xl mx-auto py-12 px-4 itbms-row">
    <!-- Success Message -->
    <div 
      v-if="showSuccessMessage" 
      class="itbms-message mb-6 p-4 text-green-800 bg-green-100 border border-green-300 rounded"
    >
      {{ successMessage }}
    </div>
    
    <h3 class="text-xl font-bold mb-4">
      <router-link
        :to="{ name: 'ListGallery'}"
        class="itbms-home-button text-blue-500"
      >
        Home
      </router-link>
      <span class="text-gray-400">> </span>
      <span class="itbms-model">{{ product.model }}</span>&thinsp;
      <span class="itbms-ramGb">{{ product.ramGb ?? "" }}</span>/
      <span class="itbms-storageGb">{{ product.storageGb ?? "" }}</span>GB
      <span class="itbms-color">{{ product.color ?? "" }}</span>
    </h3>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
      <!-- Main Image -->
      <div>
        <img :src="phoneImg" class="w-full rounded shadow" alt="Product" />
      </div>

      <!-- Product Details -->
      <div class="space-y-3 text-gray-800">
        <p><strong>Brand:</strong> <span class="itbms-brand">{{ product.brandName }}</span></p>
        <p><strong>Model:</strong> <span class="itbms-model">{{ product.model }}</span></p>
        <p><strong>Price:</strong> <span class="itbms-price">{{ product.price.toLocaleString() }}</span> Baht</p>
        <p><strong>Description:</strong> <span class="itbms-description">{{ product.description }}</span></p>
        <p>
          <strong>RAM:</strong> <span class="itbms-ramGb">{{ product.ramGb ?? "-" }}</span>&thinsp;
          <span class="itbms-ramGb-unit">GB</span>
        </p>
        <p>
          <strong>Screen Size:</strong> <span class="itbms-screenSizeInch">{{ product.screenSizeInch ?? "-" }}</span>&thinsp;
          <span class="itbms-screenSizeInch-unit">Inches</span>
        </p>
        <p>
          <strong>Storage:</strong> <span class="itbms-storageGb">{{ product.storageGb ?? "-" }}</span>&thinsp;
          <span class="itbms-storageGb-unit">GB</span>
        </p>
        <p><strong>Color:</strong> <span class="itbms-color">{{ product.color ?? "-" }}</span></p>
        <p><strong>Available Quantity:</strong> <span class="itbms-quantity">{{ product.quantity }}</span> units</p>

        <div class="flex space-x-3">
          <router-link :to="{ name: 'EditItem', params: { id: product.id }, query: { from: 'Gallery' }}" 
            class="itbms-edit-button mt-4 bg-purple-600 text-white py-2 px-6 rounded hover:bg-purple-700"
          >
            Edit
          </router-link>
          <button
            @click="confirmDelete()"
            class="itbms-delete-button mt-4 bg-gray-200 text-gray-600 py-2 px-6 rounded hover:bg-gray-300 "
          >
            Delete
          </button>
        </div>
      </div>
    </div>
  </div>

   <!-- Delete Confirmation Modal -->
   <div
    v-if="showModal"
    class="fixed inset-0 flex items-center justify-center z-50 bg-black/60 backdrop-blur-sm"
  >
    <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md p-8">
      <h2 class="text-2xl font-bold text-neutral-800 mb-4">Confirm Delete</h2>
      <p class=" itbms-message text-neutral-600 mb-6">
        Do you want to delete this sale item?
      </p>
      <div class="flex justify-end gap-4">
        <button
          @click="showModal = false"
          class="itbms-cancel-button bg-neutral-200 hover:bg-neutral-300 text-neutral-700 px-4 py-2 rounded-lg transition"
        >
          Cancel
        </button>
        <button
          @click="handleDelete"
          class="itbms-confirm-button bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-lg transition"
        >
          Delete
        </button>
      </div>
    </div>
  </div>
</template>