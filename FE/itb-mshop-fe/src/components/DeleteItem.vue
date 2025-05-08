<script setup>
import { ref } from 'vue'


const showModal = ref(false)
const selectedItem = ref(null)

function confirmDelete(item) {
  selectedItem.value = item
  showModal.value = true
}

function deleteItem() {
  console.log('Deleting:', selectedItem.value)
  // Make your API delete call here
  showModal.value = false
}
</script>

<template>
  <!-- Delete Button Trigger -->
  <button
    @click="confirmDelete({ id: 1, name: 'Example Product' })"
    class="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600"
  >
    Delete Item
  </button>

  <!-- Delete Confirmation Modal -->
  <div
    v-if="showModal"
    class="fixed inset-0 flex items-center justify-center z-50 bg-black bg-opacity-50"
  >
    <div class="bg-white rounded-lg shadow-lg w-full max-w-md p-6">
      <h2 class="text-xl font-bold mb-4">Confirm Delete</h2>
      <p class="mb-6">
        Are you sure you want to delete
        <span class="font-semibold">{{ selectedItem?.name }}</span>?
        This action cannot be undone.
      </p>
      <div class="flex justify-end gap-4">
        <button
          @click="showModal = false"
          class="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
        >
          Cancel
        </button>
        <button
          @click="deleteItem"
          class="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
        >
          Delete
        </button>
      </div>
    </div>
  </div>
</template>
