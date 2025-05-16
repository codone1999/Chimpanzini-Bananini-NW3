<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { addItem } from '@/lib/fetchUtils'

const router = useRouter()

const form = ref({
  name: '',
  websiteUrl: '',
  isActive: false,
  countryOfOrigin: ''
})

async function handleSubmit() {
  try {
    const addedItem = await addItem('http://ip24nw3.sit.kmutt.ac.th:8080/v1/brands', form.value)
    if (addedItem) {
      router.push({ name: 'ListGallery', query: { added: 'true' } })
    }
  } catch (error) {
    console.error('Error:', error)
  }
}

</script>

<template>
  <div class="p-8 bg-white rounded-lg shadow-md max-w-3xl mx-auto">
    <!-- Breadcrumb -->
    <div class="text-sm text-gray-500 mb-6 flex items-center gap-2">
      <router-link
          :to="{ name: 'ListSaleItem'}"
          class="Itbms-item-list hover:underline hover:text-[#7e5bef] transition"
        >
          Sale Item List
        </router-link>
      <span class="text-gray-400">›</span>
      <router-link
          :to="{ name: 'BrandList'}"
          class="itbms-manage-brand hover:underline hover:text-[#7e5bef] transition"
        >
          Brand List
        </router-link>
      <span class="text-gray-400">›</span>
      <span class="font-semibold text-black">New Brand</span>
    </div>

    <!-- Form -->
    <form class="space-y-6">
      <!-- Name -->
      <div>
        <label for="name" class="block text-sm font-medium text-gray-700">
          Name<span class="text-red-500">*</span>
        </label>
        <input
          v-model="form.name"
          type="text"
          class="itbms-name mt-1 w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-[#7e5bef] transition"
          required
        />
      </div>

      <!-- Website URL -->
      <div>
        <label for="websiteUrl" class="block text-sm font-medium text-gray-700">Website URL</label>
        <input
          v-model="form.websiteUrl"
          type="url"
          class="itbms-websiteUrl mt-1 w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-[#7e5bef] transition"
        />
      </div>

      <!-- Active Toggle -->
      <div class="flex items-center gap-4">
        <label for="isActive" class="text-sm font-medium text-gray-700">Active</label>
        <label class="relative inline-flex items-center cursor-pointer">
          <input
            type="checkbox"
            v-model="form.isActive"
            class="itbms-isActive sr-only"
          />
          <div
            class="w-11 h-6 bg-gray-300 rounded-full transition"
            :class="form.isActive ? 'bg-[#7e5bef]' : 'bg-gray-300'"
          >
            <div
              class="absolute w-5 h-5 bg-white rounded-full shadow transform transition"
              :class="form.isActive ? 'translate-x-5' : 'translate-x-0.5'"
            ></div>
          </div>
        </label>
      </div>

      <!-- Country Of Origin -->
      <div>
        <label for="country" class="block text-sm font-medium text-gray-700">Country Of Origin</label>
        <input
          v-model="form.countryOfOrigin"
          type="text"
          class="itbms-countryOfOrigin mt-1 w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-[#7e5bef] transition"
        />
      </div>

      <!-- Buttons -->
      <div class="flex gap-3 mt-6">
        <button
          id="save-button"
          type="button"
          @click="handleSubmit"
          class="bg-[#7e5bef] hover:bg-[#5e4ecf] text-white font-semibold py-2 px-6 rounded-lg shadow-sm transition"
        >
          Save
        </button>
        <router-link
          :to="{ name: 'BrandList' }"
          class="itbms-cancel-button px-6 py-2 border border-gray-300 text-gray-600 rounded-lg hover:bg-gray-100 transitionn"
        >
          Cancel
        </router-link>
      </div>
    </form>
  </div>
</template>

