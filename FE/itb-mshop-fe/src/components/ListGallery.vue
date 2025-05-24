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

const filterBrands = ref([])
const sortMode = ref('none') // 'asc' | 'desc' | 'none'
const brandToAdd = ref('')
const isBrandFilterOpen = ref(false)

const pageSize = ref(10) // Has 10 products per page by default
const currentPage = ref(1)
const totalPages = ref(1)

// ---------------------
// PAGE FUNCTION
// ---------------------
const visiblePages = computed(() => {
  const total = totalPages.value
  const current = currentPage.value
  const maxVisible = 10 // Follow Requirement
  const half = Math.floor(maxVisible / 2)

  let start = Math.max(1, current - half)
  let end = Math.min(total, current + half)

  if (end - start + 1 < maxVisible) {
    if (start === 1) {
      end = Math.min(total, start + maxVisible - 1)
    } else if (end === total) {
      start = Math.max(1, total - maxVisible + 1)
    }
  }

  const pages = []
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
})

function goToPage(page) {
  currentPage.value = page
}

async function fetchFilteredSaleItems() {
  let url = 'http://intproj24.sit.kmutt.ac.th/nw3/api/v2/sale-items?'
  const query = []

  // Always required
  query.push('page=' + (currentPage.value - 1))

  // Add filterBrands only if list is not empty
  if (filterBrands.value.length > 0) {
    for (const brand of filterBrands.value) {
      query.push('filterBrands=' + brand)
    }
  }

  // Add size only if explicitly set
  if (pageSize.value) {
    query.push('size=' + pageSize.value)
  }

  // Always send sortField only if user is sorting
  if (sortMode.value !== 'none') {
    query.push('sortField=brand.name')
    query.push('sortDirection=' + sortMode.value)
  }

  url += query.join('&')

  try {
    const response = await fetch(url)

    if (!response.ok) {
      throw new Error('Fetch failed: ' + response.statusText)
    }

    const data = await response.json()

    allProducts.value = data.content
    products.value = [...data.content]
    totalPages.value = data.totalPages
  } catch (error) {
    console.error('Fetch error:', error)
  }
}

// Reset page when pageSize
watch([pageSize], () => {
  currentPage.value = 1
})

watch([filterBrands, sortMode, pageSize, currentPage], () => {
  fetchFilteredSaleItems()
})

// ---------------------
// SORT & FILTER FUNCTION
// ---------------------
function applyFilterAndSort() {
  let filtered = [...allProducts.value]

  // Filter by selected brands
  if (filterBrands.value.length > 0) {
    filtered = filtered.filter(p => p.brandName && filterBrands.value.includes(p.brandName))
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
  if (filterBrands.value.includes(brandName)) {
    filterBrands.value = filterBrands.value.filter(b => b !== brandName)
  } else {
    filterBrands.value.push(brandName)
  }

  brandToAdd.value = '' // reset the dropdown
  applyFilterAndSort() // manually trigger filtering
}

function clearBrandFilters() {
  filterBrands.value = []
  applyFilterAndSort()
}

function changeSort(mode) {
  sortMode.value = mode
}

// Reapply filter/sort on change
watch([filterBrands, sortMode], applyFilterAndSort)

// ---------------------
// HANDLER DATA FUNCTION
// ---------------------
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

onMounted(async () => {
  handleQuerySuccess('added', 'The sale item has been added.')
  handleQuerySuccess('deleted', 'The sale item has been deleted.')
  handleQuerySuccess('failed_delete', 'The requested sale item does not exist.')

  await fetchFilteredSaleItems()

  // try {
  //   const data = await getItems('http://intproj24.sit.kmutt.ac.th/nw3/api/v1/sale-items') ?? []
  //   allProducts.value = data // When do some action will also affect data.
  //   products.value = [...data] // Clone data by not affect data. (Default Value)
  // } catch (error) {
  //   console.error(error)
  // }

  try {
    const item = await getItems('http://intproj24.sit.kmutt.ac.th/nw3/api/v1/brands')
    brands.value = item;
  } catch (error) {
    console.error('Failed to fetch product:', error);
  }
})
</script>


<template>
  <section class="bg-gray-100 py-16 px-4 md:px-10">
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

    <!-- Filter & Sort & Page Size -->
    <div class="flex justify-between md:flex-nowrap items-start gap-4 mb-8 w-full">

      <!-- Filter Controls (Left) -->
      <div class="relative w-full max-w-[42%]">

        <!-- Row with Tags and Buttons -->
        <div class="flex items-start gap-2 flex-wrap">
          
          <!-- Brand Tags Container -->
          <div
            class="flex-1 flex-wrap whitespace-nowrap flex gap-2 px-2 py-2 border border-gray-300 rounded-md bg-white min-h-[49px]"
          >
            <span 
              v-if="filterBrands.length === 0"
              class="mt-1 ml-1 text-gray-300"
            >
              Filter by brand(s)
            </span>

            <span
              v-else
              v-for="brand in filterBrands"
              :key="brand"
              class="inline-flex items-center gap-1 bg-purple-100 text-purple-700 px-3 pr-1.5 rounded-full text-sm font-medium shrink-0"
            >
              {{ brand }}
              <button @click="toggleBrand(brand)" class="pt-0.6 -mb-1 text-purple-500 hover:text-purple-700">
                <span class="material-icons">close</span>
              </button>
            </span>
          </div>

          <!-- Filter Button -->
          <button
            @click="isBrandFilterOpen = !isBrandFilterOpen"
            class="flex items-center gap-1 p-3 text-white bg-purple-600 hover:bg-purple-800 rounded-md transition"
            title="Filter brands"
          >
            <span class="material-icons">filter_alt</span>
          </button>

          <!-- Clear Button -->
          <button
            @click="clearBrandFilters"
            class="flex items-center gap-1 p-3 text-white bg-red-600 hover:bg-red-800 rounded-md transition"
            title="Clear all filters"
          >
            <span class="material-icons">cleaning_services</span>
          </button>
        </div>

        <!-- Dropdown Below Tags -->
        <div
          v-if="isBrandFilterOpen"
          class="absolute top-full right-0 mt-2 z-20 bg-white border border-gray-300 rounded shadow-md max-h-60 overflow-y-auto w-64"
        >
          <div
            v-for="brand in brands"
            :key="brand.id"
            class="flex items-center gap-2 p-2 hover:bg-gray-100 cursor-pointer"
          >
            <input
              type="checkbox"
              :id="`brand-${brand.name}`"
              :checked="filterBrands.includes(brand.name)"
              @change="toggleBrand(brand.name)"
              class="form-checkbox text-purple-600"
            />
            <label :for="`brand-${brand.name}`" class="text-sm text-gray-700">
              {{ brand.name }}
            </label>
          </div>
        </div>
      </div>

      <!-- PageSize & Sort Buttons (Right) -->
      <div class="flex-shrink-0 flex gap-2 items-center">
        <!-- Page Size -->
        <div class="flex items-center gap-2 text-base">
          <label for="pageSize" class="font-medium text-gray-700">Show:</label>
          <select
            id="pageSize"
            v-model="pageSize"
            class="border border-gray-300 rounded pl-3 pr-2 py-3 focus:outline-none focus:ring-2 focus:ring-purple-500"
          >
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="20">20</option>
          </select>
        </div>

        <!-- None Sort -->
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
      class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6"
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

    <!-- Pagination Controls with Page Numbers -->
    <div class="flex justify-center items-center mt-10 gap-2 flex-wrap">
      <!-- First Page -->
      <button
        @click="goToPage(1)"
        :disabled="currentPage === 1"
        class="px-3 py-1 rounded-md border text-white bg-[#7e5bef] disabled:opacity-50"
      >
        First
      </button>

      <!-- Previous Page -->
      <button
        @click="goToPage(currentPage - 1)"
        :disabled="currentPage === 1"
        class="px-3 py-1 rounded-md border text-white bg-[#7e5bef] disabled:opacity-50"
      >
        Prev
      </button>

      <!-- Page Numbers -->
      <button
        v-for="page in visiblePages"
        :key="page"
        @click="goToPage(page)"
        class="px-3 py-1 rounded-md border font-medium"
        :class="{
          'bg-[#7e5bef] text-white': page === currentPage,
          'bg-white text-[#7e5bef] border-[#7e5bef]': page !== currentPage
        }"
      >
        {{ page }}
      </button>

      <!-- Next Page -->
      <button
        @click="goToPage(currentPage + 1)"
        :disabled="currentPage === totalPages"
        class="px-3 py-1 rounded-md border text-white bg-[#7e5bef] disabled:opacity-50"
      >
        Next
      </button>

      <!-- Last Page -->
      <button
        @click="goToPage(totalPages)"
        :disabled="currentPage === totalPages"
        class="px-3 py-1 rounded-md border text-white bg-[#7e5bef] disabled:opacity-50"
      >
        Last
      </button>
    </div>

  </section>
</template>
