<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { getItems } from '@/lib/fetchUtils'
import { useRoute, useRouter } from 'vue-router'
import phoneImg from '../../public/phone.jpg'

const route = useRoute()
const router = useRouter()

const showSuccessMessage = ref(false)
const successMessage = ref('')

const allProducts = ref([])
const products = ref([])
const brands = ref([])
const selectedBrands = ref([';asldjfda;slkf',';lasjdfls;akdfj',';lsakdjf;lskadfj',';laksjdfdlkf', ';alskjdf;laskdfjs'])
const sortMode = ref('none') // 'asc' | 'desc' | 'none'
const brandToAdd = ref('')

function handleQuerySuccess(type, message) {
  if (route.query[type] === 'true') {
    showSuccessMessage.value = true
    successMessage.value = message
    setTimeout(() => {
      showSuccessMessage.value = false
      const updatedQuery = { ...route.query }
      delete updatedQuery[type]
      router.replace({ name: route.name, query: updatedQuery })
    }, 3000)
  }
}

function applyFilterAndSort() {
  let filtered = [...allProducts.value]

  // Filter by selected brands
  if (selectedBrands.value.length > 0) {
    filtered = filtered.filter(p => p.brandName && selectedBrands.value.includes(p.brandName))
  }

  // Sort
  if (sortMode.value === 'asc') {
    filtered.sort((a, b) => {
      if (a.brandName < b.brandName) return -1
      if (a.brandName > b.brandName) return 1
      return 0
    })
  } else if (sortMode.value === 'desc') {
    filtered.sort((a, b) => {
      if (a.brandName > b.brandName) return -1
      if (a.brandName < b.brandName) return 1
      return 0
    })
  }

  products.value = filtered
}

function toggleBrand(brandName) {
  if (selectedBrands.value.includes(brandName)) {
    selectedBrands.value = selectedBrands.value.filter(b => b !== brandName)
  } else {
    selectedBrands.value.push(brandName)
  }

  brandToAdd.value = '' // reset the dropdown
  applyFilterAndSort() // manually trigger filtering
}

function clearBrandFilters() {
  selectedBrands.value = []
  applyFilterAndSort()
}

function changeSort(mode) {
  sortMode.value = mode
}

onMounted(async () => {
  handleQuerySuccess('added', 'The sale item has been added.')
  handleQuerySuccess('deleted', 'The sale item has been deleted.')
  handleQuerySuccess('failed_delete', 'The requested sale item does not exist.')

  try {
    const data = await getItems('http://intproj24.sit.kmutt.ac.th/nw3/api/v1/sale-items') ?? []
    allProducts.value = data // When do some action will also affect data.
    products.value = [...data] // Clone data by not affect data. (Default Value)
  } catch (error) {
    console.error(error)
  }

  try {
    const item = await getItems('http://intproj24.sit.kmutt.ac.th/nw3/api/v1/brands')
    brands.value = item;
  } catch (error) {
    console.error('Failed to fetch product:', error);
  }
})

// Reapply filter/sort on change
watch([selectedBrands, sortMode], applyFilterAndSort)
</script>


<template>
  <section class="bg-gray-100 py-16 px-4 md:px-10 max-w-7xl mx-auto">
    <h2 class="text-3xl font-extrabold text-gray-900 mb-10 text-center tracking-tight">
      Shop Our Products
    </h2>

    <!-- Success Message -->
    <transition name="fade">
      <div
        v-if="showSuccessMessage"
        class="itbms-message mb-6 p-4 text-sm font-medium text-green-800 bg-green-100 border border-green-300 rounded-lg shadow-sm"
        role="alert"
      >
        {{ successMessage }}
      </div>
    </transition>

    <!-- Filter & Sort Controls -->
    <div class="flex flex-wrap md:flex-nowrap items-start gap-4 mb-8 w-full">
      <!-- Filter Controls (Left) -->
      <div class="flex-shrink-0 flex gap-2">
        <select
          v-model="brandToAdd"
          class="border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-purple-500"
        >
          <option disabled selected value="">Filter by brand(s)</option>
          <option 
            v-for="brand in brands" 
            :key="brand.id" 
            :value="brand.name"
          >
            {{ brand.name }}
          </option>
        </select>

        <!-- Add Filter -->
        <button
          class="px-3 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded transition"
          @click="brandToAdd && toggleBrand(brandToAdd)"
        >
          <span class="material-icons">filter_alt</span>
        </button>

        <!-- Clear Filter -->
        <button
          class="px-3 py-2 bg-gray-200 hover:bg-gray-300 rounded transition"
          @click="clearBrandFilters"
        >
          <span class="material-icons">cleaning_services</span>
        </button>
      </div>

      <!-- Brand Chips (Middle) -->
      <div class="flex-1 flex flex-wrap gap-2 items-center overflow-auto min-w-0">
        <span
          v-for="brand in selectedBrands"
          :key="brand"
          class="inline-flex items-center bg-purple-100 text-purple-700 px-3 pt-1 rounded-full text-sm"
        >
          <span class="pb-1.5">{{ brand }}</span>

          <!-- Delete Brand Filter -->
          <button
            class="ml-2 text-purple-500 hover:text-purple-700"
            @click="toggleBrand(brand)"
          >
            <span class="material-icons">close</span>
          </button>
        </span>
      </div>

      <!-- Sort Buttons (Right) -->
      <div class="flex-shrink-0 flex gap-2 items-center">
        <button
          class="px-3 py-2 text-2xl rounded transition"
          :class="sortMode === 'none' 
            ? 'bg-white text-black border border-gray-300' 
            : 'bg-[#7e5bef] text-white hover:bg-[#6847d5]'"
          @click="changeSort('none')"
        >
          <span class="material-icons">view_headline</span>
        </button>

        <!-- Ascending -->
        <button
          class="px-3 py-2 text-2xl rounded transition"
          :class="sortMode === 'asc' 
            ? 'bg-white text-black border border-gray-300' 
            : 'bg-[#7e5bef] text-white hover:bg-[#6847d5]'"
          @click="changeSort('asc')"
        >
          <span class="material-icons">north</span>
          <span class="material-icons">sort</span>
        </button>

        <!-- Descending -->
        <button
          class="px-3 py-2 text-2xl rounded transition"
          :class="sortMode === 'desc' 
            ? 'bg-white text-black border border-gray-300' 
            : 'bg-[#7e5bef] text-white hover:bg-[#6847d5]'"
          @click="changeSort('desc')"
        >
          <span class="material-icons">south</span>
          <span class="material-icons">sort</span>
        </button>
      </div>
    </div>

    <!-- Add Item Button -->
    <div class="mb-8 text-center">
      <router-link
        :to="{ name: 'AddItem', query: { from: 'Gallery' } }"
        class="itbms-sale-item-add inline-block bg-[#7e5bef] hover:bg-[#6847d5] text-white pl-3 pr-4 py-4 rounded-xl text-base font-semibold shadow-lg transition duration-300"
      >
        <div class="flex justify-between gap-1">
          <span class="material-icons">
            add
          </span>
          Add Item
        </div>
      </router-link>
    </div>

    <!-- No Products -->
    <div v-if="products.length === 0" class="text-center text-gray-500 text-lg py-10">
      no sale item
    </div>

    <!-- Product Grid -->
    <div
      v-else
      class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6"
    >
      <div
        v-for="product in products"
        :key="product.id"
        class="itbms-row bg-white rounded-2xl border border-gray-200 overflow-hidden shadow-md hover:shadow-xl transition duration-300 flex flex-col"
      >
        <router-link
          :to="{ name: 'ListDetails', params: { id: product.id } }"
          class="block h-full flex flex-col"
        >
          <img
            :src="phoneImg"
            :alt="product.model"
            class="w-full h-56 object-contain bg-gray-100"
          />

          <div class="p-5 flex flex-col flex-grow text-center">
            <h3 class="itbms-brand text-sm font-medium text-gray-500 uppercase tracking-wide">
              {{ product.brandName }}
            </h3>

            <p class="text-[#7e5bef] font-semibold mt-1 mb-2 line-clamp-2">
              <span class="itbms-model">{{ product.model }}</span> /
              <span class="itbms-ramGb">{{ product.ramGb ?? '-' }}</span>
              <span class="itbms-ramGb-unit">GB</span> /
              <span class="itbms-storageGb">{{ product.storageGb ?? '-' }}</span>
              <span class="itbms-storageGb-unit">GB</span>
            </p>

            <button
              class="itbms-button mt-auto w-full bg-[#7e5bef] hover:bg-[#6847d5] text-white py-2 rounded-lg font-bold transition"
            >
              Baht <span class="itbms-price">{{ product.price.toLocaleString() }}</span>
            </button>
          </div>
        </router-link>
      </div>
    </div>
  </section>
</template>

