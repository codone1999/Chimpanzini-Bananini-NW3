<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { editItem, getItemById } from '@/lib/fetchUtils'

const route = useRoute()
const router = useRouter()
const id = route.params.id

const form = ref({
  "name": '',
  "websiteUrl": '',
  "isActive": false,
  "countryOfOrigin": ''
})

const originalBrand = ref(null)

async function handleSubmit() {
  try {
    const addedItem = await editItem('http://ip24nw3.sit.kmutt.ac.th:8080/v1/brands', form.value, id)
    if (addedItem) {
      router.push({ name: 'BrandList' })
    }
  } catch (error) {
    console.error('Error:', error)
  }
}

onMounted(async () => {
  try {
    const item = await getItemById('http://ip24nw3.sit.kmutt.ac.th:8080/v1/brands', id)
    if (!item || item?.status === 404) {
      router.push({ name: 'BrandList' })
      alert('The requested sale item does not exist.')
      return
    }

    const data = {
      "name": item.name,
      "websiteUrl": item.webSiteUrl,
      "isActive": item.isActive,
      "countryOfOrigin": item.countryOfOrigin
    }
    form.value = data
    originalBrand.value = JSON.parse(JSON.stringify(data))
  } catch (error) {
    console.error('Failed to fetch product:', error);
  }
})

</script>

<template>
  <div class="p-6 bg-blue-50 rounded shadow-md">
    <!-- Breadcrumb -->
    <div class="text-sm text-blue-700 mb-4 flex gap-2">
      <router-link
          :to="{ name: 'ListSaleItem'}"
          class="Itbms-item-list text-blue-500"
        >
          Sale Item List
        </router-link>
      <span class="mx-2">›</span>
      <router-link
          :to="{ name: 'BrandList'}"
          class="itbms-manage-brand text-blue-500"
        >
          Brand List
        </router-link>
      <span class="mx-2">›</span>
      <span class="font-semibold text-black">New Brand</span>
    </div>

    <!-- Form -->
    <form class="space-y-4">
      <!-- Name -->
      <div>
        <label for="name" class="block font-medium text-gray-700">
          Name<span class="text-red-500">*</span>
        </label>
        <input
          v-model="form.name"
          type="text"
          class="itbms-name mt-1 block w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
          required
        />
      </div>

      <!-- Website URL -->
      <div>
        <label for="websiteUrl" class="block font-medium text-gray-700">Website URL</label>
        <input
          v-model="form.websiteUrl"
          type="url"
          class="itbms-websiteUrl mt-1 block w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
        />
      </div>

      <!-- Active Toggle -->
      <div class="flex items-center gap-4">
        <label for="isActive" class="block font-medium text-gray-700">Active</label>
        <label class="inline-flex items-center cursor-pointer">
          <input
            type="checkbox"
            v-model="form.isActive"
            class="itbms-isActive sr-only"
          />
          <div
            class="w-10 h-6 bg-gray-300 rounded-full shadow-inner"
            :class="form.isActive ? 'bg-blue-500' : 'bg-gray-300'"
          >
            <div
              class="w-5 h-5 bg-white rounded-full shadow transform transition-transform"
              :class="form.isActive ? 'translate-x-5' : 'translate-x-0'"
            ></div>
          </div>
        </label>
      </div>

      <!-- Country Of Origin -->
      <div>
        <label for="country" class="block font-medium text-gray-700">Country Of Origin</label>
        <input
          v-model="form.countryOfOrigin"
          type="text"
          class="itbms-countryOfOrigin mt-1 block w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
        />
      </div>

      <!-- Buttons -->
      <div class="flex gap-3 mt-6">
        <button
          id="save-button"
          type="button"
          @click="handleSubmit"
          class="bg-blue-500 hover:bg-blue-600 text-white font-medium py-2 px-4 rounded shadow"
        >
          Save
        </button>
        <router-link
          :to="{ name: 'BrandList' }"
          class="itbms-cancel-button px-5 py-2 border border-gray-300 text-gray-600 rounded hover:bg-gray-100 transition"
        >
          Cancel
        </router-link>
      </div>
    </form>
  </div>
</template>

