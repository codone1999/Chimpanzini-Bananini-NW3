// Sort.vue
<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  pageSize: {
    type: Number,
    required: true
  },
  sortMode: {
    type: String,
    required: true
  }
});

const emit = defineEmits(['update:pageSize', 'update:sortMode']);

const handlePageSizeChange = (event) => {
  emit('update:pageSize', parseInt(event.target.value));
};

const handleSortChange = (event) => {
  emit('update:sortMode', event.target.value);
};
</script>

<template>
  <div class="flex gap-3 ml-auto mb-auto">
    <!-- Page Size -->
    <div class="flex items-center gap-2 text-sm">
      <label class="font-semibold text-[#B55FFF] whitespace-nowrap">Show:</label>
      <select
        :value="pageSize"
        @change="handlePageSizeChange"
        class="itbms-page-size border border-[#2E2E33] bg-[#2B2B31] text-[#E5E5E5] rounded-lg px-3 py-2 
        focus:outline-none focus:ring-2 focus:ring-[#B55FFF] hover:bg-[#3A3A42] transition"
      >
        <option :value="5">5</option>
        <option :value="10">10</option>
        <option :value="20">20</option>
      </select>
    </div>
    
    <!-- Sort -->
    <div class="flex gap-2 items-center text-sm">
      <label class="font-semibold text-[#B55FFF] whitespace-nowrap">Sort by:</label>
      <select
        :value="sortMode"
        @change="handleSortChange"
        class="border border-[#2E2E33] bg-[#2B2B31] text-[#E5E5E5] rounded-lg px-3 py-2 
        focus:outline-none focus:ring-2 focus:ring-[#B55FFF] hover:bg-[#3A3A42] transition"
      >
        <option value="none">None</option>
        <option value="asc">Ascending</option>
        <option value="desc">Decending</option>
      </select>
    </div>
  </div>
</template>
