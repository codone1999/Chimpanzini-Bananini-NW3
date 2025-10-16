//Pagination.vue
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
    class="flex justify-center items-center mt-10 gap-2 flex-wrap"
    :class="props.totalPages > 1 ? 'visible': 'invisible' "
  >
    <!-- First Page -->
    <button 
      class="itbms-page-first px-5 py-2 text-lg rounded-lg border text-white bg-[#7e5bef] 
        disabled:opacity-50 hover:bg-[#6b4edb] hover:shadow-md transition duration-200"
      @click="props.goToPage(1)" 
      :disabled="props.currentPage === 1"
    >
      First
    </button>

    <!-- Previous Page -->
    <button 
      class="itbms-page-prev px-5 py-2 text-lg rounded-lg border text-white bg-[#7e5bef] 
      disabled:opacity-50 hover:bg-[#6b4edb] hover:shadow-md transition duration-200"
      @click="props.goToPage(props.currentPage - 1)"
      :disabled="props.currentPage === 1"
    >
      Prev
    </button>

    <!-- Page Numbers -->
    <button
      v-for="page in props.visiblePages"
      :key="page"
      @click="props.goToPage(page)"
      class="px-5 py-2 text-lg rounded-lg border font-medium"
      :class="[
        `itbms-page-${page - 1}`,
        {
          'bg-[#7e5bef] text-white hover:bg-[#6b4edb] hover:shadow-md': page === props.currentPage,
          'bg-white text-[#7e5bef] border-[#7e5bef] hover:bg-[#f3f0ff] hover:shadow-md': page !== props.currentPage
        },
        props.totalPages === 1 ? 'invisible' : 'visible'
      ]"
    >
      {{ page }}
    </button>

    <!-- Next Page -->
    <button 
      class="itbms-page-next px-5 py-2 text-lg rounded-lg border text-white bg-[#7e5bef] 
        disabled:opacity-50 hover:bg-[#6b4edb] hover:shadow-md transition duration-200"
      @click="props.goToPage(props.currentPage + 1)" 
      :disabled="props.currentPage === props.totalPages"
    >
      Next
    </button>

    <!-- Last Page -->
    <button 
      class="itbms-page-last px-5 py-2 text-lg rounded-lg border text-white bg-[#7e5bef] 
        disabled:opacity-50 hover:bg-[#6b4edb] hover:shadow-md transition duration-200"
      @click="props.goToPage(props.totalPages)"
      :disabled="props.currentPage === props.totalPages"
    >
      Last
    </button>
  </div>
</template>
