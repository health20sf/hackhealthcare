import Vue from 'vue'
import Router from 'vue-router'
import Home from '@/components/Home'
import CreateProject from '@/components/CreateProject'

Vue.use(Router)

export default new Router({
  routes: [
    // {
    //   path: '/project/:project', // example of how to pass parameter
    //   name: 'editor',
    //   component: Editor,
    //   props: true
    // },
    { path: '/', 
      name: 'home',
      component: Home,
    },
    { path: '/createProject', 
      name: 'createProject',
      component: CreateProject,
  }
  ]
})
