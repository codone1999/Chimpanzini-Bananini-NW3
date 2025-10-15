//ListGallery.vue
<script setup>
import { ref, onMounted, computed, watch } from "vue";
import { useRouter } from "vue-router";
import { getItems, getSellerItemsByToken } from "@/lib/fetchUtils";
import { handleQueryAlerts } from "@/lib/alertMessage";
import { useUser } from "@/composables/useUser";
import { useCart } from "@/composables/useCart";
import { getAccessToken } from "@/lib/authUtils";
import Filter from "./Filter.vue";
import Sort from "./Sort.vue";
import Pagination from "./Pagination.vue";
import phoneImg from "../../../../public/phone.png";
import Search from "./Search.vue";
import ShoppingCart from "./ShoppingCart.vue";

const router = useRouter()

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

const { userId, userRole, isLoading } = useUser();
const { addToCart: addItemToCart, cartItems, syncWithBackend } = useCart()

// Computed property to determine when we can load data
const canLoadData = computed(() => {
  return !isLoading.value || !getAccessToken();
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
  if (isLoading.value) {
    return;
  }

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
    
    // Cart sync is now handled in useUser.js loadCompleteUserData()
    // No need to sync here anymore
    
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

onMounted(async () => {
  loadSession();

  handleQueryAlerts(
    {
      added: "The sale item has been successfully added.",
      deleted: "The sale item has been deleted.",
      failed_delete: "The requested sale item does not exist.",
    },
    showSuccessMessage,
    successMessage
  );

  await fetchBrands();

  fetchFilteredSaleItems();

  syncWithBackend(userId.value)
});

</script>

<template>
  <section class="bg-gray-900 py-16 px-4 md:px-10 min-h-screen">
    <h2 class="text-3xl font-extrabold mb-10 text-center bg-gradient-to-r from-purple-400 to-indigo-500 bg-clip-text text-transparent">
      Shop Our Products
    </h2>

    <div class="flex mb-10 relative max-w-xl mx-auto">
      <Search
        v-model="search"
        @search="handleSearch"
      />
      <div class="absolute -right-16">
        <ShoppingCart />
      </div>
    </div>

    <div
      v-if="showSuccessMessage"
      class="itbms-message mb-6 p-4 text-sm font-medium text-green-800 bg-green-100 border border-green-300 rounded-lg shadow-sm"
      role="alert"
    >
      {{ successMessage }}
    </div>

    <div v-if="isLoading || !canLoadData" class="text-center text-gray-400 py-10">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-purple-500"></div>
      <p class="mt-2">Loading user data...</p>
    </div>

    <template v-else>
      <!-- Layout หลัก: Filter (ซ้าย) + เนื้อหา (ขวา) -->
      <div class="flex flex-col md:flex-row gap-6">
        <!-- Left Content -->
        <div class="md:w-1/4 w-full">
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
        <div class="md:w-3/4 w-full flex flex-col">
          <!-- Filters + Sort + Page Size -->
          <div class="flex items-center gap-3 bg-[#1A1A1D] p-4 rounded-xl shadow-md mb-4">
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

          <!-- List Sale Items -->
          <div v-if="userRole === 'SELLER'" class="mb-10 text-center">
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

          <div v-if="isLoadingData" class="text-center text-gray-400 py-10">
            <div class="inline-block animate-spin rounded-full h-6 w-6 border-b-2 border-purple-500"></div>
            <p class="mt-2">Loading products...</p>
          </div>

          <div
            v-else-if="products.length === 0"
            class="text-center text-gray-500 text-lg py-10"
          >
            no sale item
          </div>

          <div
            v-else
            class="grid grid-cols-2 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-5"
          >
            <div
              v-for="product in products"
              :key="product.id"
              class="itbms-row bg-gray-900 border border-gray-800 rounded-2xl overflow-hidden shadow-md
              hover:shadow-purple-500/30 hover:border-purple-500/60 hover:scale-[1.02]
              transition duration-300 flex flex-col cursor-pointer"
            >
              <router-link
                :to="{ name: 'ListDetails', params: { id: product.id } }"
                class="block flex flex-col flex-grow"
              >
                <img
                  :src="phoneImg"
                  :alt="product.model"
                  class="w-full h-56 object-contain bg-gray-800"
                />

                <div class="p-5 flex flex-col flex-grow text-center">
                  <h3 class="itbms-brand text-xs font-medium text-gray-400 uppercase tracking-wide">
                    {{ product.brandName }}
                  </h3>

                  <p class="text-[#7e5bef] font-semibold mt-2 mb-4 line-clamp-2">
                    <span class="itbms-model">{{ product.model }}</span> /
                    <span class="itbms-ramGb">{{ product.ramGb ?? '-' }}</span>
                    <span class="itbms-ramGb-unit">GB</span> /
                    <span class="itbms-storageGb">{{ product.storageGb ?? '-' }}</span>
                    <span class="itbms-storageGb-unit">GB</span>
                  </p>
                </div>
              </router-link>
              
              <div class="px-4 pb-3 flex items-center justify-between">
                <div class="text-left">
                  <p class="text-white font-bold text-lg">
                    ฿<span class="itbms-price">{{ product.price.toLocaleString() }}</span>
                  </p>
                </div>
                
                <button
                  @click.stop="addToCart(product)"
                  :disabled="product.quantity === 0 || (userId && !canAddToCart(product)) || (userId && userId === product.sellerId)"
                  :class="[
                    'itbms-add-to-cart-button px-4 py-2 rounded-lg font-bold shadow-inner transition whitespace-nowrap',
                    (product.quantity === 0 || (userId && !canAddToCart(product)) || (userId && userId === product.sellerId))
                      ? 'bg-gray-600 text-gray-400 cursor-not-allowed' 
                      : 'bg-gradient-to-r from-purple-500 to-indigo-600 hover:from-purple-400 hover:to-indigo-500 text-white shadow-purple-900/40'
                  ]"
                >
                  <template v-if="product.quantity === 0">
                    Sold Out
                  </template>
                  <template v-else-if="!canAddToCart(product)">
                    Max
                  </template>
                  <template v-else>
                    Add
                  </template>
                </button>
              </div>
            </div>
          </div>

          <Pagination
            :current-page="currentPage"
            :total-pages="totalPages"
            :visible-pages="visiblePages"
            :go-to-page="goToPage"
          />
        </div>
      </div>
    </template>
  </section>
</template>
