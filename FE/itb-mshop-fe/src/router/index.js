import { createRouter, createWebHistory } from "vue-router";
import { initializeAuth, isAuthenticated } from '@/lib/authUtils'
import Home from "@/views/Home.vue";
import ListGallery from "@/components/sale-items/gallery/ListGallery.vue";
import ListDetails from "@/components/sale-items/ListDetail.vue";
import AddItem from "@/components/sale-items/AddItem.vue";
import EditItem from "@/components/sale-items/EditItem.vue";
import ListSaleItems from "@/components/sale-items/ListSaleItems.vue";
import ListBrands from "@/components/brands/ListBrands.vue";
import AddBrand from "@/components/brands/AddBrand.vue";
import EditBrand from "@/components/brands/EditBrand.vue";
import PageNotFound from "@/components/common/PageNotFound.vue";
import RegisterForm from "@/components/auth/RegisterForm.vue";
import LogInForm from "@/components/auth/LogInForm.vue";
import ShowVerify from "@/components/auth/VerifyEmail.vue";
import PreviewProfile from "@/views/profile/PreviewProfile.vue";
import EditProfile from "@/views/profile/EditProfile.vue";
import Cart from "@/views/order/Cart.vue";
import OrderHistory from "@/views/order/OrderHistory.vue";
import OrderDetail from "@/views/order/OrderDetail.vue";
import ResetPassword from "@/views/password-reset/ResetPassword.vue";
import VerifyResetCode from "@/views/password-reset/VerifyResetCode.vue";
import RequestReset from "@/views/password-reset/RequestReset.vue";

const routes = [
    {
        path: '/',
        name: 'MainMenu',
        component: Home
    },
    {
        path: '/sale-items',
        name: 'ListGallery',
        component: ListGallery
    },
    {
        path: '/sale-items/:id',
        name: 'ListDetails',
        component: ListDetails
    },
    {
        path: '/sale-items/add',
        name: 'AddItem',
        component: AddItem
    },
    {
        path: '/sale-items/:id/edit',
        name: 'EditItem',
        component: EditItem
    },
    {
        path: '/sale-items/list',
        name: 'ListSaleItems',
        component: ListSaleItems
    },
    {
        path: '/brands',
        name: 'ListBrands',
        component: ListBrands
    },
    {
        path: '/brands/add',
        name: 'AddBrand',
        component: AddBrand
    },
    {
        path: '/brands/:id/edit',
        name: 'EditBrand',
        component: EditBrand
    },
    {
        path: '/register',
        name: 'Register',
        component: RegisterForm
    },
    {
        path: '/signin',
        name: 'Login',
        component: LogInForm
    },
    {
        path: '/verify-email',
        name: 'VerifyEmail',
        component: ShowVerify
    },
    {
        path: '/profile',
        name: 'Profile',
        component: PreviewProfile
    },
    {
        path: '/profile/edit',
        name: 'EditProfile',
        component: EditProfile
    },
    {
        path: '/cart',
        name: 'Cart',
        component: Cart
    },
    {
        path: '/your-orders',
        name: 'Orders',
        component: OrderHistory
    },
    {
        path: '/your-orders/:id',
        name: 'OrderDetail',
        component: OrderDetail
    },
    {
        path: '/request-reset-email',
        name: 'ResetEmail',
        component: RequestReset
    },
    {
        path: '/verify-reset-code',
        name: 'ResetCode',
        component: VerifyResetCode
    },
    {
        path: '/reset-password',
        name: 'ResetPassword',
        component: ResetPassword
    },
    {
        path: '/:NotFound(.*)',
        name: 'NotFound',
        component: PageNotFound
    }
]

const router = createRouter({
    history: createWebHistory('/nw3/'), 
    routes
})

// Global navigation guard
router.beforeEach(async (to, from, next) => {
  const publicPages = ['Login', 'Register', 'ResetEmail', 'ResetCode', 'ResetPassword'] // Add your public route names
  const authRequired = !publicPages.includes(to.name)
  
  // Try to initialize auth (refresh token if needed)
  const isAuth = await initializeAuth(import.meta.env.VITE_APP_URL2)
  
  if (authRequired && !isAuth) {
    // Redirect to login if auth is required but user is not authenticated
    return next({ name: 'Login' })
  }
  
  if (!authRequired && isAuth) {
    // If already authenticated and trying to access login/register, redirect to home
    return next({ name: 'ListGallery' })
  }
  
  next()
})

export default router
