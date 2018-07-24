//定义service
app.service("brandService", function ($http) {
    //查询所有品牌列表；this表示brandService
    this.findAll = function () {
        return $http.get("../brand/findAll.do");
    };

    //分页查询
    this.findPage = function (page, rows) {
        return $http.get("../brand/findPage.do?page=" + page + "&rows=" + rows);

    };

    //保存
    this.add = function (entity) {
        return $http.post("../brand/add.do", entity);
    };

    //更新
    this.update = function (entity) {
        return $http.post("../brand/update.do", entity);
    };

    //根据id查询
    this.findOne = function (id) {
        return $http.get("../brand/findOne.do?id=" + id);
    };

    //批量删除
    this.delete = function (selectedIds) {
        return $http.get("../brand/delete.do?ids=" + selectedIds);

    };

    //分页条件查询
    this.search = function (page, rows, searchEntity) {
        return $http.post("../brand/search.do?page=" + page + "&rows=" + rows, searchEntity);
    };

    //获取符合select2组件的品牌列表数据
    this.selectOptionList = function () {
        return $http.get("../brand/selectOptionList.do");
    };
});