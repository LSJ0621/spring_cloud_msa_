<template>
<h1>store component A</h1>
<p>일반변수값 count:{{ count }}</p>
<v-btn @click="incrementCount()">count값 증가</v-btn>
<p>store변수값 count:{{ getStoreCount }}</p>
<v-btn @click="incrementStoreCount()">store count값 증가</v-btn>
<input type="text" v-model="message">
<v-btn @click="updateMessage()">store 메세지 변경</v-btn>
<p>update message:{{ getMessage }}</p>


</template>
<script>
export default{
    data(){
        return{
            count:0,
            message:""
        }
    },
    computed:{
        getStoreCount(){
            return this.$store.getters.getCount;
        },
        getMessage(){
            return this.$store.getters.getMessage;
        }
    },
    methods:{
        incrementCount(){
            this.count++;
        },
        incrementStoreCount(){
            // vuex에서 actions를 호출할 때는 dispatch를 사용
            this.$store.dispatch('increment');
        },
        updateMessage(){
            this.$store.dispatch('updateMessage',this.message);
        }
    }
}
</script>