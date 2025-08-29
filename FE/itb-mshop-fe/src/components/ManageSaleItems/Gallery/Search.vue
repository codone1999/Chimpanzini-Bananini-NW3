<script setup>
import { ref, watch } from "vue";

// รับค่าจาก parent
const props = defineProps({
  modelValue: {
    type: String,
    required: true
  }
});

const emit = defineEmits(["update:modelValue", "search"]);

const searchText = ref(props.modelValue || "");

// ส่งค่ากลับไป parent เมื่อข้อมูลเปลี่ยน (for real-time binding)
watch(searchText, (val) => {
  emit("update:modelValue", val);
});

// Watch for changes from parent (when modelValue changes externally)
watch(() => props.modelValue, (newVal) => {
  if (searchText.value !== newVal) {
    searchText.value = newVal;
  }
});

const onSearch = () => {
  emit("search", searchText.value);
};

const clearSearch = () => {
  searchText.value = "";
  emit("search", "");
};
</script>

Search

<template>
    <div class="flex w-full max-w-md">
    <!-- Input + Search -->
    <div class="flex flex-1 items-center">
      <input
        v-model="searchText"
        type="text"
        placeholder="Search..."
        class="itbms-search-text w-full bg-neutral-900 text-gray-200 placeholder-gray-500 border border-neutral-700 rounded-l-xl px-4 py-2 outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition"
        @keyup.enter="onSearch"
      />

      <!-- Search Button -->
      <button
        @click="onSearch"
        class="material-icons p-2 bg-purple-600 text-white text-sm border border-purple-600 hover:bg-purple-500 hover:border-purple-500 rounded-r-none transition"
        title="Search"
      >
        search
      </button>
      <!-- Clear Button -->
      <button
        @click="clearSearch"
        :disabled="!searchText"
        class="itbms-search-clear-button font-medium px-4 py-2 border rounded-r-xl transition"
        :class="searchText ? 'bg-red-600 text-white border-red-600 hover:bg-red-500 hover:border-red-500' 
          : 'bg-neutral-800 text-gray-500 border-neutral-700 cursor-not-allowed'"
        title="Clear search"
      >
        Clear
      </button>
    </div>

  </div>
</template>
