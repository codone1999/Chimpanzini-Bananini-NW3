<script setup>
import { ref, onMounted, computed, watch } from "vue";
import FilterAndSort from "./FilterAndSort.vue";
import Pagination from "./Pagination.vue";
import { getItems } from "@/lib/fetchUtils";
import { handleQueryAlerts } from "@/lib/alertMessage";
import phoneImg from "../../../../public/phone.png";

// Session Keys
const SESSION_KEYS = {
  FILTER_BRANDS: "session_filterBrands",
  SORT_MODE: "session_sortMode",
  PAGE_SIZE: "session_pageSize",
  CURRENT_PAGE: "session_currentPage",
};

// Refs
const allProducts = ref([]);
const products = ref([]);
const brands = ref([]);

const filterBrands = ref([]);
const showBrandList = ref(false);
const sortMode = ref("none");
const brandToAdd = ref("");

const pageSize = ref(10);
const currentPage = ref(1);
const totalPages = ref(1);

const showSuccessMessage = ref(false);
const successMessage = ref("");

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

// Session
function loadSession() {
  const savedFilters = sessionStorage.getItem(SESSION_KEYS.FILTER_BRANDS);
  if (savedFilters) filterBrands.value = JSON.parse(savedFilters);

  const savedSort = sessionStorage.getItem(SESSION_KEYS.SORT_MODE);
  if (savedSort) sortMode.value = savedSort;

  const savedSize = sessionStorage.getItem(SESSION_KEYS.PAGE_SIZE);
  if (savedSize) pageSize.value = parseInt(savedSize);

  const savedPage = sessionStorage.getItem(SESSION_KEYS.CURRENT_PAGE);
  if (savedPage) currentPage.value = parseInt(savedPage);
}

function saveSession() {
  sessionStorage.setItem(
    SESSION_KEYS.FILTER_BRANDS,
    JSON.stringify(filterBrands.value)
  );
  sessionStorage.setItem(SESSION_KEYS.SORT_MODE, sortMode.value);
  sessionStorage.setItem(SESSION_KEYS.PAGE_SIZE, pageSize.value.toString());
  sessionStorage.setItem(
    SESSION_KEYS.CURRENT_PAGE,
    currentPage.value.toString()
  );
}

// Data Fetching
async function fetchFilteredSaleItems() {
  if (currentPage.value < 1) currentPage.value = 1;

  let url = `${import.meta.env.VITE_APP_URL2}/sale-items?`;
  const query = [];

  query.push("page=" + (currentPage.value - 1));

  if (filterBrands.value.length > 0) {
    for (const brand of filterBrands.value) {
      query.push("filterBrands=" + brand);
    }
  }

  if (pageSize.value) query.push("size=" + pageSize.value);

  if (sortMode.value === "none") {
    query.push("sortField=createdOn");
    query.push("sortDirection=asc");
  } else {
    query.push("sortField=brand.name");
    query.push("sortDirection=" + sortMode.value);
  }

  url += query.join("&");

  try {
    const data = await getItems(url);
    if (typeof data === 'number'){
      alert("Fail to Fetch Sale Items")
      return
    }
    allProducts.value = data.content;
    products.value = [...data.content];
    totalPages.value = data.totalPages;

    if (currentPage.value > totalPages.value) {
      currentPage.value = 1;
    }
  } catch (error) {
    console.error("Fetch error:", error);
  }
}

// Actions
function toggleBrand(brandName) {
  if (filterBrands.value.includes(brandName)) {
    filterBrands.value = filterBrands.value.filter((b) => b !== brandName);
  } else {
    filterBrands.value.push(brandName);
  }
  brandToAdd.value = "";
}

function clearBrandFilters() {
  filterBrands.value = [];
}

function changeSort(mode) {
  sortMode.value = mode;
}

function goToPage(page) {
  currentPage.value = page;
  fetchFilteredSaleItems();
}

// Watchers
watch(
  [filterBrands, sortMode, pageSize, currentPage],
  () => {
    saveSession();
    fetchFilteredSaleItems();
  },
  { deep: true }
);

watch(brandToAdd, (value) => {
  if (value && !filterBrands.value.includes(value)) {
    toggleBrand(value);
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

  await fetchFilteredSaleItems();

  try {
    const item = await getItems(`${import.meta.env.VITE_APP_URL}/brands`);
    if (typeof item === "number") {
      alert("Failed to fetch brands");
      return;
    }
    brands.value = item.sort((a, b) =>
      a.name.toLowerCase().localeCompare(b.name.toLowerCase())
    );
  } catch (error) {
    console.error("Failed to fetch product:", error);
  }
});
</script>

<template>
  <section class="bg-gray-100 py-16 px-4 md:px-10">
    <h2 class="text-3xl font-extrabold text-gray-900 mb-10 text-center">
      Shop Our Products
    </h2>

    <div
      v-if="showSuccessMessage"
      class="itbms-message mb-6 p-4 text-sm font-medium text-green-800 bg-green-100 border border-green-300 rounded-lg shadow-sm"
      role="alert"
    >
      {{ successMessage }}
    </div>

    <FilterAndSort
      v-model:page-size="pageSize"
      :brands="brands"
      :filter-brands="filterBrands"
      :brand-to-add="brandToAdd"
      :sort-mode="sortMode"
      :page-size="pageSize"
      :show-brand-list="showBrandList"
      :toggle-brand-list="() => showBrandList = !showBrandList"
      :onToggleBrand="toggleBrand"
      :onClearBrands="clearBrandFilters"
      :onChangeSort="changeSort"
    />

    <!-- Add Item Button -->
    <div class="mb-8 text-center">
      <router-link
        :to="{ name: 'AddItem', query: { from: 'Gallery' } }"
        class="itbms-sale-item-add inline-block bg-[#7e5bef] hover:bg-[#6847d5] text-white pl-3 pr-4 py-4 rounded-xl text-base font-semibold shadow-lg transition duration-300"
      >
        <div class="flex justify-between gap-1">
          <span class="material-icons">add</span>
          Add Item
        </div>
      </router-link>
    </div>

    <!-- No Products -->
    <div
      v-if="products.length === 0"
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
        class="itbms-row bg-white rounded-2xl border border-gray-200 overflow-hidden shadow-md hover:shadow-xl transition duration-300 flex flex-col"
      >
        <router-link
          :to="{ name: 'ListDetails', params: { id: product.id } }"
          class="block h-full flex flex-col"
        >
          <img
            :src="phoneImg"
            :alt="product.model"
            class="w-full h-56 object-contain bg-gray-300"
          />

          <div class="p-5 flex flex-col flex-grow text-center">
            <h3 class="itbms-brand text-sm font-medium text-gray-500 uppercase tracking-wide">
              {{ product.brandName }}
            </h3>

            <p class="text-[#7e5bef] font-semibold mt-1 mb-2 line-clamp-2">
              <span class="itbms-model">{{ product.model }}</span> /
              <span class="itbms-ramGb">{{ product.ramGb ?? '-' }}</span>
              <span class="itbms-ramGb-unit">GB</span> /
              <span class="itbms-storageGb">{{ product.storageGb ?? '-' }}</span>
              <span class="itbms-storageGb-unit">GB</span>
            </p>

            <button
              class="itbms-button mt-auto w-full bg-[#7e5bef] hover:bg-[#6847d5] text-white py-2 rounded-lg font-bold transition"
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
  </section>
</template>
