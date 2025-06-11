<template>
  <a-layout class="layout-demo">
    <a-layout>
      <a-layout-header style="padding-left: 20px; font-size: 25px; font-weight: bold;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          用户管理
          <div style="right: 20px; font-size: 15px; margin-right: 30px;">
            <a-popover position="bottom">
              你好，管理员
              <a-button style="background: white; top: 15px;">
                <a-avatar :style="{ backgroundColor: '#3370ff' }">
                  <IconUser />
                </a-avatar>
              </a-button>
              <template #content>
                <div style="display: flex; flex-direction: column; align-items: center;">
                  <a-button style="background: white;margin-bottom: 10px" @click="handleClick">退出系统</a-button>
                </div>
              </template>
            </a-popover>

            <a-modal v-model:visible="visibleexit" @ok="handleOk" @cancel="handleCancel">
              <template #title>
                提示
              </template>
              <div style="align-items: center; text-align: center;">确认退出该系统吗</div>
            </a-modal>
          </div>
        </div>
      </a-layout-header>

      <a-layout style="padding: 0 24px;">
        <a-breadcrumb :style="{ margin: '16px 0',height: '24px' }">
        </a-breadcrumb>

        <a-space style="position: absolute; right: 40px; margin: 16px 0;">
          <a-button type="primary" @click="onAdd">
            <template #default>新增用户</template>
          </a-button>
          <a-modal v-model:visible="visibleadd" @ok="handleSubmit">
            <template #title>新增用户</template>
            <!-- 新增管理员内容 -->
            <a-form ref="formRef" :size="form.size" :model="form" :style="{width:'100%'}" @submit="handleSubmit">
              <a-form-item field="form.username" label="用户名">
                <a-input v-model="form.username" :style="{width:'320px'}" :size="form.size" placeholder="请输入用户名"></a-input>
              </a-form-item>
              <a-form-item field="form.password" label="密码">
                <a-input v-model="form.password" :style="{width:'320px'}" :size="form.size" placeholder="请输入密码"></a-input>
              </a-form-item>
            </a-form>
          </a-modal>
        </a-space>

        <a-layout-content>
          <a-table :columns="columns"
                   :data="data"
                   :pagination="false"
                   :bordered="{wrapper: true, cell: true}"
                   :sticky-header="0"
                   :stripe="true"
                   :style="{fontSize: '16px',height: '99%',fontFamily:'微软雅黑'}"
          >
            <!--            编辑按钮    -->
            <template #edit="{ record }">
<!--              <a-space style="margin-right: 5px">-->
<!--                <a-button type="primary" status="success" v-if="!record.editable" @click="handleEdit(record)">编辑</a-button>-->
<!--                <a-button type="primary" status="success" v-else @click="handleSave(record)">保存</a-button>-->
<!--              </a-space>-->
              <a-space>
                <a-button type="primary" status="danger" @click="handleDelete(record)">删除</a-button>
              </a-space>
            </template>
          </a-table>
          <a-modal v-model:visible="deleteedit" @ok="handleDeleteOk" @cancel="handleDeleteCancel">
            <template #title>
              提示
            </template>
            <div style="align-items: center; text-align: center;">确认删除此账号？</div>
          </a-modal>
        </a-layout-content>
      </a-layout>
    </a-layout>
  </a-layout>
</template>

<script>
import {defineComponent,onMounted,reactive, ref} from 'vue';
import {
  IconUser,
} from '@arco-design/web-vue/es/icon';
import {router} from "@/router";
import axios from "axios";
import {Message} from "@arco-design/web-vue";

export default defineComponent({
  components: {
    IconUser,
  },
  methods: {
    onClickMenuItem(key) {
      this.$router.push(key);
    },
  },
  setup(){
    //编辑按钮
    // const handleEdit = (record) => {
    //   record.editable = true;
    // };
    // const handleSave = (record) => {
    //   record.editable = false;
    //
    //   const user_id=record.user_id;
    //   axios.post(`/updateUserManage/${user_id}`)
    //       .then(res => {
    //         console.log("更新成功", res.data)
    //         Message.success({content: '更新成功', duration: 2000, showIcon: true});
    //       })
    //       .catch(err => {
    //         console.log("更新失败", err);
    //         Message.warning({content: '更改失败，请重试', duration: 2000, showIcon: true});
    //       })
    // };

    //存储当前点击的记录信息
    const currentRecord = ref(null);

    //删除按钮
    const deleteedit=ref(false);
    const handleDelete =(record)=> {
      currentRecord.value=record;
      deleteedit.value=true;
    };
    const handleDeleteOk=()=>{
      const user_id=currentRecord.value.user_id
      axios.post(`/deleteUser/${user_id}`)
          .then(res => {
            console.log("用户删除成功", res.data)
            Message.success({content: '删除成功', duration: 2000, showIcon: true});
          })
          .catch(err => {
            console.log("用户删除失败", err);
            Message.warning({content: '删除失败', duration: 2000, showIcon: true});
          })
    };
    const handleDeleteCancel=()=>{
      deleteedit.value=false;
    };

    //添加用户
    const visibleadd=ref(false);
    const onAdd = () => {
      visibleadd.value = true;
    };
    const handleSubmit=()=>{
      // 在控制台输出表单数据
      const username=form.username;
      const password=form.password;
      console.log(username,password)

      axios.post(`insertUser/${username}/${password}`)
          .then(res => {
            console.log("用户添加成功", res.data)
            Message.success({content: '用户添加成功', duration: 2000, showIcon: true});
          })
          .catch(err => {
            console.log("用户添加失败", err);
            Message.warning({content: '用户添加失败', duration: 2000, showIcon: true});
          })

      visibleadd.value = false;
    };

    //获取表格数据
    const fetchData = async () => {
      try {
        const response = await axios.get('/getAllUsers');
        if (response.data && Array.isArray(response.data)) {
          console.log(response.data);
          response.data.forEach(item => {
            data.push({
              user_id: item.userId,
              username: item.username,
              password: item.password,
              usertype: item.userType,
            })
          })
        }
      } catch (err) {
        console.log(err);
      }
    };

    //数据
    const form=reactive({
      size: 'medium',
      userid:'',
      username:'',
      password:'',
      usertype:''
    });
    const data=reactive([]);
    const columns=[
      { title: '用户名',
        dataIndex: 'username',
        width:'350'
      },
      { title: '用户类型',
        dataIndex: 'usertype' ,
        slotName:'usertype',
        width:'250'
      },
      { title: '编辑',
        dataIndex: 'edit' ,
        slotName:'edit',
        width:'250'
      },
    ];


    //fetchData();
    onMounted(() => {
      fetchData()
    })


    //更改密码
    const username=ref(
        'admin'
    )
    const pwdmodify=ref(false);
    const oldPassword=ref('');
    const newPassword=ref('');
    const handlePassword=()=>{
      pwdmodify.value = true;
    };
    const handlePasswordOk = () => {
      const oldPwd=oldPassword.value;
      const newPwd=newPassword.value;
      axios.post(`/passwordModify/${username.value}/${oldPwd}/${newPwd}`)
          .then(res => {
            console.log(res.data)
            if(res.data===0){
              Message.warning({ content: '旧密码错误', duration: 2000, showIcon: true });
            }
            else if(res.data===1){
              Message.success({ content: '密码修改成功', duration: 2000, showIcon: true });
            }
            else{
              Message.warning({ content: '密码修改失败', duration: 2000, showIcon: true });
            }
          })
          .catch(err => {
            console.log("密码修改失败", err);
            Message.warning({ content: '密码修改失败', duration: 2000, showIcon: true });
          });
      oldPassword.value='';
      newPassword.value='';
    };
    const handlePasswordCancel = () => {
      pwdmodify.value = false;
    }
    //点击退出系统
    const visibleexit=ref(false);//退出系统
    const handleClick = () => {
      visibleexit.value = true;
    };
    const handleOk = () => {
      router.push('/');
    };
    const handleCancel = () => {
      visibleexit.value = false;
    };

    return {
      currentRecord,
      data,
      columns,
      fetchData,
      handleDeleteOk,
      handleDeleteCancel,
      deleteedit,
      handleDelete,
      // handleSave,
      // handleEdit,
      visibleexit,
      visibleadd,
      form,
      handleClick,
      handleOk,
      handleCancel,
      onAdd,
      handleSubmit,
      oldPassword,
      newPassword,
      handlePassword,
      handlePasswordOk,
      handlePasswordCancel,
      pwdmodify,
      username
    }
  }
});
</script>
<style scoped>
.layout-demo {
  height: 100vh;
  background: var(--color-fill-2);
}
.layout-demo :deep(.arco-layout-header)  {
  height: 64px;
  line-height: 64px;
  background: var(--color-bg-3);
}
.layout-demo :deep(.arco-layout-footer) {
  height: 48px;
  color: var(--color-text-2);
  font-weight: 400;
  font-size: 14px;
  line-height: 48px;
}
.layout-demo :deep(.arco-layout-content) {
  color: var(--color-text-2);
  font-weight: 400;
  font-size: 14px;
  background: var(--color-bg-3);
}
.layout-demo :deep(.arco-layout-footer),
.layout-demo :deep(.arco-layout-content)  {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  color: var(--color-white);
  font-size: 16px;
  font-stretch: condensed;
  text-align: center;
}
</style>
