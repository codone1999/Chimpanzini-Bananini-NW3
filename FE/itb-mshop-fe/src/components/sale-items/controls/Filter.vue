//Filter.vue
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
  onTogglePrice: {
    type: Function,
    required: true
  },
  // Storage Sizes //
  filterStorageSizes:{
    type: Object,
    required: true
  },
  onToggleStorageSize: {
    type: Function,
    required: true
  }
})

// ----------- Options -------------------- //
const storageOptions = [
  { value: "Not Specify", label: "Not Specify" },
  { value: 32, label: "32GB" },
  { value: 64, label: "64GB" },
  { value: 128, label: "128GB" },
  { value: 256, label: "256GB" },
  { value: 512, label: "512GB" },
  { value: 1, label: "1TB" }
]

const priceOptions = [
  { value: "0-5000", label: "Under ฿5,000" },
  { value: "5,001-10,000", label: "฿5,001 - ฿10,000" },
  { value: "10,001-20,000", label: "฿10,001 - ฿20,000" },
  { value: "20,001-30,000", label: "฿20,001 - ฿30,000" },
  { value: "30,001-40,000", label: "฿30,001 - ฿40,000" },
  { value: "40,001-50,000", label: "฿40,001 - ฿50,000" }
]

const customMinPrice = ref('')
const customMaxPrice = ref('')

// Collapse states
const showBrands = ref(false)
const showPrices = ref(false)
const showStorage = ref(false)

function addCustomPriceRange() {
  if (customMinPrice.value && customMaxPrice.value && Number(customMinPrice.value) <= Number(customMaxPrice.value)) {
    const customRange = `${Number(customMinPrice.value).toLocaleString()}-${Number(customMaxPrice.value).toLocaleString()}`
    
    if (!props.filterPrices.includes(customRange)) {
      props.onTogglePrice(customRange)
    }
    
    customMinPrice.value = ''
    customMaxPrice.value = ''
  }
}
</script>

<template>
  <aside class="w-80 bg-gray-950/70 border border-gray-800 rounded-2xl shadow-lg sticky top-4 self-start">
    
    <!-- Header -->
    <div class="px-6 py-5 border-b border-gray-800">
      <div class="flex items-center justify-between">
        <h3 class="text-lg font-bold text-white tracking-wide">Filters</h3>
        <button
          @click="props.onClearBrands"
          class="flex items-center gap-1 p-2 bg-red-600/90 text-white text-xs rounded-full hover:bg-red-700 transition-all duration-200"
        >
          <span class="material-icons text-sm">cleaning_services</span>
        </button>
      </div>
    </div>

    <!-- Filter Sections -->
    <div class="overflow-y-auto max-h-[calc(100vh-200px)] custom-scrollbar">
      
      <!-- Brand Filter -->
      <div class="border-b border-gray-800">
        <button
          @click="showBrands = !showBrands"
          class="w-full px-6 py-4 flex items-center justify-between hover:bg-gray-800/50 transition"
        >
          <span class="text-sm font-semibold text-gray-300">Brand</span>
          <svg
            :class="{'rotate-180': showBrands}"
            class="w-5 h-5 text-gray-400 transition-transform"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
          </svg>
        </button>
        
        <div v-if="showBrands" class="px-6 pb-4 grid grid-cols-2 gap-3">
          <label
            v-for="brand in props.brands"
            :key="brand.id"
            class="flex items-center gap-2 cursor-pointer group"
          >
            <input
              type="checkbox"
              :checked="props.filterBrands.includes(brand.name)"
              @change="props.onToggleBrand(brand.name)"
              class="custom-checkbox"
            />
            <span 
              class="text-sm transition-all"
              :class="props.filterBrands.includes(brand.name) ? 'text-purple-400 font-bold' : 'text-gray-500 font-normal'"
            >
              {{ brand.name }}
            </span>
          </label>
        </div>
      </div>

      <!-- Price Filter -->
      <div class="border-b border-gray-800">
        <button
          @click="showPrices = !showPrices"
          class="w-full px-6 py-4 flex items-center justify-between hover:bg-gray-800/50 transition"
        >
          <span class="text-sm font-semibold text-gray-300">Price</span>
          <svg
            :class="{'rotate-180': showPrices}"
            class="w-5 h-5 text-gray-400 transition-transform"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
          </svg>
        </button>
        
        <div v-if="showPrices" class="px-6 pb-4 space-y-3">
          <label
            v-for="price in priceOptions"
            :key="price.value"
            class="flex items-center gap-3 cursor-pointer group"
          >
            <input
              type="checkbox"
              :checked="props.filterPrices.includes(price.value)"
              @change="props.onTogglePrice(price.value)"
              class="custom-checkbox"
            />
            <span 
              class="text-sm transition-all"
              :class="props.filterPrices.includes(price.value) ? 'text-purple-400 font-bold' : 'text-gray-500 font-normal'"
            >
              {{ price.label }}
            </span>
          </label>
          
          <!-- Custom Price Range -->
          <div class="mt-4 pt-4 border-t border-gray-800">
            <p class="text-xs font-medium text-gray-500 mb-3">Custom Range</p>
            <div class="space-y-2 mb-3">
              <input
                type="number"
                placeholder="Min Price"
                v-model="customMinPrice"
                class="w-full px-3 py-2 text-sm bg-gray-800 border border-gray-700 rounded text-gray-200 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
              />
              <input
                type="number"
                placeholder="Max Price"
                v-model="customMaxPrice"
                class="w-full px-3 py-2 text-sm bg-gray-800 border border-gray-700 rounded text-gray-200 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
              />
            </div>
            <button
              @click="addCustomPriceRange"
              :disabled="!customMinPrice || !customMaxPrice || customMinPrice > customMaxPrice"
              class="w-full px-4 py-2 text-sm font-medium text-white bg-purple-600 rounded hover:bg-purple-700 disabled:bg-gray-700 disabled:text-gray-500 disabled:cursor-not-allowed transition"
            >
              Apply
            </button>
          </div>
        </div>
      </div>

      <!-- Storage Size Filter -->
      <div class="border-b border-gray-800">
        <button
          @click="showStorage = !showStorage"
          class="w-full px-6 py-4 flex items-center justify-between hover:bg-gray-800/50 transition"
        >
          <span class="text-sm font-semibold text-gray-300">Storage Size</span>
          <svg
            :class="{'rotate-180': showStorage}"
            class="w-5 h-5 text-gray-400 transition-transform"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
          </svg>
        </button>
        
        <div v-if="showStorage" class="px-6 pb-4 space-y-3">
          <label
            v-for="storage in storageOptions"
            :key="storage.value"
            class="flex items-center gap-3 cursor-pointer group"
          >
            <input
              type="checkbox"
              :checked="props.filterStorageSizes.includes(storage.value)"
              @change="props.onToggleStorageSize(storage.value)"
              class="custom-checkbox"
            />
            <span 
              class="text-sm transition-all"
              :class="props.filterStorageSizes.includes(storage.value) ? 'text-purple-400 font-bold' : 'text-gray-500 font-normal'"
            >
              {{ storage.label }}
            </span>
          </label>
        </div>
      </div>
     
    </div>
  </aside>
</template>

<style scoped>
/* Custom Checkbox with Purple Dot */
.custom-checkbox {
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  width: 8px;
  height: 8px;
  border: none;
  border-radius: 50%;
  background-color: transparent;
  cursor: pointer;
  position: relative;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.custom-checkbox:checked {
  background-color: #a855f7;
  box-shadow: 0 0 8px rgba(168, 85, 247, 0.6);
}

.custom-checkbox:focus {
  outline: none;
}

/* Custom Scrollbar */
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: rgba(17, 24, 39, 0.3);
  border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #7c3aed 0%, #6366f1 100%);
  border-radius: 10px;
  transition: all 0.3s ease;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, #8b5cf6 0%, #818cf8 100%);
  box-shadow: 0 0 10px rgba(124, 58, 237, 0.5);
}

/* Firefox */
.custom-scrollbar {
  scrollbar-width: thin;
  scrollbar-color: #7c3aed rgba(17, 24, 39, 0.3);
}
</style>
