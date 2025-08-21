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

// ส่งค่ากลับไป parent เมื่อข้อมูลเปลี่ยน
watch(searchText, (val) => {
  emit("update:modelValue", val);
});

const onSearch = () => {
  emit("search", searchText.value);
};

const clearSearch = () => {
  searchText.value = "";
  emit("search", "");
};
</script>

<template>
    <div class="flex w-full max-w-md">
    <!-- Input + Search -->
    <div class="relative flex-1">
      <input
        v-model="searchText"
        type="text"
        placeholder="Search..."
        class=" itbms-search-text w-full border border-gray-300 rounded-l-lg px-3 py-2 pr-20 outline-none focus:ring-purple-500 focus:border-purple-500"
        @keyup.enter="onSearch"
      />

      <!-- Search Button -->
      <button
        @click="onSearch"
        class="absolute right-2 top-1/2 -translate-y-1/2 px-3 py-1 bg-purple-600 text-white text-sm rounded hover:bg-purple-500 transition"
      >
        Search
      </button>
    </div>

    <!-- Clear Button -->
    <button
      @click="clearSearch"
      :disabled="!searchText"
      class=" itbms-search-clear-button px-4 py-2 border border-l-0 border-gray-300 rounded-r-lg transition"
      :class="searchText ? 'bg-gray-400 text-gray-700 hover:bg-gray-300' : 'bg-gray-200 text-gray-400 cursor-not-allowed'"
    >
      Clear
    </button>
  </div>
</template>
