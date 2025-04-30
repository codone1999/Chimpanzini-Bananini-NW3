import { createRouter, createWebHistory } from "vue-router";
import MainMenu from "@/components/MainMenu.vue";
import ListGallery from "@/components/ListGallery.vue";
import ListDetails from "@/components/ListDetails.vue";

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
]

const router = createRouter({history, routes})
export default router
