<script setup>

const props = defineProps({
  // Brand //
  brands: {
    type: Array,
    required: true
  },
  filterBrands: {
    type: Object,
    required: true
  },
  brandToAdd: {
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
  onChangeSort: {
    type: Function,
    required: true
  },
  // Price //
  filterPrice: {
    type: Object,
    required: true
  },
  // Storage Size //
  filterStorageSize: {
    type: Object,
    required: true
  },
  sortMode: {
    type: Object,
    required: true
  },
  pageSize: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:pageSize'])

function handlePageSizeChange(event) {
  const value = parseInt(event.target.value)
  emit('update:pageSize', value)
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

          <!-- Dropdown -->
          <div 
            v-if="props.showBrandList"
            class="absolute top-full left-0 mt-2 bg-white border border-gray-300 rounded shadow-md z-10 w-full max-h-72 overflow-y-auto"
          >
            <button
              v-for="brand in props.brands"
              :key="brand.id"
              :disabled="props.filterBrands.includes(brand.name)"
              @click="props.onToggleBrand(brand.name)"
              class="itbms-filter-item block w-full text-left px-4 py-2 text-sm hover:bg-purple-100"
              :class="props.filterBrands.includes(brand.name) ? 'text-gray-300' : 'text-black'"
            >
              {{ brand.name }}
            </button>
          </div>
        </div>
      </div>

      <!-- Pill Price -->
      <div class="flex-1 border rounded-full px-6 py-1.5 border-gray-700 bg-gray-100 shadow-sm text-left hover:border-purple-500 transition-all duration-200">
        <div class="text-sm font-medium text-gray-800">Price</div>
        <div class="text-xs text-gray-400 mt-1">Price Range
          <!-- NOTE - add filterPrice in ListGallery -->
          <!-- <span v-if="props.filterPrice.length === 0">Price Range</span>
          <span v-else>
            {{ props.filterPrice.join(', ') }}
          </span> -->
        </div>
      </div>

      <!-- Storage Size -->
      <div class="flex-1 border rounded-full px-6 py-1.5 border-gray-700 bg-gray-100 shadow-sm text-left hover:border-purple-500 transition-all duration-200">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-sm font-medium text-gray-800">Storage Size</div>
            <div class="text-xs text-gray-400 mt-1">Storage Range
              <!-- NOTE - add filterStorageSize in ListGallery -->
              <!-- <span v-if="props.filterStorageSize.length === 0">Storage Range</span>
              <span v-else>
                {{ props.filterStorageSize.join(', ') }}
              </span> -->
            </div>
          </div>
          <button
            @click="props.toggleBrandList"
            class="-mr-4 pt-2 px-2 pb-1 bg-gray-200 border border-gray-600 rounded-full hover:bg-gray-300 transition"
          >
            <span class="material-icons text-gray-700">filter_alt</span>
          </button>
        </div>
      </div>

      <!-- Filter Icon -->
      
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
