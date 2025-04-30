<script setup>

</script>

<template>
  <!-- Hero Section -->
  <main class="min-h-screen flex flex-col justify-center items-center text-center bg-gray-50 pt-24 pb-32 px-4">
    <h1 class="text-3xl font-extrabold text-gray-800 mb-4">Welcome to ITB MShop</h1>
    <p class="text-gray-600 mb-8">Discover the latest trends at unbeatable prices.</p>

    <!-- Shop Now button (bottom center on mobile) -->
    <div class="fixed bottom-6 inset-x-0 flex justify-center md:static">
        <router-link
            :to="{name: 'ListGallery'}"
            class="bg-purple-600 hover:bg-purple-700 text-white px-6 py-3 rounded-full text-lg font-medium shadow-md transition"
        >
            Shop Now
        </router-link>
    </div>
  </main>
</template>

<!-- You must import Material Icons in your index.html or main file -->
