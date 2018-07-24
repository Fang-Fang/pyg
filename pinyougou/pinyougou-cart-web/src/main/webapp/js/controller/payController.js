app.controller("payController", function ($scope,$location, cartService, payService) {
    $scope.getUsername = function () {
        cartService.getUsername().success(function (response) {
            $scope.username = response.username;
        });
    };

    //生成二维码
    $scope.createNative = function () {
        //支付日志id
        $scope.outTradeNo = $location.search()["outTradeNo"];
        payService.createNative($scope.outTradeNo).success(function (response) {
            if("SUCCESS"==response.result_code){

                //处理总金额
                $scope.totalFee = (response.totalFee/100).toFixed(2);

                //微信统一下单成功；生成二维码
                var qr = new QRious({
                    element:document.getElementById("qrious"),
                    level:"Q",
                    size:250,
                    value:response.code_url
                });


                //查询支付状态
                queryPayStatus();
            } else {
                alert("生成二维码失败");
            }

        });
    };

    //查询支付状态
    queryPayStatus = function () {
        payService.queryPayStatus($scope.outTradeNo).success(function (response) {
            if(response.success){
                //跳转到支付成功的页面
                location.href = "paysuccess.html#?money=" + $scope.totalFee;
            } else {
                if (response.message == "二维码超时") {
                    //alert("二维码超时");
                    $scope.createNative();
                } else {
                    alert(response.message);
                }
            }

        });

    };

    //获取总金额
    $scope.getMoney = function () {
        $scope.money = $location.search()["money"];

    };
});