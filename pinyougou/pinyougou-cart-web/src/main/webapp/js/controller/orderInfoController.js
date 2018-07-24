app.controller("orderInfoController", function ($scope, cartService, addressService) {
    $scope.getUsername = function () {
        cartService.getUsername().success(function (response) {
            $scope.username = response.username;
        });
    };

    //获取当前登录用户的地址列表
    $scope.findAddressList = function () {
        addressService.findAddressList().success(function (response) {
            $scope.addressList = response;

            //获取默认的地址
            for (var i = 0; i < response.length; i++) {
                var address = response[i];
                if(address.isDefault=="1"){
                    $scope.address = address;
                }
            }
        });

    };

    //选择地址
    $scope.selectAddress = function (address) {
        $scope.address = address;
    };

    //判断地址是否为当前选择的地址
    $scope.isSelectedAddress = function (address) {
        return $scope.address==address;
    };

    // 订单；默认为微信支付
    $scope.order = {"paymentType":"1"};

    //选择支付方式
    $scope.selectPaymentType = function (paymentType) {
        $scope.order.paymentType = paymentType;
    };

    //获取当前登录用户的购物车列表
    $scope.findCartList = function () {
        cartService.findCartList().success(function (response) {
            $scope.cartList = response;
            //计算总数量和总价格
            $scope.totalValue = cartService.sumTotalValue(response);
        });

    };

    //提交订单
    $scope.submitOrder = function () {
        $scope.order.receiver = $scope.address.contact;//收件人
        $scope.order.receiverMobile = $scope.address.mobile;//收件人手机号
        $scope.order.receiverAreaName = $scope.address.address;//收件地址
        cartService.submitOrder($scope.order).success(function (response) {
            if(response.success){
                //如果是微信支付则跳转到支付页面
                if("1"==$scope.order.paymentType){
                    location.href = "pay.html#?outTradeNo=" + response.message;
                } else {
                    //货到付款则跳转到支付成功页面
                    location.href = "paysuccess.html";
                }
            } else {
                alert(response.message);
            }

        });
    };
});