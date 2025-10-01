<script setup>
import { ref, computed } from 'vue';

const cartItems = ref([
  {
    id: 1,
    name: 'Wireless Headphones',
    price: 79.99,
    quantity: 2,
    seller: 'TechStore',
    selected: true,
    image: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=150&h=150&fit=crop'
  },
  {
    id: 2,
    name: 'Smart Watch',
    price: 199.99,
    quantity: 1,
    seller: 'TechStore',
    selected: true,
    image: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=150&h=150&fit=crop'
  },
  {
    id: 3,
    name: 'Laptop Stand',
    price: 45.50,
    quantity: 3,
    seller: 'OfficeGear',
    selected: true,
    image: 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=150&h=150&fit=crop'
  },
  {
    id: 4,
    name: 'USB-C Cable',
    price: 12.99,
    quantity: 2,
    seller: 'OfficeGear',
    selected: true,
    image: 'https://images.unsplash.com/photo-1588508065123-287b28e013da?w=150&h=150&fit=crop'
  },
  {
    id: 5,
    name: 'Mechanical Keyboard',
    price: 129.99,
    quantity: 1,
    seller: 'GamingHub',
    selected: true,
    image: 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=150&h=150&fit=crop'
  }
]);

// Group items by seller
const groupedBySeller = computed(() => {
  const groups = {};
  cartItems.value.forEach(item => {
    if (!groups[item.seller]) {
      groups[item.seller] = [];
    }
    groups[item.seller].push(item);
  });
  return groups;
});

// Check if all items are selected
const allSelected = computed({
  get() {
    return cartItems.value.length > 0 && cartItems.value.every(item => item.selected);
  },
  set(value) {
    cartItems.value.forEach(item => {
      item.selected = value;
    });
  }
});

// Check if all items in a seller group are selected
const isSellerSelected = (seller) => {
  const sellerItems = groupedBySeller.value[seller];
  return sellerItems.every(item => item.selected);
};

// Toggle all items in a seller group
const toggleSeller = (seller) => {
  const sellerItems = groupedBySeller.value[seller];
  const allChecked = isSellerSelected(seller);
  sellerItems.forEach(item => {
    item.selected = !allChecked;
  });
};

// Calculate totals based on selected items only
const totalItems = computed(() => {
  return cartItems.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + item.quantity, 0);
});

const total = computed(() => {
  return cartItems.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + (item.price * item.quantity), 0);
});

const updateQuantity = (itemId, change) => {
  const item = cartItems.value.find(i => i.id === itemId);
  if (item) {
    item.quantity += change;
    if (item.quantity < 1) {
      removeItem(itemId);
    }
  }
};

const removeItem = (itemId) => {
  cartItems.value = cartItems.value.filter(i => i.id !== itemId);
};

const createOrder = () => {
  const selectedItems = cartItems.value.filter(item => item.selected);
  if (selectedItems.length === 0) {
    alert('Please select at least one item to create an order');
    return;
  }
  alert(`Order created successfully!\nTotal: ฿${total.value.toFixed(2)}\nItems: ${totalItems.value}`);
};
</script>

<template>
  <div class="min-h-screen py-8 px-4">
    <div class="max-w-6xl mx-auto">
      <h1 class="text-3xl font-bold text-gray-800 mb-8">Shopping Cart</h1>
      
      <div class="grid lg:grid-cols-3 gap-8">
        <!-- Cart Items -->
        <div class="lg:col-span-2 space-y-4">
          <div v-if="cartItems.length === 0" class="bg-white rounded-lg shadow p-8 text-center">
            <p class="text-gray-500 text-lg">Your cart is empty</p>
          </div>

          <!-- Select All -->
          <div v-if="cartItems.length > 0" class="bg-white rounded-lg shadow p-4">
            <label class="flex items-center gap-3 cursor-pointer">
              <input 
                type="checkbox" 
                v-model="allSelected"
                class="w-5 h-5 rounded border-gray-300 text-blue-600 focus:ring-blue-500 cursor-pointer"
              >
              <span class="font-semibold text-gray-800">Select All</span>
            </label>
          </div>

          <!-- Grouped by Seller -->
          <div v-for="(items, seller) in groupedBySeller" :key="seller" class="space-y-3">
            <!-- Seller Header -->
            <div class="bg-gray-50 rounded-lg p-4 border border-gray-200">
              <label class="flex items-center gap-3 cursor-pointer">
                <input 
                  type="checkbox" 
                  :checked="isSellerSelected(seller)"
                  @change="toggleSeller(seller)"
                  class="w-5 h-5 rounded border-gray-300 text-blue-600 focus:ring-blue-500 cursor-pointer"
                >
                <div class="flex items-center gap-2">
                  <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"></path>
                  </svg>
                  <span class="font-bold text-gray-800">{{ seller }}</span>
                </div>
              </label>
            </div>

            <!-- Items in this seller group -->
            <div v-for="item in items" :key="item.id" 
                 class="bg-white rounded-lg shadow p-4 flex gap-4 ml-8">
              <label class="flex items-start cursor-pointer">
                <input 
                  type="checkbox" 
                  v-model="item.selected"
                  class="w-5 h-5 mt-1 rounded border-gray-300 text-blue-600 focus:ring-blue-500 cursor-pointer"
                >
              </label>

              <img :src="item.image" :alt="item.name" 
                   class="w-24 h-24 object-cover rounded">
              
              <div class="flex-1">
                <h3 class="font-semibold text-lg text-gray-800">{{ item.name }}</h3>
                <p class="text-gray-600 mt-1">฿{{ item.price.toFixed(2) }}</p>
                
                <div class="flex items-center gap-3 mt-3">
                  <button @click="updateQuantity(item.id, -1)"
                          class="w-8 h-8 rounded bg-gray-200 hover:bg-gray-300 flex items-center justify-center">
                    -
                  </button>
                  <span class="font-medium text-gray-700 w-8 text-center">{{ item.quantity }}</span>
                  <button @click="updateQuantity(item.id, 1)"
                          class="w-8 h-8 rounded bg-gray-200 hover:bg-gray-300 flex items-center justify-center">
                    +
                  </button>
                </div>
              </div>
              
              <div class="text-right flex flex-col justify-between">
                <p class="font-semibold text-lg text-gray-800">
                  ${{ (item.price * item.quantity).toFixed(2) }}
                </p>
                <button @click="removeItem(item.id)"
                        class="ml-auto text-red-500 hover:text-red-700 text-sm font-medium">
                  Remove
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Summary -->
        <div class="lg:col-span-1">
          <div class="bg-gradient-to-br from-blue-50 to-indigo-50 rounded-xl shadow-lg p-6 sticky top-8 border border-blue-100">
            <div class="flex items-center gap-2 mb-6">
              <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path>
              </svg>
              <h2 class="text-2xl font-bold text-gray-800">Order Summary</h2>
            </div>
            
            <div class="space-y-4 mb-6">
              <div class="bg-white rounded-lg p-4 shadow-sm">
                <div class="flex justify-between items-center">
                  <div class="flex items-center gap-2">
                    <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"></path>
                    </svg>
                    <span class="text-gray-600 font-medium">Total Items</span>
                  </div>
                  <span class="text-xl font-bold text-blue-600">{{ totalItems }}</span>
                </div>
              </div>
              
              <div class="bg-gradient-to-r from-blue-600 to-indigo-600 rounded-lg p-5 shadow-md">
                <div class="flex justify-between items-center text-white">
                  <div class="flex items-center gap-2">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                    <span class="font-semibold text-lg">Total Price</span>
                  </div>
                  <span class="text-2xl font-bold">฿{{ total.toFixed(2) }}</span>
                </div>
              </div>
            </div>
            
            <button @click="createOrder"
                    :disabled="totalItems === 0"
                    class="w-full bg-gradient-to-r from-green-500 to-emerald-600 hover:from-green-600 hover:to-emerald-700 disabled:from-gray-300 disabled:to-gray-400 disabled:cursor-not-allowed text-white font-bold py-4 px-6 rounded-xl shadow-lg hover:shadow-xl transition-all duration-200 transform hover:scale-105 disabled:transform-none flex items-center justify-center gap-2">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
              </svg>
              Create Order
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
