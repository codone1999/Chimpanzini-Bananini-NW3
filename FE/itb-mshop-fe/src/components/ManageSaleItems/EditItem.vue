<script setup>
import { useRoute, useRouter } from "vue-router";
import { ref, onMounted, computed } from "vue";
import { getItemById, getItems, editItem } from "@/lib/fetchUtils";
import { validateInputSaleItem, isFormSaleItemValid } from '@/lib/validateInput'
import phoneImg from "../../../public/phone.png";

const url_brands = `${import.meta.env.VITE_APP_URL}/brands`
const url_items = `${import.meta.env.VITE_APP_URL}/sale-items`

const route = useRoute();
const router = useRouter();
const id = route.params.id;
const from = route.query.from;

const brandSelected = ref(null);

const validationMessages = ref({});

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
  screenSizeInch: 0, // OPTIONAL
  quantity: 0,
  storageGb: 0, // OPTIONAL
  color: "", // OPTIONAL
});
const originalProduct = ref(null);

const isFormValid = computed(() => isFormSaleItemValid(product.value));

const isChanged = computed(() => {
  if (!originalProduct.value) return false;
  return (
    JSON.stringify(product.value) !== JSON.stringify(originalProduct.value)
  );
});

const isSaveDisabled = computed(() => {
  return !isFormValid.value || !isChanged.value;
});


const inputRefs = ref([])

function focusNext(index) {
  inputRefs.value[index + 1]?.focus()
}

async function handleSubmit() {
  try {
    product.value.color = product.value.color === "" ? null : product.value.color;

    const editedItem = await editItem(url_items, id, product.value);
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
    const item = await getItemById(url_items, id);
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
      screenSizeInch: item.screenSizeInch, // OPTIONAL
      quantity: item.quantity,
      storageGb: item.storageGb, // OPTIONAL
      color: item.color,
    };
    product.value = data;
    originalProduct.value = JSON.parse(JSON.stringify(data));
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
                <img :src="phoneImg" class="w-1/5 rounded bg-gray-100 object-cover" />
                <img :src="phoneImg" class="w-1/5 rounded bg-gray-100 object-cover" />
                <img :src="phoneImg" class="w-1/5 rounded bg-gray-100 object-cover" />
                <img :src="phoneImg" class="w-1/5 rounded bg-gray-100 object-cover" />
              </div>
            </div>

            <!-- Upload Image Button -->
            <button class=" mt-5 py-2 w-2/7 border rounded-2xl bg-amber-500 hover:bg-amber-600">
              Upload Images
            </button>
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
