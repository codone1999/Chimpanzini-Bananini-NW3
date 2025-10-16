import { createRouter, createWebHistory } from "vue-router";
import Home from "@/views/Home.vue";
import ListGallery from "@/components/sale-items/ListGallery.vue";
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

export default router
