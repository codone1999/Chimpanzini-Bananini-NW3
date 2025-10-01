<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { editItem, getItemById } from '@/lib/fetchUtils'
import { validateInputBrands, isFormBrandValid } from '@/lib/validateInput'

const url = `${import.meta.env.VITE_APP_URL}/brands`

const route = useRoute()
const router = useRouter()
const id = route.params.id

const form = ref({
  "name": '',
  "websiteUrl": '',
  "isActive": false,
  "countryOfOrigin": ''
})

const validationMessages = ref({});

const originalBrand = ref(null)

const isFormValid = computed(() => isFormBrandValid(form.value))

const isChanged = computed(() => {
  if (!originalBrand.value) return false
  return JSON.stringify(form.value) !== JSON.stringify(originalBrand.value)
})

const isSaveDisabled = computed(() => {
  return !isFormValid.value || !isChanged.value
})

async function handleSubmit() {
  try {
    const editedItem = await editItem(url, id, form.value)
    if (typeof editedItem !== 'number') {
      router.push({ name: 'ListBrands', query: {edited: 'true'} })
    } else {
      router.push({ name: 'ListBrands', query: {failed_edit: 'true'} })
    }
  } catch (error) {
    console.error('Error:', error)
  }
}

const inputRefs = ref([])

function focusNext(index) {
  inputRefs.value[index + 1]?.focus()
}


onMounted(async () => {
  try {
    const item = await getItemById(url, id)
    if (typeof item === 'number') {
      router.push({ name: 'ListBrands' })
      alert('The requested sale item does not exist.')
      return
    }

    const data = {
      "name": item.name,
      "websiteUrl": item.websiteUrl,
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
  <div class="p-8 bg-white rounded-lg shadow-md max-w-3xl mx-auto mt-16 mb-32">
    <!-- Breadcrumb -->
    <div class="text-sm text-gray-500 mb-6 flex items-center gap-2">
      <router-link
          :to="{ name: 'ListSaleItems'}"
          class="Itbms-item-list hover:underline hover:text-[#7e5bef] transition"
        >
          Sale Item List
        </router-link>
      <span class="text-gray-400">›</span>
      <router-link
          :to="{ name: 'ListBrands'}"
          class="itbms-manage-brand hover:underline hover:text-[#7e5bef] transition"
        >
          Brand List
        </router-link>
      <span class="text-gray-400">›</span>
      <span class="font-semibold text-black">Edit Brand</span>
    </div>

    <!-- Form -->
    <form class="space-y-6">
      <!-- Name -->
      <div>
        <label for="name" class="block text-sm font-medium text-gray-700">
          Name<span class="text-red-500">*</span>
        </label>
        <input
          v-model.trim="form.name"
          @blur="validateInputBrands(form, 'name', validationMessages)"
          :ref="el => inputRefs[0] = el"
          @keydown.enter.prevent="focusNext(0)"
          type="text"
          class="itbms-name mt-1 w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-[#7e5bef] transition"
          required
        />
        <p v-if="validationMessages.name" class="itbms-message text-sm text-red-600 mt-1">
          {{ validationMessages.name }}
        </p>
      </div>

      <!-- Website URL -->
      <div>
        <label for="websiteUrl" class="block text-sm font-medium text-gray-700">Website URL</label>
        <input
          v-model.trim="form.websiteUrl"
          @blur="validateInputBrands(form, 'websiteUrl', validationMessages)"
          :ref="el => inputRefs[1] = el"
          @keydown.enter.prevent="focusNext(1)"
          type="url"
          class="itbms-websiteUrl mt-1 w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-[#7e5bef] transition"
        />
        <p v-if="validationMessages.websiteUrl" class="itbms-message text-sm text-red-600 mt-1">
          {{ validationMessages.websiteUrl }}
        </p>
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
            class="relative w-11 h-6 rounded-full transition-colors duration-300"
            :class="form.isActive ? 'bg-[#7e5bef]' : 'bg-gray-300'"
          >
            <div
              class="absolute top-0.5 left-0.5 w-5 h-5 bg-white rounded-full shadow-md transform transition-transform duration-300"
              :class="form.isActive ? 'translate-x-5' : 'translate-x-0'"
            ></div>
          </div>
        </label>
      </div>

      <!-- Country Of Origin -->
      <div>
        <label for="country" class="block text-sm font-medium text-gray-700">Country Of Origin</label>
        <input
          v-model.trim="form.countryOfOrigin"
          @blur="validateInputBrands(form, 'countryOfOrigin', validationMessages)"
          :ref="el => inputRefs[2] = el"
          @keydown.enter.prevent="validateInputBrands(form, 'countryOfOrigin', validationMessages)"
          type="text"
          class="itbms-countryOfOrigin mt-1 w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-[#7e5bef] transition"
        />
        <p v-if="validationMessages.countryOfOrigin" class="itbms-message text-sm text-red-600 mt-1">
          {{ validationMessages.countryOfOrigin }}
        </p>
      </div>

      <!-- Buttons -->
      <div class="flex gap-3 mt-6">
        <button
          id="save-button"
          type="button"
          @click="handleSubmit"
          :disabled="isSaveDisabled"
          :class="[
            'itbms-save-button text-white font-medium py-2 px-4 rounded shadow',
            isSaveDisabled ? 'bg-purple-300 cursor-not-allowed' : 'bg-[#7e5bef] hover:bg-[#5e4ecf]'
          ]"
        >
          Save
        </button>
        <router-link
          :to="{ name: 'ListBrands' }"
          class="itbms-cancel-button px-6 py-2 border border-gray-300 text-gray-600 rounded-lg hover:bg-gray-100 transitionn"
        >
          Cancel
        </router-link>
      </div>
    </form>
  </div>
</template>
