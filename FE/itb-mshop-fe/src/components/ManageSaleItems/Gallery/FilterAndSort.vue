//FilterAndSort.vue
<script setup>
import {ref} from "vue";

const props = defineProps({
  // Brands //
  brands: {
    type: Array,
    required: true
  },
  filterBrands: {
    type: Object,
    required: true
  },
  showBrandList: {
    type: Object,
    required: true
  },
  toggleBrandList: {
    type: Function,
    required: true
  },
  onToggleBrand: {
    type: Function,
    required: true
  },
  onClearBrands: {
    type: Function,
    required: true
  },
  saleItems: {
    type: Array,
    required: true
  },
  // Prices //
  filterPrices:{
    type: Object,
    required: true
  },
  showPriceList: {
    type: Object,
    required: true
  },
  togglePriceList: {
    type: Function,
    required: true
  },
  onTogglePrice: {
    type: Function,
    required: true
  },
  // Storage Sizes //
  filterStorageSizes:{
    type: Object,
    required: true
  },
  showStorageSizeList: {
    type: Object,
    required: true
  },
  toggleStorageSizeList: {
    type: Function,
    required: true
  },
  onToggleStorageSize: {
    type: Function,
    required: true
  },
  // Sorting & Page Size //
  sortMode: {
    type: Object,
    required: true
  },
  onChangeSort: {
    type: Function,
    required: true
  },
  pageSize: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:pageSize'])

// ----------- Options -------------------- //
const storageOptions = [
  "Not Specify",
  32,
  64,
  128,
  256,
  512,
  1
]

const priceOptions = [
  "0-5000",
  "5,001-10,000",
  "10,001-20,000",
  "20,001-30,000",
  "30,001-40,000",
  "40,001-50,000"
]

const customMinPrice = ref('')
const customMaxPrice = ref('')

function handlePageSizeChange(event) {
  const value = parseInt(event.target.value)
  emit('update:pageSize', value)
}

function formatStorage(size) {
  if (size === "Not Specify") return "Not Specify"
  return size >= 1 && size < 10 ? `${size}TB` : `${size}GB`
}

function addCustomPriceRange() {
  if (customMinPrice.value && customMaxPrice.value && Number(customMinPrice.value) <= Number(customMaxPrice.value)) {
    const customRange = `${Number(customMinPrice.value).toLocaleString()}-${Number(customMaxPrice.value).toLocaleString()}`
    
    // Check if this range already exists
    if (!props.filterPrices.includes(customRange)) {
      props.onTogglePrice(customRange)
    }
    
    // Clear the inputs
    customMinPrice.value = ''
    customMaxPrice.value = ''
  }
}
</script>

<template>
  <div class="flex flex-col md:flex-row justify-between gap-4 items-start w-full mb-8 bg-[#1e1e1e] p-2 rounded-lg shadow-md">

    <!-- Filter Section (Expandable Left Side) -->
    <div class="flex items-center gap-3 flex-grow w-full">

      <!-- Pill Brands -->
      <div 
        class="relative flex-1 border rounded-full px-6 py-1.5 border-gray-700 bg-gray-100 shadow-sm text-left hover:border-purple-500 transition-all duration-200"
        @click="props.toggleBrandList"
      >
        <div class="text-sm font-medium text-gray-800">Brand</div>
        <div class="text-xs text-gray-400 mt-1">
          <span v-if="props.filterBrands.length === 0">Filter by brand(s)</span>
          
          <!-- Selected Brands -->
          <span 
            v-else 
            v-for="brand in props.filterBrands" :key="brand"
            class="inline-flex items-center gap-1 bg-purple-100 text-purple-700 px-3 py-0.5 pr-1.5 rounded-full text-sm font-medium"
          >
            {{ brand }}
            <button 
              class="itbms-filter-item-clear hover:text-red-500 -mb-1"
              @click.stop="props.onToggleBrand(brand)"
            >
              <span class="material-icons">close</span>
            </button>
          </span>

          <!-- Brand Dropdown -->
          <div 
            v-if="props.showBrandList"
            class="absolute top-full left-0 mt-2 bg-white border border-gray-300 rounded shadow-md z-10 w-full max-h-72 overflow-y-auto"
          >
            <button
              v-for="brand in props.brands"
              :key="brand.id"
              :disabled="props.filterBrands.includes(brand.name)"
              @click.stop="props.onToggleBrand(brand.name)"
              class="itbms-filter-item block w-full text-left px-4 py-2 text-sm hover:bg-purple-100"
              :class="props.filterBrands.includes(brand.name) ? 'text-gray-300' : 'text-black'"
            >
              {{ brand.name }}
            </button>
          </div>
        </div>
      </div>

      <!-- Pill Price -->
      <div 
        class="relative flex-1 border rounded-full px-6 py-1.5 border-gray-700 bg-gray-100 shadow-sm text-left hover:border-purple-500 transition-all duration-200"
        @click="props.togglePriceList"  
      >
        <div class="text-sm font-medium text-gray-800">Price</div>
        <div class="text-xs text-gray-400 mt-1">
          <span v-if="props.filterPrices.length === 0">Price Range</span>
          
          <!-- Selected Prices -->
          <span 
            v-else 
            v-for="price in props.filterPrices" :key="price"
            class="inline-flex items-center gap-1 bg-purple-100 text-purple-700 px-3 py-0.5 pr-1.5 rounded-full text-sm font-medium"
          >
            {{ price }}
            <button 
              class="itbms-filter-item-clear hover:text-red-500 -mb-1"
              @click.stop="props.onTogglePrice(price)"
            >
              <span class="material-icons">close</span>
            </button>
          </span>

          <!-- Price Dropdown -->
          <div 
            v-if="props.showPriceList"
            class="absolute top-full left-0 mt-2 bg-white border border-gray-300 rounded shadow-md z-10 w-full max-h-72 overflow-y-auto"
          >
            <!-- Fixed option -->
            <div class="border-b border-gray-200 pb-2">
              <button
                v-for="price in priceOptions"
                :key="price"
                :disabled="props.filterPrices.includes(price)"
                @click.stop="props.onTogglePrice(price)"
                class="itbms-filter-item block w-full text-left px-4 py-2 text-sm hover:bg-purple-100"
                :class="props.filterPrices.includes(price) ? 'text-gray-300' : 'text-black'"
              >
                {{ price }}
              </button>
            </div>
            
            <!-- Custom Price Range Input -->
            <div class="p-4">
              <div class="flex items-center gap-2">
                <input
                  type="number"
                  placeholder="Min"
                  v-model="customMinPrice"
                  class="flex-1 px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                  @click.stop
                />
                <span class="text-gray-500 text-sm">-</span>
                <input
                  type="number"
                  placeholder="Max"
                  v-model="customMaxPrice"
                  class="flex-1 px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                  @click.stop
                />
              </div>
              <button
                @click.stop="addCustomPriceRange"
                :disabled="!customMinPrice || !customMaxPrice || customMinPrice > customMaxPrice"
                class="w-full mt-3 px-4 py-2 bg-purple-600 text-white text-sm rounded-md hover:bg-purple-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
              >
                Add Range
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Pill Storage Size -->
      <div  
        class="relative flex-1 border rounded-full px-6 py-1.5 border-gray-700 bg-gray-100 shadow-sm text-left hover:border-purple-500 transition-all duration-200"
        @click="props.toggleStorageSizeList"
      >
        <div class="flex items-center justify-between">
          <div>
            <div class="text-sm font-medium text-gray-800">Storage Size</div>
            <div class="text-xs text-gray-400 mt-1">
              <span v-if="props.filterStorageSizes.length === 0">Storage Range</span>
              
              <!-- Selected Storages -->
              <span 
                v-else 
                v-for="storageSize in props.filterStorageSizes" :key="storageSize"
                class="inline-flex items-center gap-1 bg-purple-100 text-purple-700 px-3 py-0.5 pr-1.5 rounded-full text-sm font-medium"
              >
                {{ formatStorage(storageSize) }}
                <button 
                  class="itbms-filter-item-clear hover:text-red-500 -mb-1"
                  @click.stop="props.onToggleStorageSize(storageSize)"
                >
                  <span class="material-icons">close</span>
                </button>
              </span>

              <!-- Storage Dropdown -->
              <div 
                v-if="props.showStorageSizeList"
                class="absolute top-full left-0 mt-2 bg-white border border-gray-300 rounded shadow-md z-10 w-full max-h-72 overflow-y-auto"
              >
                <button
                  v-for="(size, index) in storageOptions"
                  :key="index"
                  :disabled="props.filterStorageSizes.includes(size)"
                  @click.stop="props.onToggleStorageSize(size)"
                  class="itbms-filter-item block w-full text-left px-4 py-2 text-sm hover:bg-purple-100"
                  :class="props.filterStorageSizes.includes(size) ? 'text-gray-300' : 'text-black'"
                >
                  {{ formatStorage(size) }}
                </button>
              </div>
            </div>
          </div>

          <!-- Filter Icon -->
          <!-- <button
            @click="props.toggleBrandList"
            class="-mr-4 pt-2 px-2 pb-1 bg-gray-200 border border-gray-600 rounded-full hover:bg-gray-300 transition"
          >
            <span class="material-icons text-gray-700">filter_alt</span>
          </button> -->

          <!-- Clear Filters -->
          <button 
            @click.stop="props.onClearBrands"
            class="itbms-brand-filter-clear flex items-center gap-1 -mr-4 p-3 text-white bg-red-600 hover:bg-red-800 rounded-full transition"
          >
            <span class="material-icons">cleaning_services</span>
          </button>
        </div>
      </div>
      
    </div>

    <!-- Sort + Page Size (Right Side) -->
    <div class="flex md:flex-nowrap gap-2 items-center w-full md:w-auto">
      <div class="flex items-center gap-2 text-base">
        <label for="pageSize" class="font-medium text-purple-400 text-shadow-white whitespace-nowrap">Show:</label>
        <select
          :value="props.pageSize"
          @change="handlePageSizeChange"
          class="itbms-page-size border border-gray-300 bg-gray-100 rounded pl-3 pr-2 py-3 focus:outline-none focus:ring-2 focus:ring-purple-500"
        >
          <option :value="5">5</option>
          <option :value="10">10</option>
          <option :value="20">20</option>
        </select>
      </div>

      <!-- Sort: None -->
      <button
        class="itbms-brand-none px-3 py-2 text-2xl rounded transition"
        :class="props.sortMode === 'none'
          ? 'bg-gray-100 text-black border border-gray-300'
          : 'bg-[#7e5bef] text-white hover:bg-[#6847d5]'"
        @click="props.onChangeSort('none')"
        title="No Sort"
      >
        <span class="material-icons">view_headline</span>
      </button>

      <!-- Sort: Asc -->
      <button
        class="itbms-brand-asc flex px-3 py-3 text-2xl rounded transition"
        :class="props.sortMode === 'asc' 
          ? 'bg-white text-black border border-gray-300' 
          : 'bg-[#7e5bef] text-white'"
        @click="props.onChangeSort('asc')"
        title="Sort Ascending"
      >
        <span class="material-icons">north</span>
        <span class="material-icons">sort</span>
      </button>

      <!-- Sort: Desc -->
      <button
        class="itbms-brand-desc flex px-3 py-3 text-2xl rounded transition"
        :class="props.sortMode === 'desc'
          ? 'bg-white text-black border border-gray-300'
          : 'bg-[#7e5bef] text-white hover:bg-[#6847d5]'"
        @click="props.onChangeSort('desc')"
        title="Sort Descending"
      >
        <span class="material-icons">south</span>
        <span class="material-icons">sort</span>
      </button>
    </div>
  </div>
</template>
