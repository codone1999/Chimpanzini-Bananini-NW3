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
    <div class="flex flex-1 items-center">
      <input
        v-model="searchText"
        type="text"
        placeholder="Search..."
        class=" itbms-search-text w-full border border-gray-300 rounded-l-lg px-3 py-2 outline-none focus:ring-purple-500 focus:border-purple-500"
        @keyup.enter="onSearch"
      />

      <!-- Search Button -->
      <button
        @click="onSearch"
        class="material-icons ml-auto p-2 bg-purple-600 text-white text-sm border border-1 border-purple-600 hover:bg-purple-500 hover:border-purple-500 transition"
      >
        search
      </button>
      <!-- Clear Button -->
      <button
        @click="clearSearch"
        :disabled="!searchText"
        class=" itbms-search-clear-button font-medium px-4 py-2 border border-1.5 rounded-r-lg transition"
        :class="searchText ? 'bg-red-400 text-gray-500 border-red-400 hover:bg-red-300 hover:border-red-300' : 'text-gray-400 bg-red-300 border-red-300 cursor-not-allowed'"
      >
        Clear
      </button>
    </div>

  </div>
</template>
