<template>
  <!-- router-view를 통해 router 파일에서 설정한 url 경로로 라우팅한 화면을 여기서 보여주겠다는 뜻. -->
   <!-- vuetify에서는 모든 태그의 최상단에 v-app으로 감싸져있어야 정상적으로 동작 -->
   <v-app>
    <HeaderComponent/>
    <v-main>
      <router-view/>
    </v-main>
    <FooterComponent/>
  </v-app>
</template>

<script>
import HeaderComponent from './components/HeaderComponent.vue'
import FooterComponent from './components/FooterComponent.vue'
export default{
  // 화면을 import 한 이후에 template에서 사용하려고 할 때는 components에 나열.
  components:{
    HeaderComponent,FooterComponent
  }
}
</script>
