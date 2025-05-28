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
  handleQuerySuccess('added', 'The sale item has been successfully added.')
  handleQuerySuccess('edited', 'The sale item has been edited.')

  try {
    products.value = await getItems(`http://intproj24.sit.kmutt.ac.th/nw3/api/v1/sale-items`) ?? []
  } catch (error) {
    console.error('Failed to fetch product:', error);
  }
})
</script>

<template>
  <div class="p-8 min-h-screen bg-gray-50">
    <!-- Success Message -->
    <transition name="fade">
      <div 
        v-if="showSuccessMessage" 
        class="itbms-message mb-6 p-4 text-green-800 bg-green-100 border border-green-300 rounded-lg shadow-sm text-sm font-medium"
      >
        {{ successMessage }}
      </div>
    </transition>

    <!-- Action Buttons -->
    <div class="flex justify-between items-center mb-8">
      <router-link
        :to="{ name: 'AddItem', query: { from: 'SaleItem' } }"
        class="itbms-sale-item-add flex items-center gap-2 bg-[#7e5bef] hover:bg-[#6847d5] text-white pl-3 pr-4 py-3 rounded-xl text-base font-semibold shadow-lg transition duration-300"
      >
        <span class="material-icons">
          add
        </span>
        Add Sale Item
      </router-link>

      <router-link
        :to="{ name: 'BrandList' }"
        class="itbms-manage-brand flex items-center gap-2 bg-[#7e5bef] hover:bg-[#6847d5] text-white px-4 py-3 rounded-xl text-base font-semibold shadow-lg transition duration-300"
      >
        <span class="material-icons">
          build
        </span> 
        Manage Brand
      </router-link>
    </div>

    <!-- No Items -->
    <div v-if="products.length === 0" class="text-center text-gray-500 text-lg py-10">
      No sale item found.
    </div>

    <!-- Product Table -->
    <div v-else class="overflow-x-auto shadow-xl ring-1 ring-gray-200 rounded-2xl">
      <table class="min-w-full bg-white text-sm text-center table-auto">
        <thead class="bg-purple-300 text-gray-700 font-semibold uppercase tracking-wide">
          <tr>
            <th class="px-4 py-4 border-b border-gray-300">ID</th>
            <th class="px-4 py-4 border-b border-gray-300">Brand</th>
            <th class="px-4 py-4 border-b border-gray-300">Model</th>
            <th class="px-4 py-4 border-b border-gray-300">RAM</th>
            <th class="px-4 py-4 border-b border-gray-300">Storage</th>
            <th class="px-4 py-4 border-b border-gray-300">Color</th>
            <th class="px-4 py-4 border-b border-gray-300">Price</th>
            <th class="px-4 py-4 border-b border-gray-300">Action</th>
          </tr>
        </thead>
        <tbody class="text-gray-800 divide-y divide-gray-100">
          <tr
            v-for="product in products"
            :key="product.id"
            class="itbms-row hover:bg-purple-50 transition duration-200"
          >
            <td class="itbms-id px-4 py-3">{{ product.id }}</td>
            <td class="itbms-brand px-4 py-3">{{ product.brandName }}</td>
            <td class="itbms-model px-4 py-3 text-left">{{ product.model }}</td>
            <td class="itbms-ramGb px-4 py-3">{{ product.ramGb ?? "-" }}</td>
            <td class="itbms-storageGb px-4 py-3">{{ product.storageGb ?? "-" }}</td>
            <td class="itbms-color px-4 py-3">{{ product.color ?? "-" }}</td>
            <td class="itbms-price px-4 py-3">{{ product.price.toLocaleString() }}</td>
            <td class="px-4 py-3">
              <div class="flex justify-center gap-2">
                <router-link 
                  :to="{ name: 'EditItem', params: { id: product.id }, query: { from: 'SaleItem' } }" 
                  class="itbms-edit-button bg-purple-600 hover:bg-purple-700 text-white p-2 rounded-lg font-medium shadow-sm transition"
                >
                  <span class="material-icons">
                  edit
                  </span>
                </router-link>
                <button
                  @click="confirmDelete(product.id)"
                  class="itbms-delete-button bg-red-500 hover:bg-red-600 text-white p-2 rounded-lg font-medium shadow-sm transition"
                >
                  <span class="material-icons">
                  delete
                  </span>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Delete Confirmation Modal -->
    <div
      v-if="showModal"
      class="fixed inset-0 flex items-center justify-center z-50 bg-black/60 backdrop-blur-sm"
    >
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md p-8">
        <h2 class="text-2xl font-bold text-gray-800 mb-4">Confirm Delete</h2>
        <p class="itbms-message text-gray-600 mb-6">
          Do you want to delete this sale item?
        </p>
        <div class="flex justify-end gap-4">
          <button
            @click="showModal = false"
            class="itbms-cancel-button bg-gray-200 hover:bg-gray-300 text-gray-700 px-4 py-2 rounded-lg transition"
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
  </div>
</template>

