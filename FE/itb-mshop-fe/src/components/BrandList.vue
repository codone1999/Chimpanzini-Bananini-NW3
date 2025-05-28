<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ref, onMounted } from "vue";
import { getItems, deleteItemById } from "@/lib/fetchUtils";

const route = useRoute()
const router = useRouter()

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
    const item = await deleteItemById('http://intproj24.sit.kmutt.ac.th/nw3/api/v1/brands', selectedBrandId.value)
    if (!item || item?.status === 404 || item === 404) {
      showModal.value = false
      isCanDelete.value = false
      handleDeleteSuccess('An error has occurred, the brand does not exist.')
      return
    } else if (item && item.noOfSaleItems > 0) {
      showModal.value = false
      isCanDelete.value = false
      handleDeleteSuccess(`Delete ${selectedBrand.value} is not allowed. There are sale items with ${selectedBrand.value} brand.`)
      return
    }
  } catch (error) {
    console.error('Failed to fetch product:', error);
  }

  showModal.value = false
  handleDeleteSuccess('The brand has been deleted.')
}

async function handleDeleteSuccess(message){
  showSuccessMessage.value = true
  successMessage.value = message

  setTimeout(() => {
    showSuccessMessage.value = false
  }, 3000)

  // Fetch the updated brand list
  try {
    const updatedItems = await getItems('http://intproj24.sit.kmutt.ac.th/nw3/api/v1/brands')
    if (updatedItems && updatedItems !== 404) {
      brands.value = updatedItems
    }
  } catch (err) {
    console.error('Failed to reload brand list:', err)
  }
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
  handleQuerySuccess('added', 'The brand has been added.')
  handleQuerySuccess('failed_add', 'The brand could not be added.')
  handleQuerySuccess('edited', 'The brand has been updated.')
  handleQuerySuccess('failed_edit', 'The brand does not exit.')

  try {
    const item = await getItems('http://intproj24.sit.kmutt.ac.th/nw3/api/v1/brands')
    if (!item || item?.status === 404) {
      router.push('ListSaleItem')
      return
    }
    brands.value = item;
  } catch (error) {
    console.error('Failed to fetch product:', error);
  }
})
</script>

<template>
  <div class="p-8 min-h-screen bg-gray-50">
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
        <div class="text-sm text-gray-500 flex items-center gap-2">
          <router-link
            :to="{ name: 'ListSaleItem'}"
            class="Itbms-item-list hover:underline hover:text-[#7e5bef] transition"
          >
            Sale Item List
          </router-link>
          <span class="text-gray-400">›</span>
          <router-link
            :to="{ name: 'AddBrand'}"
            class="itbms-add-button hover:underline hover:text-[#7e5bef] transition"
          >
            Add Brand
          </router-link>
        </div>
      </div>

      <!-- Brand Table -->
      <div class="overflow-x-auto shadow-xl ring-1 ring-gray-200 rounded-2xl">
        <table class="min-w-full bg-white text-sm text-center table-auto">
          <thead class="bg-purple-300 text-gray-700 font-semibold uppercase tracking-wide">
            <tr>
              <th class="px-4 py-4 border-b border-gray-300">ID</th>
              <th class="px-4 py-4 border-b border-gray-300">Name</th>
              <th class="px-4 py-4 border-b border-gray-300">Action</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="brand in brands" :key="brand.id" 
              class="itbms-row hover:bg-purple-50 transition duration-200"
            >
              <td class="itbms-id px-4 py-3">{{ brand.id }}</td>
              <td class="itbms-name px-4 py-3">{{ brand.name }}</td>
              <td class="px-4 py-3">
                <div class="flex justify-center items-center gap-2">
                  <router-link
                    :to="{ name: 'EditBrand', params: { id: brand.id }}"
                    class="itbms-edit-button bg-[#9f7aea] hover:bg-[#805ad5] text-white p-2 rounded-lg font-medium shadow transition duration-300"
                  >
                    <span class="material-icons">edit</span>
                  </router-link>
                  <button
                    @click="confirmDelete(brand.id, brand.name)"
                    class="itbms-delete-button bg-red-500 hover:bg-red-600 text-white p-2 rounded-lg font-medium shadow transition duration-300"
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

  <!-- Delete Confirmation Modal -->
  <div
    v-if="showModal"
    class="fixed inset-0 flex items-center justify-center z-50 bg-black/60 backdrop-blur-sm"
  >
    <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md p-8">
      <h2 class="text-2xl font-bold text-gray-800 mb-4">Confirm Delete</h2>
      <p class="itbms-message text-gray-600 mb-6">
        {{ deleteMessage }}
      </p>
      <div class="flex justify-end gap-4">
        <button
          @click="showModal = false"
          class="itbms-cancel-button bg-gray-200 hover:bg-gray-300 text-gray-700 px-4 py-2 rounded-lg transition"
        >
          Cancel
        </button>
        <button
          v-if="isCanDelete"
          @click="handleDelete"
          class="itbms-confirm-button bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-lg transition"
        >
          Delete
        </button>
      </div>
    </div>
  </div>
</template>

