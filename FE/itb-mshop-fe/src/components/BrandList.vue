<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ref, onMounted } from "vue";
import { getItems, deleteItemById } from "@/lib/fetchUtils";

const route = useRoute()
const router = useRouter()

const showModal = ref(false)

const brands = ref([
  { id: 1, "name": "Samsung"},
  { id: 2, "name": "Apple"},
  { id: 3, "name": "Xiaomi"},
  { id: 4, "name": "Huawei"}
])

const selectedBrandId = ref(null)
const selectedBrand = ref(null)

function confirmDelete(id, name) {
  selectedBrandId.value = id
  selectedBrand.value = name
  showModal.value = true
}

async function handleDelete() {
  if (!selectedBrandId.value) return

  try {
    const item = await deleteItemById('http://ip24nw3.sit.kmutt.ac.th:8080/v1/brands', selectedBrandId.value)
    if (!item || item?.status === 404 || item === 404) {
      showModal.value = false
      router.push({ name: 'BrandList' })
      return
    }
  } catch (error) {
    console.error('Failed to fetch product:', error);
  }

  showModal.value = false
  router.push({ name: 'BrandList'})
}

onMounted(async () => {
  try {
    const item = await getItems('http://ip24nw3.sit.kmutt.ac.th:8080/v1/brands')
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
  <div class="p-6">
    <!-- Breadcrumb & Header -->
    <div class="flex justify-between items-center mb-4">
      <div class="text-gray-500">
        <router-link
          :to="{ name: 'ListSaleItem'}"
          class="Itbms-item-list text-blue-500"
        >
          Sale Item List
        </router-link>
        <span class="mx-2">›</span>
        <router-link
          :to="{ name: 'AddBrand'}"
          class="itbms-add-button text-blue-500"
        >
          Add Brand
        </router-link>
      </div>
    </div>

    <!-- Task Status Table -->
    <table class="min-w-full border border-gray-200 rounded overflow-hidden">
      <thead class="bg-gray-100 text-center">
        <tr>
          <th class="px-4 py-2 border border-gray-300">id</th>
          <th class="px-4 py-2 border border-gray-300">Name</th>
          <th class="px-4 py-2 border border-gray-300">Action</th>
        </tr>
      </thead>
      <tbody>
        <tr 
          v-for="brand in brands" :key="brand.id" 
          class="itbms-row text-center border-t"
        >
          <td class="px-4 py-2 border border-gray-200">{{ brand.id }}</td>
          <td class="px-4 py-2 border border-gray-200">{{ brand.name }}</td>
          <td class="px-4 py-2 border border-gray-200">
            <div class="flex justify-center items-center gap-2">
              <router-link :to="{ name: 'EditBrand', params: { id: brand.id }}" 
                class="itbms-edit-button bg-[#9f7aea] hover:bg-[#805ad5] text-white px-4 py-2 rounded-lg font-medium shadow transition duration-300"
              >
                EDIT
              </router-link>
              <button
                @click="confirmDelete(brand.id, brand.name)"
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

  <!-- Delete Confirmation Modal -->
   <div
    v-if="showModal"
    class="fixed inset-0 flex items-center justify-center z-50 bg-black/60"
  >
    <div class="bg-white rounded-lg shadow-lg w-full max-w-md p-6">
      <h2 class="text-xl font-bold mb-4">Confirm Delete</h2>
      <p class=" itbms-message mb-6">
        Do you want to delete {{ selectedBrand }} brand?
      </p>
      <div class="flex justify-end gap-4">
        <button
          @click="showModal = false"
          class="itbms-cancel-button px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
        >
          Cancel
        </button>
        <button
          @click="handleDelete"
          class="itbms-confirm-button px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
        >
          Delete
        </button>
      </div>
    </div>
  </div>
</template>
