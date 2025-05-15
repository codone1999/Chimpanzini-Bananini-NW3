<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ref, onMounted } from "vue";
import { getItems } from "@/lib/fetchUtils";

const route = useRoute()
const router = useRouter()
const id = route.params.id

const showModal = ref(false)

const products = ref([
  {
    id: 1,
    brand: "Apple",
    model: "iPhone 14 Pro Max",
    ram: 6,
    storage: 512,
    color: "Space Black",
    screenSize: 6.7,
    price: "42,900",
    quantity: 5,
  },
  {
    id: 2,
    brand: "Apple",
    model: "iPhone 14",
    ram: 6,
    storage: 256,
    color: "Midnight",
    screenSize: 6.1,
    price: "29,700",
    quantity: 8,
  },
  {
    id: 3,
    brand: "Apple",
    model: "iPhone 13 Pro",
    ram: 6,
    storage: 256,
    color: "Sierra Blue",
    screenSize: 6.1,
    price: "33,000",
    quantity: 3,
  },
  {
    id: 4,
    brand: "Apple",
    model: "iPhone 13",
    ram: 4,
    storage: 128,
    color: "Pink",
    screenSize: 6.1,
    price: "23,100",
    quantity: 10,
  },
  {
    id: 5,
    brand: "Apple",
    model: "iPhone 12 Pro Max",
    ram: 6,
    storage: 256,
    color: "Pacific Blue",
    screenSize: 6.7,
    price: "29,700",
    quantity: 4,
  },
  {
    id: 6,
    brand: "Apple",
    model: "iPhone 12",
    ram: 4,
    storage: 128,
    color: "Purple",
    screenSize: 6.1,
    price: "19,800",
    quantity: 6,
  },
  {
    id: 7,
    brand: "Apple",
    model: "iPhone SE 2022",
    ram: 4,
    storage: 64,
    color: "Starlight",
    screenSize: 4.7,
    price: "14,900",
    quantity: 15,
  },
  {
    id: 8,
    brand: "Apple",
    model: "iPhone 14 Plus",
    ram: 6,
    storage: 256,
    color: "",
    screenSize: 6.7,
    price: "29,700",
    quantity: 7,
  },
]);

function confirmDelete() {
  showModal.value = true
}

async function handleDelete() {
  try {
    const item = await deleteItemById('http://ip24nw3.sit.kmutt.ac.th:8080/v1/sale-items', id)
    if (!item || item?.status === 404 || item === 404) {
      showModal.value = false
      router.push({ name: 'ListGallery', query: {failed_delete: true} })
      return
    }
  } catch (error) {
    console.error('Failed to fetch product:', error);
  }

  showModal.value = false
  router.push({ name: 'ListGallery', query: {deleted: true} })
}

// onMounted(async () => {
//   try {
//     products.value = await getItems(`http://ip24nw3.sit.kmutt.ac.th:8080/v1/sale-items`) ?? []
//   } catch (error) {
//     console.error('Failed to fetch product:', error);
//   }
// })
</script>

<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-4">
      <router-link
        :to="{ name: 'AddItem'}"
        class="itbms-sale-item-add bg-[#7e5bef] hover:bg-[#6847d5] text-white px-6 py-3 rounded-xl text-base font-semibold shadow-lg transition duration-300"
      >
          Add Sale Item
      </router-link>

      <router-link
        :to="{ name: 'BrandList'}"
        class="itbms-manage-brand bg-[#7e5bef] hover:bg-[#6847d5] text-white px-6 py-3 rounded-xl text-base font-semibold shadow-lg transition duration-300"
      >
        Manage Brand
      </router-link>
    </div>

    <table class="w-full border-collapse border border-gray-300">
      <thead>
        <tr class="bg-gray-100 text-center">
          <th class="border border-gray-300 px-4 py-2">Id</th>
          <th class="border border-gray-300 px-4 py-2">Brand</th>
          <th class="border border-gray-300 px-4 py-2">Model</th>
          <th class="border border-gray-300 px-4 py-2">Ram</th>
          <th class="border border-gray-300 px-4 py-2">Storage</th>
          <th class="border border-gray-300 px-4 py-2">Color</th>
          <th class="border border-gray-300 px-4 py-2">Screen Size</th>
          <th class="border border-gray-300 px-4 py-2">Price</th>
          <th class="border border-gray-300 px-4 py-2">Quantity</th>
          <th class="border border-gray-300 px-4 py-2">Action</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="product in products"
          :key="product.id"
          class="itbms-row hover:bg-gray-50 text-center"
        >
          <td class="border border-gray-300 px-4 py-2">{{ product.id }}</td>
          <td class="itbms-brand border border-gray-300 px-4 py-2">{{ product.brand }}</td>
          <td class="itbms-model border border-gray-300 px-4 py-2 text-left">{{ product.model }}</td>
          <td class="itbms-ramGb border border-gray-300 px-4 py-2">{{ product.ram }}</td>
          <td class="itbms-storageGb border border-gray-300 px-4 py-2">{{ product.storage }}</td>
          <td class="itbms-color border border-gray-300 px-4 py-2">{{ product.color }}</td>
          <td class="itbms-screenSizeInch border border-gray-300 px-4 py-2">{{ product.screenSize }}</td>
          <td class="itbms-price border border-gray-300 px-4 py-2">{{ product.price }}</td>
          <td class="itbms-quantity border border-gray-300 px-4 py-2">{{ product.quantity }}</td>
          <td class="border border-gray-300 px-4 py-2 flex justify-center items-center space-x-2">
            <router-link :to="{ name: 'EditItem', params: { id: product.id }}" 
              class="itbms-edit-button bg-blue-400 text-white px-2 py-1 rounded hover:bg-blue-600"
            >
              E
            </router-link>
            <button
              @click="confirmDelete()"
              class="itbms-delete-button bg-red-400 text-white px-2 py-1 rounded hover:bg-red-600"
            >
              D
            </button>
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
        Do you want to delete this sale item?
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
