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

const products = ref([{"id":1,"model":"iPhone 14 Pro Max","brandName":"Apple","price":42900,"storageGb":512,"ramGb":6,"color":"Space Black"},{"id":2,"model":"iPhone 14","brandName":"Apple","price":29700,"storageGb":256,"ramGb":6,"color":"Midnight"},{"id":3,"model":"iPhone 13 Pro","brandName":"Apple","price":33000,"storageGb":256,"ramGb":6,"color":"Sierra Blue"},{"id":4,"model":"iPhone 13","brandName":"Apple","price":23100,"storageGb":128,"ramGb":4,"color":"Pink"},{"id":5,"model":"iPhone 12 Pro Max","brandName":"Apple","price":29700,"storageGb":256,"ramGb":6,"color":"Pacific Blue"},{"id":6,"model":"iPhone 12","brandName":"Apple","price":19800,"storageGb":128,"ramGb":4,"color":"Purple"},{"id":7,"model":"iPhone SE 2022","brandName":"Apple","price":14190,"storageGb":64,"ramGb":4,"color":"Starlight"},{"id":8,"model":"iPhone 14 Plus","brandName":"Apple","price":29700,"storageGb":256,"ramGb":6,"color":"Blue"},{"id":9,"model":"iPhone 13 mini","brandName":"Apple","price":19800,"storageGb":128,"ramGb":4,"color":"Green"},{"id":10,"model":"iPhone 12 mini","brandName":"Apple","price":16500,"storageGb":64,"ramGb":4,"color":"Red"},{"id":16,"model":"Galaxy S23 Ultra","brandName":"Samsung","price":39600,"storageGb":512,"ramGb":null,"color":null},{"id":17,"model":"Galaxy S23+","brandName":"Samsung","price":33000,"storageGb":256,"ramGb":8,"color":"Cream"},{"id":18,"model":"Galaxy Z Fold4","brandName":"Samsung","price":59400,"storageGb":256,"ramGb":12,"color":"Phantom Green"},{"id":19,"model":"Galaxy Z Flip4","brandName":"Samsung","price":33000,"storageGb":128,"ramGb":8,"color":"Bora Purple"},{"id":20,"model":"Galaxy A53 5G","brandName":"Samsung","price":14850,"storageGb":128,"ramGb":6,"color":"Awesome Blue"},{"id":21,"model":"Galaxy A33 5G","brandName":"Samsung","price":11550,"storageGb":128,"ramGb":6,"color":"Awesome White"},{"id":22,"model":"Galaxy S22","brandName":"Samsung","price":26400,"storageGb":128,"ramGb":8,"color":"Pink Gold"},{"id":23,"model":"Galaxy M53","brandName":"Samsung","price":14850,"storageGb":128,"ramGb":6,"color":"Green"},{"id":24,"model":"Galaxy A73 5G","brandName":"Samsung","price":16500,"storageGb":256,"ramGb":8,"color":"Gray"},{"id":25,"model":"Galaxy S21 FE","brandName":"Samsung","price":19800,"storageGb":128,"ramGb":6,"color":"Olive"},{"id":31,"model":"13 Pro","brandName":"Xiaomi","price":33000,"storageGb":256,"ramGb":12,"color":"Black"},{"id":32,"model":"13T Pro","brandName":"Xiaomi","price":23100,"storageGb":null,"ramGb":12,"color":"Alpine Blue"},{"id":33,"model":"POCO F5","brandName":"Xiaomi","price":13200,"storageGb":256,"ramGb":8,"color":"Carbon Black"},{"id":34,"model":"Redmi Note 12 Pro","brandName":"Xiaomi","price":9900,"storageGb":128,"ramGb":8,"color":"Sky Blue"},{"id":35,"model":"12T Pro","brandName":"Xiaomi","price":21450,"storageGb":256,"ramGb":8,"color":"Cosmic Black"},{"id":36,"model":"POCO X5 Pro","brandName":"Xiaomi","price":9900,"storageGb":128,"ramGb":8,"color":"Yellow"},{"id":37,"model":"Redmi 12C","brandName":"Xiaomi","price":5940,"storageGb":64,"ramGb":4,"color":"Ocean Blue"},{"id":38,"model":"12 Lite","brandName":"Xiaomi","price":13200,"storageGb":128,"ramGb":8,"color":"Lite Pink"},{"id":39,"model":"POCO M5","brandName":"Xiaomi","price":7590,"storageGb":128,"ramGb":6,"color":"Power Black"},{"id":40,"model":"Redmi Note 11","brandName":"Xiaomi","price":8250,"storageGb":128,"ramGb":6,"color":"Star Blue"},{"id":46,"model":"P60 Pro","brandName":"Huawei","price":36300,"storageGb":256,"ramGb":12,"color":"Rococo Pearl"},{"id":47,"model":"Mate 50 Pro","brandName":"Huawei","price":42900,"storageGb":256,"ramGb":8,"color":"Silver Black"},{"id":48,"model":"nova 11 Pro","brandName":"Huawei","price":19800,"storageGb":256,"ramGb":8,"color":"Green"},{"id":49,"model":"P50 Pro","brandName":"Huawei","price":29700,"storageGb":256,"ramGb":8,"color":"Cocoa Gold"},{"id":50,"model":"nova 10","brandName":"Huawei","price":16500,"storageGb":128,"ramGb":8,"color":"Starry Silver"},{"id":51,"model":"Mate X3","brandName":"Huawei","price":66000,"storageGb":512,"ramGb":12,"color":"Feather Gold"},{"id":52,"model":"nova 9","brandName":"Huawei","price":13200,"storageGb":128,"ramGb":8,"color":"Starry Blue"},{"id":53,"model":"P50 Pocket","brandName":"Huawei","price":46200,"storageGb":256,"ramGb":8,"color":"Premium Gold"},{"id":54,"model":"nova Y70","brandName":"Huawei","price":9900,"storageGb":128,"ramGb":4,"color":"Crystal Blue"},{"id":55,"model":"Mate 40 Pro","brandName":"Huawei","price":26400,"storageGb":256,"ramGb":8,"color":"Mystic Silver"},{"id":61,"model":"ROG Phone 7","brandName":"ASUS","price":33000,"storageGb":512,"ramGb":16,"color":"Phantom Black"},{"id":62,"model":"ROG Phone 6D","brandName":"ASUS","price":29700,"storageGb":256,"ramGb":16,"color":"Space Gray"},{"id":63,"model":"Zenfone 9","brandName":"ASUS","price":23100,"storageGb":128,"ramGb":8,"color":"Midnight Black"},{"id":64,"model":"ROG Phone 6","brandName":"ASUS","price":29700,"storageGb":256,"ramGb":12,"color":"Storm White"},{"id":65,"model":"Zenfone 8","brandName":"ASUS","price":19800,"storageGb":128,"ramGb":8,"color":"Obsidian Black"},{"id":66,"model":"ROG Phone 5s","brandName":"ASUS","price":26400,"storageGb":256,"ramGb":12,"color":"Phantom Black"},{"id":67,"model":"Zenfone 8 Flip","brandName":"ASUS","price":26400,"storageGb":256,"ramGb":8,"color":"Galactic Black"},{"id":68,"model":"ROG Phone 5","brandName":"ASUS","price":23100,"storageGb":256,"ramGb":12,"color":"Storm White"},{"id":69,"model":"Zenfone 7","brandName":"ASUS","price":19800,"storageGb":128,"ramGb":8,"color":"Aurora Black"},{"id":70,"model":"ROG Phone 3","brandName":"ASUS","price":16500,"storageGb":256,"ramGb":12,"color":"Black Glare"},{"id":76,"model":"Find X6 Pro","brandName":"OPPO","price":33000,"storageGb":256,"ramGb":12,"color":"Cosmos Black"},{"id":77,"model":"Reno9 Pro+","brandName":"OPPO","price":23100,"storageGb":256,"ramGb":12,"color":"Eternal Gold"},{"id":78,"model":"Find N2 Flip","brandName":"OPPO","price":33000,"storageGb":256,"ramGb":8,"color":"Astral Black"},{"id":79,"model":"Reno8 Pro","brandName":"OPPO","price":19800,"storageGb":256,"ramGb":8,"color":"Glazed Green"},{"id":80,"model":"Find X5 Pro","brandName":"OPPO","price":29700,"storageGb":256,"ramGb":12,"color":"Ceramic White"},{"id":81,"model":"A78","brandName":"OPPO","price":9900,"storageGb":128,"ramGb":8,"color":"Glowing Black"},{"id":82,"model":"Reno7","brandName":"OPPO","price":13200,"storageGb":128,"ramGb":8,"color":"Startrails Blue"},{"id":83,"model":"Find X5 Lite","brandName":"OPPO","price":14850,"storageGb":128,"ramGb":8,"color":"Starry Black"},{"id":84,"model":"A77","brandName":"OPPO","price":8250,"storageGb":128,"ramGb":6,"color":"Ocean Blue"},{"id":85,"model":"Reno6 Pro","brandName":"OPPO","price":16500,"storageGb":256,"ramGb":12,"color":"Arctic Blue"}]);

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

