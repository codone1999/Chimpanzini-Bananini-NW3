<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// Mock user data (replace with API call)
const user = ref({
  nickName: 'JohnDoe',
  email: 'john@example.com',
  fullName: 'John Doe',
  mobile: '123456789',
  bankAccountNo: '987654321',
  bankName: 'Bank of Vue',
  nationalCardNo: 'AB123456',
  nationalCardFront: '/images/card-front.jpg',
  nationalCardBack: '/images/card-back.jpg',
  role: 'buyer'
})

function goToEdit() {
  router.push({ name: 'EditProfile' })
}
</script>

<template>
  <div class="max-w-3xl mx-auto bg-white shadow-lg rounded-xl overflow-hidden my-12">
    <!-- Header -->
    <div class="bg-gradient-to-r from-purple-600 to-indigo-600 p-6 flex items-center space-x-4">
      <!-- Avatar with initial -->
      <div class="w-16 h-16 rounded-full bg-white flex items-center justify-center text-purple-600 font-bold text-2xl shadow-md">
        {{ user.nickName.charAt(0).toUpperCase() }}
      </div>
      <div>
        <h2 class="text-2xl font-bold text-white">{{ user.fullName }}</h2>
        <p class="text-purple-100 text-sm">@{{ user.nickName }}</p>
      </div>
    </div>

    <!-- Body -->
    <div class="p-6 space-y-6">
      <!-- Basic Info -->
      <div>
        <h3 class="text-lg font-semibold text-gray-700 border-b pb-2 mb-4">Account Information</h3>
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-y-3 gap-x-6 text-gray-600">
          <p><span class="font-medium">Email:</span> {{ user.email }}</p>
          <p><span class="font-medium">Full Name:</span> {{ user.fullName }}</p>
          <p v-if="user.role === 'seller'"><span class="font-medium">Mobile:</span> {{ user.mobile }}</p>
          <p v-if="user.role === 'seller'"><span class="font-medium">Bank Account:</span> {{ user.bankAccountNo }}</p>
          <p v-if="user.role === 'seller'"><span class="font-medium">Bank Name:</span> {{ user.bankName }}</p>
          <p v-if="user.role === 'seller'"><span class="font-medium">National Card No:</span> {{ user.nationalCardNo }}</p>
        </div>
      </div>

      <!-- Card Images -->
      <div v-if="user.role === 'seller'">
        <h3 class="text-lg font-semibold text-gray-700 border-b pb-2 mb-4">National Card</h3>
        <div class="flex gap-6">
          <div class="flex-1 text-center">
            <p class="text-sm font-medium text-gray-500 mb-2">Front</p>
            <img :src="user.nationalCardFront" class="w-full h-40 object-cover rounded-lg border shadow-sm" />
          </div>
          <div class="flex-1 text-center">
            <p class="text-sm font-medium text-gray-500 mb-2">Back</p>
            <img :src="user.nationalCardBack" class="w-full h-40 object-cover rounded-lg border shadow-sm" />
          </div>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <div class="bg-gray-50 px-6 py-4 flex justify-end">
      <button
        @click="goToEdit"
        class="px-6 py-2 bg-purple-600 text-white font-medium rounded-md shadow hover:bg-purple-700 transition"
      >
        Edit Profile
      </button>
    </div>
  </div>
</template>
