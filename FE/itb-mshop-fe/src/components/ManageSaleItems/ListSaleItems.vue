<script setup>
import { ref, onMounted } from "vue";
import { getItems, deleteItemById } from "@/lib/fetchUtils";
import { handleQueryAlerts, handleDeleteAlerts } from "@/lib/alertMessage";

const url = `${import.meta.env.VITE_APP_URL}/sale-items`

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
    const item = await deleteItemById(url,  selectedProductId.value)
    if (typeof item === 'number') {
      showModal.value = false
      handleDeleteAlerts(showSuccessMessage, successMessage, 'The requested sale item does not exist.', products, url)
      return
    }
  } catch (error) {
    console.error('Failed to fetch product:', error);
  }

  showModal.value = false
  handleDeleteAlerts(showSuccessMessage, successMessage, 'The sale item has been deleted.', products, url)
}

onMounted(async () => {
  handleQueryAlerts(
    {
      added: 'The sale item has been successfully added.',
      edited: 'The sale item has been edited.',    
    },
    showSuccessMessage,
    successMessage
  )

  try {
    const items = await getItems(url)
    if (typeof items === 'number'){
      alert("Failed To Fetch Sale Items")
      products.value = []
    }
    else
      products.value = items

  } catch (error) {
    console.error('Failed to fetch product:', error);
  }
})
</script>

<template>
  <div class="p-8 min-h-screen bg-gray-900 text-gray-200">
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
        class="itbms-sale-item-add flex items-center gap-2 bg-purple-600 hover:bg-purple-500 text-white pl-3 pr-4 py-3 rounded-xl text-base font-semibold shadow-lg transition duration-300"
      >
        <span class="material-icons">
          add
        </span>
        Add Sale Item
      </router-link>

      <router-link
        :to="{ name: 'ListBrands' }"
        class="itbms-manage-brand flex items-center gap-2 bg-gray-700 hover:bg-gray-600 text-gray-200 px-4 py-3 rounded-xl text-base font-semibold shadow-lg transition duration-300"
      >
        <span class="material-icons">
          build
        </span> 
        Manage Brand
      </router-link>
    </div>

    <!-- No Items -->
    <div v-if="products.length === 0" class="text-center text-gray-400 text-lg py-10">
      No sale item found.
    </div>

    <!-- Product Table -->
    <div v-else class="overflow-x-auto shadow-2xl rounded-2xl border border-gray-700">
      <table class="min-w-full bg-gray-800 text-sm text-center table-auto rounded-2xl overflow-hidden">
        <thead class="bg-gray-700 text-gray-300 font-semibold uppercase tracking-wide">
          <tr>
            <th class="px-4 py-4 border-b border-gray-600">ID</th>
            <th class="px-4 py-4 border-b border-gray-600">Brand</th>
            <th class="px-4 py-4 border-b border-gray-600">Model</th>
            <th class="px-4 py-4 border-b border-gray-600">RAM</th>
            <th class="px-4 py-4 border-b border-gray-600">Storage</th>
            <th class="px-4 py-4 border-b border-gray-600">Color</th>
            <th class="px-4 py-4 border-b border-gray-600">Price</th>
            <th class="px-4 py-4 border-b border-gray-600">Action</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-700">
          <tr
            v-for="product in products"
            :key="product.id"
            class="itbms-row hover:bg-gray-700/50 transition duration-200"
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
                  class="itbms-edit-button bg-gradient-to-r from-purple-500 to-indigo-600 hover:opacity-90 text-white p-2 rounded-lg shadow transition duration-300"
                >
                  <span class="material-icons">
                  edit
                  </span>
                </router-link>
                <button
                  @click="confirmDelete(product.id)"
                  class="itbms-delete-button bg-red-600 hover:bg-red-700 text-white p-2 rounded-lg shadow transition duration-300"
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

