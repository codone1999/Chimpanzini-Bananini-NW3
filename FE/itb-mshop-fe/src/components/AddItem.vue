<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { addItem, getItems } from '@/lib/fetchUtils'
import phoneImg from '../../public/phone.jpg';

const router = useRouter()

const brandSelected = ref(null)
const newSaleItem = ref({
  "id": null,
  "brand": '',            // Receive like {id: 1, name: Example}
  "model": '',
  "description": '',
  "price": 0,
  "quantity": 0,
  "screenSizeInch": null, // OPTIONAL
  "ramGb": null,          // OPTIONAL
  "storageGb": null,      // OPTIONAL
  "color": null,          // OPTIONAL
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
  <div class="min-h-screen bg-gray-50 text-gray-800">
    <main class="container mx-auto p-6 bg-white shadow-md rounded-md">
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
            <span class="text-gray-400">> </span>
            <span class="itbms-brand">New Sale Item</span>
            
            <img :src="phoneImg" class="w-100 h-125 bg-gray-200" />
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
            <select v-model="newSaleItem.brand" class="itbms-brand w-full border px-3 py-2 rounded">
              <option disabled value=''>Select Brand</option>
              <option v-for="brand in brandSelected" :key="brand.id" :value="{id: null, name: brand.name}">
                {{ brand.name }}
              </option>
            </select>
          </div>

          <div class="mb-4">
            <label class="block mb-1 font-medium">Model</label>
            <input v-model.trim="newSaleItem.model" type="text" class="itbms-model w-full border px-3 py-2 rounded" required />
          </div>

          <div class="mb-4">
            <label class="block mb-1 font-medium">Price (Baht)</label>
            <input v-model.number="newSaleItem.price" type="number" class="itbms-price w-full border px-3 py-2 rounded" required />
          </div>

          <div class="mb-4">
            <label class="block mb-1 font-medium">Description</label>
            <textarea v-model.trim="newSaleItem.description" rows="3" class="itbms-description w-full border px-3 py-2 rounded" required></textarea>
          </div>

          <div class="grid grid-cols-2 gap-4 mb-4">
            <div>
              <label class="block mb-1 font-medium">RAM (GB)</label>
              <input v-model.number="newSaleItem.ramGb" type="number" class="itbms-ramGb w-full border px-3 py-2 rounded" />
            </div>
            <div>
              <label class="block mb-1 font-medium">Screen Size (Inches)</label>
              <input v-model.number="newSaleItem.screenSizeInch" type="number" step="any" class="itbms-screenSizeInch w-full border px-3 py-2 rounded" />
            </div>
            <div>
              <label class="block mb-1 font-medium">Storage (GB)</label>
              <input v-model.number="newSaleItem.storageGb" type="number" class="itbms-storageGb w-full border px-3 py-2 rounded" />
            </div>
            <div>
              <label class="block mb-1 font-medium">Color</label>
              <input v-model.trim="newSaleItem.color" type="text" class="itbms-color w-full border px-3 py-2 rounded" />
            </div>
          </div>

          <div class="mb-6">
            <label class="block mb-1 font-medium">Quantity</label>
            <input v-model.number="newSaleItem.quantity" type="number" class="itbms-quantity w-full border px-3 py-2 rounded" required/>
          </div>

          <!-- Buttons -->
          <div class="flex justify-end space-x-4">
            <router-link
              :to="{ name: 'ListGallery' }"
              class="itbms-cancel-button px-5 py-2 bg-gray-200 rounded hover:bg-gray-300 text-gray-700"
            >
              Cancel
            </router-link>
            <button
              type="submit"
              class="itbms-save-button px-5 py-2 bg-purple-600 text-white rounded hover:bg-purple-700"
            >
              Save
            </button>
          </div>
        </div>
      </form>
    </main>
  </div>
</template>
