//ListSaleItems.vue
<script setup>
import { ref, onMounted, computed, watch } from "vue";
import { deleteItemById, getSellerItemsByToken, getItems } from "@/lib/fetchUtils";
import { handleQueryAlerts, handleDeleteAlerts } from "@/lib/alertMessage";
import { useUser } from "@/composables/useUser";
import FilterAndSort from "./Gallery/FilterAndSort.vue";
import Pagination from "./Gallery/Pagination.vue";
import Search from "./Gallery/Search.vue";
import { getAccessToken } from "@/lib/authUtils";

// Get user data from composable
const { 
  userId, 
  userRole,
  isLoading: userIsLoading, 
  hasCompleteUserData, 
} = useUser();

// Session Keys for Sale Items
const SESSION_KEYS = {
  FILTER_BRANDS: "session_saleItems_filterBrands",
  FILTER_PRICES: "session_saleItems_filterPrices",
  FILTER_STORAGE_SIZES: "session_saleItems_filterStorage",
  SORT_MODE: "session_saleItems_sortMode",
  PAGE_SIZE: "session_saleItems_pageSize",
  CURRENT_PAGE: "session_saleItems_currentPage",
  SEARCH_KEYWORD: "session_saleItems_searchKeyword",
};

// Refs for data
const allProducts = ref([]);
const products = ref([]);
const isLoadingData = ref(false);

// Filter refs
const brands = ref([]);
const filterBrands = ref([]);
const showBrandList = ref(false);
const brandToAdd = ref("");

const filterPrices = ref([]);
const showPriceList = ref(false);
const priceToAdd = ref("");

const filterStorageSizes = ref([]);
const showStorageSizeList = ref(false);
const storageSizeToAdd = ref("");

// Search ref
const search = ref("");

// Sort ref
const sortMode = ref("none");

// Pagination refs
const pageSize = ref(10);
const currentPage = ref(1);
const totalPages = ref(1);

// Modal refs
const showModal = ref(false);
const selectedProductId = ref(null);

// Message refs
const showSuccessMessage = ref(false);
const successMessage = ref("");

// Computed properties
const isLoading = computed(() => {
  return userIsLoading.value || isLoadingData.value;
});

const canLoadData = computed(() => {
  return hasCompleteUserData.value && userId.value;
});

const showContent = computed(() => {
  return canLoadData.value && !isLoading.value;
});

// Visible Pages for pagination
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

// ------------------- Session Management ----------------------- //
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
  sessionStorage.setItem(SESSION_KEYS.FILTER_BRANDS, JSON.stringify(filterBrands.value));
  sessionStorage.setItem(SESSION_KEYS.FILTER_PRICES, JSON.stringify(filterPrices.value));
  sessionStorage.setItem(SESSION_KEYS.FILTER_STORAGE_SIZES, JSON.stringify(filterStorageSizes.value));
  sessionStorage.setItem(SESSION_KEYS.SORT_MODE, sortMode.value);
  sessionStorage.setItem(SESSION_KEYS.PAGE_SIZE, pageSize.value.toString());
  sessionStorage.setItem(SESSION_KEYS.CURRENT_PAGE, currentPage.value.toString());
  sessionStorage.setItem(SESSION_KEYS.SEARCH_KEYWORD, search.value);
}

// ------------------- Data Fetching ------------------//
async function fetchFilteredSaleItems() {
  // Don't fetch if user data isn't available yet
  if (!canLoadData.value) {
    console.log('User data not available yet, skipping fetch');
    return;
  }

  if (currentPage.value < 1) currentPage.value = 1;

  isLoadingData.value = true;

  let url = `${import.meta.env.VITE_APP_URL2}/seller/${userId.value}/sale-item?`;
  const query = [];

  // Add pagination
  query.push("page=" + (currentPage.value - 1));
  if (pageSize.value) query.push("size=" + pageSize.value);

  // Add brand filters
  if (filterBrands.value.length > 0) {
    for (const brand of filterBrands.value) {
      query.push("filterBrands=" + brand);
    }
  }

  // Add price filters
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

  // Add storage filters
  if (filterStorageSizes.value.length > 0) {
    const hasNotSpecify = filterStorageSizes.value.includes("Not Specify");
    const specificStorages = filterStorageSizes.value.filter(storage => storage !== "Not Specify");
    
    if (hasNotSpecify) {
      query.push("filterNullStorage=true");
    }
    
    if (specificStorages.length > 0) {
      for (const storage of specificStorages) {
        query.push("filterStorages=" + storage);
      }
    }
  }

  // Add search keyword
  if (search.value && search.value.trim() !== "") {
    query.push("searchKeyword=" + encodeURIComponent(search.value.trim()));
  }

  // Add sorting
  if (sortMode.value === "none") {
    query.push("sortField=createdOn");
    query.push("sortDirection=asc");
  } else {
    query.push("sortField=brand");
    query.push("sortDirection=" + sortMode.value);
  }

  const finalUrl = url + query.join("&");

  try {
    const token = getAccessToken()

    const data = await getSellerItemsByToken(finalUrl, token);
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

// -------------- Filter Actions ------------------- //
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
    filterPrices.value = filterPrices.value.filter((p) => p !== saleItemPrice);
  } else {
    filterPrices.value.push(saleItemPrice);
  }
  priceToAdd.value = "";
}

function toggleStorageSize(saleItemStorageSize) {
  if (filterStorageSizes.value.includes(saleItemStorageSize)) {
    filterStorageSizes.value = filterStorageSizes.value.filter((s) => s !== saleItemStorageSize);
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

// -------------- Delete Actions ------------------- //
function confirmDelete(id) {
  selectedProductId.value = id;
  showModal.value = true;
}

async function handleDelete() {
  if (!selectedProductId.value || !userId.value) return;

  try {
    const baseUrl = `${import.meta.env.VITE_APP_URL2}/seller/${userId.value}/sale-item`;
    const item = await deleteItemById(baseUrl, selectedProductId.value);
    
    if (typeof item === 'number') {
      showModal.value = false;
      handleDeleteAlerts(showSuccessMessage, successMessage, 'The requested sale item does not exist.', products, baseUrl);
      return;
    }
  } catch (error) {
    console.error('Failed to delete product:', error);
  }

  showModal.value = false;
  handleDeleteAlerts(showSuccessMessage, successMessage, 'The sale item has been deleted.', products, `${import.meta.env.VITE_APP_URL2}/seller/${userId.value}/sale-item`);
  
  // Refresh the filtered results
  await fetchFilteredSaleItems();
}

//  ------------- Watchers ------------------ //
// Watch for user data to become available
watch(canLoadData, (canLoad) => {
  if (canLoad) {
    fetchFilteredSaleItems();
  }
}, { immediate: true });

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

// ------------------- Initialization ------------------- //
onMounted(async () => {
  if(userRole.value !== 'SELLER'){
    router.push({ name: 'ListGallery'})
    return
  }

  loadSession();

  handleQueryAlerts(
    {
      added: 'The sale item has been successfully added.',
      edited: 'The sale item has been edited.',    
    },
    showSuccessMessage,
    successMessage
  );

  // Load brands (this doesn't require user data)
  await fetchBrands();

  // fetchFilteredSaleItems will be called by the watcher when user data is available
});
</script>

<template>
  <div class="p-8 min-h-screen bg-gray-900 text-gray-200">
    <h2 class="text-3xl font-extrabold mb-10 text-center bg-gradient-to-r from-purple-400 to-indigo-500 bg-clip-text text-transparent">
      My Sale Items
    </h2>

    <!-- Loading State -->
    <div v-if="isLoading" class="text-center py-20">
      <div class="animate-spin w-12 h-12 border-4 border-purple-500 border-t-transparent rounded-full mx-auto mb-4"></div>
      <p class="text-gray-400">Loading your sale items...</p>
    </div>

    <!-- Main Content -->
    <template v-else-if="showContent">
      <!-- Search Box -->
      <div class="mb-6 max-w-xl mx-auto">
        <Search
          v-model="search"
          @search="handleSearch"
        />
      </div>

      <!-- Success Message -->
      <transition name="fade">
        <div 
          v-if="showSuccessMessage" 
          class="itbms-message mb-6 p-4 text-green-800 bg-green-100 border border-green-300 rounded-lg shadow-sm text-sm font-medium"
        >
          {{ successMessage }}
        </div>
      </transition>

      <!-- Filter and Sort Component -->
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

      <!-- Action Buttons -->
      <div class="flex justify-between items-center mb-8">
        <router-link
          :to="{ name: 'AddItem', query: { from: 'SaleItem' } }"
          class="itbms-sale-item-add flex items-center gap-2 bg-purple-600 hover:bg-purple-500 text-white pl-3 pr-4 py-3 rounded-xl text-base font-semibold shadow-lg transition duration-300"
        >
          <span class="material-icons">add</span>
          Add Sale Item
        </router-link>

        <router-link
          :to="{ name: 'ListBrands' }"
          class="itbms-manage-brand flex items-center gap-2 bg-gray-700 hover:bg-gray-600 text-gray-200 px-4 py-3 rounded-xl text-base font-semibold shadow-lg transition duration-300"
        >
          <span class="material-icons">build</span> 
          Manage Brand
        </router-link>
      </div>

      <!-- No Items -->
      <div v-if="products.length === 0 && !isLoadingData" class="text-center text-gray-400 text-lg py-10">
        No sale items found.
      </div>

      <!-- Product Table -->
      <div v-else-if="products.length > 0" class="overflow-x-auto shadow-2xl rounded-2xl border border-gray-700 mb-8">
        <table class="min-w-full bg-gray-800 text-sm text-center table-auto rounded-2xl overflow-hidden">
          <thead class="bg-gray-700 text-gray-300 font-semibold uppercase tracking-wide">
            <tr>
              <th class="px-4 py-4 border-b border-gray-600">ID</th>
              <th class="px-4 py-4 border-b border-gray-600">Brand</th>
              <th class="px-4 py-4 border-b border-gray-600">Model</th>
              <th class="px-4 py-4 border-b border-gray-600">RAM</th>
              <th class="px-4 py-4 border-b border-gray-600">Storage</th>
              <th class="px-4 py-4 border-b border-gray-600">Color</th>
              <th class="px-4 py-4 border-b border-gray-600">Price</th>
              <th class="px-4 py-4 border-b border-gray-600">Action</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-700">
            <tr
              v-for="product in products"
              :key="product.id"
              class="itbms-row hover:bg-gray-700/50 transition duration-200"
            >
              <td class="itbms-id px-4 py-3">{{ product.id }}</td>
              <td class="itbms-brand px-4 py-3">{{ product.brandName }}</td>
              <td class="itbms-model px-4 py-3 text-left">{{ product.model }}</td>
              <td class="itbms-ramGb px-4 py-3">{{ product.ramGb ?? "-" }}</td>
              <td class="itbms-storageGb px-4 py-3">{{ product.storageGb ?? "-" }}</td>
              <td class="itbms-color px-4 py-3">{{ product.color ?? "-" }}</td>
              <td class="itbms-price px-4 py-3">{{ product.price.toLocaleString() }}</td>
              <td class="px-4 py-3">
                <div class="flex justify-center gap-2">
                  <router-link 
                    :to="{ name: 'EditItem', params: { id: product.id }, query: { from: 'SaleItem' } }" 
                    class="itbms-edit-button bg-gradient-to-r from-purple-500 to-indigo-600 hover:opacity-90 text-white p-2 rounded-lg shadow transition duration-300"
                  >
                    <span class="material-icons">edit</span>
                  </router-link>
                  <button
                    @click="confirmDelete(product.id)"
                    class="itbms-delete-button bg-red-600 hover:bg-red-700 text-white p-2 rounded-lg shadow transition duration-300"
                  >
                    <span class="material-icons">delete</span>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <Pagination
        v-if="products.length > 0"
        :current-page="currentPage"
        :total-pages="totalPages"
        :visible-pages="visiblePages"
        :go-to-page="goToPage"
      />
    </template>

    <!-- Delete Confirmation Modal -->
    <div
      v-if="showModal"
      class="fixed inset-0 flex items-center justify-center z-50 bg-black/60 backdrop-blur-sm"
    >
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md p-8">
        <h2 class="text-2xl font-bold text-gray-800 mb-4">Confirm Delete</h2>
        <p class="itbms-message text-gray-600 mb-6">
          Do you want to delete this sale item?
        </p>
        <div class="flex justify-end gap-4">
          <button
            @click="showModal = false"
            class="itbms-cancel-button bg-gray-200 hover:bg-gray-300 text-gray-700 px-4 py-2 rounded-lg transition"
          >
            Cancel
          </button>
          <button
            @click="handleDelete"
            class="itbms-confirm-button bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-lg transition"
          >
            Delete
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
