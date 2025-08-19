<script setup>
import { useRoute, useRouter } from "vue-router";
import { ref, onMounted, computed } from "vue";
import { getItemById, getItems, editItemAndImage, editItem } from "@/lib/fetchUtils";
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
const showMaxImageWarning = ref(false);

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

// Computed property for image previews
const filePreviews = computed(() => {
  return images.value.map(image => {
    if (image.src) {
      // Existing image from server
      return image.src;
    } else if (image.file) {
      // New uploaded file
      return URL.createObjectURL(image.file);
    }
    return null;
  }).filter(Boolean);
});

// Updated to handle imageViewOrder
const isChanged = computed(() => {
  if (!originalProduct.value) return false;
  
  // Check product
  const productDataChanged = JSON.stringify(product.value) !== JSON.stringify(originalProduct.value);
  
  // Check images with order
  const originalImages = originalProduct.value.saleItemImages
    ?.sort((a, b) => a.imageViewOrder - b.imageViewOrder)
    .map(img => ({ fileName: img.fileName, imageViewOrder: img.imageViewOrder })) || [];
  
  const currentImages = images.value
    ?.map((img, index) => ({
      fileName: img.fileName || img.name,
      imageViewOrder: index + 1
    })) || [];
  
  const imagesChanged = JSON.stringify(originalImages) !== JSON.stringify(currentImages);
  
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
  if (!event.target.files) return;
  
  const newFiles = Array.from(event.target.files);
  const totalImages = images.value.length + newFiles.length;
  
  // Check if user is trying to upload more than 4 total
  if (totalImages > 4) {
    showMaxImageWarning.value = true;
    const allowedCount = 4 - images.value.length;
    if (allowedCount > 0) {
      // Only add the allowed number of files
      const filesToAdd = newFiles.slice(0, allowedCount).map((file, index) => ({
        file: file, // Store the actual File object
        name: file.name,
        imageViewOrder: images.value.length + index + 1,
        isNewFile: true
      }));
      images.value = [...images.value, ...filesToAdd];
    }
  } else {
    // Add all new files if within limit
    const filesToAdd = newFiles.map((file, index) => ({
      file: file, // Store the actual File object
      name: file.name,
      imageViewOrder: images.value.length + index + 1,
      isNewFile: true
    }));
    images.value = [...images.value, ...filesToAdd];
    showMaxImageWarning.value = false;
  }
  
  // Reset the input value so the same files can be selected again if needed
  event.target.value = '';
}

function onToggleImage(fileName) {
  images.value = images.value.filter(image => (image.fileName || image.name) !== fileName);
  // Hide warning if we're back under the limit
  if (images.value.length <= 4) {
    showMaxImageWarning.value = false;
  }
  // Reorder remaining images
  reorderImages();
}

function moveImageUp(index) {
  if (index > 0) {
    const temp = images.value[index - 1]
    images.value[index - 1] = images.value[index]
    images.value[index] = temp
    reorderImages()
  }
}

function moveImageDown(index) {
  if (index < images.value.length - 1) {
    const temp = images.value[index + 1]
    images.value[index + 1] = images.value[index]
    images.value[index] = temp
    reorderImages()
  }
}

function reorderImages() {
  images.value.forEach((image, index) => {
    image.imageViewOrder = index + 1
  })
}

function getImageName(item) {
  if (typeof item === 'string') {
    // It's a URL - extract filename after the last '/'
    return item.split('/').pop()
  } else if (item.fileName) {
    // It's an existing image object with fileName
    return item.fileName
  } else if (item.name) {
    // It's a new file object with name property
    return item.name
  } else {
    // Fallback
    return 'Unknown'
  }
}

async function loadImages() {
  // Check if product and images array exist
  if (!product.value?.saleItemImages) {
    console.warn('No images found for this product');
    return;
  }

  // Sort images by imageViewOrder before loading
  const sortedImages = [...product.value.saleItemImages].sort((a, b) => a.imageViewOrder - b.imageViewOrder);
  
  // Clear existing images
  images.value = [];

  // Load each image with error handling
  for (let i = 0; i < sortedImages.length; i++) {
    try {
      const imageData = sortedImages[i];
      if (imageData && imageData.fileName) {
        const imageUrl = `${url_items}/picture/${imageData.fileName}`;
        
        // Create image object with metadata
        const imageObject = {
          src: imageUrl,
          fileName: imageData.fileName,
          imageViewOrder: imageData.imageViewOrder,
        };
        
        images.value.push(imageObject);
      }
    } catch (error) {
      console.warn(`Failed to load image ${i}:`, error);
    }
  }
}

async function urlToFile(imageName) {
  const response = await fetch(`${url_items}/picture/${imageName}`)
  const blob = await response.blob()
  return new File([blob], imageName, { type: blob.type })
}

async function handleSubmit() {
  try {
    product.value.color = product.value.color === "" ? null : product.value.color

    // Build image info + collect files
    const imagesInfos = []

    for (let i = 0; i < images.value.length; i++) {
      const image = images.value[i]
      let fileObj

      if (image.fileName) {
        // Convert server image into File
        fileObj = await urlToFile(image.fileName)
      } else if (image.file) {
        // It's a new uploaded file
        fileObj = image.file
      }

      if (fileObj) {
        imagesInfos.push({
          pictureFile: fileObj,
          order: i + 1,
          status: null,
          pictureName: image.fileName || image.name
        })
      }
    }

    const saleItemForSubmit = {
      id: product.value.id,
      model: product.value.model,
      brandName: product.value.brandName,
      description: product.value.description,
      price: product.value.price,
      ramGb: product.value.ramGb,
      screenSizeInch: product.value.screenSizeInch,
      quantity: product.value.quantity,
      storageGb: product.value.storageGb,
      color: product.value.color
    }

    // Create the correct structure for editItemAndImage
    const saleItem = {
      saleItem: saleItemForSubmit,
      imagesInfos: imagesInfos
    };

    const editedItem = await editItemAndImage(url_items2, id, saleItem);

    if (typeof editedItem !== 'number') {
      if (from === "Gallery") {
        router.push({
          name: "ListDetails",
          params: { id: product.value.id },
          query: { edited: "true" }
        })
      } else {
        router.push({ name: "ListSaleItems", query: { edited: "true" } })
      }
    } else {
      alert("Fail to Edit Sale Item")
    }

  } catch (error) {
    console.error("Error:", error)
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
                  v-for="(preview, index) in filePreviews"
                  :key="index"
                  :src="preview"
                  class="w-1/5 rounded bg-gray-100 object-cover"
                />
              </div>
            </div>

            <!-- Warning Message -->
            <div 
              v-if="showMaxImageWarning" 
              class="mb-3 p-2 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm font-medium"
            >
              Maximum 4 pictures are allowed.
            </div>

            <!-- Upload Image Button -->
            <label
              for="fileInput"
              :class="[
                'mt-5 py-2 w-2/7 font-medium border rounded-2xl cursor-pointer text-center transition',
                images.length >= 4
                  ? 'bg-gray-300 text-gray-500 cursor-not-allowed border-gray-300'
                  : 'bg-purple-500 text-white hover:bg-purple-600 border-purple-500'
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
              :disabled="images.length >= 4"
              @change="handleFileChange"
            />
            
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
                    @click.stop="onToggleImage(getImageName(image))"
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
                      class="flex-shrink-0"
                      :class="index === 0 ? 'cursor-not-allowed text-gray-500' : 'hover:text-purple-600'"
                      @click="moveImageUp(index)"
                      type="button"
                    >
                      <span class="material-icons text-base">arrow_drop_up</span>
                    </button>

                    <!-- Swap Down -->
                    <button
                      class="flex-shrink-0"
                      :class="index === images.length - 1 ? 'cursor-not-allowed text-gray-500' : 'hover:text-purple-600'"
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