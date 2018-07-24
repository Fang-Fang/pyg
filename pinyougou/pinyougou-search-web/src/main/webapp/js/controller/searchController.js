app.controller("searchController", function ($scope,$location, searchService) {

    //初始化
    $scope.searchMap = {"keywords":"","category":"","brand":"","spec":{},"price":"", "pageNo":1,"pageSize":40, "sortField":"","sort":""};

    //查询
    $scope.search = function () {
        searchService.search($scope.searchMap).success(function (response) {
            $scope.resultMap = response;

            //构造分页导航条
            buildPageInfo();
        });

    };

    //构造分页导航条
    buildPageInfo = function () {

        //总共要显示的页号集合
        $scope.pageNoList = [];

        //要显示的页号总数
        var showPageNoTotal = 5;

        //起始页号
        var startPageNo = 1;
        //结束页号
        var endPageNo = $scope.resultMap.totalPages;

        if($scope.resultMap.totalPages > showPageNoTotal){
            //总页数大于要在导航条中明显显示的页数的话

            //距离页号的左右间隔数
            var interval = Math.floor(showPageNoTotal/2);

            startPageNo = parseInt($scope.searchMap.pageNo) - interval;
            endPageNo = parseInt($scope.searchMap.pageNo) + interval;

            if(startPageNo > 0){
                //起始页号无问题，则只需要判断结束页号是否超过总页数
                if(endPageNo > $scope.resultMap.totalPages){
                    startPageNo = startPageNo - (endPageNo - $scope.resultMap.totalPages);
                    endPageNo = $scope.resultMap.totalPages;
                }
            } else {
                endPageNo = endPageNo - (startPageNo -1);
                startPageNo = 1;
            }
        }

        $scope.frontDot = false;
        if(startPageNo > 1){
            $scope.frontDot = true;
        }
        $scope.backDot = false;
        if(endPageNo < $scope.resultMap.totalPages){
            $scope.backDot = true;
        }


        //遍历要显示的页号
        for (var i = startPageNo; i <= endPageNo; i++) {
            $scope.pageNoList.push(i);
        }

    };

    //添加过滤条件
    $scope.addSearchItem = function (key, value) {
        if("category"==key || "brand"==key||"price"==key){
            $scope.searchMap[key] = value;
        } else {
            //规格
            $scope.searchMap.spec[key] = value;
        }

        $scope.searchMap.pageNo = 1;

        //重新根据最新的过滤条件查询
        $scope.search();
    };

    //移除过滤条件
    $scope.removeSearchItem = function (key) {
        if("category"==key || "brand"==key||"price"==key){
            $scope.searchMap[key] = "";
        } else {
            //规格
            delete $scope.searchMap.spec[key];
        }
        $scope.searchMap.pageNo = 1;

        //重新根据最新的过滤条件查询
        $scope.search();
    };

    //根据页号查询
    $scope.queryByPageNo = function (pageNo) {
        if(0 < pageNo && pageNo <= $scope.resultMap.totalPages){
            $scope.searchMap.pageNo = pageNo;
            $scope.search();
        }
    };

    //设置排序
    $scope.sortSearch = function (sortField, sort) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;

        $scope.search();
    };

    //判断当前的搜索关键字中是否包含品牌
    $scope.isKeywordsBrand = function () {
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            var brand = $scope.resultMap.brandList[i].text;
            if($scope.searchMap.keywords.indexOf(brand) >=0){
                return true;
            }
        }
        return false;
    };


    //加载搜索关键字并搜索
    $scope.loadKeywords = function () {
        //获取请求参数
        $scope.searchMap.keywords = $location.search()["keywords"];
        $scope.search();
    };

});