$(document).ready(function() {
    calulateNotificationDate();
    checkingAllNotification();
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
function checkingAllNotification(){
    $(".headerReadAll").click(function () {
        // 서버로 ajax요청
    })
}