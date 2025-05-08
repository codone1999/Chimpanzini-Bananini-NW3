<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ref, onMounted } from 'vue'
import { getItemById } from '@/lib/fetchUtils'
import phoneImg from '../../public/phone.jpg';

const route = useRoute()
const router = useRouter()
const id = route.params.id
const product = ref(
  {id: 1,
  brand: 'Apple',
  brandName: 'Apple',
  model: 'iPhone 13 Pro',
  price: 35900,
  description: 'Flagship iPhone with A15 Bionic, 120Hz ProMotion display, and triple-camera setup.',
  ramGb: 6,
  storageGb: 256,
  color: 'Sierra Blue',
  screenSizeInch: 6.1,
  quantity: 10}
)

// onMounted(async () => {
//   try {
//     const item = await getItemById('http://ip24nw3.sit.kmutt.ac.th:8080/v1/sale-items', id)
//     if (!item || item?.status === 404) {
//       router.push('/sale-items')
//       alert('The requested sale item does not exist.')
//       return
//     }
//     product.value = item;
//   } catch (error) {
//     console.error('Failed to fetch product:', error);
//   }
// })
</script>

<template>
    <div class="min-h-screen bg-gray-50 text-gray-800">  
      <!-- Add Item Form -->
      <main class="container mx-auto p-6 bg-white shadow-md rounded-md">
        <!-- <h2 class="text-2xl font-bold mb-6 text-center text-gray-800">Add New Sale Item</h2> -->
        <form class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- Image section -->
          <div class="flex flex-col items-center justify-center">
            <!-- Main -->
            <img :src="phoneImg" class="w-100 h-125 bg-gray-200 flex items-center justify-center text-gray-500 mb-4" />
            <!-- Subtance -->
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
                <label class="block mb-1 font-medium" for="brand">Brand</label>
                <select id="itbms-brand" v-model="product.brandName" 
                    class="itbms-brand w-full border border-gray-300 rounded px-3 py-2"
                >
                    <option>Select Brand</option>
                    <option>Apple</option>
                    <option>Samsung</option>
                    <option>Huawei</option>
                </select>
            </div>
  
            <div class="mb-4">
              <label class="block mb-1 font-medium" for="model">Model</label>
              <input id="itbms-model" type="text" v-model="product.model" 
                class="itbms-model w-full border border-gray-300 rounded px-3 py-2" />
            </div>
  
            <div class="mb-4">
              <label class="block mb-1 font-medium" for="price">Price (Baht)</label>
              <input id="itbms-price" type="number" v-model="product.price" 
                class="itbms-price w-full border border-gray-300 rounded px-3 py-2" />
            </div>
  
            <div class="mb-4">
              <label class="block mb-1 font-medium" for="desc">Description</label>
              <textarea id="itbms-description" rows="3" v-model="product.description" 
                class="itbms-description w-full border border-gray-300 rounded px-3 py-2"></textarea>
            </div>
  
            <div class="grid grid-cols-2 gap-4 mb-4">
              <div>
                <label class="block mb-1 font-medium">RAM (GB)</label>
                <input id="itbms-ramGb" type="number" v-model="product.ramGb" 
                    class="itbms-ramGb w-full border border-gray-300 rounded px-3 py-2" />
              </div>
              <div>
                <label class="block mb-1 font-medium">Screen Size (Inches)</label>
                <input id="itbms-screenSizeInch" type="number" v-model="product.screenSizeInch" 
                    class="itbms-screenSizeInch w-full border border-gray-300 rounded px-3 py-2" />
              </div>
              <div>
                <label class="block mb-1 font-medium">Storage (GB)</label>
                <input id="itbms-storageGb" type="number" v-model="product.storageGb" 
                    class="itbms-storageGb w-full border border-gray-300 rounded px-3 py-2" />
              </div>
              <div>
                <label class="block mb-1 font-medium">Color</label>
                <input id="itbms-color" type="text" v-model="product.color" 
                    class="itbms-color w-full border border-gray-300 rounded px-3 py-2" />
              </div>
            </div>
  
            <div class="mb-6">
              <label class="block mb-1 font-medium">Quantity</label>
              <input id="itbms-quantity" type="number" v-model="product.quantity" 
                class="itbms-quantity w-full border border-gray-300 rounded px-3 py-2" />
            </div>
  
            <!-- Buttons -->
            <div class="flex justify-end space-x-4">
                <router-link :to="{ name: 'ListGallery'}"  class="itbms-cancel-button px-5 py-2 bg-gray-200 rounded hover:bg-gray-300 text-gray-700">
                    Cancel
                </router-link>
                <button id="itbms-save-button" type="submit"
                    class="itbms-save-button px-5 py-2 bg-purple-600 text-white rounded hover:bg-purple-700">
                    Save
                </button>
            </div>
          </div>
        </form>
      </main>
    </div>
</template>