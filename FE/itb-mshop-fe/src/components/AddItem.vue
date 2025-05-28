<script setup>
import { onMounted, ref, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { addItem, getItems } from "@/lib/fetchUtils";
import phoneImg from "../../public/phone.png";

const route = useRoute();
const router = useRouter();
const from = route.query.from;

const showValidateMessage = ref(false);
const validateMessage = ref("");
const validateField = ref("");

const brandSelected = ref(null);
const newSaleItem = ref({
  id: null,
  brand: {
    id: null,
    name: "",
  }, // Receive like {id: 1, name: Example}
  brandName: "",
  model: "",
  description: "",
  price: 0,
  quantity: 0,
  screenSizeInch: null, // OPTIONAL
  ramGb: null, // OPTIONAL
  storageGb: null, // OPTIONAL
  color: null, // OPTIONAL
});

const isFormValid = computed(() => {
  return (
    newSaleItem.value.brandName?.trim() !== '' &&

    newSaleItem.value.model.trim().length >= 1 &&
    newSaleItem.value.model.trim().length <= 60 &&

    newSaleItem.value.description.trim().length >= 1 &&
    newSaleItem.value.description.trim().length <= 16384 &&

    (
      typeof newSaleItem.value.price === 'number' &&
      newSaleItem.value.price > 0
    ) &&

    (
      newSaleItem.value.ramGb === null ||
      newSaleItem.value.ramGb === '' ||
      (
        typeof newSaleItem.value.ramGb === 'number' &&
        newSaleItem.value.ramGb > 0
      )
    ) &&

    (
      newSaleItem.value.screenSizeInch === null ||
      newSaleItem.value.screenSizeInch === '' ||
      (
        typeof newSaleItem.value.screenSizeInch === 'number' &&
        newSaleItem.value.screenSizeInch > 0 &&
        /^\d+(\.\d{1,2})?$/.test(String(newSaleItem.value.screenSizeInch))
      )
    ) &&

    (
      newSaleItem.value.storageGb === null ||
      newSaleItem.value.storageGb === '' ||
      (
        typeof newSaleItem.value.storageGb === 'number' &&
        newSaleItem.value.storageGb > 0
      )
    ) &&

    (
      newSaleItem.value.color === null ||
      newSaleItem.value.color.trim() === '' ||
      (
        newSaleItem.value.color.trim().length >= 1 &&
        newSaleItem.value.color.trim().length <= 40
      )
    ) &&

    (
      typeof newSaleItem.value.quantity === 'number' &&
      newSaleItem.value.quantity > 0
    )
  );
});


function validateInput(field) {
  const value = newSaleItem.value[field];
  let message = '';

  switch (field) {
    case 'brandName':
      message = value.trim() !== ''
        ? ''
        : 'Brand must be selected.';
      break;
    case 'model':
      message = value.trim().length >= 1 && value.trim().length <= 60
        ? ''
        : 'Model must be 1-60 characters long.'; 
      break;
    case 'description':
      message = value.trim().length >= 1 && value.trim().length <= 16384
        ? ''
        : 'Description must be 1-16,384 characters long.';
      break;
    case 'price':
      message = typeof value === 'number' && value > 0
        ? ''
        : 'Price must be non-negative integer.';
      break;
    case 'ramGb':
      message = value === null || typeof value === 'number' && value > 0
        ? ''
        : 'RAM size must be positive integer or not specified.';
      break;
    case 'screenSizeInch':
      message = value === null || ( typeof value === 'number' && value > 0 && /^\d+(\.\d{1,2})?$/.test( String(value) ) )
        ? ''
        : 'Screen size must be positive number with at most 2 decimal points or not specified.';
      break;
    case 'storageGb':
      message = value === null || typeof value === 'number' && value > 0
        ? ''
        : 'Storage size must be positive integer or not specified.';
      break;
    case 'color':
      message =
        value === null || value.trim() === '' || (value.trim().length >= 1 && value.trim().length <= 40)
          ? ''
          : 'Color must be 1-40 characters long or not specified.';
      break;
    case 'quantity':
      message = typeof value === 'number' && value > 0
        ? ''
        : 'Quantity must be non-negative integer.';
      break;
  }
  
  if (message) {
    // Field is invalid
    validateField.value = field;
    validateMessage.value = message;
    showValidateMessage.value = true;
  } else {
    // Field became valid — only clear message if it's the one that caused the error
    if (validateField.value === field) {
      validateMessage.value = '';
      showValidateMessage.value = false;
      validateField.value = '';
    }
  }
}

async function handleSubmit() {
  try {
    const addedItem = await addItem(
      "http://intproj24.sit.kmutt.ac.th/nw3/api/v1/sale-items",
      newSaleItem.value
    );
    if (addedItem) {
      if (from === "Gallery")
        router.push({ name: "ListGallery", query: { added: "true" } });
      else router.push({ name: "ListSaleItem", query: { added: "true" } });
    }
  } catch (error) {
    console.error("Error:", error);
  }
}

onMounted(async () => {
  try {
    const brand = await getItems(
      "http://intproj24.sit.kmutt.ac.th/nw3/api/v1/brands"
    );
    if (!brand || brand?.status === 404) {
      // alert('The requested sale brand does not exist.')
      return;
    }
    brandSelected.value = brand;
  } catch (error) {
    console.error("Failed to fetch newSaleItem:", error);
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
        <span class="text-gray-800 font-semibold">New Sale Item</span>
      </div>

      <!-- Title -->
      <h1 class="text-3xl font-bold text-gray-900 mb-8">Add New newSaleItem</h1>

      <!-- Pop Up Message -->
      <div
        v-if="showValidateMessage"
        class="itbms-message mb-6 p-4 text-sm font-medium text-red-800 bg-red-100 border border-red-300 rounded-lg shadow-sm"
      >
        {{ validateMessage }}
      </div>

      <!-- Main -->
      <div class="bg-white rounded-2xl shadow-xl p-10">
        <form
          class="grid grid-cols-1 md:grid-cols-2 gap-10"
          @submit.prevent="handleSubmit"
        >
          <!-- Image Section -->
          <div class="flex flex-col items-center space-y-4">
            <img
              :src="phoneImg"
              class="w-100 h-125 object-cover bg-gray-200 rounded-lg"
            />
            <div class="flex space-x-3">
              <img :src="phoneImg" class="w-20 h-25 rounded bg-gray-100 object-cover" />
              <img :src="phoneImg" class="w-20 h-25 rounded bg-gray-100 object-cover" />
              <img :src="phoneImg" class="w-20 h-25 rounded bg-gray-100 object-cover" />
              <img :src="phoneImg" class="w-20 h-25 rounded bg-gray-100 object-cover" />
            </div>
          </div>

          <!-- Form Fields -->
          <div class="space-y-6">
            <div>
              <label class="block mb-1 font-semibold text-gray-700"
                >Brand</label
              >
              <select
                v-model="newSaleItem.brandName"
                @blur="validateInput('brandName')"
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
            </div>

            <div>
              <label class="block mb-1 font-semibold text-gray-700">Model</label>
              <input
                v-model.trim="newSaleItem.model"
                @blur="validateInput('model')"
                type="text"
                class="itbms-model w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500"
                required
              />
            </div>

            <div>
              <label class="block mb-1 font-semibold text-gray-700">Price (Baht)</label>
              <input
                v-model.number="newSaleItem.price"
                @blur="validateInput('price')"
                type="number"
                class="itbms-price w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500"
                required
              />
            </div>

            <div>
              <label class="block mb-1 font-semibold text-gray-700">Description</label>
              <textarea
                v-model.trim="newSaleItem.description"
                @blur="validateInput('description')"
                rows="3"
                class="itbms-description w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500"
                required
              ></textarea>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block mb-1 font-semibold text-gray-700">RAM (GB)</label>
                <input
                  v-model.number="newSaleItem.ramGb"
                  @blur="validateInput('ramGb')"
                  type="number"
                  class="itbms-ramGb w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500"
                />
              </div>
              <div>
                <label class="block mb-1 font-semibold text-gray-700">Screen Size (Inch)</label>
                <input
                  v-model.number="newSaleItem.screenSizeInch"
                  @blur="validateInput('screenSizeInch')"
                  type="number"
                  step="any"
                  class="itbms-screenSizeInch w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500"
                />
              </div>
              <div>
                <label class="block mb-1 font-semibold text-gray-700">Storage (GB)</label>
                <input
                  v-model.number="newSaleItem.storageGb"
                  @blur="validateInput('storageGb')"
                  type="number"
                  class="itbms-storageGb w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500"
                />
              </div>
              <div>
                <label class="block mb-1 font-semibold text-gray-700">Color</label>
                <input
                  v-model.trim="newSaleItem.color"
                  @blur="validateInput('color')"
                  type="text"
                  class="itbms-color w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500"
                />
              </div>
            </div>

            <div>
              <label class="block mb-1 font-semibold text-gray-700">Quantity</label>
              <input
                v-model.number="newSaleItem.quantity"
                @blur="validateInput('quantity')"
                type="number"
                class="itbms-quantity w-full border border-gray-300 px-3 py-2 rounded focus:ring-2 focus:ring-purple-500"
                required
              />
            </div>

            <!-- Buttons -->
            <div class="flex justify-end space-x-4 pt-6">
              <router-link
                :to="
                  from === 'Gallery'
                    ? { name: 'ListGallery' }
                    : { name: 'ListSaleItem' }
                "
                class="itbms-cancel-button px-5 py-2 border border-gray-300 text-gray-600 rounded hover:bg-gray-100 transition"
              >
                Cancel
              </router-link>
              <button
                type="submit"
                :disabled="!isFormValid"
                :class="[
                  'itbms-save-button px-5 py-2 text-white rounded transition',
                  isFormValid
                    ? 'bg-purple-600 hover:bg-purple-700'
                    : 'bg-purple-300 cursor-not-allowed',
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
