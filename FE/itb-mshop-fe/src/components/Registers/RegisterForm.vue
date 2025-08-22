<script setup>
import { ref } from 'vue'

// active tab
const activeRole = ref('buyer') // Set Default to buyer

// form state
const form = ref({
  nickname: '',
  email: '',
  password: '',
  fullname: '',
  mobile: '',
  bankAccountNo: '',
  bankName: '',
  nationalCardNo: '',
  nationalCardFront: null,
  nationalCardBack: null
})

function setRole(role) {
  activeRole.value = role
  resetForm()
}

function resetForm() {
  form.value = {
    nickname: '',
    email: '',
    password: '',
    fullname: '',
    mobile: '',
    bankAccountNo: '',
    bankName: '',
    nationalCardNo: '',
    nationalCardFront: null,
    nationalCardBack: null
  }
}

function handleFileUpload(event, side) {
  const file = event.target.files[0] || null
  if (file) {
    form.value[side] = {
      file,
      preview: URL.createObjectURL(file)
    }
  }
}

function removeFile(side) {
  form.value[side] = null
}

function handleSubmit() {
  console.log('Submitting form:', activeRole.value, form.value)
}

function handleCancel() {
  resetForm()
}
</script>

<template>
  <div class="max-w-md mx-auto bg-white shadow-md rounded-lg p-6 my-20">
    <!-- Tabs Buyer/Seller -->
    <div class="itbms-account-type flex mb-6 border-b">
      <button
        @click="setRole('buyer')"
        :class="[
          'flex-1 py-2 text-center font-semibold',
          activeRole === 'buyer'
            ? 'bg-orange-500 text-white rounded-t-lg'
            : 'bg-gray-200 text-gray-600 rounded-t-lg'
        ]"
      >
        Buyer
      </button>
      <button
        @click="setRole('seller')"
        :class="[
          'flex-1 py-2 text-center font-semibold',
          activeRole === 'seller'
            ? 'bg-orange-500 text-white rounded-t-lg'
            : 'bg-gray-200 text-gray-600 rounded-t-lg'
        ]"
      >
        Seller
      </button>
    </div>

    <!-- Buyer form -->
    <form v-if="activeRole === 'buyer'" @submit.prevent="handleSubmit" class="space-y-4">
      <div>
        <label class="itbms-nickname block text-sm font-medium">Nickname</label>
        <input v-model="form.nickname" type="text" class="mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200" />
      </div>

      <div>
        <label class="itbms-email block text-sm font-medium">Email</label>
        <input v-model="form.email" type="email" class="mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200" />
      </div>

      <div>
        <label class="itbms-password block text-sm font-medium">Password</label>
        <input v-model="form.password" type="password" class="mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200" />
      </div>

      <div>
        <label class="itbms-fullname block text-sm font-medium">Fullname</label>
        <input v-model="form.fullname" type="text" class="mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200" />
      </div>

      <div class="flex gap-2 pt-4">
        <button type="submit" class="itbms-submit-button flex-1 bg-green-500 text-white py-2 rounded-md hover:bg-green-600">Submit</button>
        <button type="button" @click="handleCancel" class="itbms-cancel-button flex-1 bg-gray-300 text-gray-700 py-2 rounded-md hover:bg-gray-400">Cancel</button>
      </div>
    </form>

    <!-- Seller form -->
    <form v-else @submit.prevent="handleSubmit" class="space-y-4">
      <div>
        <label class=" block text-sm font-medium">Nickname</label>
        <input v-model="form.nickname" type="text"
          class="itbms-nickname mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200" />
      </div>

      <div>
        <label class=" block text-sm font-medium">Email</label>
        <input v-model="form.email" type="email"
          class="itbms-email mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200" />
      </div>

      <div>
        <label class="block text-sm font-medium">Password</label>
        <input v-model="form.password" type="password"
          class="itbms-password mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200" />
      </div>

      <div>
        <label class="block text-sm font-medium">Fullname</label>
        <input v-model="form.fullname" type="text"
          class="itbms-fullname mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200" />
      </div>

      <div>
        <label class="block text-sm font-medium">Mobile</label>
        <input v-model="form.mobile" type="text"
          class="itbms-mobile mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200" />
      </div>

      <div>
        <label class="block text-sm font-medium">Bank Account No</label>
        <input v-model="form.bankAccountNo" type="text"
          class="itbms-bank-account-no mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200" />
      </div>

      <div>
        <label class="block text-sm font-medium">Bank Name</label>
        <input v-model="form.bankName" type="text"
          class="itbms-back-name mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200" />
      </div>

      <div>
        <label class="block text-sm font-medium">National Card No</label>
        <input v-model="form.nationalCardNo" type="text"
          class="itbms-card-no mt-1 w-full border rounded-md px-3 py-2 focus:ring focus:ring-orange-200" />
      </div>

      <!-- National Card Photo Section -->
      <div>
        <label class="block text-sm font-medium mb-2">National Card Photo</label>
        <div class="flex gap-4">
          
          <!-- FRONT -->
          <div
            class="relative flex-1 h-32 border-2 border-dashed rounded-md flex items-center justify-center text-gray-500 cursor-pointer hover:border-orange-400 overflow-hidden group"
          >
            <!-- Show preview if exists -->
            <template v-if="form.nationalCardFront">
              <img :src="form.nationalCardFront.preview" alt="Front Preview" class="object-cover w-full h-full" />
              <!-- Hover overlay with X -->
              <button
                type="button"
                @click="removeFile('nationalCardFront')"
                class="absolute inset-0 bg-red-500 bg-opacity-50 flex items-center justify-center text-white text-xl opacity-0 hover:opacity-60 transition"
              >
                ✕
              </button>
            </template>
            <!-- Show placeholder if no image -->
            <template v-else>
              <span>Front</span>
              <input type="file" accept="image/*" 
                class="itbms-card-photo-front absolute inset-0 opacity-0 cursor-pointer"
                @change="e => handleFileUpload(e, 'nationalCardFront')" />
            </template>
          </div>

          <!-- BACK -->
          <div
            class="relative flex-1 h-32 border-2 border-dashed rounded-md flex items-center justify-center text-gray-500 cursor-pointer hover:border-orange-400 overflow-hidden group"
          >
            <!-- Show preview if exists -->
            <template v-if="form.nationalCardBack">
              <img :src="form.nationalCardBack.preview" alt="Back Preview" class="object-cover w-full h-full" />
              <!-- Hover overlay with X -->
              <button
                type="button"
                @click="removeFile('nationalCardBack')"
                class="absolute inset-0 bg-red-500 bg-opacity-50 flex items-center justify-center text-white text-xl opacity-0 group-hover:opacity-60 transition"
              >
                ✕
              </button>
            </template>
            <!-- Show placeholder if no image -->
            <template v-else>
              <span>Back</span>
              <input type="file" accept="image/*" 
                class="itbms-card-photo-back absolute inset-0 opacity-0 cursor-pointer"
                @change="e => handleFileUpload(e, 'nationalCardBack')" />
            </template>
          </div>

        </div>
      </div>

      <!-- Buttons -->
      <div class="flex gap-2 pt-4">
        <button type="submit"
          class="flex-1 bg-green-500 text-white py-2 rounded-md hover:bg-green-600">Submit</button>
        <button type="button" @click="handleCancel"
          class="flex-1 bg-gray-300 text-gray-700 py-2 rounded-md hover:bg-gray-400">Cancel</button>
      </div>
    </form>

  </div>
</template>
