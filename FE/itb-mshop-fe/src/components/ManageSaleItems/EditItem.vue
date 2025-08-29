//EditItem.vue
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
const oversizedFiles = ref([])
const selectedImageIndex = ref(0)

const isSubmitting = ref(false)

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

// ----------------------- Computed ------------------------ //
const mainDisplayImage = computed(() => {
  if (images.value.length > 0 && selectedImageIndex.value < images.value.length) {
    const selectedImage = images.value[selectedImageIndex.value]
    if (selectedImage.src) {
      // Existing image from server
      return selectedImage.src
    } else if (selectedImage.file) {
      // New uploaded file
      return URL.createObjectURL(selectedImage.file)
    }
  }
  return phoneImg // Fallback to default phone image
})

const isFormValid = computed(() => isFormSaleItemValid(product.value));

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

const isChanged = computed(() => {
  if (!originalProduct.value) return false;
  
  // Deep comparison function for objects
  const deepEqual = (obj1, obj2) => {
    if (obj1 === obj2) return true;
    
    if (obj1 == null || obj2 == null) return obj1 === obj2;
    
    if (typeof obj1 !== 'object' || typeof obj2 !== 'object') {
      return obj1 === obj2;
    }
    
    const keys1 = Object.keys(obj1);
    const keys2 = Object.keys(obj2);
    
    if (keys1.length !== keys2.length) return false;
    
    for (let key of keys1) {
      if (!keys2.includes(key)) return false;
      if (!deepEqual(obj1[key], obj2[key])) return false;
    }
    
    return true;
  };
  
  // Create clean copies for comparison (excluding nested objects that might cause issues)
  const currentProductClean = {
    id: product.value.id,
    model: product.value.model?.trim() || "",
    brandName: product.value.brandName?.trim() || "",
    description: product.value.description?.trim() || "",
    price: Number(product.value.price) || 0,
    ramGb: Number(product.value.ramGb) || 0,
    screenSizeInch: Number(product.value.screenSizeInch) || 0,
    quantity: Number(product.value.quantity) || 0,
    storageGb: Number(product.value.storageGb) || 0,
    color: product.value.color?.trim() || "",
  };
  
  const originalProductClean = {
    id: originalProduct.value.id,
    model: originalProduct.value.model?.trim() || "",
    brandName: originalProduct.value.brandName?.trim() || "",
    description: originalProduct.value.description?.trim() || "",
    price: Number(originalProduct.value.price) || 0,
    ramGb: Number(originalProduct.value.ramGb) || 0,
    screenSizeInch: Number(originalProduct.value.screenSizeInch) || 0,
    quantity: Number(originalProduct.value.quantity) || 0,
    storageGb: Number(originalProduct.value.storageGb) || 0,
    color: originalProduct.value.color?.trim() || "",
  };
  
  // Check if product data changed
  const productDataChanged = !deepEqual(currentProductClean, originalProductClean);
  
  // Enhanced image comparison with better handling of edge cases
  const originalImages = originalProduct.value.saleItemImages || [];
  const originalImagesList = originalImages
    .sort((a, b) => (a.imageViewOrder || 0) - (b.imageViewOrder || 0))
    .map(img => ({
      fileName: img.fileName,
      imageViewOrder: img.imageViewOrder || 0
    }));
  
  const currentImagesList = (images.value || [])
    .map((img, index) => ({
      fileName: img.fileName || img.name || "",
      imageViewOrder: index + 1
    }));
  
  // Check if images changed (count, order, or files)
  const imagesChanged = !deepEqual(originalImagesList, currentImagesList);
  
  return productDataChanged || imagesChanged;
});

const isSaveDisabled = computed(() => {
  // Check if form is valid
  if (!isFormValid.value) return true;
  
  // Check if anything actually changed
  if (!isChanged.value) return true;
  
  // Check if currently submitting
  if (isSubmitting.value) return true;
  
  // Additional validation: ensure required fields are not empty after trimming
  const requiredFields = ['model', 'brandName', 'description'];
  for (const field of requiredFields) {
    if (!product.value[field] || product.value[field].toString().trim() === '') {
      return true;
    }
  }
  
  // Ensure price and quantity are positive numbers
  if (product.value.price <= 0 || product.value.quantity <= 0) {
    return true;
  }
  
  return false;
});

// ----------------------- Form ---------------------- //
const inputRefs = ref([])

function focusNext(index) {
  inputRefs.value[index + 1]?.focus()
}

// ---------------- Image --------------------- //
function selectImageForDisplay(index) {
  selectedImageIndex.value = index
}

function handleFileChange(event) {
  if (!event.target.files) return;
  
  const newFiles = Array.from(event.target.files);
  const maxFileSize = 2 * 1024 * 1024; // 2MB in bytes
  const previousLength = images.value.length;
  
  // Separate valid and oversized files
  const validFiles = [];
  const currentOversizedFiles = [];
  
  newFiles.forEach(file => {
    // Check for duplicate files by name and size
    const isDuplicate = images.value.some(existingImg => 
      (existingImg.name === file.name || existingImg.fileName === file.name) &&
      existingImg.file?.size === file.size
    );
    
    if (isDuplicate) {
      // Skip duplicate files silently or show a message
      return;
    }
    
    if (file.size > maxFileSize) {
      currentOversizedFiles.push(file.name);
    } else {
      validFiles.push(file);
    }
  });

  // Update oversized files list
  oversizedFiles.value = currentOversizedFiles;

  const totalImages = images.value.length + validFiles.length;
  
  // Check if user is trying to upload more than 4 total
  if (totalImages > 4) {
    showMaxImageWarning.value = true;
    const allowedCount = 4 - images.value.length;
    if (allowedCount > 0) {
      // Only add the allowed number of files
      const filesToAdd = validFiles.slice(0, allowedCount).map((file, index) => ({
        file: file,
        name: file.name,
        imageViewOrder: images.value.length + index + 1,
        isNewFile: true
      }));
      images.value = [...images.value, ...filesToAdd];
    }
  } else {
    // Add all new files if within limit
    const filesToAdd = validFiles.map((file, index) => ({
      file: file,
      name: file.name,
      imageViewOrder: images.value.length + index + 1,
      isNewFile: true
    }));
    images.value = [...images.value, ...filesToAdd];
    showMaxImageWarning.value = false;

    // Auto-select first uploaded image
    if (previousLength === 0 && validFiles.length > 0) {
      selectedImageIndex.value = 0;
    }
  }
  
  // Reset the input value so same files can be selected again
  event.target.value = '';
}

function removeImage(fileName) {
  const removedIndex = images.value.findIndex(image => (image.fileName || image.name) === fileName)
  images.value = images.value.filter(image => (image.fileName || image.name) !== fileName);
  
  // Adjust selected image index if necessary
  if (removedIndex <= selectedImageIndex.value) {
    if (images.value.length === 0) {
      selectedImageIndex.value = 0 // Reset to show default image
    } else if (selectedImageIndex.value >= images.value.length) {
      selectedImageIndex.value = images.value.length - 1
    }
  }
  
  // Hide warning if we're back under the limit
  if (images.value.length <= 4) {
    showMaxImageWarning.value = false;
  }
  // Reorder remaining images
  reorderImages();
}

function moveImageUp(index) {
  if (index > 0) {
    // Swap the images in the array
    const temp = images.value[index - 1]
    images.value[index - 1] = images.value[index]
    images.value[index] = temp

    // Update selected index if it's affected
    if (selectedImageIndex.value === index) {
      selectedImageIndex.value = index - 1
    } else if (selectedImageIndex.value === index - 1) {
      selectedImageIndex.value = index
    }

    // Update the imageViewOrder for all images after swapping
    reorderImages()
  }
}

function moveImageDown(index) {
  if (index < images.value.length - 1) {
    // Swap the images in the array
    const temp = images.value[index + 1]
    images.value[index + 1] = images.value[index]
    images.value[index] = temp

    // Update selected index if it's affected
    if (selectedImageIndex.value === index) {
      selectedImageIndex.value = index + 1
    } else if (selectedImageIndex.value === index + 1) {
      selectedImageIndex.value = index
    }

    // Update the imageViewOrder for all images after swapping
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

function checkAndClearOversized() {
  if (oversizedFiles.value.length > 0) {
    setTimeout(() => {
      oversizedFiles.value = [];
    }, 2000);
    return true;
  }
  return false;
}

async function loadImages() {
  // Check if product and images array exist
  if (!product.value?.saleItemImages) {
    console.warn('No images found for this product');
    return;
  }

  // Sort images by imageViewOrder before loading
  const sortedImages = [...product.value.saleItemImages]
    .sort((a, b) => (a.imageViewOrder || 0) - (b.imageViewOrder || 0))
  
  // Clear existing images
  images.value = [];

  // Load each image with error handling and proper structure
  for (let i = 0; i < sortedImages.length; i++) {
    try {
      const imageData = sortedImages[i];
      if (imageData && imageData.fileName) {
        const imageUrl = `${url_items}/picture/${imageData.fileName}`;
        
        // Create image object with all necessary properties
        const imageObject = {
          src: imageUrl,
          fileName: imageData.fileName,
          imageViewOrder: i + 1, // Ensure sequential order starting from 1
        };
        
        images.value.push(imageObject);
      }
    } catch (error) {
      console.warn(`Failed to load image ${i}:`, error);
    }
  }

  // Auto-select first image if images were loaded
  if (images.value.length > 0) {
    selectedImageIndex.value = 0
  }
}

async function urlToFile(imageName) {
  const response = await fetch(`${url_items}/picture/${imageName}`)
  const blob = await response.blob()
  return new File([blob], imageName, { type: blob.type })
}

// ---------------- Submit & OnMounted --------------------- //
async function handleSubmit() {
  if (isSubmitting.value) return

  try {
    isSubmitting.value = true

    product.value.color = product.value.color === "" ? null : product.value.color

    // Build image info + collect files
    const imagesInfos = []

    // FIRST: Handle removed images (do this ONCE, outside the main loop)
    if (originalProduct.value?.saleItemImages) {
      const removedImages = originalProduct.value.saleItemImages.filter(
        originalImg => !images.value.some(currentImg => 
          currentImg.fileName === originalImg.fileName
        )
      )
      
      // Sort removed images by their original order (process in order)
      removedImages
        .sort((a, b) => a.imageViewOrder - b.imageViewOrder)
        .forEach(removedImg => {
          imagesInfos.push({
            pictureFile: null, // No file needed for removal
            order: removedImg.imageViewOrder,
            status: "remove",
            pictureName: removedImg.fileName
          })
        })
    }

    // SECOND: Handle current images (existing and new)
    // Process all current images and send them with their new positions
    for (let i = 0; i < images.value.length; i++) {
      const image = images.value[i]
      let fileObj
      let status = "add" // Default for new files

      if (image.fileName) {
        // This is an existing image - convert to File
        fileObj = await urlToFile(image.fileName)
        
        // For existing images, always use "replace" so backend can handle reordering
        status = "replace"
      } else if (image.file) {
        // This is a new uploaded file
        fileObj = image.file
        status = "add"
      }

      if (fileObj) {
        imagesInfos.push({
          pictureFile: fileObj,
          order: i + 1, // New position in the reordered array
          status: status,
          pictureName: image.fileName || image.name
        })
      }
    }

    // IMPORTANT: Sort the imagesInfos array to process in the correct order
    // Process removes first (in original order), then adds and replaces (in new order)
    imagesInfos.sort((a, b) => {
      // Removes first (by original order)
      if (a.status === "remove" && b.status === "remove") {
        return a.order - b.order
      }
      if (a.status === "remove") return -1
      if (b.status === "remove") return 1
      
      // Then adds and replaces by new order
      return a.order - b.order
    })

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
        }).then(() => {
          // Refresh the page to show updated data
          router.go(0)
        })
      } else {
        router.push({ name: "ListSaleItems", query: { edited: "true" } })
      }
    } else {
      alert("Fail to Edit Sale Item")
      isSubmitting.value = false
    }

  } catch (error) {
    console.error("Error:", error)
    isSubmitting.value = false
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
        <router-link
          :to="{ name: 'ListDetails', params: { id: product.id } }"
          class="itbms-back-button text-gray-200 font-semibold"
        >
          <span>{{ product.model }}</span
          >&thinsp; <span>{{ product.ramGb ?? "" }}</span
          >/<span>{{ product.storageGb ?? "" }}</span
          >GB
          <span>{{ product.color ?? "" }}</span>
        </router-link>
      </div>

      <!-- Title -->
      <h1 class="text-3xl font-bold text-white mb-6">
        <span>{{ product.model }}</span
        >&thinsp; <span>{{ product.ramGb ?? "" }}</span
        >/<span>{{ product.storageGb ?? "" }}</span
        >GB
        <span>{{ product.color ?? "" }}</span>
      </h1>

      <!-- Main -->
      <div class="bg-gray-800 rounded-2xl shadow-2xl py-8 px-8 border border-gray-700">
        <form
          class="grid grid-cols-1 md:grid-cols-2 gap-10"
          @submit.prevent="handleSubmit"
        >
          <div class="flex flex-col">
            <!-- Image Section -->
            <div class="flex flex-col items-center space-y-4">
              <!-- Main Display Image -->
              <div class="relative w-full">
                <div class="aspect-[3/4] bg-gray-700 rounded-lg overflow-hidden">
                  <img
                    :src="mainDisplayImage"
                    class="w-full h-full object-cover"
                    :alt="images.length > 0 ? 'Product image' : 'Default phone image'"
                  />
                </div>
                
                <!-- Image indicator if there are uploaded images -->
                <div 
                  v-if="images.length > 0" 
                  class="absolute top-2 right-2 bg-black bg-opacity-50 text-white px-2 py-1 rounded text-xs"
                >
                  {{ selectedImageIndex + 1 }} / {{ images.length }}
                </div>
              </div>
              
              <!-- Image Thumbnails for Selection with 9:16 aspect ratio -->
              <div class="flex space-x-8 items-center justify-center max-w-sm w-full">
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
                images.length >= 4
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
              :disabled="images.length >= 4"
              @change="handleFileChange"
            />
            
            <!-- Uploaded Image -->
            <div class="flex flex-col gap-3 mt-3 max-w-3/5 md:max-w-1/2">
              <span
                v-if="images.length > 0"
                v-for="(image, index) in images"
                :key="index"
                class="inline-flex items-center justify-between gap-3 
                bg-gray-800 border border-purple-600 text-gray-200 
                px-4 py-1.5 rounded-full text-sm font-medium shadow-sm"
              >
                <!-- Filename with truncate -->
                <span class="truncate min-w-0 max-w-[200px]">
                  {{ getImageName(image) }}
                </span>

                <!-- Button Right Side -->
                <div class="flex gap-2 items-center">
                  <!-- Remove Image -->
                  <button 
                    class="flex-shrink-0 hover:text-red-500 -mb-1"
                    @click.stop="removeImage(getImageName(image))"
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
                      :class="index === 0 ? 'cursor-not-allowed text-gray-500' : 'hover:text-purple-400'"
                      @click="moveImageUp(index)"
                      type="button"
                    >
                      <span class="material-icons text-base">arrow_drop_up</span>
                    </button>

                    <!-- Swap Down -->
                    <button
                      class="flex-shrink-0"
                      :class="index === images.length - 1 ? 'cursor-not-allowed text-gray-500' : 'hover:text-purple-400'"
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
              <label class="block mb-1 font-semibold text-gray-200">Brand</label>
              <select
                v-model="product.brandName"
                @blur="validateInputSaleItem(product, 'brandName', validationMessages)"
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
                v-model.trim="product.model"
                @blur="validateInputSaleItem(product, 'model', validationMessages)"
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
                v-model.number="product.price"
                @blur="validateInputSaleItem(product, 'price', validationMessages)"
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
                v-model.trim="product.description"
                @blur="validateInputSaleItem(product, 'description', validationMessages)"
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
                  v-model.number="product.ramGb"
                  @blur="validateInputSaleItem(product, 'ramGb', validationMessages)"
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
                  v-model.number="product.screenSizeInch"
                  @blur="validateInputSaleItem(product, 'screenSizeInch', validationMessages)"
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
                  v-model.number="product.storageGb"
                  @blur="validateInputSaleItem(product, 'storageGb', validationMessages)"
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
                  v-model.trim="product.color"
                  @blur="validateInputSaleItem(product, 'color', validationMessages)"
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
                v-model.number="product.quantity"
                @blur="validateInputSaleItem(product, 'quantity', validationMessages)"
                :ref="el => inputRefs[8] = el"
                @keydown.enter.prevent="validateInputSaleItem(product, 'quantity', validationMessages)"
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
                    ? { name: 'ListDetails', params: { id: product.id } }
                    : { name: 'ListSaleItems' }
                "
                class="itbms-cancel-button px-5 py-2 rounded-lg border border-gray-600 bg-gray-700 text-gray-300 hover:bg-gray-600 transition"
              >
                Cancel
              </router-link>
              <button
                type="submit"
                :disabled="isSaveDisabled || isSubmitting"
                :class="[
                  'itbms-save-button px-5 py-2 rounded-lg transition',
                  isSaveDisabled
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
