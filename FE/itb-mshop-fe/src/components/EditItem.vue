<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ref, onMounted, computed } from 'vue'
import { getItemById, getItems, editItem } from '@/lib/fetchUtils'
import phoneImg from '../../public/phone.jpg';

const route = useRoute()
const router = useRouter()
const id = route.params.id
const from = route.query.from

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
    const editedItem = await editItem('http://intproj24.sit.kmutt.ac.th/nw3/api/v1/sale-items', id, product.value)
    if (editedItem) {
      if (from === 'Gallery')
        router.push({ name: 'ListDetails', params: { id: product.id }, query: {edited: 'true'}})
      else
        router.push({ name: 'ListSaleItem', query: {edited: 'true'}})
    }
    console.log(product.value)
  } catch (error) {
    console.error('Error:', error)
  }
}

onMounted(async () => {
  try {
    const item = await getItemById('http://intproj24.sit.kmutt.ac.th/nw3/api/v1/sale-items', id)
    // const item = await getItemById('http://localhost:8080/v1/sale-items', id)
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
    const brand = await getItems('http://intproj24.sit.kmutt.ac.th/nw3/api/v1/brands')
    // const brand = await getItems('http://localhost:8080/v1/brands')
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
  <div class="min-h-screen bg-gray-100 text-gray-800 font-sans">
    <main class="max-w-6xl mx-auto py-10 px-6">
      <!-- Header -->
      <div class="mb-6 text-sm text-gray-500 flex items-center space-x-2">
        <router-link
          :to="{ name: 'ListGallery' }"
          class="text-purple-600 hover:underline font-medium"
        >
          Home
        </router-link>
        <span>/</span>
        <router-link 
          :to="{ name: 'ListDetails', params: { id: product.id }}"
          class="itbms-back-button text-purple-600 hover:underline font-medium"  
        >
          <span>{{ product.model }}</span>&thinsp;
          <span>{{ product.ramGb ?? "" }}</span>/<span>{{ product.storageGb ?? "" }}</span>GB
          <span>{{ product.color ?? "" }}</span>
        </router-link>
      </div>

      <!-- Title -->
      <h1 class="text-3xl font-bold text-gray-900 mb-8">
        <span>{{ product.model }}</span>&thinsp;
        <span>{{ product.ramGb ?? "" }}</span>/<span>{{ product.storageGb ?? "" }}</span>GB
        <span>{{ product.color ?? "" }}</span>
      </h1>

      <!-- Main -->
      <div class="bg-white rounded-2xl shadow-xl p-10">
        <form
          class="grid grid-cols-1 md:grid-cols-2 gap-10"
          @submit.prevent="handleSubmit"
        >
          <!-- Image Section -->
          <div class="flex flex-col items-center space-y-4">
            <img :src="phoneImg" class="w-100 h-125 object-cover bg-gray-200 rounded-lg" />
            <div class="flex space-x-3">
              <img :src="phoneImg" class="w-20 h-25 rounded bg-gray-100 object-cover" />
              <img :src="phoneImg" class="w-20 h-25 rounded bg-gray-100 object-cover" />
              <img :src="phoneImg" class="w-20 h-25 rounded bg-gray-100 object-cover" />
              <img :src="phoneImg" class="w-20 h-25 rounded bg-gray-100 object-cover" />
            </div>
          </div>

          <!-- Form Fields -->
            <div class="space-y-6">
              <div>
                <label class="block mb-1 font-semibold text-gray-700">Brand</label>
                <select v-model="product.newBrandName" class="itbms-brand w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500">
                  <option disabled value=''>Select Brand</option>
                  <option v-for="brand in brandSelected" :key="brand.id" :value="brand.name">
                    {{ brand.name }}
                  </option>
                </select>
              </div>

              <div>
                <label class="block mb-1 font-semibold text-gray-700">Model</label>
                <input v-model.trim="product.model" maxlength="60" type="text" class="itbms-model w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500" required />
              </div>

              <div>
                <label class="block mb-1 font-semibold text-gray-700">Price (Baht)</label>
                <input v-model.number="product.price" type="number" class="itbms-price w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500" required />
              </div>

              <div>
                <label class="block mb-1 font-semibold text-gray-700">Description</label>
                <textarea v-model.trim="product.description" rows="3" class="itbms-description w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500" required></textarea>
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block mb-1 font-semibold text-gray-700">RAM (GB)</label>
                  <input v-model.number="product.ramGb" type="number" class="itbms-ramGb w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500" />
                </div>
                <div>
                  <label class="block mb-1 font-semibold text-gray-700">Screen Size (Inch)</label>
                  <input v-model.number="product.screenSizeInch" type="number" step="any" class="itbms-screenSizeInch w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500" />
                </div>
                <div>
                  <label class="block mb-1 font-semibold text-gray-700">Storage (GB)</label>
                  <input v-model.number="product.storageGb" type="number" class="itbms-storageGb w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500" />
                </div>
                <div>
                  <label class="block mb-1 font-semibold text-gray-700">Color</label>
                  <input v-model.trim="product.color" type="text" class="itbms-color w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500" />
                </div>
              </div>

              <div>
                <label class="block mb-1 font-semibold text-gray-700">Quantity</label>
                <input v-model.number="product.quantity" type="number" class="itbms-quantity w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500" required />
              </div>

              <!-- Buttons -->
              <div class="flex justify-end space-x-4">
                  <router-link
                    :to="from === 'Gallery' ? { name: 'ListDetails', params: { id: product.id }} : { name: 'ListSaleItem'}"
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
        </div>
      </main>
    </div>
</template>