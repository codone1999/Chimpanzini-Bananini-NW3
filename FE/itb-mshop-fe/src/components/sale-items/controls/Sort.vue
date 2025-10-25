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
    <div class="flex gap-3 ml-auto mb-auto items-center gap-4">
      <!-- Page Size -->
      <div class="flex items-center gap-2">
        <label class="text-sm font-semibold text-gray-300 whitespace-nowrap">Show:</label>
        <select
          :value="pageSize"
          @change="handlePageSizeChange"
          class="px-3 py-2 text-sm bg-gray-800 border border-gray-700 rounded text-gray-200
          focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent
          hover:bg-gray-700 transition-all cursor-pointer"
        >
          <option :value="5">5</option>
          <option :value="10">10</option>
          <option :value="20">20</option>
        </select>
      </div>
      
      <!-- Sort -->
      <div class="flex items-center gap-2">
        <label class="text-sm font-semibold text-gray-300 whitespace-nowrap">Sort by:</label>
        <select
          :value="sortMode"
          @change="handleSortChange"
          class="px-3 py-2 text-sm bg-gray-800 border border-gray-700 rounded text-gray-200
          focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent
          hover:bg-gray-700 transition-all cursor-pointer"
        >
          <option value="none">None</option>
          <option value="asc">Ascending</option>
          <option value="desc">Descending</option>
        </select>
      </div>
    </div>
</template>

<style scoped>
select {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3E%3Cpath stroke='%239ca3af' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M6 8l4 4 4-4'/%3E%3C/svg%3E");
  background-position: right 0.5rem center;
  background-repeat: no-repeat;
  background-size: 1.5em 1.5em;
  padding-right: 2.5rem;
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
}

select option {
  background-color: #1f2937;
  color: #e5e7eb;
}
</style>