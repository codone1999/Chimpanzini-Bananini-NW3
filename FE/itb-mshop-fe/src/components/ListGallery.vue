<script setup>
const products = [
  {
    id: 1,
    brand: 'Apple',
    model: 'iPhone 12 Pro Max',
    ramGb: 6,
    storageGb: 256,
    storageUnit: 'GB',
    price: '99',
    priceUnit: 'Bath',
    image: '/public/favicon.ico',
  },
  {
    id: 2,
    brand: 'Apple',
    model: 'iPhone 13 Pro',
    ramGb: 6,
    storageGb: 256,
    storageUnit: 'GB',
    price: '99',
    priceUnit: 'Bath',
    image: '/public/favicon.ico',
  },
  {
    id: 3,
    brand: 'Apple',
    model: 'iPhone 14',
    ramGb: 6,
    storageGb: 256,
    storageUnit: 'GB',
    price: '99',
    priceUnit: 'Bath',
    image: '/public/favicon.ico',
  },
  {
    id: 4,
    brand: 'Apple',
    model: 'iPhone 15 Air',
    ramGb: 6,
    storageGb: 256,
    storageUnit: 'GB',
    price: '99',
    priceUnit: 'Bath',
    image: '/public/favicon.ico',
  },
  {
    id: 5,
    brand: 'Apple',
    model: 'iPhone 16 thin',
    ramGb: 6,
    storageGb: 256,
    storageUnit: 'GB',
    price: '99',
    priceUnit: 'Bath',
    image: '/public/favicon.ico',
  },
]
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
              <h3 class="font-semibold text-gray-700">{{ product.brand }}</h3>
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
