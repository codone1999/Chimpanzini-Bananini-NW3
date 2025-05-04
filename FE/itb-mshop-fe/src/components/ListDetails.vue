<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ref, onMounted } from 'vue'
import { getItemById } from '@/lib/fetchUtils'
import phoneImg from '../../public/phone.jpg';

const route = useRoute()
const router = useRouter()
const id = route.params.id
const product = ref(null)

onMounted(async () => {
  try {
    const item = await getItemById('http://ip24nw3.sit.kmutt.ac.th:8080/v1/sale-items', id)
    if (!item || item?.status === 404) {
      router.push('/sale-items')
      alert('The requested sale item does not exist.')
      return
    }
    product.value = item;
  } catch (error) {
    console.error('Failed to fetch product:', error);
  }
})
</script>

<template>
  <div class="max-w-5xl mx-auto py-12 px-4 itbms-row">
    <h1 class="text-2xl font-bold mb-4">
      <span class="itbms-brand">{{ product.brand }}</span>
      <span class="itbms-model">{{ product.model }}</span>
      <span class="itbms-ramGb">{{ product.ramGb ?? "" }}</span> GB
      <span class="itbms-color">{{ product.color ?? "" }}</span>
    </h1>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
      <!-- Main Image -->
      <div>
        <img :src="phoneImg" class="w-full rounded shadow" alt="Product" />
      </div>

      <!-- Product Details -->
      <div class="space-y-3 text-gray-800">
        <p><strong>Brand:</strong> <span class="itbms-brand">{{ product.brandName }}</span></p>
        <p><strong>Model:</strong> <span class="itbms-model">{{ product.model }}</span></p>
        <p><strong>Price:</strong> <span class="itbms-price">{{ product.price.toLocaleString() }}</span> Baht</p>
        <p><strong>Description:</strong> <span class="itbms-description">{{ product.description }}</span></p>
        <p><strong>RAM:</strong> <span class="itbms-ramGb">{{ product.ramGb ?? "-" }}</span> GB</p>
        <span class="itbms-ramGb-unit">GB</span>
        <p><strong>Screen Size:</strong> <span class="itbms-screenSizeInch">{{ product.screenSizeInch ?? "-" }}</span></p>
        <span class="itbms-screenSizeInch-unit">Inches</span>
        <p><strong>Storage:</strong> <span class="itbms-storageGb">{{ product.storageGb ?? "-" }}</span> GB</p>
        <span class="itbms-storageGb-unit">GB</span>
        <p><strong>Color:</strong> <span class="itbms-color">{{ product.color ?? "-" }}</span></p>
        <p><strong>Available Quantity:</strong> <span class="itbms-quantity">{{ product.quantity }}</span> units</p>

        <button class="mt-4 bg-purple-600 text-white py-2 px-6 rounded hover:bg-purple-700">
          Add to Cart
        </button>
      </div>
    </div>
  </div>
</template>