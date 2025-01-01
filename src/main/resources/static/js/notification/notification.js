// 문서 로드 완료시 실행
$(document).ready(function() {
    initialize();
    clickToLocate()
    readNotification()
    readAllNotification()
    pagination();
});


function initialize(){
    $(".headerNotificationWrapper").hide(); //알림 페이지에서는 알림 헤더를 감춤
}

//알림 클릭시 해당 알림이 생겨난 페이지로 이동 : 알림 먼저 삭제후 이동
function clickToLocate(){
    $(document).on("click", ".notificationInfo", function (){
        //서버로 알림 삭제 요청
        const notificationId = $(this).data("notificationid");
        const nextUrl = $(this).data("url");

        $.ajax({
            url: "/api/notification/read",
            method: "post",
            data: {notificationId: notificationId},
            success: function (response, textStatus, xhr){
                if (xhr.status === 200) { //서버에서 알림 삭제가 완료 되면 페이지이동
                    window.location.href = nextUrl;
                }
                console.log("/api/notification/read ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log("/api/notification/read ajax failed")
            }
        })
    })
}

//알림 리스트를 새로고침
function pageReload(){
    const pageIdx = $(".pageNum.active").data("pageidx");
    $.ajax({
        url: "/notification",
        method: "get",
        data: {pageIdx: pageIdx},
        success: function (data){
            var data = $.parseHTML(data);
            var dataHtml = $("<div>").append(data);
            $("#notificationList").replaceWith(dataHtml.find("#notificationList"));

            console.log("/notification ajax success")
        },
        error: function (xhr){
            console.log(xhr.responseText);
            console.log("/notification ajax failed")
        }
    })
}

// 단일 알림 삭제
function readNotification() {
    $(document).on("click", ".btnDelete", function (){
        const notificationId = $(this).data("notificationid");

        $.ajax({
            url: "/api/notification/read",
            method: "post",
            data: {notificationId: notificationId},
            success: function (response, textStatus, xhr){
                if (xhr.status === 200) {
                    pageReload(); //페이지 새로고침
                    console.log(response); // 콘솔 출력
                }
                console.log("/api/notification/read ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log("/api/notification/read ajax failed")
            }
        })
    })
}

// 모두 읽음 처리
function readAllNotification() {
    $(document).on("click", ".btnReadAll", function (){
        $.ajax({
            url: "/api/notification/readAll",
            method: "post",
            success: function (response, textStatus, xhr){
                if (xhr.status === 200) {
                    alert("모든 알림을 삭제 하였습니다.")
                    pageReload(); //페이지 새로고침
                    console.log(response); // 콘솔 출력
                }
                console.log("/api/notification/readAll ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log("/api/notification/readAll ajax failed")
            }
        })
    })
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




