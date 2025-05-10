<script setup>
import { ref, onMounted } from 'vue'
import { getItems } from '@/lib/fetchUtils';
import { useRoute, useRouter } from 'vue-router'

import phoneImg from '../../public/phone.jpg';

const route = useRoute()
const router = useRouter()
const showSuccessMessage = ref(false)

const products = ref([])


onMounted(async() => {
  if (route.query.added === 'true') {
    showSuccessMessage.value = true
    setTimeout(() => {
      showSuccessMessage.value = false
      // Reset the query param properly
      router.replace({ 
        name: route.name, 
        query: { ...route.query, added: undefined } // remove 'added'
      })
    }
    , 3000)
  }

  try {
    products.value = await getItems(`http://ip24nw3.sit.kmutt.ac.th:8080/v1/sale-items`) ?? []
  } catch (error) {
    console.error(error)
  }
})

</script>

<template>
  <section id="shop" class="bg-white py-12 px-4 md:px-8 max-w-7xl mx-auto">
    <h2 class="text-2xl font-bold text-gray-800 mb-6 text-center">Shop Our Products</h2>

    <!-- Success Message -->
    <div 
      v-if="showSuccessMessage" 
      class="itbms-message mb-6 p-4 text-green-800 bg-green-100 border border-green-300 rounded"
    >
      The sale item has been successfully added.
    </div>

    <button>
      <router-link :to="{ name: 'AddItem'}"
        class="itbms-sale-item-add bg-purple-600 hover:bg-purple-700 text-white px-6 py-3 rounded-lg text-lg font-medium shadow-md transition"
      >
        Add Item
      </router-link>
    </button>

    <!-- No products -->
    <div v-if="products.length === 0" class="text-center text-gray-500 text-lg py-10">
      No sale items.
    </div>

    <!-- Product Grid -->
    <div
      v-else
      class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6"
    >
    <div
        v-for="product in products"
        :key="product.id"
        class="itbms-row border rounded-lg overflow-hidden shadow hover:shadow-md transition"
      >
        <router-link :to="{ name: 'ListDetails', params: { id: product.id }}">
          <img :src="phoneImg" :alt="product.name" class="w-full h-56 object-cover" />
          <div class="p-4 text-center">
            <h3 class="itbms-brand font-semibold text-gray-700">{{ product.brandName }}</h3>
            <p class="text-purple-600 font-bold mt-1">
              <span class="itbms-model">{{ product.model }}</span> /
              <span class="itbms-ramGb">{{ product.ramGb }}</span>
              <span class="itbms-ramGb-unit">GB</span> /
              <span class="itbms-storageGb">{{ product.storageGb }}</span>
              <span class="itbms-storageGb-unit">GB</span>
            </p>
            <button
              class="mt-3 w-full bg-purple-600 text-white py-2 rounded hover:bg-purple-700 transition"
            >
              <span class="itbms-price-unit">Bath </span>
              <span class="itbms-price">{{ product.price.toLocaleString() }}</span>
            </button>
          </div>
        </router-link>
      </div>
    </div>
  </section>
</template>