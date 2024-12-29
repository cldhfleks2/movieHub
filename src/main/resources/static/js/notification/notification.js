// 문서 로드 완료시 실행
$(document).ready(function() {
    initialize();
    readNotification()
    readAllNotification()
    pagination();
    notificationClick();
});


function initialize(){
    $(".headerNotificationWrapper").hide(); //알림 페이지에서는 알림 헤더를 감춤
}

// 단일 알림 삭제
function readNotification() {
    $('.btnDelete').on('click', function(e) {
        e.preventDefault();
        const notificationId = $(this).closest('.notificationItem').data('id');

        //ajax
    });
}

// 모두 읽음 처리
function readAllNotification() {
    $('.btnReadAll').on('click', function(e) {
        e.preventDefault();

        //ajax
    });
}

// 페이지네이션
function pagination() {
    $(document).on("click", "#prevPage, #nextPage, .pageNum", function () {
        const pageIdx = $(this).data("pageidx")
        $.ajax({
            url: "/notification",
            method: "get",
            data: {pageIdx: pageIdx},
            success: function (data){
                var data = $.parseHTML(data);
                var dataHtml = $("<div>").append(data);
                $("#notificationContainer").replaceWith(dataHtml.find("#notificationContainer"));

                console.log("/notification pagination ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log("/notification pagination ajax failed")
            }
        });
    })
}

// 알림 클릭시 해당 위치로 이동
function notificationClick() {
    $('.notificationItem').on('click', function(e) {
        if (!$(e.target).hasClass('btnDelete')) {
            const $item = $(this);
            const notificationType = $item.data('type');
            const targetId = $item.data('target-id');


            //알림 클릭시 페이지이동할건지.. 고민


            // if (redirectUrl) {
            //     window.location.href = redirectUrl;
            // }
        }
    });
}


