//ListGallery.vue
<script setup>
import { ref, onMounted, computed, watch } from "vue";
import { useRouter } from "vue-router";
import FilterAndSort from "./FilterAndSort.vue";
import Pagination from "./Pagination.vue";
import { getItems, getSellerItemsByToken } from "@/lib/fetchUtils";
import { handleQueryAlerts } from "@/lib/alertMessage";
import phoneImg from "../../../../public/phone.png";
import Search from "./Search.vue";
import { useUser } from "@/composables/useUser";
import { getAccessToken } from "@/lib/authUtils";
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
const showBrandList = ref(false);
const brandToAdd = ref("");

const filterPrices = ref([])
const showPriceList = ref(false)
const priceToAdd = ref("")

const filterStorageSizes = ref([])
const showStorageSizeList = ref(false)
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

// Computed property to determine when we can load data
const canLoadData = computed(() => {
  // For non-authenticated users, we can load data immediately
  // For authenticated users, wait for user data to be loaded
  return !isLoading.value || !getAccessToken();
});

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
  // Don't fetch if authenticated user data isn't available yet
  if (isLoading.value) {
    return;
  }

  if (currentPage.value < 1) currentPage.value = 1;

  isLoadingData.value = true;

  let url = null;
  
  // Check if user is authenticated and is a seller
  if (getAccessToken() && userRole.value === "SELLER") {
    // Seller-specific endpoint - requires userId
    if (!userId.value) {
      console.warn("Seller user ID not available yet");
      isLoadingData.value = false;
      return;
    }
    url = `${import.meta.env.VITE_APP_URL2}/seller/${userId.value}/sale-item?`;
  } else {
    // Public endpoint for everyone else (non-authenticated users and buyers)
    url = `${import.meta.env.VITE_APP_URL2}/sale-items?`;
  }

  const query = [];

  // Add pagination (required parameter)
  query.push("page=" + (currentPage.value - 1));
  if (pageSize.value) query.push("size=" + pageSize.value);

  // Add brand filters (matches Spring's filterBrands parameter)
  if (filterBrands.value.length > 0) {
    for (const brand of filterBrands.value) {
      query.push("filterBrands=" + encodeURIComponent(brand));
    }
  }

  // Add price filters (matches Spring's filterPriceLower/filterPriceUpper)
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

  // Add storage filters (matches Spring's filterStorages parameter)
  if (filterStorageSizes.value.length > 0) {
    const hasNotSpecify = filterStorageSizes.value.includes("Not Specify");
    const specificStorages = filterStorageSizes.value.filter(storage => storage !== "Not Specify");
    
    // Handle null storage filter (matches Spring's filterNullStorage)
    if (hasNotSpecify) {
      query.push("filterNullStorage=true");
    }
    
    // Add specific storage values
    if (specificStorages.length > 0) {
      for (const storage of specificStorages) {
        query.push("filterStorages=" + encodeURIComponent(storage));
      }
    }
  }

  // Add search keyword (matches Spring's searchKeyword parameter)
  if (search.value && search.value.trim() !== "") {
    query.push("searchKeyword=" + encodeURIComponent(search.value.trim()));
  }

  // Add sorting (matches Spring's sortField and sortDirection parameters)
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
    
    // Use appropriate fetch method based on endpoint
    if (getAccessToken() && userRole.value === "SELLER") {
      data = await getSellerItemsByToken(finalUrl, getAccessToken());
    } else {
      data = await getItems(finalUrl);
    }

    if (typeof data === 'number') {
      alert("Failed To Fetch Sale Items");
      return;
    }

    // Handle both paginated and non-paginated responses
    if (data.content && Array.isArray(data.content)) {
      // Paginated response
      allProducts.value = data.content;
      products.value = [...data.content];
      totalPages.value = data.totalPages || 1;
    } else if (Array.isArray(data)) {
      // Non-paginated response - simulate pagination
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
// Load brands for filtering
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

// Handle search action from Search component
function handleSearch(searchKeyword) {
  search.value = searchKeyword;
  currentPage.value = 1; // Reset to first page when searching
  saveSession(); // Save the new search term
  fetchFilteredSaleItems(); // Fetch with new search term
}

//  ------------- Watchers ------------------ //

// Watch for user data availability and fetch when ready
watch(canLoadData, (newValue) => {
  if (newValue) {
    if (userRole.value !== "SELLER") { router.push({name: "ListGallery"}); return; }
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
  // Load session data first
  loadSession();

  // Handle query alerts
  handleQueryAlerts(
    {
      added: "The sale item has been successfully added.",
      deleted: "The sale item has been deleted.",
      failed_delete: "The requested sale item does not exist.",
    },
    showSuccessMessage,
    successMessage
  );

  // Load brands (this doesn't require user data)
  await fetchBrands();

  fetchFilteredSaleItems();
});

</script>

<template>
  <section class="bg-gray-900 py-16 px-4 md:px-10 min-h-screen">
    <h2 class="text-3xl font-extrabold mb-10 text-center bg-gradient-to-r from-purple-400 to-indigo-500 bg-clip-text text-transparent">
      Shop Our Products
    </h2>

    <!-- Search Box with Cart -->
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

    <!-- Loading indicator while user data is loading -->
    <div v-if="isLoading || !canLoadData" class="text-center text-gray-400 py-10">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-purple-500"></div>
      <p class="mt-2">Loading user data...</p>
    </div>

    <!-- Main content - only show when user data is loaded -->
    <template v-else>
      <FilterAndSort
        v-model:page-size="pageSize"
        :brands="brands"
        :filter-brands="filterBrands"
        :show-brand-list="showBrandList"
        :toggle-brand-list="() => showBrandList = !showBrandList"
        :on-toggle-brand="toggleBrand"
        :on-clear-brands="clearAllFilters"

        :sale-items="products"

        :filter-prices="filterPrices"
        :show-price-list="showPriceList"
        :toggle-price-list="() => showPriceList = !showPriceList"
        :on-toggle-price="togglePrice"

        :filter-storage-sizes="filterStorageSizes"
        :show-storage-size-list="showStorageSizeList"
        :toggle-storage-size-list="() => showStorageSizeList = !showStorageSizeList"
        :on-toggle-storage-size="toggleStorageSize"

        :on-change-sort="changeSort"
        :sort-mode="sortMode"
        :page-size="pageSize"
      />

      <!-- Add Item Button -->
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

      <!-- Loading indicator for data fetching -->
      <div v-if="isLoadingData" class="text-center text-gray-400 py-10">
        <div class="inline-block animate-spin rounded-full h-6 w-6 border-b-2 border-purple-500"></div>
        <p class="mt-2">Loading products...</p>
      </div>

      <!-- No Products -->
      <div
        v-else-if="products.length === 0"
        class="text-center text-gray-500 text-lg py-10"
      >
        no sale item
      </div>

      <!-- Product Grid -->
      <div
        v-else
        class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6"
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
            class="block h-full flex flex-col"
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

              <button
                class="itbms-add-to-cart-button mt-auto w-full bg-gradient-to-r from-purple-500 to-indigo-600 
                hover:from-purple-400 hover:to-indigo-500 text-white py-2 rounded-lg font-bold 
                shadow-inner shadow-purple-900/40 transition"
              >
                Baht <span class="itbms-price">{{ product.price.toLocaleString() }}</span>
              </button>
            </div>
          </router-link>
        </div>
      </div>

      <Pagination
        :current-page="currentPage"
        :total-pages="totalPages"
        :visible-pages="visiblePages"
        :go-to-page="goToPage"
      />
    </template>
  </section>
</template>
