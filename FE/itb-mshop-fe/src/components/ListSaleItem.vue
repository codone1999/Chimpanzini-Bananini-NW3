<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ref, onMounted } from "vue";
import { getItems, deleteItemById } from "@/lib/fetchUtils";

const route = useRoute()
const router = useRouter()

const showModal = ref(false)
const selectedProductId = ref(null)

const showSuccessMessage = ref(false)
const successMessage = ref('')

const products = ref([]);

function confirmDelete(id) {
  selectedProductId.value = id
  showModal.value = true
}

async function handleDelete() {
  if (!selectedProductId.value) return

  try {
    const item = await deleteItemById('http://intproj24.sit.kmutt.ac.th/nw3/api/v1/sale-items',  selectedProductId.value)
    if (!item || item?.status === 404 || item === 404) {
      showModal.value = false
      handleDeleteSuccess('The requested sale item does not exist.')
      return
    }
  } catch (error) {
    console.error('Failed to fetch product:', error);
  }

  showModal.value = false
  handleDeleteSuccess('The sale item has been deleted.')
}
function handleDeleteSuccess(message){
  showSuccessMessage.value = true
  successMessage.value = message

  setTimeout(() => {
    showSuccessMessage.value = false
  }, 3000)
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
  handleQuerySuccess('added', 'The sale item has been added.')
  handleQuerySuccess('edited', 'The sale item has been edited.')

  try {
    products.value = await getItems(`http://intproj24.sit.kmutt.ac.th/nw3/api/v1/sale-items`) ?? []
  } catch (error) {
    console.error('Failed to fetch product:', error);
  }
})
</script>

<template>
  <div class="p-6">
    <!-- Success Message -->
    <div 
      v-if="showSuccessMessage" 
      class="itbms-message mb-6 p-4 text-green-800 bg-green-100 border border-green-300 rounded"
    >
      {{ successMessage }}
    </div>
    <div class="flex justify-between items-center mb-6">

      <router-link
        :to="{ name: 'AddItem', query: { from: 'SaleItem' } }"
        class="itbms-sale-item-add flex items-center gap-2 bg-[#7e5bef] hover:bg-[#6847d5] text-white px-5 py-3 rounded-xl text-base font-semibold shadow-md transition duration-300"
      >
          Add Sale Item
      </router-link>

      <router-link
        :to="{ name: 'BrandList'}"
        class="itbms-manage-brand flex items-center gap-2 bg-[#7e5bef] hover:bg-[#6847d5] text-white px-5 py-3 rounded-xl text-base font-semibold shadow-md transition duration-300"
      >
        Manage Brand
      </router-link>
    </div>
    <div class="overflow-x-auto shadow ring-1 ring-gray-200 rounded-lg">
      <table class="min-w-full bg-white text-sm text-center">
        <thead class="bg-gray-100 text-gray-700 font-semibold uppercase">
          <tr class="bg-gray-100 text-center ">
            <th class="px-4 py-3 border-gray-300 border-r border-b">Id</th>
            <th class="px-4 py-3 border-gray-300 border-r border-b">Brand</th>
            <th class="px-4 py-3 border-gray-300 border-r border-b">Model</th>
            <th class="px-4 py-3 border-gray-300 border-r border-b">Ram</th>
            <th class="px-4 py-3 border-gray-300 border-r border-b">Storage</th>
            <th class="px-4 py-3 border-gray-300 border-r border-b">Color</th>
            <th class="px-4 py-3 border-gray-300 border-r border-b">Price</th>
            <th class="px-4 py-3 border-gray-300 border-b">Action</th>
          </tr>
        </thead>
        <tbody class="text-gray-800">
          <tr
            v-for="product in products"
            :key="product.id"
            class="itbms-row hover:bg-gray-50 transition"
          >
            <td class="itbms-id px-4 py-3 border-t border-r border-gray-200 ">{{ product.id }}</td>
            <td class="itbms-brand border-t border-r border-gray-200  px-4 py-3">{{ product.brandName }}</td>
            <td class="itbms-model border-t border-r border-gray-200  px-4 py-3 text-left">{{ product.model }}</td>
            <td class="itbms-ramGb border-t border-r border-gray-200  px-4 py-3 ">{{ product.ramGb }}</td>
            <td class="itbms-storageGb border-t border-r border-gray-200  px-4 py-3">{{ product.storageGb }}</td>
            <td class="itbms-color border-t border-r border-gray-200  px-4 py-3 ">{{ product.color }}</td>
            <td class="itbms-price border-t border-r border-gray-200  px-4 py-3">{{ product.price }}</td>
            <td class="px-4 py-3 border-t border-gray-200 ">
              <div class="flex justify-center items-center gap-2">
                <router-link 
                  :to="{ name: 'EditItem', params: { id: product.id }, query: { from: 'SaleItem' } }" 
                  class="itbms-edit-button bg-[#9f7aea] hover:bg-[#805ad5] text-white px-4 py-2 rounded-lg font-medium shadow transition duration-300"
                >
                  EDIT
                </router-link>
                <button
                  @click="confirmDelete(product.id)"
                  class="itbms-delete-button bg-red-500 hover:bg-red-600 text-white p-2 rounded px-4 py-2 rounded-lg font-medium shadow transition duration-300"
                >
                  DELETE
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
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
