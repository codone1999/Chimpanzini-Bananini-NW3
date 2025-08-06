<script setup>

const props = defineProps({
  brands: {
    type: Array,
    required: true
  },
  filterBrands: {
    type: Object,
    required: true
  },
  filterPrice: {
    type: Object,
    required: true
  },
  filterStorageSize: {
    type: Object,
    required: true
  },
  brandToAdd: {
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
  }
})

const emit = defineEmits(['update:pageSize'])

function handlePageSizeChange(event) {
  const value = parseInt(event.target.value)
  emit('update:pageSize', value)
}

</script>

<template>
  <div class="flex flex-col md:flex-row justify-between gap-5 items-start w-full mb-8">

    <!-- Filter Section (Expandable Left Side) -->
    <div class="flex items-center gap-2 flex-grow ">

      <!-- Each Pill will expand equally -->
      <div class="flex-1 border rounded-full px-6 py-1.5 bg-white shadow-sm text-left">
        <div class="text-sm font-semibold text-gray-800">Brand</div>
        <div class="text-xs text-gray-400 truncate">
          <span v-if="props.filterBrands.length === 0">Filter by brand(s)</span>
          <span v-else>
            {{ props.filterBrands.join(', ') }}
          </span>
        </div>
      </div>

      <div class="flex-1 border rounded-full px-6 py-1.5 bg-white shadow-sm text-left">
        <div class="text-sm font-semibold text-gray-800">Price</div>
        <div class="text-xs text-gray-400">Price Range
          <!-- NOTE - add filterPrice in ListGallery -->
          <!-- <span v-if="props.filterPrice.length === 0">Price Range</span>
          <span v-else>
            {{ props.filterPrice.join(', ') }}
          </span> -->
        </div>
      </div>

      <div class="flex-1 border rounded-full px-6 py-1.5 bg-white shadow-sm text-left">
        <div class="text-sm font-semibold text-gray-800">Storage Size</div>
        <div class="text-xs text-gray-400">Storage Range
          <!-- NOTE - add filterStorageSize in ListGallery -->
          <!-- <span v-if="props.filterStorageSize.length === 0">Storage Range</span>
          <span v-else>
            {{ props.filterStorageSize.join(', ') }}
          </span> -->
        </div>
      </div>

      <!-- Filter Icon -->
      <button
        @click="props.toggleBrandList"
        class="p-3 flex items-center justify-center bg-gray-200 border border-gray-600 rounded-full hover:bg-gray-300 transition"
      >
        <span class="material-icons text-gray-700">filter_alt</span>
      </button>
    </div>

    <!-- Sort + Page Size (Right Side) -->
    <div class="flex md:flex-nowrap gap-2 items-center w-full md:w-auto">
      <div class="flex items-center gap-2 text-base">
        <label for="pageSize" class="font-medium text-gray-700 whitespace-nowrap">Show:</label>
        <select
          :value="props.pageSize"
          @change="handlePageSizeChange"
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
        :class="props.sortMode === 'none'
          ? 'bg-white text-black border border-gray-300'
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
