$(function(){
    // console.log("-----")
    var vm = new Vue({
        el:'#rrapp',
        data:{
            processId:'',
            leave:{
                startDate:'',
                endDate:'',
                reason:''
            }
        },
        created(){
            this.getParams()
        },
        watch: {
            // '$route': 'getParams'
        },
        methods:{
            getParams:function(){
                // 取到路由带过来的参数
                // 将数据放在当前组件的数据内
                this.processId = this.getQueryString("processId")
            },
            getQueryString:function(name){
                var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
                console.log(window.location.search)
                var r = window.location.search.substr(1).match(reg);
                if (r != null) return unescape(r[2]);
                return null;
            },

            submitForm:function(){
                var data = "processId="+this.processId;
                // console.log(this.leave.reason)
                // console.log(this.processId)
                $.ajax({
                    type: "POST",
                    url: "http://localhost:8080/admin/submitLeave",
                    data: data,
                    dataType: "json",
                    success: function(result){
                        // if(result.code == 0){//登录成功
                        //     parent.location.href ='index.html';
                        // }else{
                        //     vm.error = true;
                        //     vm.errorMsg = result.msg;
                        //     vm.refreshCode();
                        // }
                        console.log("---test success---")
                    }
                });
            }
        }
    });
})