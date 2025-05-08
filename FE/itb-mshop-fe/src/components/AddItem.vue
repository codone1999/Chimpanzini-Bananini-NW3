<script setup>
  import phoneImg from '../../public/phone.jpg';
  
</script>
  
<style>
/* Optional: You can add transition or animation classes if needed */
</style>

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
              <select id="itbms-brand" class="w-full border border-gray-300 rounded px-3 py-2">
                <option>Select Brand</option>
                <option>Apple</option>
                <option>Samsung</option>
                <option>Huawei</option>
              </select>
            </div>
  
            <div class="mb-4">
              <label class="block mb-1 font-medium" for="model">Model</label>
              <input id="itbms-model" type="text" class="w-full border border-gray-300 rounded px-3 py-2" />
            </div>
  
            <div class="mb-4">
              <label class="block mb-1 font-medium" for="price">Price (Baht)</label>
              <input id="itbms-price" type="number" class="w-full border border-gray-300 rounded px-3 py-2" />
            </div>
  
            <div class="mb-4">
              <label class="block mb-1 font-medium" for="desc">Description</label>
              <textarea id="itbms-description" rows="3" class="w-full border border-gray-300 rounded px-3 py-2"></textarea>
            </div>
  
            <div class="grid grid-cols-2 gap-4 mb-4">
              <div>
                <label class="block mb-1 font-medium">RAM (GB)</label>
                <input id="itbms-ramGb" type="number" class="w-full border border-gray-300 rounded px-3 py-2" />
              </div>
              <div>
                <label class="block mb-1 font-medium">Screen Size (Inches)</label>
                <input id="itbms-screenSizeInch" type="number" class="w-full border border-gray-300 rounded px-3 py-2" />
              </div>
              <div>
                <label class="block mb-1 font-medium">Storage (GB)</label>
                <input id="itbms-storageGb" type="number" class="w-full border border-gray-300 rounded px-3 py-2" />
              </div>
              <div>
                <label class="block mb-1 font-medium">Color</label>
                <input id="itbms-color" type="text" class="w-full border border-gray-300 rounded px-3 py-2" />
              </div>
            </div>
  
            <div class="mb-6">
              <label class="block mb-1 font-medium">Quantity</label>
              <input id="itbms-quantity" type="number" class="w-full border border-gray-300 rounded px-3 py-2" />
            </div>
  
            <!-- Buttons -->
            <div class="flex justify-end space-x-4">
              <button id="itbms-cancel-button" type="button" class="px-5 py-2 bg-gray-200 rounded hover:bg-gray-300 text-gray-700">
                <router-link :to="{ name: 'ListGallery'}">
                    Cancel
                </router-link>
              </button>
              <button id="itbms-save-button" type="submit" class="px-5 py-2 bg-purple-600 text-white rounded hover:bg-purple-700">
                Save
              </button>
            </div>
          </div>
        </form>
      </main>
    </div>
</template>
  