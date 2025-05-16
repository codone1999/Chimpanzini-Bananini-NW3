<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ref, onMounted } from "vue";
import { getItems, deleteItemById } from "@/lib/fetchUtils";

const route = useRoute()
const router = useRouter()
const id = route.params.id

const showModal = ref(false)

const brands = ref([
  { id: 1, "name": "Samsung"},
  { id: 2, "name": "Apple"},
  { id: 3, "name": "Xiaomi"},
  { id: 4, "name": "Huawei"}
])

function confirmDelete() {
  showModal.value = true
}

async function handleDelete() {
  try {
    const item = await deleteItemById('http://ip24nw3.sit.kmutt.ac.th:8080/v1/brands', id)
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
  <div class="p-8 bg-white min-h-screen">
    <!-- Breadcrumb & Header -->
    <div class="flex justify-between items-center mb-6">
      <div class="text-sm text-neutral-500 tracking-wide">
        <router-link
          :to="{ name: 'ListSaleItem'}"
          class="Itbms-item-list text-[#9147ff] hover:underline"
        >
          Sale Item List
        </router-link>
        <span class="mx-2">›</span>
        <router-link
          :to="{ name: 'AddBrand'}"
          class="itbms-add-button text-[#9147ff] hover:underline"
        >
          Add Brand
        </router-link>
      </div>
    </div>

    <!-- Task Status Table -->
    <div class="overflow-hidden rounded-xl border border-neutral-200 shadow-sm">
      <table class="min-w-full divide-y divide-neutral-200 bg-white">
        <thead class="bg-neutral-100 text-neutral-600 text-sm font-semibold tracking-wide">
          <tr>
            <th class="px-6 py-3 text-left">id</th>
            <th class="px-6 py-3 text-left">Name</th>
            <th class="px-6 py-3 ">Action</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-neutral-200 text-neutral-700">
          <tr 
            v-for="brand in brands" :key="brand.id" 
            class="itbms-row hover:bg-neutral-50 transition"
          >
            <td class="px-6 py-4">{{ brand.id }}</td>
            <td class="px-6 py-4">{{ brand.name }}</td>
            <td class="px-6 py-4">
              <div class="flex justify-center gap-3">
                <router-link :to="{ name: 'EditBrand', params: { id: brand.id }}" 
                  class="itbms-edit-button bg-[#9f7aea] hover:bg-[#805ad5] text-white px-4 py-2 rounded-lg font-medium shadow transition duration-300"
                >
                  EDIT
                </router-link>
                <button
                  @click="confirmDelete()"
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
        Do you want to delete this brand?
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
          class="itbms-confirm-button bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-lg transition0"
        >
          Delete
        </button>
      </div>
    </div>
  </div>
</template>
