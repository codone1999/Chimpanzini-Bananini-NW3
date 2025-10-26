//ListGallery.vue
<script setup>
import { ref, onMounted, computed, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { getItems, getSellerItemsByToken } from "@/lib/fetchUtils";
import { handleQueryAlerts } from "@/lib/alertMessage";
import { useUser } from "@/composables/useUser";
import { useCart } from "@/composables/useCart";
import { getAccessToken } from "@/lib/authUtils";
import phoneImg from "../../../public/phone.png";
import Search from "./controls/Search.vue";
import Filter from "./controls/Filter.vue";
import Sort from "./controls/Sort.vue";
import Pagination from "./controls/Pagination.vue";
import ShoppingCart from "./controls/ShoppingCart.vue";

const router = useRouter()
const route = useRoute()

// Session Keys
const SESSION_KEYS = {
  FILTER_BRANDS: "session_filterBrands",
  FILTER_PRICES: "session_filterPrices",
  FILTER_STORAGE_SIZES: "session_filterStorage",
  SORT_MODE: "session_sortMode",
  PAGE_SIZE: "session_pageSize",
  CURRENT_PAGE: "session_currentPage",
  SEARCH_KEYWORD: "session_searchKeyword",
};

// Refs
const allProducts = ref([]);
const products = ref([]);

const brands = ref([]);
const filterBrands = ref([]);
const brandToAdd = ref("");

const filterPrices = ref([])
const priceToAdd = ref("")

const filterStorageSizes = ref([])
const storageSizeToAdd = ref("")

const search = ref("");

const sortMode = ref("none");

const pageSize = ref(10);
const currentPage = ref(1);
const totalPages = ref(1);

const showSuccessMessage = ref(false);
const successMessage = ref("");

const isLoadingData = ref(false);

const { userId, userRole, isLoading, waitForInit, isInitialized } = useUser();
const { addToCart: addItemToCart, cartItems, syncWithBackend } = useCart()

const canLoadData = computed(() => {
  // Can load data when user initialization is complete
  return isInitialized.value;
});

// Function to get current cart quantity for a product
function getCartQuantity(productId) {
  const cartItem = cartItems.value.find(item => item.id === productId)
  return cartItem ? cartItem.quantity : 0
}

// Function to check if can add more to cart
function canAddToCart(product) {
  const currentCartQty = getCartQuantity(product.id)
  return currentCartQty < product.quantity
}

// Visible Pages
const visiblePages = computed(() => {
  const total = totalPages.value;
  const current = currentPage.value;
  const maxVisible = 10;
  const half = Math.floor(maxVisible / 2);

  let start = Math.max(1, current - half);
  let end = Math.min(total, current + half);

  if (end - start + 1 < maxVisible) {
    if (start === 1) {
      end = Math.min(total, start + maxVisible - 1);
    } else if (end === total) {
      start = Math.max(1, total - maxVisible + 1);
    }
  }

  const pages = [];
  for (let i = start; i <= end; i++) {
    pages.push(i);
  }

  return pages;
});

// ------------------- Session ----------------------- //
function loadSession() {
  const savedBrandFilters = sessionStorage.getItem(SESSION_KEYS.FILTER_BRANDS);
  if (savedBrandFilters) filterBrands.value = JSON.parse(savedBrandFilters);

  const savedPriceFilters = sessionStorage.getItem(SESSION_KEYS.FILTER_PRICES);
  if (savedPriceFilters) filterPrices.value = JSON.parse(savedPriceFilters);
  
  const savedStorageFilters = sessionStorage.getItem(SESSION_KEYS.FILTER_STORAGE_SIZES);
  if (savedStorageFilters) filterStorageSizes.value = JSON.parse(savedStorageFilters);

  const savedSort = sessionStorage.getItem(SESSION_KEYS.SORT_MODE);
  if (savedSort) sortMode.value = savedSort;

  const savedSize = sessionStorage.getItem(SESSION_KEYS.PAGE_SIZE);
  if (savedSize) pageSize.value = parseInt(savedSize);

  const savedPage = sessionStorage.getItem(SESSION_KEYS.CURRENT_PAGE);
  if (savedPage) currentPage.value = parseInt(savedPage);

  const savedSearch = sessionStorage.getItem(SESSION_KEYS.SEARCH_KEYWORD);
  if (savedSearch) search.value = savedSearch;
}

function saveSession() {
  sessionStorage.setItem(SESSION_KEYS.FILTER_BRANDS, JSON.stringify(filterBrands.value))
  sessionStorage.setItem(SESSION_KEYS.FILTER_PRICES, JSON.stringify(filterPrices.value))
  sessionStorage.setItem(SESSION_KEYS.FILTER_STORAGE_SIZES, JSON.stringify(filterStorageSizes.value))

  sessionStorage.setItem(SESSION_KEYS.SORT_MODE, sortMode.value);
  sessionStorage.setItem(SESSION_KEYS.PAGE_SIZE, pageSize.value.toString());
  sessionStorage.setItem(SESSION_KEYS.CURRENT_PAGE, currentPage.value.toString());
  sessionStorage.setItem(SESSION_KEYS.SEARCH_KEYWORD, search.value);
}

// ------------------- Data Fetching ------------------//
async function fetchFilteredSaleItems() {
  if (currentPage.value < 1) currentPage.value = 1;

  isLoadingData.value = true;

  let url = null;

  url = `${import.meta.env.VITE_APP_URL2}/sale-items?`;

  const query = [];

  query.push("page=" + (currentPage.value - 1));
  if (pageSize.value) query.push("size=" + pageSize.value);

  if (filterBrands.value.length > 0) {
    for (const brand of filterBrands.value) {
      query.push("filterBrands=" + encodeURIComponent(brand));
    }
  }

  if (filterPrices.value.length > 0) {
    const allMinPrices = [];
    const allMaxPrices = [];
    
    for (const priceRange of filterPrices.value) {
      const parts = priceRange.split("-");
      if (parts.length === 2) {
        try {
          const min = parseFloat(parts[0].replace(/,/g, ""));
          const max = parseFloat(parts[1].replace(/,/g, ""));
          allMinPrices.push(min);
          allMaxPrices.push(max);
        } catch (error) {
          console.warn("Invalid price range:", priceRange);
        }
      }
    }
    
    if (allMinPrices.length > 0 && allMaxPrices.length > 0) {
      const filterPriceLower = Math.min(...allMinPrices);
      const filterPriceUpper = Math.max(...allMaxPrices);
      query.push("filterPriceLower=" + filterPriceLower);
      query.push("filterPriceUpper=" + filterPriceUpper);
    }
  }

  if (filterStorageSizes.value.length > 0) {
    const hasNotSpecify = filterStorageSizes.value.includes("Not Specify");
    const specificStorages = filterStorageSizes.value.filter(storage => storage !== "Not Specify");
    
    if (hasNotSpecify) {
      query.push("filterNullStorage=true");
    }
    
    if (specificStorages.length > 0) {
      for (const storage of specificStorages) {
        query.push("filterStorages=" + encodeURIComponent(storage));
      }
    }
  }

  if (search.value && search.value.trim() !== "") {
    query.push("searchKeyword=" + encodeURIComponent(search.value.trim()));
  }

  if (sortMode.value === "none") {
    query.push("sortField=createdOn");
    query.push("sortDirection=asc");
  } else {
    query.push("sortField=brand");
    query.push("sortDirection=" + sortMode.value);
  }

  const finalUrl = url + query.join("&");

  try {
    let data = null;
    
    if (getAccessToken() && userRole.value === "SELLER") {
      data = await getSellerItemsByToken(finalUrl, getAccessToken());
    } else {
      data = await getItems(finalUrl);
    }

    if (typeof data === 'number') {
      alert("Failed To Fetch Sale Items");
      return;
    }

    if (data.content && Array.isArray(data.content)) {
      allProducts.value = data.content;
      products.value = [...data.content];
      totalPages.value = data.totalPages || 1;
    } else if (Array.isArray(data)) {
      allProducts.value = data;
      products.value = [...data];
      totalPages.value = 1;
    }

    if (currentPage.value > totalPages.value) {
      currentPage.value = 1;
    }
  } catch (error) {
    console.error("Fetch error:", error);
    products.value = [];
  } finally {
    isLoadingData.value = false;
  }
}

async function fetchBrands() {
  try {
    const item = await getItems(`${import.meta.env.VITE_APP_URL}/brands`);
    if (typeof item === "number") {
      console.error("Failed to fetch brands");
      return;
    }
    brands.value = item.sort((a, b) =>
      a.name.toLowerCase().localeCompare(b.name.toLowerCase())
    );
  } catch (error) {
    console.error("Failed to fetch brands:", error);
  }
}

// -------------- Actions ------------------- //
function toggleBrand(brandName) {
  if (filterBrands.value.includes(brandName)) {
    filterBrands.value = filterBrands.value.filter((b) => b !== brandName);
  } else {
    filterBrands.value.push(brandName);
  }
  brandToAdd.value = "";
}

function togglePrice(saleItemPrice) {
  if (filterPrices.value.includes(saleItemPrice)) {
    filterPrices.value = filterPrices.value.filter((b) => b !== saleItemPrice);
  } else {
    filterPrices.value.push(saleItemPrice);
  }
  priceToAdd.value = "";
}

function toggleStorageSize(saleItemStorageSize) {
  if (filterStorageSizes.value.includes(saleItemStorageSize)) {
    filterStorageSizes.value = filterStorageSizes.value.filter((b) => b !== saleItemStorageSize);
  } else {
    filterStorageSizes.value.push(saleItemStorageSize);
  }
  storageSizeToAdd.value = "";
}

function clearAllFilters() {
  filterBrands.value = [];
  filterPrices.value = [];
  filterStorageSizes.value = [];
  search.value = "";
}

function changeSort(mode) {
  sortMode.value = mode;
}

function goToPage(page) {
  currentPage.value = page;
  fetchFilteredSaleItems();
}

function handleSearch(searchKeyword) {
  search.value = searchKeyword;
  currentPage.value = 1;
  saveSession();
  fetchFilteredSaleItems();
}

async function addToCart(product) {
  if (!userId.value) {
    router.push({ name: 'Login' })
    return
  }
  
  if (userId.value === product.sellerId) {
    alert("You cannot add your own items to cart")
    return
  }
  
  if (product.quantity === 0) {
    alert("This item is out of stock")
    return
  }
  
  const currentCartQty = getCartQuantity(product.id)
  if (currentCartQty >= product.quantity) {
    alert(`You already have the maximum available quantity (${product.quantity}) in your cart`)
    return
  }
  
  // Pass userId to sync with backend
  await addItemToCart(product, 1, userId.value)
}

//  ------------- Watchers ------------------ //

watch(canLoadData, async (newValue) => {
  if (newValue) {
    if (userRole.value !== "SELLER") { 
      router.push({name: "ListGallery"}); 
      return; 
    }
    
    fetchFilteredSaleItems();
  }
});

watch(
  [filterBrands, filterPrices, filterStorageSizes, sortMode, pageSize, currentPage],
  () => {
    if (canLoadData.value) {
      saveSession();
      fetchFilteredSaleItems();
    }
  },
  { deep: true }
);

watch(brandToAdd, (value) => {
  if (value && !filterBrands.value.includes(value)) {
    toggleBrand(value);
  }
});

watch(priceToAdd, (value) => {
  if (value && !filterPrices.value.includes(value)) {
    togglePrice(value);
  }
});

watch(storageSizeToAdd, (value) => {
  if (value && !filterStorageSizes.value.includes(value)) {
    toggleStorageSize(value);
  }
});

// Watch for userId changes (login/logout) and refetch items
watch(userId, async (newUserId, oldUserId) => {
  if (newUserId !== oldUserId) {
    await fetchFilteredSaleItems();
    
    if (newUserId) {
      await syncWithBackend(newUserId);
    }
  }
});

onMounted(async () => {
  loadSession();

  await waitForInit();

  handleQueryAlerts(
    route,
    router,
    {
      added: "The sale item has been successfully added.",
      deleted: "The sale item has been deleted.",
      failed_delete: "The requested sale item does not exist.",
    },
    showSuccessMessage,
    successMessage
  );

  await fetchBrands();

  await fetchFilteredSaleItems();

  if (userId.value) {
    await syncWithBackend(userId.value);
  }
});

</script>

<template>
  <section class="bg-gray-900 py-16 px-4 md:px-10 min-h-screen">
    <h2 class="text-3xl font-extrabold mb-10 text-center bg-gradient-to-r from-purple-400 to-indigo-500 bg-clip-text text-transparent">
      Shop Our Products
    </h2>

    <!-- Search & Cart -->
    <div class="flex items-center justify-center sm:gap-3 mb-10 px-3 sm:px-4">
      <div class="flex items-center gap-2 sm:gap-7 w-full max-w-2xl">
        <div class="flex-1 min-w-0">
          <Search v-model="search" @search="handleSearch" />
        </div>
        <div class="flex-shrink-0">
          <ShoppingCart />
        </div>
      </div>
    </div>

    <!-- Alert Message -->
    <div
      v-if="showSuccessMessage"
      class="itbms-message mb-6 p-4 text-sm font-medium text-green-800 bg-green-100 border border-green-300 rounded-lg shadow-sm"
      role="alert"
    >
      {{ successMessage }}
    </div>

    <!-- Loading -->
    <div v-if="isLoading || !canLoadData" class="text-center text-gray-400 py-10">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-purple-500"></div>
      <p class="mt-2">Loading user data...</p>
    </div>

    <template v-else>
        <!-- Filter -Left Side & Content - Right Side -->
        <div class="flex flex-col md:flex-row">
          <!-- Left Content -->
          <div class="md:w-1/5 w-full">
            <Filter
              :brands="brands"
              :filter-brands="filterBrands"
              :on-toggle-brand="toggleBrand"
              :on-clear-brands="clearAllFilters"
  
              :sale-items="products"
  
              :filter-prices="filterPrices"
              :on-toggle-price="togglePrice"
  
              :filter-storage-sizes="filterStorageSizes"
              :on-toggle-storage-size="toggleStorageSize"
            />
          </div>
          <!-- Right Content -->
          <div class="md:w-4/5 w-full flex flex-col">
            <!-- Filters + Sort + Page Size -->
            <div class="flex items-center gap-3 bg-gray-950/70 border border-gray-800 rounded-2xl shadow-lg p-4 mb-4 fade-in">
              <!-- Merged Filters Display -->
              <div 
                v-if="filterBrands.length > 0 || filterPrices.length > 0 || filterStorageSizes.length > 0" 
                class="flex flex-wrap items-center gap-2"
              >
                
                <!-- Brand Filters -->
                <div
                  v-for="brand in filterBrands"
                  :key="`brand-${brand}`"
                  class="inline-flex items-center gap-1 bg-blue-600 text-white px-3 py-1 rounded-full text-xs font-medium shadow-md"
                >
                  {{ brand }}
                  <button
                    @click.stop="toggleBrand(brand)"
                    class="hover:text-red-300 transition -mb-0.5"
                  >
                    <span class="material-icons text-sm">close</span>
                  </button>
                </div>
                
                <!-- Price Filters -->
                <div
                  v-for="price in filterPrices"
                  :key="`price-${price}`"
                  class="inline-flex items-center gap-1 bg-green-600 text-white px-3 py-1 rounded-full text-xs font-medium shadow-md"
                >
                  ฿{{ price }}
                  <button
                    @click.stop="togglePrice(price)"
                    class="hover:text-red-300 transition -mb-0.5"
                  >
                    <span class="material-icons text-sm">close</span>
                  </button>
                </div>
                
                <!-- Storage Filters -->
                <div
                  v-for="storage in filterStorageSizes"
                  :key="`storage-${storage}`"
                  class="inline-flex items-center gap-1 bg-purple-600 text-white px-3 py-1 rounded-full text-xs font-medium shadow-md"
                >
                  {{ storage === "Not Specify" ? "Not Specify" : (storage >= 1 && storage < 10 ? `${storage}TB` : `${storage}GB`) }}
                  <button
                    @click.stop="toggleStorageSize(storage)"
                    class="hover:text-red-300 transition -mb-0.5"
                  >
                    <span class="material-icons text-sm">close</span>
                  </button>
                </div>
                
              </div>
  
              <!-- Page Size & Sort -->
              <Sort
                :page-size="pageSize"
                :sort-mode="sortMode"
                @update:pageSize="pageSize = $event"
                @update:sortMode="changeSort($event)"
              />
            </div>
  
            <!-- Add Items Button (ONLY SELLER) -->
            <div v-if="userRole === 'SELLER'" class="mb-4 text-center">
              <router-link
                :to="{ name: 'AddItem', query: { from: 'Gallery' } }"
                class="itbms-sale-item-add inline-flex items-center gap-2 bg-gradient-to-r from-purple-500 to-indigo-600 
                hover:opacity-90 text-white px-5 py-3 rounded-xl text-base font-semibold shadow-lg 
                transition duration-300"
              >
                <div class="flex justify-between gap-1">
                  <span class="material-icons">add</span>
                  Add Item
                </div>
              </router-link>
            </div>
  
            <!-- Loading -->
            <div v-if="isLoadingData" class="text-center text-gray-400 py-10">
              <div class="inline-block animate-spin rounded-full h-6 w-6 border-b-2 border-purple-500"></div>
              <p class="mt-2">Loading products...</p>
            </div>
  
            <!-- No Sale Item -->
            <div
              v-else-if="products.length === 0"
              class="text-center text-gray-500 text-lg py-10"
            >
              no sale item
            </div>
  
            <!-- List Sale Items -->
            <div v-else class="grid grid-cols-2 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-5">
              <div
                v-for="(product, index) in products"
                :key="product.id"
                class="itbms-row bg-gray-900 border border-gray-800 rounded-2xl overflow-hidden shadow-md
                hover:shadow-purple-500/30 hover:border-purple-500/60 hover:scale-[1.02]
                transition duration-300 flex flex-col cursor-pointer animate-product relative"
                :style="{ '--delay': `${index * 50}ms` }"
              >
                <!-- "My Item" Icon - Top Right -->
                <div
                  v-if="userId && userId === product.sellerId"
                  class="absolute top-1.5 right-1.5 z-15 bg-gradient-to-r from-yellow-400 to-orange-500 
                  text-gray-900 p-1 rounded-lg text-xs font-bold shadow-lg 
                  flex items-center gap-1"
                >
                  <span class="material-symbols-outlined">person_edit</span>
                </div>
                <!-- Out of Stock Overlay -->
                <div
                  v-if="product.quantity === 0"
                  class="absolute inset-0 bg-gray-900/70 z-10 flex items-center justify-center rounded-2xl"
                >
                  <div class="text-center">
                    <p class="text-gray-300 text-xl font-bold tracking-wider">OUT OF STOCK</p>
                  </div>
                </div>
  
                <!-- Main Data -->
                <router-link
                  :to="{ name: 'ListDetails', params: { id: product.id } }"
                  class="flex flex-col flex-grow"
                  :class="{ 'pointer-events-none': product.quantity === 0 }"
                >
                  <!-- Image -->
                  <img
                    :src="phoneImg"
                    :alt="product.model"
                    class="w-full h-56 object-contain bg-gray-800"
                  />
  
                  <!-- Content -->
                  <div class="pt-3 px-5 flex flex-col flex-grow text-center">
                    <h3 class="itbms-brand text-xl md:text-base font-bold text-[#7e5bef] uppercase tracking-wide mb-1">
                      {{ product.brandName }}
                    </h3>
  
                    <p class="text-white font-semibold text-base line-clamp-2">
                      <span class="itbms-model">{{ product.model }}</span> <br>
                      <span class="itbms-ramGb text-gray-400 font-normal">{{ product.ramGb ?? '-' }}</span>
                      <span class="itbms-ramGb-unit text-gray-400 font-normal">GB</span> /
                      <span class="itbms-storageGb text-gray-400 font-normal">{{ product.storageGb ?? '-' }}</span>
                      <span class="itbms-storageGb-unit text-gray-400 font-normal">GB</span>
                    </p>
                  </div>
                </router-link>
                <hr class="w-4/5 mx-auto mt-3 pb-3 text-gray-600/40 border-t-2">
  
                <!-- Price & Add To Cart Button -->
                <div class="px-6 pb-4 flex items-center justify-between">
                  <!-- Price -->
                  <span class="text-white font-extrabold text-xl">
                    ฿ <span class="itbms-price">{{ product.price.toLocaleString() }}</span>
                  </span>
                  
                  <!-- Only show Add button if NOT out of stock -->
                  <button
                    @click.stop="addToCart(product)"
                    :disabled="(userId && !canAddToCart(product)) || (userId && userId === product.sellerId)"
                    :class="[
                      'itbms-add-to-cart-button px-3 py-1.5 rounded-lg font-bold shadow-inner transition whitespace-nowrap',
                      ((userId && !canAddToCart(product)) || (userId && userId === product.sellerId) || (product.quantity <= 0))
                        ? 'bg-gray-600 text-gray-400 cursor-not-allowed' 
                        : 'bg-gradient-to-r from-purple-500 to-indigo-600 hover:from-purple-400 hover:to-indigo-500 text-white shadow-purple-900/40'
                    ]"
                  >
                    <!-- Out of Stock -->
                    <template v-if="product.quantity <= 0">
                      <span class="material-symbols-outlined">shopping_cart_off</span>
                    </template>
                    <!-- [Max] Add to cart -->
                    <template v-else-if="!canAddToCart(product)">
                      Max
                    </template>
                    <!-- Add To Cart -->
                    <template v-else>
                      <span class="material-symbols-outlined align-middle">add_shopping_cart</span>
                    </template>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- Pagination -->
        <div class="flex justify-center w-full mt-12">
          <Pagination
            v-if="products.length > 0"
            :current-page="currentPage"
            :total-pages="totalPages"
            :visible-pages="visiblePages"
            :go-to-page="goToPage"
          />
        </div>
    </template>
  </section>
</template>

<style>
@keyframes fadeScale {
  0% {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.animate-product {
  animation: fadeScale 0.4s ease forwards;
  animation-delay: var(--delay);
}

.fade-in {
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
