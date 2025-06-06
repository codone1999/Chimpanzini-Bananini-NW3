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
  <div class="flex justify-center items-center mt-10 gap-2 flex-wrap" v-if="props.totalPages > 1">
    <button @click="props.goToPage(1)" :disabled="props.currentPage === 1">First</button>
    <button @click="props.goToPage(props.currentPage - 1)" :disabled="props.currentPage === 1">Prev</button>

    <button
      v-for="page in props.visiblePages"
      :key="page"
      @click="props.goToPage(page)"
      :class="page === props.currentPage ? 'bg-[#7e5bef] text-white' : 'bg-white text-[#7e5bef]'"
      class="px-4 py-2 rounded border"
    >
      {{ page }}
    </button>

    <button @click="props.goToPage(props.currentPage + 1)" :disabled="props.currentPage === props.totalPages">Next</button>
    <button @click="props.goToPage(props.totalPages)" :disabled="props.currentPage === props.totalPages">Last</button>
  </div>
</template>
