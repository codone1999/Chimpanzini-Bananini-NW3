import { createRouter, createWebHistory } from "vue-router";
import MainMenu from "@/components/MainMenu.vue";
import ListGallery from "@/components/ManageSaleItems/Gallery/ListGallery.vue";
import ListDetails from "@/components/ManageSaleItems/ListDetail.vue";
import AddItem from "@/components/ManageSaleItems/AddItem.vue";
import EditItem from "@/components/ManageSaleItems/EditItem.vue";
import ListSaleItems from "@/components/ManageSaleItems/ListSaleItems.vue";
import ListBrands from "@/components/ManageBrands/ListBrands.vue";
import AddBrand from "@/components/ManageBrands/AddBrand.vue";
import EditBrand from "@/components/ManageBrands/EditBrand.vue";
import PageNotFound from "@/components/PageNotFound.vue";
import RegisterForm from "@/components/Registers/RegisterForm.vue";
import LogInForm from "@/components/Registers/LogInForm.vue";
import ShowVerify from "@/components/Registers/VerifyEmail.vue";

const routes = [
    {
        path: '/',
        name: 'MainMenu',
        component: MainMenu
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
