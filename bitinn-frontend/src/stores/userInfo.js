/**
 * 用户信息状态管理 Store
 * 管理当前登录用户的个人信息，支持持久化存储
 * @author aceFelix
 */
import { defineStore } from "pinia";
import { ref } from "vue";

export const userInfoStore = defineStore("userInfo", () => {
  /** 用户信息对象（id, username, nickname, email, avatar 等） */
  let userInfo = ref({});

  /** 设置用户信息（登录成功或刷新后调用） */
  const setUserInfo = (newUserInfo) => {
    userInfo.value = newUserInfo;
  }

  /** 清除用户信息（登出时调用） */
  const removeUserInfo = () => {
    userInfo.value = {};
  }

  return {
    userInfo,
    setUserInfo,
    removeUserInfo,
  }
},
{
    persist:true // 开启持久化存储，用户信息自动保存到 localStorage
});