$(document).ready(function() {
    categorySection();
    sortSection();
    pagination();
});

//카테고리 선택기능
function categorySection() {
    // 카테고리 탭 클릭 이벤트 위임
    $(document).on('click', '.categoryTab', function () {
        // 모든 카테고리에서 'active' 클래스 제거
        $('.categoryTab').removeClass('active');

        // 현재 클릭된 탭에 'active' 클래스 추가
        $(this).addClass('active');

        //1페이지로 선택한 카테고리 게시글들을 가져옴
        postListReload();
    });
}

function sortSection(){
    $(document).on('change', '.sortSelect', function () {
        //1페이지로 정렬기준을 적용해서 게시글들을 가져옴
        postListReload();
    });
}

//페이지네이션 : 페이지버튼들 동작
function pagination() {
    $(document).on("click", "#prevPage, #nextPage, .pageNumber", function () {
        const pageIdx = $(this).data("pageidx")
        postListReload(pageIdx)
    })
}

function postListReload(pageIdx = 1){
    const category = $(".categoryTab.active").data("category"); //ALL, FREE, NEWS, DISCUSSION
    const sort = $(".sortSelect").val() //latest, popular, review
    console.log(category);
    console.log(sort);

    $.ajax({
        url: "/community",
        method: "get",
        data: { pageIdx: pageIdx, category: category, sort: sort },
        success: function (data){
            var data = $.parseHTML(data);
            var dataHtml = $("<div>").append(data);
            $("#postList").replaceWith(dataHtml.find("#postList"));
            $("#pagination").replaceWith(dataHtml.find("#pagination"));

            console.log("/community pagination ajax success")
        },
        error: function (xhr){
            console.log(xhr.responseText);
            console.log("/community pagination ajax failed")
        }
    });
}