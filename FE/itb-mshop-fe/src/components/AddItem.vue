<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { addItem, getItems } from '@/lib/fetchUtils'
import phoneImg from '../../public/phone.jpg';

const router = useRouter()

const brandSelected = ref(null)
const newSaleItem = ref({
  "id": null,
  "brand": {
    "id": null,
    "name": ''
  },
  //"brandName": '',
  "model": '',
  "description": '',
  "price": 0,
  "quantity": 0,
  "screenSizeInch": null, // OPTIONAL
  "ramGb": null,          // OPTIONAL
  "storageGb": null,      // OPTIONAL
  "color": null,          // OPTIONAL
})

const isFormValid = computed(() => {
  return (
    newSaleItem.value.brandName?.trim() &&
    newSaleItem.value.model.trim() !== '' &&
    newSaleItem.value.description.trim() !== '' &&
    newSaleItem.value.price > 0 &&
    newSaleItem.value.quantity > 0
  )
})

async function handleSubmit() {
  try {
    const addedItem = await addItem('http://ip24nw3.sit.kmutt.ac.th:8080/v1/sale-items', newSaleItem.value)
    if (addedItem) {
      router.push({ name: 'ListGallery', query: { added: 'true' } })
    }
  } catch (error) {
    console.error('Error:', error)
  }
}

onMounted(async () => {
  try {
    const brand = await getItems('http://ip24nw3.sit.kmutt.ac.th:8080/v1/brands')
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
        <span class="text-gray-800 font-semibold">New Sale Item</span>
      </div>

      <!-- Title -->
      <h1 class="text-3xl font-bold text-gray-900 mb-8">Add New Product</h1>

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
                <select v-model="newSaleItem.brand" class="itbms-brand w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500">
                  <option disabled value=''>Select Brand</option>
                  <option v-for="brand in brandSelected" :key="brand.id" :value="{id: null, name: brand.name}">
                    {{ brand.name }}
                  </option>
                </select>
              </div>

              <div>
                <label class="block mb-1 font-semibold text-gray-700">Model</label>
                <input v-model.trim="newSaleItem.model" type="text" maxlength="60" class="itbms-model w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500" required />
              </div>

              <div>
                <label class="block mb-1 font-semibold text-gray-700">Price (Baht)</label>
                <input v-model.number="newSaleItem.price" type="number" class="itbms-price w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500" required />
              </div>

              <div>
                <label class="block mb-1 font-semibold text-gray-700">Description</label>
                <textarea v-model.trim="newSaleItem.description" rows="3" class="itbms-description w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500" required></textarea>
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block mb-1 font-semibold text-gray-700">RAM (GB)</label>
                  <input v-model.number="newSaleItem.ramGb" type="number" class="itbms-ramGb  w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500" />
                </div>
                <div>
                  <label class="block mb-1 font-semibold text-gray-700">Screen Size (Inch)</label>
                  <input v-model.number="newSaleItem.screenSizeInch" type="number" step="any" class="itbms-screenSizeInch w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500" />
                </div>
                <div>
                  <label class="block mb-1 font-semibold text-gray-700">Storage (GB)</label>
                  <input v-model.number="newSaleItem.storageGb" type="number" class="itbms-storageGb w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500" />
                </div>
                <div>
                  <label class="block mb-1 font-semibold text-gray-700">Color</label>
                  <input v-model.trim="newSaleItem.color" type="text" class="itbms-color w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500" />
                </div>
              </div>

              <div>
                <label class="block mb-1 font-semibold text-gray-700">Quantity</label>
                <input v-model.number="newSaleItem.quantity" type="number" class="itbms-quantity w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500" required />
              </div>

              <!-- Buttons -->
              <div class="flex justify-end space-x-4 pt-6">
                <router-link
                  :to="{ name: 'ListGallery' }"
                  class="itbms-cancel-button px-5 py-2 border border-gray-300 text-gray-600 rounded hover:bg-gray-100 transition"
                >
                  Cancel
                </router-link>
                <button
                  type="submit"
                  :disabled="!isFormValid"
                   :class="[
                      'itbms-save-button px-5 py-2 text-white rounded transition',
                      isFormValid ? 'bg-purple-600 hover:bg-purple-700' : 'bg-purple-300 cursor-not-allowed'
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
