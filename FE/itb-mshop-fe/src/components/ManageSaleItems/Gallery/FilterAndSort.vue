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
  <div class="flex flex-col gap-4 md:flex-row md:justify-between md:items-start w-full mb-8">

    <!-- Filters -->
    <div class="flex flex-wrap md:flex-nowrap gap-2 w-full items-start">
      <!-- Selected Filters Box -->
      <div class="itbms-brand-filter flex flex-wrap items-center gap-2 px-3 py-2 border border-gray-300 rounded bg-white min-h-[49px] max-w-[500px] flex-grow relative">
        <span v-if="props.filterBrands.length === 0" class="text-gray-400">Filter by brand(s)</span>

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

      <!-- Show Dropdown -->
      <button 
        @click="props.toggleBrandList"
        class="itbms-brand-filter-button flex items-center gap-1 p-3 text-white bg-purple-600 hover:bg-purple-800 rounded-md transition"
      >
        <span class="material-icons">filter_alt</span>
      </button>

      <!-- Clear Filters -->
      <button 
        @click="props.onClearBrands"
        class="itbms-brand-filter-clear flex items-center gap-1 p-3 text-white bg-red-600 hover:bg-red-800 rounded-md transition"
      >
        <span class="material-icons">cleaning_services</span>
      </button>
    </div>

    <!-- Sort + Page Size -->
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
