<script setup>
import { ref, onMounted } from 'vue'
import { getItems } from '@/lib/fetchUtils';
import { useRoute, useRouter } from 'vue-router'

import phoneImg from '../../public/phone.jpg';

const route = useRoute()
const router = useRouter()
const showSuccessMessage = ref(false)

const products = ref()


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
  <section id="shop" class="bg-gray-100 py-16 px-4 md:px-10 max-w-7xl mx-auto">
    <h2 class="text-3xl font-extrabold text-gray-900 mb-10 text-center tracking-tight">
      Shop Our Products
    </h2>

    <!-- Success Message -->
    <div 
      v-if="showSuccessMessage" 
      class="itbms-message mb-6 p-4 text-green-800 bg-green-100 border border-green-300 rounded"
    >
      The sale item has been successfully added.
    </div>

    <button class="mb-6">
      <router-link
        :to="{ name: 'AddItem'}"
        class="bg-[#7e5bef] hover:bg-[#6847d5] text-white px-6 py-3 rounded-xl text-base font-semibold shadow-lg transition duration-300"
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
        class="bg-white rounded-2xl border border-gray-200 overflow-hidden shadow-md hover:shadow-xl transition duration-300"
      >
        <router-link :to="{ name: 'ListDetails', params: { id: product.id }}">
          <img :src="phoneImg" :alt="product.name" class="w-full h-56 object-contain bg-[#f2f2f2]" />
          <div class="p-5 text-center">
            <h3 class="itbms-brand text-sm font-medium text-gray-500 uppercase tracking-wide">{{ product.brandName }}</h3>
            <p class="text-[#7e5bef] font-semibold mt-1">
              <span class="itbms-model">{{ product.model }}</span> /
              <span class="itbms-ramGb">{{ product.ramGb }}</span>
              <span class="itbms-ramGb-unit">GB</span> /
              <span class="itbms-storageGb">{{ product.storageGb }}</span>
              <span class="itbms-storageGb-unit">GB</span>
            </p>
            <button
              class="mt-4 w-full bg-[#7e5bef] text-white py-2 rounded-lg font-bold hover:bg-[#6847d5] transition"
            >
              Baht {{ product.price.toLocaleString() }}
            </button>
          </div>
        </router-link>
      </div>
    </div>
  </section>
</template>