<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ref, onMounted, computed } from 'vue'
import { getItemById, getItems, editItem } from '@/lib/fetchUtils'
import phoneImg from '../../public/phone.jpg';

const route = useRoute()
const router = useRouter()
const id = route.params.id

const brandSelected = ref(null)

const product = ref({
  "id": null,
  "model": "",
  "brand": {
    "id": null,
    "name": ""
  },
  "newBrandName": "",
  "description": "",
  "price": 0,
  "ramGb": 0,             // OPTIONAL
  "screenSizeInch": 0,    // OPTIONAL
  "quantity": 0,
  "storageGb": 0,         // OPTIONAL
  "color": ""             // OPTIONAL
})
const originalProduct = ref(null)

const isFormValid = computed(() => {
  return (
    product.value.newBrandName?.trim() &&
    product.value.model.trim() !== '' &&
    product.value.description.trim() !== '' &&
    product.value.price > 0 &&
    product.value.quantity > 0
  )
})

const isChanged = computed(() => {
  if (!originalProduct.value) return false
  return JSON.stringify(product.value) !== JSON.stringify(originalProduct.value)
})

const isSaveDisabled = computed(() => {
  return !isFormValid.value || !isChanged.value
})

async function handleSubmit() {
   try {
    product.value.color = product.value.color === '' ? null : product.value.color
    const editedItem = await editItem('http://ip24nw3.sit.kmutt.ac.th:8080/v1/sale-items', id, product.value)
    if (editedItem) {
      router.push({ name: 'ListDetails', params: { id: product.id }, query: {edited: true}}) 
    }
    console.log(product.value)
  } catch (error) {
    console.error('Error:', error)
  }
}

onMounted(async () => {
  try {
    //const item = await getItemById('http://ip24nw3.sit.kmutt.ac.th:8080/v1/sale-items', id)
    const item = await getItemById('http://localhost:8080/v1/sale-items', id)
    if (!item || item?.status === 404) {
      router.push('/sale-items')
      alert('The requested sale item does not exist.')
      return
    }

    const data = {
      "id": item.id,
      "model": item.model,
      "brand": {
        "id": null,
        "name": item.brandName
      },
      "newBrandName": item.brandName,
      "description": item.description,
      "price": item.price,
      "ramGb": item.ramGb,                      // OPTIONAL
      "screenSizeInch": item.screenSizeInch,    // OPTIONAL
      "quantity": item.quantity,
      "storageGb": item.storageGb,              // OPTIONAL
      "color": item.color 
    }
    product.value = data
    originalProduct.value = JSON.parse(JSON.stringify(data))
  } catch (error) {
    console.error('Failed to fetch product:', error);
  }

  try {
    //const brand = await getItems('http://ip24nw3.sit.kmutt.ac.th:8080/v1/brands')
    const brand = await getItems('http://localhost:8080/v1/brands')
    if (!brand || brand?.status === 404) {
      // alert('The requested sale brand does not exist.')
      return
    }
    brandSelected.value = brand
  } catch (error) {
    console.error('Failed to fetch product:', error);
  }
})
</script>

<template>
    <div class="min-h-screen bg-gray-50 text-gray-800">
      <!-- Add Item Form -->
      <main class="container mx-auto p-6 bg-white shadow-md rounded-md">
        <!-- <h2 class="text-2xl font-bold mb-6 text-center text-gray-800">Add New Sale Item</h2> -->
        <form
          class="grid grid-cols-1 md:grid-cols-2 gap-6"
          @submit.prevent="handleSubmit"
        >
          <!-- Image section -->
          <div class="flex flex-col items-center justify-center">
            <div class="text-xl font-bold mb-4">
              <router-link
                :to="{ name: 'ListGallery'}"
                class="itbms-home-button text-blue-500"
              >
                Home
              </router-link>
              <router-link 
                :to="{ name: 'ListDetails', params: { id: product.id }}"
                class="itbms-back-button text-blue-500"  
              >
                <span class="text-gray-400">> </span>
                <span>{{ product.model }}</span>&thinsp;
                <span>{{ product.ramGb ?? "" }}</span>/
                <span>{{ product.storageGb ?? "" }}</span>GB
                <span>{{ product.color ?? "" }}</span>
                      
                <img :src="phoneImg" class="w-100 h-125 bg-gray-200" />
              </router-link>
            </div>
            <div class="flex space-x-3">
              <img :src="phoneImg" class="w-20 h-25 bg-gray-200 text-xs" />
              <img :src="phoneImg" class="w-20 h-25 bg-gray-200 text-xs" />
              <img :src="phoneImg" class="w-20 h-25 bg-gray-200 text-xs" />
              <img :src="phoneImg" class="w-20 h-25 bg-gray-200 text-xs" />
            </div>
          </div>

          <!-- Form fields -->
          <div>
            <div class="mb-4">
              <label class="block mb-1 font-medium">Brand</label>
              <select v-model="product.newBrandName" class="itbms-brand w-full border px-3 py-2 rounded">
                <option disabled value="">Select Brand</option>
                <option v-for="brand in brandSelected" :key="brand.id" :value="brand.name">
                  {{ brand.name }}
                </option>
              </select>
            </div>

            <div class="mb-4">
              <label class="block mb-1 font-medium">Model</label>
              <input v-model.trim="product.model" type="text" class="itbms-model w-full border px-3 py-2 rounded" required />
            </div>

            <div class="mb-4">
              <label class="block mb-1 font-medium">Price (Baht)</label>
              <input v-model.number="product.price" type="number" class="itbms-price w-full border px-3 py-2 rounded" required />
            </div>

            <div class="mb-4">
              <label class="block mb-1 font-medium">Description</label>
              <textarea v-model.trim="product.description" rows="3" class="itbms-description w-full border px-3 py-2 rounded" required></textarea>
            </div>

            <div class="grid grid-cols-2 gap-4 mb-4">
              <div>
                <label class="block mb-1 font-medium">RAM (GB)</label>
                <input v-model.number="product.ramGb" type="number" class="itbms-ramGb w-full border px-3 py-2 rounded" />
              </div>
              <div>
                <label class="block mb-1 font-medium">Screen Size (Inches)</label>
                <input v-model.number="product.screenSizeInch" type="number" step="any" class="itbms-screenSizeInch w-full border px-3 py-2 rounded" />
              </div>
              <div>
                <label class="block mb-1 font-medium">Storage (GB)</label>
                <input v-model.number="product.storageGb" type="number" class="itbms-storageGb w-full border px-3 py-2 rounded" />
              </div>
              <div>
                <label class="block mb-1 font-medium">Color</label>
                <input v-model.trim="product.color" type="text" class="itbms-color w-full border px-3 py-2 rounded" />
              </div>
            </div>

            <div class="mb-6">
              <label class="block mb-1 font-medium">Quantity</label>
              <input v-model.number="product.quantity" type="number" class="itbms-quantity w-full border px-3 py-2 rounded" required/>
            </div>

            <!-- Buttons -->
            <div class="flex justify-end space-x-4">
                <router-link 
                  :to="{ name: 'ListDetails', params: { id: product.id }}"  
                  class="itbms-cancel-button px-5 py-2 bg-gray-200 rounded hover:bg-gray-300 text-gray-700"
                >
                    Cancel
                </router-link>
                <button
                  type="submit"
                  :disabled="isSaveDisabled"
                  :class="[
                    'itbms-save-button px-5 py-2 text-white rounded transition',
                    isSaveDisabled ? 'bg-purple-300 cursor-not-allowed' : 'bg-purple-600 hover:bg-purple-700'
                  ]"
                >
                  Save
                </button>
            </div>
          </div>
        </form>
      </main>
    </div>
</template>