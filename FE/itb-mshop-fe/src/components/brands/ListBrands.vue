<script setup>
import { useRouter, useRoute } from 'vue-router'
import { ref, onMounted } from "vue";
import { getItems, deleteItemById } from "@/lib/fetchUtils";
import { handleQueryAlerts, handleDeleteAlerts } from "@/lib/alertMessage";
import DeleteBrands from './DeleteBrands.vue';

const url = `${import.meta.env.VITE_APP_URL}/brands`
const router = useRouter()
const route = useRoute()

const showModal = ref(false)

const showSuccessMessage = ref(false)
const successMessage = ref('')
const deleteMessage = ref('')
const isCanDelete = ref(true)

const brands = ref([])

const selectedBrandId = ref(null)
const selectedBrand = ref(null)

function confirmDelete(id, name) {
  deleteMessage.value = `Do you want to delete ${name} brand?`
  isCanDelete.value = true

  const brand = brands.value.find(obj => obj.id === id)
  if (brand && brand.noOfSaleItems > 0) {
    deleteMessage.value = `Delete ${name} is not allowed. There are sale items with ${name} brand.`
    isCanDelete.value = false
  }

  selectedBrandId.value = id
  selectedBrand.value = name
  showModal.value = true
}

async function handleDelete() {
  if (!selectedBrandId.value) return

  try {
    const item = await deleteItemById(url, selectedBrandId.value)
    if (item === 400 || item === 500 || item === 404) {
      showModal.value = false
      isCanDelete.value = false
      handleDeleteAlerts(router, showSuccessMessage, successMessage, 'An error has occurred, the brand does not exist.', brands, url)
      return
    } else if (item && item.noOfSaleItems > 0) {
      showModal.value = false
      isCanDelete.value = false
      handleDeleteAlerts(router, showSuccessMessage, successMessage, 
        `Delete ${selectedBrand.value} is not allowed. There are sale items with ${selectedBrand.value} brand.`, brands, url)
      return
    }
  } catch (error) {
    console.error('Failed to fetch product:', error);
  }

  showModal.value = false
  handleDeleteAlerts(router, showSuccessMessage, successMessage, 'The brand has been deleted.', brands, url)
}

onMounted(async () => {
  handleQueryAlerts(
    route,
    router,
    {
      added: 'The brand has been added.',
      failed_add: 'The brand could not be added.',
      edited: 'The brand has been updated.',
      failed_edit: 'The brand does not exit.'
    },
    showSuccessMessage,
    successMessage
  )

  try {
    const item = await getItems(url)
    if (typeof item === 'number') {
      router.push({ name: 'ListSaleItems'})
      return
    }
    brands.value = item;
  } catch (error) {
    console.error('Failed to fetch :', error);
  }
})
</script>

<template>
  <div class="p-8 min-h-screen bg-gray-900 text-gray-200">
    <div class="w-full max-w-screen-lg mx-auto">

      <!-- Success Message -->
      <transition name="fade">
        <div 
          v-if="showSuccessMessage" 
          class="itbms-message mb-6 p-4 text-green-800 bg-green-100 border border-green-300 rounded-lg shadow-sm text-sm font-medium"
        >
          {{ successMessage }}
        </div>
      </transition>

      <!-- Breadcrumb & Header -->
      <div class="flex justify-between items-center mb-6">
        <div class="text-sm text-gray-400 flex items-center gap-2">
          <router-link
            :to="{ name: 'ListSaleItems'}"
            class="Itbms-item-list hover:underline hover:text-[#7e5bef] transition"
          >
            Sale Item List
          </router-link>
          <span class="text-gray-600">›</span>
          <router-link
            :to="{ name: 'AddBrand'}"
            class="itbms-add-button hover:underline hover:text-[#7e5bef] transition"
          >
            Add Brand
          </router-link>
        </div>
      </div>

      <!-- Brand Table -->
      <div class="overflow-x-auto shadow-2xl border border-gray-800 rounded-2xl bg-gradient-to-b from-gray-900 to-gray-950">
        <table class="min-w-full text-sm text-center table-auto">
          <thead class="bg-gradient-to-t from-purple-700 to-indigo-700 text-gray-100 font-semibold uppercase tracking-wide">
            <tr>
              <th class="px-4 py-4 border-b border-gray-800">ID</th>
              <th class="px-4 py-4 border-b border-gray-800">Name</th>
              <th class="px-4 py-4 border-b border-gray-800">Action</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="brand in brands" :key="brand.id" 
              class="itbms-row hover:bg-purple-800/30 transition duration-200"
            >
              <td class="itbms-id px-4 py-3 text-gray-300">{{ brand.id }}</td>
              <td class="itbms-name px-4 py-3 font-medium">{{ brand.name }}</td>
              <td class="px-4 py-3">
                <div class="flex justify-center items-center gap-2">
                  <router-link
                    :to="{ name: 'EditBrand', params: { id: brand.id }}"
                    class="itbms-edit-button bg-gradient-to-r from-purple-500 to-indigo-600 hover:opacity-90 text-white p-2 rounded-lg shadow transition duration-300"
                  >
                    <span class="material-icons">edit</span>
                  </router-link>
                  <button
                    @click="confirmDelete(brand.id, brand.name)"
                    class="itbms-delete-button bg-red-600 hover:bg-red-700 text-white p-2 rounded-lg shadow transition duration-300"
                  >
                    <span class="material-icons">delete</span>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

    </div>
  </div>

  <!-- Delete Confirmation -->
  <DeleteBrands 
    :show-modal="showModal"
    :not-show-model="function(){showModal = false}"
    :is-can-delete="isCanDelete"
    :delete-message="deleteMessage"
    :handle-delete="handleDelete"
  />
</template>

