<script setup>
import { useRoute, useRouter } from "vue-router";
import { ref, onMounted, computed } from "vue";
import { getItemById, getItems, editItemAndImage } from "@/lib/fetchUtils";
import { validateInputSaleItem, isFormSaleItemValid } from '@/lib/validateInput'
import phoneImg from "../../../public/phone.png";

const url_brands = `${import.meta.env.VITE_APP_URL}/brands`
const url_items = `${import.meta.env.VITE_APP_URL}/sale-items`
const url_items2 = `${import.meta.env.VITE_APP_URL2}/sale-items`

const route = useRoute();
const router = useRouter();
const id = route.params.id;
const from = route.query.from;

const brandSelected = ref(null);
const images = ref([]);

const validationMessages = ref({});

// -------------- Form --------------------- //
const product = ref({
  id: null,
  model: "",
  brand: {
    id: null,
    name: "",
  },
  brandName: "",
  description: "",
  price: 0,
  ramGb: 0, // OPTIONAL
  saleItemImages: [],
  screenSizeInch: 0, // OPTIONAL
  quantity: 0,
  storageGb: 0, // OPTIONAL
  color: "", // OPTIONAL
});
const originalProduct = ref(null);

const isFormValid = computed(() => isFormSaleItemValid(product.value));

const isChanged = computed(() => {
  if (!originalProduct.value) return false;
  
  // Check product
  const productDataChanged = JSON.stringify(product.value) !== JSON.stringify(originalProduct.value);
  
  // Check images
  const originalImageFileNames = originalProduct.value.saleItemImages?.map(img => img.fileName) || [];
  const currentImageFileNames = images.value?.map(file => file.fileName || file.name) || [];
  
  // Compare image arrays - check if lengths differ or if any filenames are different
  const imagesChanged = 
    originalImageFileNames.length !== currentImageFileNames.length ||
    !originalImageFileNames.every((fileName, index) => fileName === currentImageFileNames[index]);
  
  return productDataChanged || imagesChanged;
});

const isSaveDisabled = computed(() => {
  return !isFormValid.value || !isChanged.value;
});

const inputRefs = ref([])

function focusNext(index) {
  inputRefs.value[index + 1]?.focus()
}

// ---------------- Image --------------------- //
function handleFileChange(event) {
  if (!event.target.images) return
  images.value = Array.from(event.target.images)
}

function onToggleImage(fileName) {
  images.value = images.value.filter(image => image.name !== fileName)
}

function moveImageUp(index) {
  if (index > 0) {
    const temp = images.value[index - 1]
    images.value[index - 1] = images.value[index]
    images.value[index] = temp
  }
}

function moveImageDown(index) {
  if (index < images.value.length - 1) {
    const temp = images.value[index + 1]
    images.value[index + 1] = images.value[index]
    images.value[index] = temp
  }
}

function getImageName(item) {
  if (typeof item === 'string') {
    // It's a URL - extract filename after the last '/'
    return item.split('/').pop()
  } else {
    // It's a File object - return the name
    return item.name
  }
}

async function loadImages() {
  // Check if product and images array exist
  if (!product.value?.saleItemImages) {
    console.warn('No images found for this product');
    return;
  }

  // Load each image with error handling
  for (let i = 0; i <  product.value.saleItemImages.length; i++) {
    try {
      // Only try to load if the image ID exists
      if (product.value.saleItemImages[i]) {
        images.value[i] = `${url_items}/picture/${product.value.saleItemImages[i].fileName}`;
      }
    } catch (error) {
      console.warn(`Failed to load image ${i}:`, error);
    }
  }
}

async function handleSubmit() {
  try {
    product.value.color = product.value.color === "" ? null : product.value.color;

    const editedItem = await editItemAndImage(url_items2, id, product.value);
    if (typeof editedItem !== 'number') {
      if (from === "Gallery")
        router.push({ name: "ListDetails", params: { id: product.id }, query: { edited: "true" },});
      else 
        router.push({ name: "ListSaleItems", query: { edited: "true" } });
    } else {
      alert("Fail to Edit Sale Item")
    }
    console.log(product.value);
  } catch (error) {
    console.error("Error:", error);
  }
}

onMounted(async () => {
  try {
    const item = await getItemById(url_items2, id);
    if (typeof item === 'number') {
      router.push({ name: 'ListGallery'});
      alert("The requested sale item does not exist.");
      return;
    }

    const data = {
      id: item.id,
      brand: {
        id: null,
        name: item.brandName,
      },
      brandName: item.brandName,
      model: item.model,
      description: item.description,
      price: item.price,
      ramGb: item.ramGb, // OPTIONAL
      saleItemImages: item.saleItemImages,
      screenSizeInch: item.screenSizeInch, // OPTIONAL
      quantity: item.quantity,
      storageGb: item.storageGb, // OPTIONAL
      color: item.color,
    };
    product.value = data;
    originalProduct.value = JSON.parse(JSON.stringify(data));

    await loadImages();
  } catch (error) {
    console.error("Failed to fetch product:", error);
  }

  try {
    const brand = await getItems(url_brands);
    if (typeof brand === 'number') {
      alert('The requested sale brand does not exist.')
      return;
    }
    brandSelected.value = brand;
  } catch (error) {
    console.error("Failed to fetch brands:", error);
  }
});
</script>

<template>
  <div class="min-h-screen bg-gray-100 text-gray-800 font-sans">
    <main class="max-w-6xl mx-auto py-10 px-6">
      <!-- Header -->
      <div class="mb-6 text-sm text-gray-500 flex items-center space-x-2">
        <router-link
          :to="{ name: 'ListGallery' }"
          class="text-purple-600 hover:underline font-medium"
        >
          Home
        </router-link>
        <span>/</span>
        <router-link
          :to="{ name: 'ListDetails', params: { id: product.id } }"
          class="itbms-back-button text-purple-600 hover:underline font-medium"
        >
          <span>{{ product.model }}</span
          >&thinsp; <span>{{ product.ramGb ?? "" }}</span
          >/<span>{{ product.storageGb ?? "" }}</span
          >GB
          <span>{{ product.color ?? "" }}</span>
        </router-link>
      </div>

      <!-- Title -->
      <h1 class="text-3xl font-bold text-gray-900 mb-4">
        <span>{{ product.model }}</span
        >&thinsp; <span>{{ product.ramGb ?? "" }}</span
        >/<span>{{ product.storageGb ?? "" }}</span
        >GB
        <span>{{ product.color ?? "" }}</span>
      </h1>

      <!-- Main -->
      <div class="bg-white rounded-2xl shadow-xl p-10">
        <form
          class="grid grid-cols-1 md:grid-cols-2 gap-10"
          @submit.prevent="handleSubmit"
        >
          <div class="flex flex-col">
            <!-- Image Section -->
            <div class="flex flex-col items-center space-y-2">
              <img
                :src="phoneImg"
                class="w-full object-cover bg-gray-200 rounded-lg"
              />
              <div class="flex space-x-3 items-center justify-center">
                <img
                  v-for="(image, index) in images" 
                  :key="index"
                  :src="image"
                  :alt="`Product View ${index + 1}`"
                  class="w-1/5 rounded bg-gray-100 object-cover"
                />
              </div>
            </div>

            <!-- Upload Image Button -->
            <input
              class="hidden"
              id="fileInput"
              type="file"
              multiple
              accept="image/*"
              @change="handleFileChange"
            />
            <label
              for="fileInput"
              class="mt-5 py-2 w-2/7 text-white font-medium border rounded-2xl bg-purple-500 hover:bg-purple-600 cursor-pointer text-center"
            >
              Upload Images
            </label>

            <!-- Uploaded Image -->
            <div class="flex flex-col gap-3 mt-3 max-w-3/5 md:max-w-1/2">
              <span
                v-if="images.length > 0"
                v-for="(image, index) in images"
                :key="index"
                class="inline-flex items-center justify-between gap-2 bg-purple-100 text-purple-700 px-3 py-0.5 rounded-full text-sm font-medium"
              >
                <!-- Filename with truncate -->
                <span class="truncate min-w-0 max-w-[200px]">
                  {{ getImageName(image) }}
                </span>

                <!-- Button Right Side -->
                <div class="flex gap-1">
                  <!-- Remove Image -->
                  <button 
                    class="flex-shrink-0 hover:text-red-500 -mb-1"
                    @click.stop="onToggleImage(file.name)"
                  >
                    <span class="material-icons text-sm">close</span>
                  </button>

                  <!-- Swap Up/Down Button -->
                  <div 
                    class="flex flex-col leading-none"
                  >
                    <!-- Swap Up -->
                    <button
                      v-if="index !== 0"
                      class="flex-shrink-0 hover:text-purple-600"
                      @click="moveImageUp(index)"
                    >
                      <span class="material-icons text-base">arrow_drop_up</span>
                    </button>

                    <!-- Swap Down -->
                    <button
                      v-if="index !== images.length - 1"
                      class="flex-shrink-0 hover:text-purple-600"
                      @click="moveImageDown(index)"
                    >
                      <span class="material-icons text-base">arrow_drop_down</span>
                    </button>
                  </div> <!-- END--Swap Up/Down Button -->

                </div> <!-- END--Button Right Side -->
              </span>
              
            </div> <!-- END--Upload Image-->
          </div>

          <!-- Form Fields -->
          <div class="space-y-6 mt-10">
            <div>
              <label class="block mb-1 font-semibold text-gray-700">Brand</label>
              <select
                v-model="product.brandName"
                @blur="validateInputSaleItem(product, 'brandName', validationMessages)"
                :ref="el => inputRefs[0] = el"
                @keydown.enter.prevent="focusNext(0)"
                class="itbms-brand w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500"
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
              <label class="block mb-1 font-semibold text-gray-700">Model</label>
              <input
                v-model.trim="product.model"
                @blur="validateInputSaleItem(product, 'model', validationMessages)"
                :ref="el => inputRefs[1] = el"
                @keydown.enter.prevent="focusNext(1)"
                type="text"
                class="itbms-model w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500"
                required
              />
              <p v-if="validationMessages.model" class="itbms-message text-sm text-red-600 mt-1">
                {{ validationMessages.model }}
              </p>
            </div>

            <div>
              <label class="block mb-1 font-semibold text-gray-700">Price (Baht)</label>
              <input
                v-model.number="product.price"
                @blur="validateInputSaleItem(product, 'price', validationMessages)"
                :ref="el => inputRefs[2] = el"
                @keydown.enter.prevent="focusNext(2)"
                type="number"
                class="itbms-price w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500"
                required
              />
              <p v-if="validationMessages.price" class="itbms-message text-sm text-red-600 mt-1">
                {{ validationMessages.price }}
              </p>
            </div>

            <div>
              <label class="block mb-1 font-semibold text-gray-700">Description</label>
              <textarea
                v-model.trim="product.description"
                @blur="validateInputSaleItem(product, 'description', validationMessages)"
                :ref="el => inputRefs[3] = el"
                @keydown.enter.prevent="focusNext(3)"
                rows="3"
                class="itbms-description w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500"
                required
              ></textarea>
              <p v-if="validationMessages.description" class="itbms-message text-sm text-red-600 mt-1">
                {{ validationMessages.description }}
              </p>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block mb-1 font-semibold text-gray-700">RAM (GB)</label>
                <input
                  v-model.number="product.ramGb"
                  @blur="validateInputSaleItem(product, 'ramGb', validationMessages)"
                  :ref="el => inputRefs[4] = el"
                 @keydown.enter.prevent="focusNext(4)"
                  type="number"
                  class="itbms-ramGb w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500"
                />
                <p v-if="validationMessages.ramGb" class="itbms-message text-sm text-red-600 mt-1">
                  {{ validationMessages.ramGb }}
                </p>
              </div>
              <div>
                <label class="block mb-1 font-semibold text-gray-700">Screen Size (Inch)</label>
                <input
                  v-model.number="product.screenSizeInch"
                  @blur="validateInputSaleItem(product, 'screenSizeInch', validationMessages)"
                  :ref="el => inputRefs[5] = el"
                  @keydown.enter.prevent="focusNext(5)"
                  type="number"
                  step="any"
                  class="itbms-screenSizeInch w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500"
                />
                <p v-if="validationMessages.screenSizeInch" class="itbms-message text-sm text-red-600 mt-1">
                  {{ validationMessages.screenSizeInch }}
                </p>
              </div>
              <div>
                <label class="block mb-1 font-semibold text-gray-700">Storage (GB)</label>
                <input
                  v-model.number="product.storageGb"
                  @blur="validateInputSaleItem(product, 'storageGb', validationMessages)"
                  :ref="el => inputRefs[6] = el"
                  @keydown.enter.prevent="focusNext(6)"
                  type="number"
                  class="itbms-storageGb w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500"
                />
                <p v-if="validationMessages.storageGb" class="itbms-message text-sm text-red-600 mt-1">
                  {{ validationMessages.storageGb }}
                </p>
              </div>
              <div>
                <label class="block mb-1 font-semibold text-gray-700">Color</label>
                <input
                  v-model.trim="product.color"
                  @blur="validateInputSaleItem(product, 'color', validationMessages)"
                  :ref="el => inputRefs[7] = el"
                  @keydown.enter.prevent="focusNext(7)"
                  type="text"
                  class="itbms-color w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500"
                />
                <p v-if="validationMessages.color" class="itbms-message text-sm text-red-600 mt-1">
                  {{ validationMessages.color }}
                </p>
              </div>
            </div>

            <div>
              <label class="block mb-1 font-semibold text-gray-700">Quantity</label>
              <input
                v-model.number="product.quantity"
                @blur="validateInputSaleItem(product, 'quantity', validationMessages)"
                :ref="el => inputRefs[8] = el"
                @keydown.enter.prevent="validateInputSaleItem(product, 'quantity', validationMessages)"
                type="number"
                class="itbms-quantity w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500"
                required
              />
              <p v-if="validationMessages.quantity" class="itbms-message text-sm text-red-600 mt-1">
                {{ validationMessages.quantity }}
              </p>
            </div>

            <!-- Buttons -->
            <div class="flex justify-end space-x-4">
              <router-link
                :to="
                  from === 'Gallery'
                    ? { name: 'ListDetails', params: { id: product.id } }
                    : { name: 'ListSaleItems' }
                "
                class="itbms-cancel-button px-5 py-2 bg-gray-200 rounded hover:bg-gray-300 text-gray-700"
              >
                Cancel
              </router-link>
              <button
                type="submit"
                :disabled="isSaveDisabled"
                :class="[
                  'itbms-save-button px-5 py-2 text-white rounded transition',
                  isSaveDisabled
                    ? 'bg-purple-300 cursor-not-allowed'
                    : 'bg-purple-600 hover:bg-purple-700',
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
