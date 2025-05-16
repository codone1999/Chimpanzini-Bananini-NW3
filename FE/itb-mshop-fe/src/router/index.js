import { createRouter, createWebHistory } from "vue-router";
import MainMenu from "@/components/MainMenu.vue";
import ListGallery from "@/components/ListGallery.vue";
import ListDetails from "@/components/ListDetails.vue";
import AddItem from "@/components/AddItem.vue";
import EditItem from "@/components/EditItem.vue";
import ListSaleItem from "@/components/ListSaleItem.vue";
import BrandList from "@/components/BrandList.vue";
import AddBrand from "@/components/AddBrand.vue";
import EditBrand from "@/components/EditBrand.vue";

const history = createWebHistory()
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
        name: 'ListSaleItem',
        component: ListSaleItem
    },
    {
        path: '/brands',
        name: 'BrandList',
        component: BrandList
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
    }
]

const router = createRouter({history, routes})
export default router
