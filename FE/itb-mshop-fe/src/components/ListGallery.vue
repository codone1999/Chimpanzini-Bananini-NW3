<script setup>
import { ref, onMounted } from 'vue'
import { getItems } from '@/lib/fetchUtils';

const products = ref([])

onMounted(async() => {
  try {
    products.value = await getItems(`http://ip24nw3.sit.kmutt.ac.th:8080/v1/sale-items`)
  } catch (error) {
    console.error(error)
  }
})

</script>

<template>
  <section id="shop" class="bg-white py-12 px-4 md:px-8 max-w-7xl mx-auto">
    <h2 class="text-2xl font-bold text-gray-800 mb-6 text-center">Shop Our Products</h2>

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
        class="border rounded-lg overflow-hidden shadow hover:shadow-md transition"
      >
        <router-link :to="{ name: 'ListDetails', params: { id: product.id }}">
            <img :src="product.image" :alt="product.name" class="w-full h-56 object-cover" />
            <div class="p-4 text-center">
              <h3 class="font-semibold text-gray-700">{{ product.brandName }}</h3>
              <p class="text-purple-600 font-bold mt-1">{{ product.model }} / {{ product.ramGb }}GB / {{ product.storageGb }}{{ product.storageUnit }}</p>
              <button
                class="mt-3 w-full bg-purple-600 text-white py-2 rounded hover:bg-purple-700 transition"
              >
                {{ product.price }} {{ product.priceUnit }}
              </button>
            </div>
        </router-link>
      </div>
    </div>
  </section>
</template>