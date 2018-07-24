app.controller("cartController", function ($scope, cartService) {
    $scope.getUsername = function () {
        cartService.getUsername().success(function (response) {
            $scope.username = response.username;
        });
    };

    //获取购物车列表
    $scope.findCartList = function () {
        cartService.findCartList().success(function (response) {
            $scope.cartList = response;

            //计算总数量和总价格
            $scope.totalValue = cartService.sumTotalValue(response);
        });

    };
    
    //加入购物车
    $scope.addItemToCartList = function (itemId, num) {
        cartService.addItemToCartList(itemId, num).success(function (response){
            if(response.success){
                //加入购物车成功，刷新列表
                $scope.findCartList();
            } else {
                alert(response.message);
            }
        });

    };
});