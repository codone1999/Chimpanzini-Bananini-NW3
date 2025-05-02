<script setup>
import { useRoute } from 'vue-router'
import { ref, onMounted } from 'vue'
import { getItemById } from '@/lib/fetchUtils'

const route = useRoute()
const id = route.params.id
const product = ref(null)

onMounted(async () => {
  try {
    product.value = await getItemById('http://ip24nw3.sit.kmutt.ac.th:8080/v1/sale-items', id)
  } catch (error) {
    console.error('Failed to fetch product:', error)
  }
})
</script>

<template>
  <div class="max-w-5xl mx-auto py-12 px-4">
    <h1 class="text-2xl font-bold mb-4">{{ product.brand }} {{ product.model }} {{ product.ramGb }}/GB {{ product.color }}</h1>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
      <!-- Main Image -->
      <div>
        <img :src="product.image" class="w-full rounded shadow" alt="Product" />
      </div>

      <!-- Product Details -->
      <div class="space-y-3 text-gray-800">
        <p><strong>Brand:</strong> {{ product.brandName }}</p>
        <p><strong>Model:</strong> {{ product.model }}</p>
        <p><strong>Price:</strong> {{ product.price }} Baht</p>
        <p><strong>Description:</strong> {{ product.description }}</p>
        <p><strong>RAM:</strong> {{ product.ramGb }} GB</p>
        <p><strong>Screen Size:</strong> {{ product.screenSizeInch }}"</p>
        <p><strong>Storage:</strong> {{ product.storageGb }} GB</p>
        <p><strong>Color:</strong> {{ product.color }}</p>
        <p><strong>Available Quantity:</strong> {{ product.quantity }} units</p>
        <button class="mt-4 bg-purple-600 text-white py-2 px-6 rounded hover:bg-purple-700">
          Add to Cart
        </button>
      </div>
    </div>
  </div>
</template>

sysadmin@ip24nw3:~/Chimpanzini-Bananini-NW3/FE/itb-mshop-fe/src/components$ cat ListGallery.vue
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