<template>
    <v-container>
        <v-row justify="center">
            <v-col class="text-center text-h5">
                장바구니 목록
            </v-col>
        </v-row>
        <v-row justify="space-between">
            <v-col cols="auto">
                <v-btn color="secondary" @click="clearCart()">장바구니 비우기</v-btn>
            </v-col>
            <v-col cols="auto">
                <v-btn color="primary" @click="orderCreate()">주문하기</v-btn>
            </v-col>
        </v-row>
        <v-row>
            <v-col>
                <v-table>
                    <thead>
                        <tr>
                            <th>제품ID</th>
                            <th>제품명</th>
                            <th>주문 수량</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="product in getProductsInCart" :key="product.productId">
                            <td>{{ product.productId }}</td>
                            <td>{{ product.name }}</td>
                            <td>{{ product.productCount }}</td>
                        </tr>
                    </tbody>
                </v-table>
            </v-col>
        </v-row>
    </v-container>
</template>
<script>
import axios from 'axios';

export default{
    computed:{
        getProductsInCart(){
            return this.$store.getters.getProductsInCart;
        }
    },
    methods:{
        clearCart(){
            this.$store.dispatch('clearCart');
        },
        async orderCreate(){
            const orderData = this.getProductsInCart.map(p=>{return {productId:p.productId,productCount:p.productCount}});
            try{
                await axios.post(`${process.env.VUE_APP_API_BASE_URL}/ordering/create`,orderData);
                alert("주문이 정상적으로 처리되었습니다.");
                this.clearCart();
            }catch(e){
                console.log(e);
                alert("주문에 실패하였습니다.")
            }
        }
    }
}
</script>