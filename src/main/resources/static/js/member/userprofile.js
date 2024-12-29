$(document).ready(function() {
    tabs();
    pagination();
});

function tabs() {
    $('.tabButton').on('click', function() {
        const tabId = $(this).data('tab');

        // 탭 버튼 활성화 상태 변경
        $('.tabButton').removeClass('active');
        $(this).addClass('active');

        // 컨텐츠 섹션 표시/숨김
        $('.contentSection').removeClass('active');
        $(".contentSection").hide();
        $(`#${tabId}Section`).addClass('active');
        $(`#${tabId}Section`).show();
    });
}

function pagination(){
    $(document).on("click", "#prevPage, #nextPage, .pageNum", function () {
        const pageIdx = $(this).data("pageidx")
        //const dateSort = $(".dropdownContent.latest .filterBtn.active").text() === "최신순" ? "recent" : "old";
        //const ratingSort = $(".dropdownContent.rating .filterBtn.active").text() === "별점높은순" ? "high" : "low";
        $.ajax({
            url: "",
            method: "",
            //data: {pageIdx: pageIdx, dateSort: dateSort, ratingSort: ratingSort},
            data: {pageIdx: pageIdx},
            success: function (data){
                var data = $.parseHTML(data);
                var dataHtml = $("<div>").append(data);
                $("#전체뷰").replaceWith(dataHtml.find("#전체뷰"));

                console.log(" pagination ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log(" pagination ajax failed")
            }
        });
    })
}

