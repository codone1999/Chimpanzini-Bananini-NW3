//ShoppingCart.vue
<script setup>
import { useRouter } from 'vue-router';
import { useCart } from '@/composables/useCart'; // Import cart composable

const router = useRouter();
const { totalQuantity } = useCart(); // Get totalQuantity from cart

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
    
    <!-- Cart Badge - now shows real cart count -->
    <span 
      v-if="totalQuantity > 0"
      class="itbms-cart-quantity absolute -top-2 -right-1.5 bg-red-500 text-white text-sm font-bold rounded-full w-5 h-5 flex items-center justify-center"
    >
      {{ totalQuantity > 99 ? '99+' : totalQuantity }}
    </span>
  </button>
</template>
