<template>
    <v-container>
        <v-row justify="center">
            <v-card>
                <v-card-title class="text-center text-h5">
                    회원정보
                </v-card-title>
                <v-card-text>
                    <v-table>
                        <tbody>
                            <tr>
                                <td>ID:</td><td>{{ memberInfo.id }}</td>
                            </tr>
                            <tr>
                                <td>이름:</td><td>{{ memberInfo.name }}</td>
                            </tr>
                            <tr>
                                <td>EMAIL:</td><td>{{ memberInfo.email }}</td>
                            </tr>
                            <tr>
                                <td>주문건수:</td><td>{{ memberInfo.orderCount }}</td>
                            </tr>
                        </tbody>
                    </v-table>
                </v-card-text>
            </v-card>
        </v-row>
    </v-container>
    <OrderListComponent
    :isMyPage="true"
    />
</template>
<script>
import OrderListComponent from '@/components/OrderListComponent.vue';
import axios from 'axios';

export default{
    data(){
        return{
            memberInfo:""
        }
    },
    async created(){
        try{
            const memberInfo = await axios.get(`${process.env.VUE_APP_API_BASE_URL}/member/myinfo`);
            this.memberInfo = memberInfo.data;
        }catch(e){
            console.log(e);
        }
    },
    components:{
        OrderListComponent
    }
}
</script>