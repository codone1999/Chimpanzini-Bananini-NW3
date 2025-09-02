//AddItem.vue
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getItems, addItemAndImage } from '@/lib/fetchUtils'
import { validateInputSaleItem, isFormSaleItemValid } from '@/lib/validateInput'
import { useImageUpload } from '@/composables/useImageUpload'

const route = useRoute()
const router = useRouter()
const from = route.query.from

const brandSelected = ref([])
const inputRefs = ref([]) // Focus next field in Form
const isSubmitting = ref(false)

// Use image upload composable (false = AddItem mode)
const {
  files,
  showMaxImageWarning,
  selectedImageIndex,
  filePreviews,
  mainDisplayImage,
  selectImageForDisplay,
  handleFileChange,
  removeImage,
  moveImageUp,
  moveImageDown,
  checkAndClearOversized
} = useImageUpload(false)

const newSaleItem = ref({
  sellerId: 1,
  brand: {
    id: null,
  },
  brandName: '',
  model: '',
  description: '',
  price: '',
  quantity: '',
  screenSizeInch: null,
  ramGb: null,
  storageGb: null,
  color: null,
})

const validationMessages = ref({})

// ---------------- Computed ------------------------ //
const isFormValid = computed(() => isFormSaleItemValid(newSaleItem.value))

// ------------- Form --------------------- //
function focusNext(index) {
  inputRefs.value[index + 1]?.focus()
}

async function handleSubmit() {
  if (isSubmitting.value) return

  try {
    isSubmitting.value = true

    const addedItem = await addItemAndImage(`${import.meta.env.VITE_APP_URL2}/sale-items`, newSaleItem.value, files.value)
    
    if (addedItem && addedItem.id) { // Check for successful response
      router.push({ 
        name: from === 'Gallery' ? 'ListGallery' : 'ListSaleItems', 
        query: { added: 'true' } 
      })
    } else {
      alert("Failed to submit item!")
      isSubmitting.value = false
    }
  } catch (error) {
    console.error('Error:', error)
    alert("Failed to submit item!")
    isSubmitting.value = false
  }
}

onMounted(async () => {
  try {
    const brands = await getItems(`${import.meta.env.VITE_APP_URL}/brands`)
    if (typeof brands === 'number')
      alert("Fail to Fetch Sale Items")
    brandSelected.value = brands
  } catch (error) {
    console.error('Error loading brands:', error)
  }
})
</script>

<template>
  <div class="min-h-screen bg-gray-900 text-gray-100 font-sans">
    <main class="max-w-6xl mx-auto py-10 px-6">
      <!-- Header -->
      <div class="mb-6 text-sm text-gray-400 flex items-center space-x-2">
        <router-link
          :to="{ name: 'ListGallery' }"
          class="text-purple-400 hover:underline font-medium"
        >
          Home
        </router-link>
        <span>/</span>
        <span class="text-gray-200 font-semibold">New Sale Item</span>
      </div>

      <!-- Title -->
      <h1 class="text-3xl font-bold text-white mb-6">Add New Sale Item</h1>

      <!-- Main -->
      <div class="bg-gray-800 rounded-2xl shadow-2xl py-8 px-8 border border-gray-700">
        <form
          class="grid grid-cols-1 md:grid-cols-2 gap-10"
          @submit.prevent="handleSubmit"
        >
          <div class="flex flex-col">
            <!-- Image Section -->
            <div class="flex flex-col items-center space-y-5">
              <!-- Main Display Image -->
              <div class="relative w-full">
                <div class="aspect-[3/4] bg-gray-700 rounded-lg overflow-hidden">
                  <img
                    :src="mainDisplayImage"
                    class="w-full h-full object-cover"
                    :alt="files.length > 0 ? 'Uploaded product image' : 'Default phone image'"
                  />
                </div>
                
                <!-- Image indicator if there are uploaded images -->
                <div 
                  v-if="files.length > 0" 
                  class="absolute top-2 right-2 bg-black bg-opacity-50 text-white px-2 py-1 rounded text-xs"
                >
                  {{ selectedImageIndex + 1 }} / {{ files.length }}
                </div>
              </div>
              
              <!-- Image Thumbnails for Selection with 9:16 aspect ratio -->
              <div class="flex space-x-8 items-center justify-center w-full">
                <div
                  v-for="(preview, index) in filePreviews"
                  :key="index"
                  :class="[
                    'flex-1 max-w-[6rem] cursor-pointer transition-all duration-200 rounded overflow-hidden',
                    selectedImageIndex === index 
                      ? 'ring-2 ring-purple-500 ring-offset-2 ring-offset-gray-900' 
                      : 'hover:ring-2 hover:ring-purple-300 hover:ring-offset-1'
                  ]"
                  @click="selectImageForDisplay(index)"
                >
                  <div class="aspect-[3/4] bg-gray-700">
                    <img
                      :src="preview"
                      class="w-full h-full object-cover"
                      :alt="`Product image ${index + 1}`"
                    />
                  </div>
                </div>
              </div>
            </div>

            <!-- Warning Messages -->
            <div class="space-y-2 mt-4">
              <!-- File Size Warning -->
              <div 
                v-if="checkAndClearOversized()" 
                class="p-2 bg-red-900 border border-red-700 rounded-lg text-red-300 text-sm font-medium"
              >
                The picture file size cannot be larger than 2MB
              </div>

              <!-- Max Images Warning -->
              <div 
                v-if="showMaxImageWarning" 
                class="p-2 bg-red-900 border border-red-700 rounded-lg text-red-300 text-sm font-medium"
              >
                Maximum 4 pictures are allowed.
              </div>
            </div>

            <!-- Upload Image Button -->
            <label
              for="fileInput"
              :class="[
                'mt-6 py-2 w-2/7 font-medium border rounded-xl cursor-pointer text-center transition',
                files.length >= 4
                  ? 'bg-gray-600 text-gray-400 cursor-not-allowed border-gray-600'
                  : 'bg-purple-600 text-white hover:bg-purple-700 border-purple-600'
              ]"
            >
              Upload Images
            </label>
            
            <!-- Disable file input when 4 images are reached -->
            <input
              class="hidden"
              id="fileInput"
              type="file"
              multiple
              accept="image/*"
              :disabled="files.length >= 4"
              @change="handleFileChange"
            />
            
            <!-- Uploaded Image -->
            <div class="flex flex-col gap-3 mt-3 max-w-3/5 md:max-w-1/2">
              <span
                v-if="files.length > 0"
                v-for="(file, index) in files"
                :key="index"
                class="inline-flex items-center justify-between gap-3 
                bg-gray-800 border border-purple-600 text-gray-200 
                px-4 py-1.5 rounded-full text-sm font-medium shadow-sm"
              >
                <!-- Filename with truncate -->
                <span class="truncate min-w-0 max-w-[200px]">
                  {{ file.name }}
                </span>

                <!-- Button Right Side -->
                <div class="flex gap-2 items-center">
                  <!-- Remove Image -->
                  <button 
                    class="flex-shrink-0 hover:text-red-500 -mb-1"
                    @click.stop="removeImage(index)"
                    type="button"
                  >
                    <span class="material-icons text-sm">close</span>
                  </button>

                  <!-- Swap Up/Down Button -->
                  <div 
                    class="flex flex-col leading-none"
                  >
                    <!-- Swap Up -->
                    <button
                      class="flex-shrink-0 "
                      :class="index === 0 ? 'cursor-not-allow text-gray-500 ': 'hover:text-purple-400' "
                      @click="moveImageUp(index)"
                      type="button"
                    >
                      <span class="material-icons text-base">arrow_drop_up</span>
                    </button>

                    <!-- Swap Down -->
                    <button
                      class="flex-shrink-0"
                      :class="index === files.length - 1 ? 'cursor-not-allow text-gray-500' : 'hover:text-purple-400' "
                      @click="moveImageDown(index)"
                      type="button"
                    >
                      <span class="material-icons text-base">arrow_drop_down</span>
                    </button>
                  </div> <!-- END--Swap Up/Down Button -->

                </div> <!-- END--Button Right Side -->
              </span>
              
            </div> <!-- END--Upload Image-->
          </div>

          <!-- Form Fields -->
          <div class="space-y-6 mt-6 md:mt-10">
            <div>
              <label class="block mb-1 font-semibold text-gray-200"
                >Brand</label
              >
              <select
                v-model="newSaleItem.brandName"
                @blur="validateInputSaleItem(newSaleItem, 'brandName', validationMessages)"
                :ref="el => inputRefs[0] = el"
                @keydown.enter.prevent="focusNext(0)"
                class="itbms-brand w-full bg-gray-700 border border-gray-600 px-3 py-2 rounded-lg focus:ring-2 focus:ring-purple-500 text-gray-100"
              >
                <option value="">Select Brand</option>
                <option
                  v-for="brand in brandSelected"
                  :key="brand.id"
                  :value="brand.name"
                >
                  {{ brand.name }}
                </option>
              </select>
              <p v-if="validationMessages.brandName" class="itbms-message text-sm text-red-600 mt-1">
                {{ validationMessages.brandName }}
              </p>
            </div>

            <div>
              <label class="block mb-1 font-semibold text-gray-200">Model</label>
              <input
                v-model.trim="newSaleItem.model"
                @blur="validateInputSaleItem(newSaleItem, 'model', validationMessages)"
                :ref="el => inputRefs[1] = el"
                @keydown.enter.prevent="focusNext(1)"
                type="text"
                class="itbms-model w-full bg-gray-700 border border-gray-600 px-3 py-2 rounded-lg focus:ring-2 focus:ring-purple-500 text-gray-100"
                required
              />
              <p v-if="validationMessages.model" class="itbms-message text-sm text-red-600 mt-1">
                {{ validationMessages.model }}
              </p>
            </div>

            <div>
              <label class="block mb-1 font-semibold text-gray-200">Price (Baht)</label>
              <input
                v-model.number="newSaleItem.price"
                @blur="validateInputSaleItem(newSaleItem, 'price', validationMessages)"
                :ref="el => inputRefs[2] = el"
                @keydown.enter.prevent="focusNext(2)"
                type="number"
                class="itbms-price w-full bg-gray-700 border border-gray-600 px-3 py-2 rounded-lg focus:ring-2 focus:ring-purple-500 text-gray-100"
                required
              />
              <p v-if="validationMessages.price" class="itbms-message text-sm text-red-600 mt-1">
                {{ validationMessages.price }}
              </p>
            </div>

            <div>
              <label class="block mb-1 font-semibold text-gray-200">Description</label>
              <textarea
                v-model.trim="newSaleItem.description"
                @blur="validateInputSaleItem(newSaleItem, 'description', validationMessages)"
                :ref="el => inputRefs[3] = el"
                @keydown.enter.prevent="focusNext(3)"
                rows="3"
                class="itbms-description w-full bg-gray-700 border border-gray-600 px-3 py-2 rounded-lg focus:ring-2 focus:ring-purple-500 text-gray-100"
                required
              ></textarea>
              <p v-if="validationMessages.description" class="itbms-message text-sm text-red-600 mt-1">
                {{ validationMessages.description }}
              </p>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block mb-1 font-semibold text-gray-200">RAM (GB)</label>
                <input
                  v-model.number="newSaleItem.ramGb"
                  @blur="validateInputSaleItem(newSaleItem, 'ramGb', validationMessages)"
                  :ref="el => inputRefs[4] = el"
                  @keydown.enter.prevent="focusNext(4)"
                  type="number"
                  class="itbms-ramGb w-full bg-gray-700 border border-gray-600 px-3 py-2 rounded-lg focus:ring-2 focus:ring-purple-500 text-gray-100"
                />
                <p v-if="validationMessages.ramGb" class="itbms-message text-sm text-red-600 mt-1">
                  {{ validationMessages.ramGb }}
                </p>
              </div>
              <div>
                <label class="block mb-1 font-semibold text-gray-200">Screen Size (Inch)</label>
                <input
                  v-model.number="newSaleItem.screenSizeInch"
                  @blur="validateInputSaleItem(newSaleItem, 'screenSizeInch', validationMessages)"
                  :ref="el => inputRefs[5] = el"
                  @keydown.enter.prevent="focusNext(5)"
                  type="number"
                  step="any"
                  class="itbms-screenSizeInch w-full bg-gray-700 border border-gray-600 px-3 py-2 rounded-lg focus:ring-2 focus:ring-purple-500 text-gray-100"
                />
                <p v-if="validationMessages.screenSizeInch" class="itbms-message text-sm text-red-600 mt-1">
                  {{ validationMessages.screenSizeInch }}
                </p>
              </div>
              <div>
                <label class="block mb-1 font-semibold text-gray-200">Storage (GB)</label>
                <input
                  v-model.number="newSaleItem.storageGb"
                  @blur="validateInputSaleItem(newSaleItem, 'storageGb', validationMessages)"
                  :ref="el => inputRefs[6] = el"
                  @keydown.enter.prevent="focusNext(6)"
                  type="number"
                  class="itbms-storageGb w-full bg-gray-700 border border-gray-600 px-3 py-2 rounded-lg focus:ring-2 focus:ring-purple-500 text-gray-100"
                />
                <p v-if="validationMessages.storageGb" class="itbms-message text-sm text-red-600 mt-1">
                  {{ validationMessages.storageGb }}
                </p>
              </div>
              <div>
                <label class="block mb-1 font-semibold text-gray-200">Color</label>
                <input
                  v-model.trim="newSaleItem.color"
                  @blur="validateInputSaleItem(newSaleItem, 'color', validationMessages)"
                  :ref="el => inputRefs[7] = el"
                  @keydown.enter.prevent="focusNext(7)"
                  type="text"
                  class="itbms-color w-full bg-gray-700 border border-gray-600 px-3 py-2 rounded-lg focus:ring-2 focus:ring-purple-500 text-gray-100"
                />
                <p v-if="validationMessages.color" class="itbms-message text-sm text-red-600 mt-1">
                  {{ validationMessages.color }}
                </p>
              </div>
            </div>

            <div>
              <label class="block mb-1 font-semibold text-gray-200">Quantity</label>
              <input
                v-model.number="newSaleItem.quantity"
                @blur="validateInputSaleItem(newSaleItem, 'quantity', validationMessages)"
                :ref="el => inputRefs[8] = el"
                @keydown.enter.prevent="validateInputSaleItem(newSaleItem, 'quantity', validationMessages)"
                type="number"
                class="itbms-quantity w-full bg-gray-700 border border-gray-600 px-3 py-2 rounded-lg focus:ring-2 focus:ring-purple-500 text-gray-100"
                required
              />
              <p v-if="validationMessages.quantity" class="itbms-message text-sm text-red-600 mt-1">
                {{ validationMessages.quantity }}
              </p>
            </div>

            <!-- Buttons -->
            <div class="flex justify-end space-x-4 pt-6">
              <router-link
                :to="
                  from === 'Gallery'
                    ? { name: 'ListGallery' }
                    : { name: 'ListSaleItems' }
                "
                class="itbms-cancel-button px-5 py-2 rounded-lg border border-gray-600 bg-gray-700 text-gray-300 hover:bg-gray-600 transition"
              >
                Cancel
              </router-link>
              <button
                type="submit"
                :disabled="!isFormValid || isSubmitting"
                :class="[
                  'itbms-save-button px-5 py-2 rounded-lg transition',
                  isFormValid
                    ? 'bg-purple-600 text-white hover:bg-purple-700'
                    : 'bg-purple-900 text-gray-400 cursor-not-allowed',
                  isSubmitting
                    ? 'cursor-not-allowed'
                    : ''
                ]"
              >
                Save
              </button>
            </div>
          </div>
        </form>
      </div>
    </main>
  </div>
</template>
