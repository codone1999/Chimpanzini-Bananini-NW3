<script setup>
import { ref, onMounted, h } from 'vue'
import { getItems } from '@/lib/fetchUtils';
import { useRoute, useRouter } from 'vue-router'

import phoneImg from '../../public/phone.jpg';

const route = useRoute()
const router = useRouter()

const showSuccessMessage = ref(false)
const successMessage = ref('')

const products = ref([{"id":1,"model":"iPhone 14 Pro Max","brandName":"Apple","price":42900,"storageGb":512,"ramGb":6,"color":"Space Black"},{"id":2,"model":"iPhone 14","brandName":"Apple","price":29700,"storageGb":256,"ramGb":6,"color":"Midnight"},{"id":3,"model":"iPhone 13 Pro","brandName":"Apple","price":33000,"storageGb":256,"ramGb":6,"color":"Sierra Blue"},{"id":4,"model":"iPhone 13","brandName":"Apple","price":23100,"storageGb":128,"ramGb":4,"color":"Pink"},{"id":5,"model":"iPhone 12 Pro Max","brandName":"Apple","price":29700,"storageGb":256,"ramGb":6,"color":"Pacific Blue"},{"id":6,"model":"iPhone 12","brandName":"Apple","price":19800,"storageGb":128,"ramGb":4,"color":"Purple"},{"id":7,"model":"iPhone SE 2022","brandName":"Apple","price":14190,"storageGb":64,"ramGb":4,"color":"Starlight"},{"id":8,"model":"iPhone 14 Plus","brandName":"Apple","price":29700,"storageGb":256,"ramGb":6,"color":"Blue"},{"id":9,"model":"iPhone 13 mini","brandName":"Apple","price":19800,"storageGb":128,"ramGb":4,"color":"Green"},{"id":10,"model":"iPhone 12 mini","brandName":"Apple","price":16500,"storageGb":64,"ramGb":4,"color":"Red"},{"id":16,"model":"Galaxy S23 Ultra","brandName":"Samsung","price":39600,"storageGb":512,"ramGb":null,"color":null},{"id":17,"model":"Galaxy S23+","brandName":"Samsung","price":33000,"storageGb":256,"ramGb":8,"color":"Cream"},{"id":18,"model":"Galaxy Z Fold4","brandName":"Samsung","price":59400,"storageGb":256,"ramGb":12,"color":"Phantom Green"},{"id":19,"model":"Galaxy Z Flip4","brandName":"Samsung","price":33000,"storageGb":128,"ramGb":8,"color":"Bora Purple"},{"id":20,"model":"Galaxy A53 5G","brandName":"Samsung","price":14850,"storageGb":128,"ramGb":6,"color":"Awesome Blue"},{"id":21,"model":"Galaxy A33 5G","brandName":"Samsung","price":11550,"storageGb":128,"ramGb":6,"color":"Awesome White"},{"id":22,"model":"Galaxy S22","brandName":"Samsung","price":26400,"storageGb":128,"ramGb":8,"color":"Pink Gold"},{"id":23,"model":"Galaxy M53","brandName":"Samsung","price":14850,"storageGb":128,"ramGb":6,"color":"Green"},{"id":24,"model":"Galaxy A73 5G","brandName":"Samsung","price":16500,"storageGb":256,"ramGb":8,"color":"Gray"},{"id":25,"model":"Galaxy S21 FE","brandName":"Samsung","price":19800,"storageGb":128,"ramGb":6,"color":"Olive"},{"id":31,"model":"13 Pro","brandName":"Xiaomi","price":33000,"storageGb":256,"ramGb":12,"color":"Black"},{"id":32,"model":"13T Pro","brandName":"Xiaomi","price":23100,"storageGb":null,"ramGb":12,"color":"Alpine Blue"},{"id":33,"model":"POCO F5","brandName":"Xiaomi","price":13200,"storageGb":256,"ramGb":8,"color":"Carbon Black"},{"id":34,"model":"Redmi Note 12 Pro","brandName":"Xiaomi","price":9900,"storageGb":128,"ramGb":8,"color":"Sky Blue"},{"id":35,"model":"12T Pro","brandName":"Xiaomi","price":21450,"storageGb":256,"ramGb":8,"color":"Cosmic Black"},{"id":36,"model":"POCO X5 Pro","brandName":"Xiaomi","price":9900,"storageGb":128,"ramGb":8,"color":"Yellow"},{"id":37,"model":"Redmi 12C","brandName":"Xiaomi","price":5940,"storageGb":64,"ramGb":4,"color":"Ocean Blue"},{"id":38,"model":"12 Lite","brandName":"Xiaomi","price":13200,"storageGb":128,"ramGb":8,"color":"Lite Pink"},{"id":39,"model":"POCO M5","brandName":"Xiaomi","price":7590,"storageGb":128,"ramGb":6,"color":"Power Black"},{"id":40,"model":"Redmi Note 11","brandName":"Xiaomi","price":8250,"storageGb":128,"ramGb":6,"color":"Star Blue"},{"id":46,"model":"P60 Pro","brandName":"Huawei","price":36300,"storageGb":256,"ramGb":12,"color":"Rococo Pearl"},{"id":47,"model":"Mate 50 Pro","brandName":"Huawei","price":42900,"storageGb":256,"ramGb":8,"color":"Silver Black"},{"id":48,"model":"nova 11 Pro","brandName":"Huawei","price":19800,"storageGb":256,"ramGb":8,"color":"Green"},{"id":49,"model":"P50 Pro","brandName":"Huawei","price":29700,"storageGb":256,"ramGb":8,"color":"Cocoa Gold"},{"id":50,"model":"nova 10","brandName":"Huawei","price":16500,"storageGb":128,"ramGb":8,"color":"Starry Silver"},{"id":51,"model":"Mate X3","brandName":"Huawei","price":66000,"storageGb":512,"ramGb":12,"color":"Feather Gold"},{"id":52,"model":"nova 9","brandName":"Huawei","price":13200,"storageGb":128,"ramGb":8,"color":"Starry Blue"},{"id":53,"model":"P50 Pocket","brandName":"Huawei","price":46200,"storageGb":256,"ramGb":8,"color":"Premium Gold"},{"id":54,"model":"nova Y70","brandName":"Huawei","price":9900,"storageGb":128,"ramGb":4,"color":"Crystal Blue"},{"id":55,"model":"Mate 40 Pro","brandName":"Huawei","price":26400,"storageGb":256,"ramGb":8,"color":"Mystic Silver"},{"id":61,"model":"ROG Phone 7","brandName":"ASUS","price":33000,"storageGb":512,"ramGb":16,"color":"Phantom Black"},{"id":62,"model":"ROG Phone 6D","brandName":"ASUS","price":29700,"storageGb":256,"ramGb":16,"color":"Space Gray"},{"id":63,"model":"Zenfone 9","brandName":"ASUS","price":23100,"storageGb":128,"ramGb":8,"color":"Midnight Black"},{"id":64,"model":"ROG Phone 6","brandName":"ASUS","price":29700,"storageGb":256,"ramGb":12,"color":"Storm White"},{"id":65,"model":"Zenfone 8","brandName":"ASUS","price":19800,"storageGb":128,"ramGb":8,"color":"Obsidian Black"},{"id":66,"model":"ROG Phone 5s","brandName":"ASUS","price":26400,"storageGb":256,"ramGb":12,"color":"Phantom Black"},{"id":67,"model":"Zenfone 8 Flip","brandName":"ASUS","price":26400,"storageGb":256,"ramGb":8,"color":"Galactic Black"},{"id":68,"model":"ROG Phone 5","brandName":"ASUS","price":23100,"storageGb":256,"ramGb":12,"color":"Storm White"},{"id":69,"model":"Zenfone 7","brandName":"ASUS","price":19800,"storageGb":128,"ramGb":8,"color":"Aurora Black"},{"id":70,"model":"ROG Phone 3","brandName":"ASUS","price":16500,"storageGb":256,"ramGb":12,"color":"Black Glare"},{"id":76,"model":"Find X6 Pro","brandName":"OPPO","price":33000,"storageGb":256,"ramGb":12,"color":"Cosmos Black"},{"id":77,"model":"Reno9 Pro+","brandName":"OPPO","price":23100,"storageGb":256,"ramGb":12,"color":"Eternal Gold"},{"id":78,"model":"Find N2 Flip","brandName":"OPPO","price":33000,"storageGb":256,"ramGb":8,"color":"Astral Black"},{"id":79,"model":"Reno8 Pro","brandName":"OPPO","price":19800,"storageGb":256,"ramGb":8,"color":"Glazed Green"},{"id":80,"model":"Find X5 Pro","brandName":"OPPO","price":29700,"storageGb":256,"ramGb":12,"color":"Ceramic White"},{"id":81,"model":"A78","brandName":"OPPO","price":9900,"storageGb":128,"ramGb":8,"color":"Glowing Black"},{"id":82,"model":"Reno7","brandName":"OPPO","price":13200,"storageGb":128,"ramGb":8,"color":"Startrails Blue"},{"id":83,"model":"Find X5 Lite","brandName":"OPPO","price":14850,"storageGb":128,"ramGb":8,"color":"Starry Black"},{"id":84,"model":"A77","brandName":"OPPO","price":8250,"storageGb":128,"ramGb":6,"color":"Ocean Blue"},{"id":85,"model":"Reno6 Pro","brandName":"OPPO","price":16500,"storageGb":256,"ramGb":12,"color":"Arctic Blue"}])

function handleQuerySuccess(type, message) {
  if (route.query[type] === 'true') {
    showSuccessMessage.value = true
    successMessage.value = message

    setTimeout(() => {
      showSuccessMessage.value = false
      const updatedQuery = { ...route.query }
      delete updatedQuery[type] // remove query param
      router.replace({ name: route.name, query: updatedQuery })
    }, 3000)
  }
}

onMounted(async() => {
  handleQuerySuccess('added', 'The sale item has been added.')
  handleQuerySuccess('deleted', 'The sale item has been deleted.')
  handleQuerySuccess('failed_delete', 'The requested sale item does not exist.')

  try {
    products.value = await getItems(`http://intproj24.sit.kmutt.ac.th/nw3/api/v1/sale-items`) ?? []
    // products.value = await getItems(`http://localhost:8080/v1/sale-items`) ?? []
  } catch (error) {
    console.error(error)
  }
})

</script>

<template>
  <section id="shop" class="bg-gray-100 py-16 px-4 md:px-10 max-w-7xl mx-auto">
    <h2 class="text-3xl font-extrabold text-gray-900 mb-10 text-center tracking-tight">
      Shop Our Products
    </h2>

    <!-- Success Message -->
    <transition name="fade">
      <div
        v-if="showSuccessMessage"
        class="itbms-message mb-6 p-4 text-sm font-medium text-green-800 bg-green-100 border border-green-300 rounded-lg shadow-sm"
        role="alert"
      >
        {{ successMessage }}
      </div>
    </transition>

    <!-- Add Item Button -->
    <div class="mb-8 text-center">
      <router-link
        :to="{ name: 'AddItem', query: { from: 'Gallery' } }"
        class="itbms-sale-item-add inline-block bg-[#7e5bef] hover:bg-[#6847d5] text-white pl-3 pr-4 py-4 rounded-xl text-base font-semibold shadow-lg transition duration-300"
      >
        <div class="flex justify-between gap-1">
          <span class="material-icons">
            add
          </span>
          Add Item
        </div>
      </router-link>
    </div>

    <!-- No Products -->
    <div v-if="products.length === 0" class="text-center text-gray-500 text-lg py-10">
      no sale item
    </div>

    <!-- Product Grid -->
    <div
      v-else
      class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6"
    >
      <div
        v-for="product in products"
        :key="product.id"
        class="itbms-row bg-white rounded-2xl border border-gray-200 overflow-hidden shadow-md hover:shadow-xl transition duration-300 flex flex-col"
      >
        <router-link
          :to="{ name: 'ListDetails', params: { id: product.id } }"
          class="block h-full flex flex-col"
        >
          <img
            :src="phoneImg"
            :alt="product.model"
            class="w-full h-56 object-contain bg-gray-100"
          />

          <div class="p-5 flex flex-col flex-grow text-center">
            <h3 class="itbms-brand text-sm font-medium text-gray-500 uppercase tracking-wide">
              {{ product.brandName }}
            </h3>

            <p class="text-[#7e5bef] font-semibold mt-1 mb-2 line-clamp-2">
              <span class="itbms-model">{{ product.model }}</span> /
              <span class="itbms-ramGb">{{ product.ramGb ?? '-' }}</span>
              <span class="itbms-ramGb-unit">GB</span> /
              <span class="itbms-storageGb">{{ product.storageGb ?? '-' }}</span>
              <span class="itbms-storageGb-unit">GB</span>
            </p>

            <button
              class="itbms-button mt-auto w-full bg-[#7e5bef] hover:bg-[#6847d5] text-white py-2 rounded-lg font-bold transition"
            >
              Baht <span class="itbms-price">{{ product.price.toLocaleString() }}</span>
            </button>
          </div>
        </router-link>
      </div>
    </div>
  </section>
</template>

