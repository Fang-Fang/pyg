app.service("uploadService",function ($http) {

    this.uploadFile = function () {
        //使用了html5的表单数据
        var formData = new FormData();
        //file.files[0] 表示使用页面中id为file的元素，它是一个文件获取第一个文件
        formData.append("file", file.files[0]);
        return $http({
            url:"../upload.do",
            method:"post",
            data:formData,
            headers:{"Content-Type": undefined},
            transformRequest: angular.identity
        });
    };
});