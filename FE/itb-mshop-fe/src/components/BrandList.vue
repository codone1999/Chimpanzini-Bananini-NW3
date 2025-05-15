<script setup>
import { ref } from 'vue'

const statuses = ref([
  { id: 1, name: 'No Status', description: 'The default status' },
  { id: 2, name: 'To Do', description: '' },
  { id: 3, name: 'Doing', description: '' },
  { id: 4, name: 'Done', description: '' }
])

const goHome = () => {
  alert('Navigating to Home...')
}

const addStatus = () => {
  alert('Add Status clicked')
}

const editStatus = (status) => {
  alert('Edit: ' + JSON.stringify(status))
}

const deleteStatus = (id) => {
  const confirmed = confirm('Are you sure you want to delete this status?')
  if (confirmed) {
    statuses.value = statuses.value.filter(status => status.id !== id)
  }
}
</script>

<template>
  <div class="p-6">
    <!-- Breadcrumb & Header -->
    <div class="flex justify-between items-center mb-4">
      <div class="text-gray-500">
        <span class="text-blue-600 cursor-pointer hover:underline" @click="goHome">Home</span>
        <span class="mx-2">›</span>
        <span class="font-semibold text-gray-700">Task Status</span>
      </div>
      <button @click="addStatus" class="bg-gray-200 text-gray-700 px-4 py-2 rounded hover:bg-gray-300">
        Add Status
      </button>
    </div>

    <!-- Task Status Table -->
    <table class="min-w-full border border-gray-200 rounded overflow-hidden">
      <thead class="bg-gray-100 text-center">
        <tr>
          <th class="px-4 py-2 border">#</th>
          <th class="px-4 py-2 border">Name</th>
          <th class="px-4 py-2 border">Description</th>
          <th class="px-4 py-2 border">Action</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(status, index) in statuses" :key="status.id" class="text-center border-t">
          <td class="px-4 py-2 border">{{ status.id }}</td>
          <td class="px-4 py-2 border">{{ status.name }}</td>
          <td class="px-4 py-2 border">{{ status.description }}</td>
          <td class="px-4 py-2 border">
            <div class="flex justify-center space-x-2">
              <button @click="editStatus(status)" class="bg-gray-300 px-3 py-1 rounded hover:bg-gray-400">
                Edit
              </button>
              <button @click="deleteStatus(status.id)" class="bg-gray-300 px-3 py-1 rounded hover:bg-red-400">
                Delete
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
