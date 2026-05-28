/**
 * Token 状态管理 Store
 * 管理用户登录凭证的存储、设置与清除，支持持久化存储
 * @author aceFelix
 */
import { defineStore } from "pinia";
import { ref } from "vue";

export const useTokenStore = defineStore("token", ()=>{
    /** JWT Token 字符串 */
    let token = ref('');

    /** 设置 Token */
    const setToken = (newToken)=>{
        token.value = newToken;
    }

    /** 移除 Token（登出时调用） */
    const removeToken = ()=>{
        token.value = '';
    }

    return {
        token,
        setToken,
        removeToken
    }
},
{
    persist:true // 开启持久化存储，Token 自动保存到 localStorage
});