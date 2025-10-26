  // Search.vue
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

  <template>
    <div class="flex w-full">
      <!-- Input + Search -->
      <div class="flex flex-1 items-center relative">
        <input
          v-model="searchText"
          type="text"
          placeholder="Search for models..."
          class="itbms-search-text w-full bg-neutral-900 text-gray-200 placeholder-gray-500 border border-neutral-700 rounded-l-xl px-4 py-2.5 pr-12 outline-none focus:ring-1 focus:ring-purple-500 focus:border-purple-500 transition-all duration-300"
          @keyup.enter="onSearch"
        />

        <!-- Clear Button (Icon) - Shows when typing -->
        <Transition name="clear">
          <button
            v-if="searchText"
            @click="clearSearch"
            class="absolute right-17 material-icons text-gray-400 hover:text-red-500 transition-all duration-200 hover:scale-110 active:scale-95"
            title="Clear search"
          >
            close
          </button>
        </Transition>

        <!-- Search Button -->
        <button
          @click="onSearch"
          class="material-icons px-4 py-2.5 bg-purple-600 text-white border border-purple-600 hover:bg-purple-500 hover:border-purple-500 active:scale-95 rounded-r-xl transition-all duration-200 hover:shadow-lg hover:shadow-purple-500/50"
          title="Search"
        >
          search
        </button>
      </div>
    </div>
  </template>

  <style scoped>
  /* Clear button animations */
  .clear-enter-active {
    animation: slideIn 0.3s ease-out;
  }

  .clear-leave-active {
    animation: slideOut 0.2s ease-in;
  }

  @keyframes slideIn {
    from {
      opacity: 0;
      transform: translateX(10px) scale(0.8);
    }
    to {
      opacity: 1;
      transform: translateX(0) scale(1);
    }
  }

  @keyframes slideOut {
    from {
      opacity: 1;
      transform: translateX(0) scale(1);
    }
    to {
      opacity: 0;
      transform: translateX(10px) scale(0.8);
    }
  }

  /* Input focus animation */
  .itbms-search-text:focus {
    box-shadow: 0 0 0 3px rgba(168, 85, 247, 0.1);
  }

  /* Pulse animation for search button when input has text */
  @keyframes pulse {
    0%, 100% {
      opacity: 1;
    }
    50% {
      opacity: 0.8;
    }
  }
  </style>
