//ShoppingCart.vue
<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

function goToCartPage(){
  router.push({ name: "Cart"})
}

</script>

<template>
  <button
    @click="goToCartPage()"
    class="relative pt-1.5 pb-1 px-2 bg-neutral-900 border border-neutral-700 rounded-xl hover:bg-neutral-800 hover:border-purple-500 transition group flex-shrink-0"
    title="Shopping Cart"
  >
    <span class="material-icons text-gray-200 group-hover:text-purple-400">
      shopping_cart
    </span>
    
    <!-- Cart Badge (optional - shows item count) -->
    <span class="itbms-cart-quantity absolute -top-2 -right-1.5 bg-red-500 text-white text-sm font-bold rounded-full w-5 h-5 flex items-center justify-center">
      3
    </span>
  </button>
</template>
