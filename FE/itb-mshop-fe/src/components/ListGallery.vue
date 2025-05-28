<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { getItems } from '@/lib/fetchUtils'
import { useRoute, useRouter } from 'vue-router'
import phoneImg from '../../public/phone.png'

const route = useRoute()
const router = useRouter()

const SESSION_KEYS = {
  FILTER_BRANDS: 'session_filterBrands',
  SORT_MODE: 'session_sortMode',
  PAGE_SIZE: 'session_pageSize',
  CURRENT_PAGE: 'session_currentPage'
}

const showSuccessMessage = ref(false)
const successMessage = ref('')

const allProducts = ref([])
const products = ref([])
const brands = ref([])

const filterBrands = ref([])
const showBrandList = ref(false)
const sortMode = ref('none') // 'asc' | 'desc' | 'none'
const brandToAdd = ref('')

const pageSize = ref(10) // Has 10 products per page by default
const currentPage = ref(1)
const totalPages = ref(1)

// -------------------------
// Session Storage
// -------------------------
function loadSession() {
  const savedFilters = sessionStorage.getItem(SESSION_KEYS.FILTER_BRANDS)
  if (savedFilters) filterBrands.value = JSON.parse(savedFilters)

  const savedSort = sessionStorage.getItem(SESSION_KEYS.SORT_MODE)
  if (savedSort) sortMode.value = savedSort  // saveSort = 'asc' | 'desc' | 'none'

  const savedSize = sessionStorage.getItem(SESSION_KEYS.PAGE_SIZE)
  if (savedSize) pageSize.value = parseInt(savedSize)

  const savedPage = sessionStorage.getItem(SESSION_KEYS.CURRENT_PAGE)
  if (savedPage) currentPage.value = parseInt(savedPage)
}

function saveSession() {
  sessionStorage.setItem(SESSION_KEYS.FILTER_BRANDS, JSON.stringify(filterBrands.value))
  sessionStorage.setItem(SESSION_KEYS.SORT_MODE, sortMode.value)
  sessionStorage.setItem(SESSION_KEYS.PAGE_SIZE, pageSize.value.toString())
  sessionStorage.setItem(SESSION_KEYS.CURRENT_PAGE, currentPage.value.toString())
}

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
  fetchFilteredSaleItems()
}

async function fetchFilteredSaleItems() {
  if (currentPage.value < 1) {
    currentPage.value = 1
  }

  let url = 'http://intproj24.sit.kmutt.ac.th/nw3/api/v2/sale-items?'
  const query = []

  query.push('page=' + (currentPage.value - 1))

  if (filterBrands.value.length > 0) {
    for (const brand of filterBrands.value) {
      query.push('filterBrands=' + brand)
    }
  }

  if (pageSize.value) {
    query.push('size=' + pageSize.value)
  }

  if (sortMode.value === 'none') {
    query.push('sortField=createdOn')
    query.push('sortDirection=asc')
  } else {
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

    if (currentPage.value > totalPages.value) {
      // currentPage.value = totalPages.value
      currentPage.value = 1
    }

  } catch (error) {
    console.error('Fetch error:', error)
  }
}

watch([filterBrands, sortMode, pageSize, currentPage], () => {
  saveSession()
  fetchFilteredSaleItems()
}, {deep: true})


// ---------------------
// SORT & FILTER FUNCTION
// ---------------------
function toggleBrand(brandName) {
  if (filterBrands.value.includes(brandName)) {
    filterBrands.value = filterBrands.value.filter(b => b !== brandName)
  } else {
    filterBrands.value.push(brandName)
  }

  brandToAdd.value = '' // reset the dropdown
}

// Auto add brand after selected
watch(brandToAdd, (value) => {
  if (value && !filterBrands.value.includes(value)) {
    toggleBrand(value)
  }
})

function clearBrandFilters() {
  filterBrands.value = []
}

function changeSort(mode) {
  sortMode.value = mode
}

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
  loadSession()

  handleQuerySuccess('added', 'The sale item has been successfully added.')
  handleQuerySuccess('deleted', 'The sale item has been deleted.')
  handleQuerySuccess('failed_delete', 'The requested sale item does not exist.')

  await fetchFilteredSaleItems()

  try {
    const item = await getItems('http://intproj24.sit.kmutt.ac.th/nw3/api/v1/brands')
    // Sort Alphabetically by brand name (case-insensitive)
    brands.value = item.sort((a, b) =>
      a.name.toLowerCase().localeCompare(b.name.toLowerCase())
    )
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
    <div
      v-if="showSuccessMessage"
      class="itbms-message mb-6 p-4 text-sm font-medium text-green-800 bg-green-100 border border-green-300 rounded-lg shadow-sm"
      role="alert"
    >
      {{ successMessage }}
    </div>

    <!-- Filter & Sort & Page Size -->
    <div class="flex flex-col gap-4 md:flex-row md:justify-between md:items-start w-full mb-8">

      <!-- Filter Controls -->
      <div class="flex flex-wrap md:flex-nowrap gap-2 w-full items-start">

        <!-- Filter Box -->
        <div
          class="itbms-brand-filter flex flex-wrap items-center content-start gap-2 px-3 py-2 border border-gray-300 rounded bg-white min-h-[49px] max-w-[500px] flex-grow cursor-pointer relative"
        > 
          <!-- Placeholder -->
          <span 
            v-if="filterBrands.length === 0" 
            class="pt-1 text-gray-400"
          >
            Filter by brand(s)
          </span>

          <!-- Selected Brands -->
          <span
            v-else
            v-for="brand in filterBrands"
            :key="brand"
            class="inline-flex items-center gap-1 bg-purple-100 text-purple-700 px-3 pr-1.5 mt-0.5 rounded-full text-sm font-medium shrink-0 z-1"
          >
            <span class="itbms-filter-item">
              {{ brand }}
            </span>
            <button @click.stop="toggleBrand(brand)" class="itbms-filter-item-clear pt-0.6 -mb-1 text-purple-500 hover:text-red-700">
              <span class="material-icons">close</span>
            </button>
          </span>

          <!-- Dropdown Brand list -->
          <div
            v-if="showBrandList === true" 
            class="absolute top-full left-0 mt-2 bg-white border border-gray-300 rounded shadow-md z-10 w-full max-h-72 overflow-y-auto"
          >
            <button
              v-for="brand in brands"
              :key="brand.id"
              :disabled="filterBrands.includes(brand.name)"
              @click="toggleBrand(brand.name)"
              class="itbms-filter-item block w-full text-left px-4 py-2 text-sm hover:bg-purple-100"
              :class="filterBrands.includes(brand.name)
                ? 'text-gray-300'
                : 'text-black'
              "
            >
              {{ brand.name }}
            </button>
          </div>
        </div>

        <!-- Add Filter -->
        <button
          class="itbms-brand-filter-button flex items-center gap-1 p-3 text-white bg-purple-600 hover:bg-purple-800 rounded-md transition"
          @click="showBrandList = !showBrandList"
          title="Filter brands"
        >
          <span class="material-icons">filter_alt</span>
        </button>

        <!-- Clear Filter -->
        <button
          @click="clearBrandFilters"
          class="itbms-brand-filter-clear flex items-center gap-1 p-3 text-white bg-red-600 hover:bg-red-800 rounded-md transition"
          title="Clear all filters"
        >
          <span class="material-icons">cleaning_services</span>
        </button>
      </div>

      <!-- Page Size & Sort Buttons -->
      <div class="flex flex-wrap md:flex-nowrap gap-2 items-center w-full md:w-auto">

        <!-- Page Size -->
        <div class="flex items-center gap-2 text-base">
          <label for="pageSize" class="font-medium text-gray-700 whitespace-nowrap">Show:</label>
          <select
            id="pageSize"
            v-model="pageSize"
            class="itbms-page-size border border-gray-300 rounded pl-3 pr-2 py-3 focus:outline-none focus:ring-2 focus:ring-purple-500"
          >
            <option :value="5">5</option>
            <option :value="10">10</option>
            <option :value="20">20</option>
          </select>
        </div>

        <!-- Sort: None -->
        <button
          class="itbms-brand-none px-3 py-2 text-2xl rounded transition"
          :class="sortMode === 'none'
            ? 'bg-white text-black border border-gray-300'
            : 'bg-[#7e5bef] text-white hover:bg-[#6847d5]'"
          @click="changeSort('none')"
          title="No Sort"
        >
          <span class="material-icons">view_headline</span>
        </button>

        <!-- Sort: Asc -->
        <button
          class="itbms-brand-asc flex px-3 py-3 text-2xl rounded transition"
          :class="sortMode === 'asc'
            ? 'bg-white text-black border border-gray-300'
            : 'bg-[#7e5bef] text-white hover:bg-[#6847d5]'"
          @click="changeSort('asc')"
          title="Sort Ascending"
        >
          <span class="material-icons">north</span>
          <span class="material-icons">sort</span>
        </button>

        <!-- Sort: Desc -->
        <button
          class="itbms-brand-desc flex px-3 py-3 text-2xl rounded transition"
          :class="sortMode === 'desc'
            ? 'bg-white text-black border border-gray-300'
            : 'bg-[#7e5bef] text-white hover:bg-[#6847d5]'"
          @click="changeSort('desc')"
          title="Sort Descending"
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
            class="w-full h-56 object-contain bg-gray-300"
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
        class="itbms-page-first px-5 py-2 text-lg rounded-lg border text-white bg-[#7e5bef] disabled:opacity-50 hover:bg-[#6b4edb] hover:shadow-md transition duration-200"
        :class="totalPages === 1 ? 'invisible': 'visible' "
      >
        First
      </button>

      <!-- Previous Page -->
      <button
        @click="goToPage(currentPage - 1)"
        :disabled="currentPage === 1"
        class="itbms-page-prev px-5 py-2 text-lg rounded-lg border text-white bg-[#7e5bef] disabled:opacity-50 hover:bg-[#6b4edb] hover:shadow-md transition duration-200"
        :class="totalPages === 1 ? 'invisible' : 'visible' "
      >
        Prev
      </button>

      <!-- Page Numbers -->
      <button
        v-for="page in visiblePages"
        :key="page"
        @click="goToPage(page)"
        class="px-5 py-2 text-lg rounded-lg border font-medium"
        :class="`itbms-page-${page-1}`,
        {
          'bg-[#7e5bef] text-white hover:bg-[#6b4edb] hover:shadow-md': page === currentPage,
          'bg-white text-[#7e5bef] border-[#7e5bef] hover:bg-[#f3f0ff] hover:shadow-md': page !== currentPage
        }, 
        totalPages === 1 ? 'invisible' : 'visible'
        "
      >
        {{ page }}
      </button>

      <!-- Next Page -->
      <button
        @click="goToPage(currentPage + 1)"
        :disabled="currentPage === totalPages"
        class="itbms-page-next px-5 py-2 text-lg rounded-lg border text-white bg-[#7e5bef] disabled:opacity-50 hover:bg-[#6b4edb] hover:shadow-md transition duration-200"
        :class="totalPages === 1 ? 'invisible' : 'visible' "
      >
        Next
      </button>

      <!-- Last Page -->
      <button
        @click="goToPage(totalPages)"
        :disabled="currentPage === totalPages"
        class="itbms-page-last px-5 py-2 text-lg rounded-lg border text-white bg-[#7e5bef] disabled:opacity-50 hover:bg-[#6b4edb] hover:shadow-md transition duration-200"
        :class="totalPages === 1 ? 'invisible' : 'visible' "
      >
        Last
      </button>
    </div>

  </section>
</template>
