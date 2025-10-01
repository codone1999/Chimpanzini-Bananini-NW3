<script setup>
  defineProps({
    showModal: {
      type: Boolean,
      required: true
    },
    notShowModel: {
      type: Function,
      required: true
    },
    handleDelete: {
      type: Function,
      required: true
    }
  })
</script>

<template>
  <div
    v-if="showModal"
    class="fixed inset-0 flex items-center justify-center z-50 bg-black/60 backdrop-blur-sm"
  >
    <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md p-8">
      <h2 class="text-2xl font-bold text-gray-800 mb-4">Confirm Delete</h2>
      <p class="itbms-message text-gray-600 mb-6">
        Do you want to delete this sale item?
      </p>
      <div class="flex justify-end gap-4">
        <button
          @click="notShowModel"
          class="itbms-cancel-button bg-gray-200 hover:bg-gray-300 text-gray-700 px-4 py-2 rounded-lg transition"
        >
          Cancel
        </button>
        <button
          @click="handleDelete"
          class="itbms-confirm-button bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-lg transition"
        >
          Delete
        </button>
      </div>
    </div>
  </div>
</template>