<script setup>
import { useRoute } from 'vue-router'
import { computed } from 'vue'

const route = useRoute()
const id = computed(() => route.params.id)

// Replace this with actual API or store
const product = {
  id: 1,
  name: 'iPhone 12 Pro Max',
  brand: 'Apple',
  model: 'iPhone 12 Pro Max',
  price: '29,700',
  description: '2020 flagship model',
  ramGb: 6,
  screenSizeInch: 6.7,
  storageGb: 256,
  color: 'Pacific Blue',
  quantity: 4,
  image: '/public/favicon.ico',
}
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
        <p><strong>Brand:</strong> {{ product.brand }}</p>
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
