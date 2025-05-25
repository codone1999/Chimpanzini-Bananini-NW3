<script setup>

</script>
 
<template>
  <div class="relative min-h-screen flex flex-col justify-center items-center text-center bg-gradient-to-br from-neutral-900 via-zinc-900 to-black text-white overflow-hidden px-4">
    
    <!-- เอฟเฟกต์ background blur -->
    <div class="absolute inset-0 pointer-events-none">
      <div class="absolute w-72 h-72 bg-purple-700 rounded-full opacity-30 blur-3xl top-20 left-1/4 animate-pulse"></div>
      <div class="absolute w-56 h-56 bg-amber-500 rounded-full opacity-20 blur-3xl bottom-40 right-20 animate-pulse"></div>
      <div class="absolute w-32 h-32 bg-gray-300 rounded-full opacity-10 blur-3xl top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 animate-pulse"></div>
    </div>

    <!-- เนื้อหาหลัก -->
    <div class="z-10 max-w-xl">
      <h1 class="text-7xl font-extrabold text-purple-500 drop-shadow mb-6 animate-fade-in">404</h1>
      <p class="text-2xl md:text-3xl text-neutral-300 mb-10 animate-fade-in delay-100">
        Oops... The page you're looking for doesn't exist.
      </p>

      <router-link
        :to="{ name: 'MainMenu' }"
        class="bg-purple-600 hover:bg-purple-700 text-white px-6 py-3 rounded-2xl text-lg font-semibold shadow-xl transition-all duration-200 ease-in-out animate-fade-up"
      >
        Back To Home
      </router-link>
    </div>
  </div>
</template>
