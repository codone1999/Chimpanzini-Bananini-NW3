<script setup>
const props = defineProps({
  currentPage: {
    type: Number,
    required: true
  },
  totalPages: {
    type: Number,
    required: true
  },
  visiblePages: {
    type: Array,
    required: true
  },
  goToPage: {
    type: Function,
    required: true
  }
})
</script>

<template>
  <div 
    class="flex justify-center items-center gap-2 flex-wrap px-3 py-2 bg-gray-800 rounded-xl visible"
    :class="props.totalPages > 1 ? 'visible': 'invisible' "
  >
    <!-- First Page -->
    <button 
      class="itbms-page-first px-4 py-2 rounded-lg border border-gray-700 text-gray-200 bg-gray-800
        disabled:opacity-40 disabled:cursor-not-allowed
        hover:bg-purple-600 hover:border-purple-500 hover:shadow-lg hover:shadow-purple-500/30
        transition-all duration-200 font-medium"
      @click="props.goToPage(1)" 
      :disabled="props.currentPage === 1"
    >
      First
    </button>

    <!-- Previous Page -->
    <button 
      class="itbms-page-prev px-4 py-2 rounded-lg border border-gray-700 text-gray-200 bg-gray-800
        disabled:opacity-40 disabled:cursor-not-allowed
        hover:bg-purple-600 hover:border-purple-500 hover:shadow-lg hover:shadow-purple-500/30
        transition-all duration-200 font-medium"
      @click="props.goToPage(props.currentPage - 1)"
      :disabled="props.currentPage === 1"
    >
      <span class="material-icons text-sm align-middle">chevron_left</span>
      Prev
    </button>

    <!-- Page Numbers -->
    <button
      v-for="page in props.visiblePages"
      :key="page"
      @click="props.goToPage(page)"
      class="min-w-[44px] px-4 py-2 rounded-lg border font-semibold transition-all duration-200"
      :class="[
        `itbms-page-${page - 1}`,
        page === props.currentPage
          ? 'bg-gradient-to-r from-purple-600 to-indigo-600 border-purple-500 text-white shadow-lg shadow-purple-500/40 scale-110'
          : 'bg-gray-800 border-gray-700 text-gray-300 hover:bg-gray-700 hover:border-purple-500 hover:text-purple-400 hover:shadow-md',
        props.totalPages === 1 ? 'invisible' : 'visible'
      ]"
    >
      {{ page }}
    </button>

    <!-- Next Page -->
    <button 
      class="itbms-page-next px-4 py-2 rounded-lg border border-gray-700 text-gray-200 bg-gray-800
        disabled:opacity-40 disabled:cursor-not-allowed
        hover:bg-purple-600 hover:border-purple-500 hover:shadow-lg hover:shadow-purple-500/30
        transition-all duration-200 font-medium"
      @click="props.goToPage(props.currentPage + 1)" 
      :disabled="props.currentPage === props.totalPages"
    >
      Next
      <span class="material-icons text-sm align-middle">chevron_right</span>
    </button>

    <!-- Last Page -->
    <button 
      class="itbms-page-last px-4 py-2 rounded-lg border border-gray-700 text-gray-200 bg-gray-800
        disabled:opacity-40 disabled:cursor-not-allowed
        hover:bg-purple-600 hover:border-purple-500 hover:shadow-lg hover:shadow-purple-500/30
        transition-all duration-200 font-medium"
      @click="props.goToPage(props.totalPages)"
      :disabled="props.currentPage === props.totalPages"
    >
      Last
    </button>
  </div>
</template>
