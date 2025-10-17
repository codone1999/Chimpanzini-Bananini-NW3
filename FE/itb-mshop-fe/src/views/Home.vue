<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import phoneimg from '../../public/phone.png'

const featuredItems = [
  {
    "id": 1,
    "model": "iPhone 14 Pro Max",
    "brandName": "Apple",
    "price": 42900,
    "storageGb": 512,
    "ramGb": 6,
    "color": "Space Black"
  },
  {
    "id": 2,
    "model": "iPhone 14",
    "brandName": "Apple",
    "price": 29700,
    "storageGb": 256,
    "ramGb": 6,
    "color": "Midnight"
  },
  {
    "id": 3,
    "model": "iPhone 13 Pro",
    "brandName": "Apple",
    "price": 33000,
    "storageGb": 256,
    "ramGb": 6,
    "color": "Sierra Blue"
  },
  {
    "id": 4,
    "model": "iPhone 13",
    "brandName": "Apple",
    "price": 23100,
    "storageGb": 128,
    "ramGb": 4,
    "color": "Pink"
  },
  {
    "id": 5,
    "model": "iPhone 12 Pro Max",
    "brandName": "Apple",
    "price": 29700,
    "storageGb": 256,
    "ramGb": 6,
    "color": "Pacific Blue"
  },
  {
    "id": 6,
    "model": "iPhone 12",
    "brandName": "Apple",
    "price": 19800,
    "storageGb": 128,
    "ramGb": 4,
    "color": "Purple"
  },
  {
    "id": 7,
    "model": "iPhone SE 2022",
    "brandName": "Apple",
    "price": 14190,
    "storageGb": 64,
    "ramGb": 4,
    "color": "Starlight"
  },
  {
    "id": 8,
    "model": "iPhone 14 Plus",
    "brandName": "Apple",
    "price": 29700,
    "storageGb": 256,
    "ramGb": 6,
    "color": "Blue"
  },
  {
    "id": 9,
    "model": "iPhone 13 mini",
    "brandName": "Apple",
    "price": 19800,
    "storageGb": 128,
    "ramGb": 4,
    "color": "Green"
  },
  {
    "id": 10,
    "model": "iPhone 12 mini",
    "brandName": "Apple",
    "price": 16500,
    "storageGb": 64,
    "ramGb": 4,
    "color": "Red"
  },
  {
    "id": 16,
    "model": "Galaxy S23 Ultra",
    "brandName": "Samsung",
    "price": 39600,
    "storageGb": 512,
    "ramGb": null,
    "color": null
  },
  {
    "id": 17,
    "model": "Galaxy S23+",
    "brandName": "Samsung",
    "price": 33000,
    "storageGb": 256,
    "ramGb": 8,
    "color": "Cream"
  },
  {
    "id": 18,
    "model": "Galaxy Z Fold4",
    "brandName": "Samsung",
    "price": 59400,
    "storageGb": 256,
    "ramGb": 12,
    "color": "Phantom Green"
  },
  {
    "id": 19,
    "model": "Galaxy Z Flip4",
    "brandName": "Samsung",
    "price": 33000,
    "storageGb": 128,
    "ramGb": 8,
    "color": "Bora Purple"
  },
  {
    "id": 20,
    "model": "Galaxy A53 5G",
    "brandName": "Samsung",
    "price": 14850,
    "storageGb": 128,
    "ramGb": 6,
    "color": "Awesome Blue"
  },
   {
    "id": 21,
    "model": "Galaxy A33 5G",
    "brandName": "Samsung",
    "price": 11550,
    "storageGb": 128,
    "ramGb": 6,
    "color": "Awesome White"
  },
  {
    "id": 22,
    "model": "Galaxy S22",
    "brandName": "Samsung",
    "price": 26400,
    "storageGb": 128,
    "ramGb": 8,
    "color": "Pink Gold"
  },
  {
    "id": 23,
    "model": "Galaxy M53",
    "brandName": "Samsung",
    "price": 14850,
    "storageGb": 128,
    "ramGb": 6,
    "color": "Green"
  },
  {
    "id": 24,
    "model": "Galaxy A73 5G",
    "brandName": "Samsung",
    "price": 16500,
    "storageGb": 256,
    "ramGb": 8,
    "color": "Gray"
  },
  {
    "id": 25,
    "model": "Galaxy S21 FE",
    "brandName": "Samsung",
    "price": 19800,
    "storageGb": 128,
    "ramGb": 6,
    "color": "Olive"
  },
  {
    "id": 31,
    "model": "13 Pro",
    "brandName": "Xiaomi",
    "price": 33000,
    "storageGb": 256,
    "ramGb": 12,
    "color": "Black"
  },
]

const currentSlide = ref(0)
const itemsPerSlide = 3
let slideInterval = null

const totalSlides = Math.ceil(featuredItems.length / itemsPerSlide)

const getSlideItems = (slideIndex) => {
  const start = slideIndex * itemsPerSlide
  const end = start + itemsPerSlide
  return featuredItems.slice(start, end)
}

const nextSlide = () => {
  currentSlide.value = (currentSlide.value + 1) % totalSlides
}

const goToSlide = (index) => {
  currentSlide.value = index
}

onMounted(() => {
  slideInterval = setInterval(nextSlide, 6000)
})

onUnmounted(() => {
  if (slideInterval) {
    clearInterval(slideInterval)
  }
})
</script>

<template>
  <!-- Hero Section -->
  <main class="relative min-h-screen flex flex-col justify-center items-center text-center bg-gray-950 overflow-hidden pt-24 pb-32 px-4">

    <!-- Decorative particles or blurred elements -->
    <div class="absolute inset-0 pointer-events-none">
      <div class="absolute w-75 h-75 bg-purple-700 rounded-full opacity-30 blur-3xl top-10 left-1/3 animate-pulse"></div>
      <div class="absolute w-55 h-55 bg-amber-500 rounded-full opacity-20 blur-3xl bottom-50 right-35 animate-pulse"></div>
      <div class="absolute w-30 h-30 bg-gray-300 rounded-full opacity-20 blur-3xl bottom-50 left-50 animate-pulse"></div>
    </div>

    <!-- Topic & Shop + Seller Button -->
    <div class="z-10 max-w-[685px]">
      <h1 class="text-5xl md:text-6xl font-extrabold text-white mb-6 tracking-tight leading-tight animate-fade-in">
        Welcome to <span class="bg-gradient-to-r from-purple-400 to-indigo-500 bg-clip-text text-transparent drop-shadow-lg">ITB MShop</span>
      </h1>
      <p class="typewriter text-gray-400 text-lg md:text-xl mb-12">
        Discover the latest trends in tech and fashion — at unbeatable prices.
      </p>

      <!-- Shop Now button (bottom center on mobile) -->
      <div class="relative inset-x-0 flex mt-12 justify-around px-4 md:static md:justify-center md:gap-8">
        <router-link
          :to="{ name: 'ListGallery' }"
          class="itbms-shopnow bg-gradient-to-r from-purple-500 to-indigo-600 hover:opacity-90 text-white px-6 py-3 
          rounded-xl text-lg font-semibold shadow-lg transition duration-200 ease-in-out animate-fade-up"
        >
          Shop Now
        </router-link>

        <router-link
          :to="{ name: 'ListSaleItems' }"
          class="itbms-seller bg-gradient-to-r from-amber-500 to-orange-600 hover:opacity-90 text-white px-7 py-3 
          rounded-xl text-lg font-semibold shadow-lg transition duration-200 ease-in-out animate-fade-up delay-150"
        >
          Seller
        </router-link>
      </div>
    </div>

    <section class="z-10 mt-18 max-w-6xl w-full text-left">
      <h2 class="text-3xl md:text-4xl font-bold p-1 mb-12 text-center bg-gradient-to-r from-purple-400 to-indigo-500 bg-clip-text text-transparent animate-fade-in">Why Shop with ITB MShop?</h2>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-10 animate-fade-up delay-150">

        <!-- Feature 1 -->
        <div class="bg-gray-900 border border-gray-800 rounded-2xl p-6 shadow-lg hover:border-purple-500/40 transition">
          <h3 class="text-xl font-semibold text-white mb-2">Latest Smartphones</h3>
          <p class="text-gray-400 text-sm">
            From flagship models to budget-friendly phones — explore the newest devices from top brands.
          </p>
        </div>

        <!-- Feature 2 -->
        <div class="bg-gray-900 border border-gray-800 rounded-2xl p-6 shadow-lg hover:border-indigo-500/40 transition">
          <h3 class="text-xl font-semibold text-white mb-2">Unbeatable Prices</h3>
          <p class="text-gray-400 text-sm">
            Get your dream phone at the best deals — no hidden fees, just value.
          </p>
        </div>

        <!-- Feature 3 -->
        <div class="bg-gray-900 border border-gray-800 rounded-2xl p-6 shadow-lg hover:border-gray-500/40 transition">
          <h3 class="text-xl font-semibold text-white mb-2">Fast & Secure Delivery</h3>
          <p class="text-gray-400 text-sm">
            Receive your phone in days, safely packed and tracked from our store to your doorstep.
          </p>
        </div>

      </div>
    </section>

    <!-- Header Slide Show -->
    <h2 class="text-3xl md:text-4xl font-bold p-1 mt-30 text-center bg-gradient-to-r from-purple-400 to-indigo-500 
      bg-clip-text text-transparent animate-fade-in">
    Explore Our Products
    </h2>
    <p class="text-gray-400 text-lg md:text-xl pt-4 animate-fade-up">
      Featured phones at amazing prices — find your perfect match.
    </p>

    <!-- Featured Items Slideshow -->
    <div class="relative w-full max-w-6xl mx-auto mt-12 overflow-hidden rounded-2xl bg-gray-950/50  shadow-2xl animate-fade-up delay-100">
      <div class="relative h-[450px]">
        <!-- Slides -->
        <div 
          v-for="(slideIndex) in totalSlides" 
          :key="slideIndex"
          class="absolute inset-0 transition-all duration-1000 ease-in-out"
          :class="currentSlide === slideIndex - 1 ? 'opacity-100 translate-x-0' : currentSlide > slideIndex - 1 ? 'opacity-0 -translate-x-full' : 'opacity-0 translate-x-full'"
        >
          <div class="grid grid-cols-3 gap-6 h-full px-8 pb-10">
            <div 
              v-for="item in getSlideItems(slideIndex - 1)" 
              :key="item.id"
              class="flex flex-col justify-center items-center text-center bg-gradient-to-br from-gray-900 to-gray-800 backdrop-blur-sm rounded-3xl p-4 border border-gray-800 hover:border-purple-500/40 transition-all duration-300 hover:shadow-lg hover:shadow-purple-500/20"
            >
              
              <!-- Phone Image -->
              <img :src="phoneimg" :alt="item.model" class="h-70 object-contain drop-shadow-2xl" />
              
              <h3 class="text-lg font-bold text-[#7e5bef] mb-1">{{ item.brandName }}</h3>
              <h3 class="text-lg font-bold text-white mb-1">{{ item.model }}</h3>
              <p class="text-gray-400 text-xs mb-2">{{ item.storageGb }}GB • {{ item.ramGb }}GB RAM</p>
              <p class="text-2xl font-extrabold bg-gradient-to-r from-amber-400 to-orange-500 bg-clip-text text-transparent">
                ฿{{ item.price.toLocaleString() }}
              </p>
            </div>
          </div>
        </div>
      </div>

    </div>

    <!-- Dots Navigation -->
    <div class="flex mt-5 gap-3 bg-gradient-to-br from-gray-900 to-gray-800 backdrop-blur-sm px-4 py-3 rounded-full">
      <button
        v-for="slideIndex in totalSlides"
        :key="`dot-${slideIndex}`"
        @click="goToSlide(slideIndex - 1)"
        class="w-3.5 h-3.5 rounded-full transition-all duration-300"
        :class="currentSlide === slideIndex - 1 ? 'bg-purple-500 w-10' : 'bg-gray-600 hover:bg-gray-500'"
      ></button>
    </div>
  </main>
</template>

<style scoped>
@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(80px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fade-up {
  0% {
    opacity: 0;
    transform: translateY(50px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in {
  animation: fade-in 1s ease-out forwards;
}
.animate-fade-up {
  animation: fade-up 0.8s ease-out forwards;
}
.animate-fade-up.delay-150 {
  animation-delay: 0.15s;
}
.animate-fade-up.delay-100 {
  animation-delay: 0.1s;
}

.typewriter {
  overflow: hidden;
  border-right: .15em solid #a855f7; /* เส้นสีม่วงปลายข้อความ */
  white-space: nowrap;
  margin: 0 auto;
  letter-spacing: .05em;
  animation:
    typing 3s steps(60, end),
    blink-caret .75s step-end infinite;
}

@keyframes typing {
  from { width: 0 }
  to { width: 100% }
}

@keyframes blink-caret {
  from, to { border-color: transparent }
  50% { border-color: #a855f7 }
}
</style>
