$(document).ready(function() {
    calulateNotificationDate();
    readAllNotification();
    clickToLocate();
});

//알림 시간을 표시하는 로직
function calulateNotificationDate(){
    // 알림 시간이 표시된 span 요소들을 선택
    $('.headerNotificationTime span').each(function() {
        var notificationTime = new Date($(this).text());
        var currentTime = new Date();

        var timeDifference = currentTime - notificationTime;
        var seconds = Math.floor(timeDifference / 1000);
        var minutes = Math.floor(seconds / 60);
        var hours = Math.floor(minutes / 60);
        var days = Math.floor(hours / 24);

        var timeAgo = '';
        if (days > 0) {
            timeAgo = days + '일 전';
        } else if (hours > 0) {
            timeAgo = hours + '시간 전';
        } else if (minutes > 0) {
            timeAgo = minutes + '분 전';
        } else {
            timeAgo = seconds + '초 전';
        }

        $(this).text(timeAgo);  // 시간 표시 업데이트
    });
}

//알림 모두 읽음 버튼 동작 : 모든 알림의 isRead = 0세팅
function readAllNotification(){
    $(".headerReadAll").click(function () {
        // 서버로 ajax요청 : 알림 모두 읽음
        $.ajax({
            url: "/api/notification/readAll",
            method: "post",
            success: function (response, textStatus, xhr){
                if (xhr.status === 200) {
                    //정상적으로 모든 알림을 읽음 처리 했으면 헤더를 새로고침할것
                    headerReload();
                    console.log(response); // 콘솔 출력
                }else {
                    //다른이유로 성공했다? 일단 헤더 새로고침X
                    console.log(response); // 콘솔 출력
                }
                console.log("/api/notification/readAll ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log("/api/notification/readAll ajax failed")
            }
        })
        // ajax : 헤더를 다시 가져옴
        function headerReload(){
            $.ajax({
                url: "/api/header/get",
                method: "get",
                success: function (data){
                    //성공시 헤더를 다시 가져옴
                    var data = $.parseHTML(data);
                    var dataHtml = $("<div>").append(data);
                    $("#header").replaceWith(dataHtml.find("#header")); //헤더만 새로고침

                    console.log("/api/header/get ajax success")
                },
                error: function (xhr){
                    console.log(xhr.responseText);
                    console.log("/api/header/get ajax failed")
                }
            })
        }
    })
}


//알림 클릭시 해당 알림이 생겨난 페이지로 이동 : 알림 먼저 삭제후 이동
function clickToLocate(){
    $(document).on("click", ".headerNotificationInfo", function (){
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